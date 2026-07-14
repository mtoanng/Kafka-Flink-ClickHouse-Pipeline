package com.f1telemetry;

import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.sink.ClickHouseSink;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * F1 Telemetry Flink Job — single DataStream API pipeline.
 *
 * Pipeline:
 *   KafkaSource[Avro] (car-telemetry-events)
 *     → watermark (event_time, 3s out-of-orderness)
 *     ├→ raw sink → ClickHouse raw_telemetry
 *     └→ keyBy(driver_number) → window(10s) → aggregate(SpeedRollupAggregator)
 *        → rollup sink → ClickHouse rollup_10s
 *
 * Run in-process: mvn -pl flink-jobs/f1-telemetry-job exec:java
 *   (from repo root, with Kafka + Schema Registry running locally)
 */
public class F1TelemetryJob {

    private static final Logger LOG = LoggerFactory.getLogger(F1TelemetryJob.class);

    public static void main(String[] args) throws Exception {
        // ── Config from environment ──────────────────────────
        String bootstrap = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
        String srUrl = env("SCHEMA_REGISTRY_URL", "http://localhost:8081");
        String topic = env("KAFKA_TOPIC", "car-telemetry-events");

        // ClickHouse Cloud (HTTPS:8443 with SSL)
        String chHost = env("CLICKHOUSE_HOST", "localhost");
        String chPort = env("CLICKHOUSE_PORT", "8443");
        String chUser = env("CLICKHOUSE_USER", "default");
        String chPass = env("CLICKHOUSE_PASSWORD", "");
        String chDb = env("CLICKHOUSE_DATABASE", "f1_telemetry");
        String chJdbcUrl = String.format("jdbc:ch://%s:%s/%s?ssl=true", chHost, chPort, chDb);

        LOG.info("=== F1 Telemetry Flink Job ===");
        LOG.info("Kafka={} | SR={} | topic={}", bootstrap, srUrl, topic);
        LOG.info("ClickHouse: {}:{} (user={})", chHost, chPort, chUser);

        // ── Execution environment ────────────────────────────
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(30000); // 30s
        env.setParallelism(1);          // dev/demo

        // ── Kafka source (Avro via Schema Registry) ──────────
        KafkaSource<CarTelemetryEvent> source = KafkaSource.<CarTelemetryEvent>builder()
                .setBootstrapServers(bootstrap)
                .setTopics(topic)
                .setGroupId("f1-telemetry-job")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(
                        ConfluentRegistryAvroDeserializationSchema.forSpecific(
                                CarTelemetryEvent.class, srUrl))
                .build();

        // ── Source + watermark ───────────────────────────────
        DataStream<CarTelemetryEvent> telemetry = env
                .fromSource(source,
                        WatermarkStrategy
                                .<CarTelemetryEvent>forBoundedOutOfOrderness(Duration.ofSeconds(3))
                                .withTimestampAssigner((event, ts) -> event.getEventTime()),
                        "kafka-source");

        // ── Sink 1: Raw → ClickHouse ─────────────────────────
        telemetry.addSink(new ClickHouseSink.RawTelemetrySink(chJdbcUrl, chUser, chPass))
                .name("clickhouse-raw-sink");

        // ── Sink 2: Windowed aggregate → ClickHouse ──────────
        telemetry
                .keyBy(CarTelemetryEvent::getDriverNumber)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .aggregate(new SpeedRollupAggregator())
                .name("rollup-10s")
                .addSink(new ClickHouseSink.RollupSink(chJdbcUrl, chUser, chPass))
                .name("clickhouse-rollup-sink");

        env.execute("F1 Live Telemetry Pipeline");
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return v != null ? v : def;
    }
}
