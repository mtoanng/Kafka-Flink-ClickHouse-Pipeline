package com.f1telemetry;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import com.f1telemetry.sink.ClickHouseSink;

import java.time.Duration;

/**
 * F1 Telemetry Flink Job
 * 
 * Pipeline:
 * 1. Kafka Source (Avro deserialization via Confluent Schema Registry)
 * 2. Watermark (BoundedOutOfOrderness 3s)
 * 3. keyBy(driver_number)
 * 4. TumblingEventTimeWindows (10s)
 * 5. Aggregate (avg/max speed, avg throttle)
 * 6. Dual Sink: raw_telemetry + rollup_10s (ClickHouse JDBC)
 */
public class F1TelemetryJob {
    private static final Logger LOG = LoggerFactory.getLogger(F1TelemetryJob.class);
    
    // Config from env or defaults
    private static final String KAFKA_BOOTSTRAP = System.getenv()
            .getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    private static final String SCHEMA_REGISTRY_URL = System.getenv()
            .getOrDefault("SCHEMA_REGISTRY_URL", "http://localhost:8081");
    private static final String TOPIC = System.getenv()
            .getOrDefault("KAFKA_TOPIC", "car-telemetry-events");
    private static final String CLICKHOUSE_URL = System.getenv()
            .getOrDefault("CLICKHOUSE_URL", "jdbc:clickhouse://localhost:8123/f1_telemetry");
    
    public static void main(String[] args) throws Exception {
        // Setup execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // Enable checkpointing (EXACTLY_ONCE)
        env.enableCheckpointing(30000); // 30s interval
        env.getCheckpointConfig().setCheckpointTimeout(60000);
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        
        LOG.info("Flink job starting. Kafka={}, Registry={}, Topic={}, ClickHouse={}",
                KAFKA_BOOTSTRAP, SCHEMA_REGISTRY_URL, TOPIC, CLICKHOUSE_URL);
        
        // Step 1: Kafka Source with Avro deserialization
        KafkaSource<CarTelemetryEvent> source = KafkaSource.<CarTelemetryEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP)
                .setTopics(TOPIC)
                .setGroupId("f1-telemetry-job")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(
                        ConfluentRegistryAvroDeserializationSchema.forSpecific(
                                CarTelemetryEvent.class, SCHEMA_REGISTRY_URL))
                .build();
        
        // Step 2: Source stream + Watermark strategy
        DataStream<CarTelemetryEvent> telemetryStream = env
                .fromSource(source, 
                        WatermarkStrategy
                                .<CarTelemetryEvent>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((event, timestamp) -> event.getEventTime()),
                        "kafka-source");
        
        LOG.info("Kafka source configured. Watermark: BoundedOutOfOrderness(3s)");
        
        // Step 3: Sink raw telemetry to ClickHouse
        telemetryStream.addSink(new ClickHouseSink.RawTelemetrySink(CLICKHOUSE_URL))
                .name("raw-telemetry-sink");
        
        LOG.info("Raw telemetry sink added (ClickHouse: raw_telemetry)");
        
        // Step 4: Windowed aggregation (10s tumbling windows)
        DataStream<TelemetryRollup> rollupStream = telemetryStream
                .keyBy(CarTelemetryEvent::getDriverNumber)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .aggregate(new SpeedRollupAggregator())
                .name("10s-window-aggregation");
        
        LOG.info("Windowed aggregation configured: TumblingEventTimeWindows(10s)");
        
        // Step 5: Sink rollup to ClickHouse
        rollupStream.addSink(new ClickHouseSink.RollupSink(CLICKHOUSE_URL))
                .name("rollup-sink");
        
        LOG.info("Rollup sink added (ClickHouse: rollup_10s)");
        
        // Execute
        LOG.info("Executing Flink job: F1 Telemetry Stream Processing");
        env.execute("F1 Telemetry Stream Processing");
    }
}
