package com.f1telemetry;

import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import com.f1telemetry.sink.ClickHouseSinkFactory;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.formats.avro.registry.confluent.ConfluentRegistryAvroDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * F1 Telemetry Flink Pipeline — official ClickHouse connector + proper windowed aggregation.
 * 
 * Pipeline:
 *   Kafka (Avro) → Flink DataStream (watermark 5s)
 *     ├→ raw sink → ClickHouse raw_telemetry
 *     └→ keyBy(driver) → window(10s) → aggregate → ClickHouse rollup_10s
 */
public class F1TelemetryPipeline {

    private static final Logger LOG = LoggerFactory.getLogger(F1TelemetryPipeline.class);

    // -------------------------------------------------------------------------
    // Config — externalize to environment variables or config file in production
    // -------------------------------------------------------------------------

    private static final String KAFKA_BOOTSTRAP      = env("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092");
    private static final String KAFKA_TOPIC          = env("KAFKA_TOPIC", "car-telemetry-events");
    private static final String KAFKA_CONSUMER_GROUP = env("KAFKA_CONSUMER_GROUP", "f1-flink-pipeline");
    private static final String SCHEMA_REGISTRY_URL  = env("SCHEMA_REGISTRY_URL", "http://localhost:8081");

    private static final String CH_HOST     = env("CLICKHOUSE_HOST", "localhost");
    private static final int    CH_PORT     = Integer.parseInt(env("CLICKHOUSE_PORT", "8443"));
    private static final String CH_USER     = env("CLICKHOUSE_USER", "default");
    private static final String CH_PASSWORD = env("CLICKHOUSE_PASSWORD", "");
    private static final String CH_DATABASE = env("CLICKHOUSE_DATABASE", "f1_telemetry");
    private static final String CH_TABLE_RAW    = env("CLICKHOUSE_TABLE_RAW", "raw_telemetry");
    private static final String CH_TABLE_ROLLUP = env("CLICKHOUSE_TABLE_ROLLUP", "rollup_10s");

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws Exception {
        LOG.info("=== F1 Telemetry Pipeline ===");
        LOG.info("Kafka: {} | Topic: {} | Schema Registry: {}", KAFKA_BOOTSTRAP, KAFKA_TOPIC, SCHEMA_REGISTRY_URL);
        LOG.info("ClickHouse: {}:{}/{}", CH_HOST, CH_PORT, CH_DATABASE);

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Checkpointing — required for AsyncSinkBase to flush on failures cleanly
        env.enableCheckpointing(30_000); // every 30 seconds
        env.setParallelism(1); // dev/demo mode

        // ── Kafka Source ───────────────────────────────────────────────────────

        KafkaSource<CarTelemetryEvent> kafkaSource = KafkaSource.<CarTelemetryEvent>builder()
                .setBootstrapServers(KAFKA_BOOTSTRAP)
                .setTopics(KAFKA_TOPIC)
                .setGroupId(KAFKA_CONSUMER_GROUP)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(
                        ConfluentRegistryAvroDeserializationSchema.forSpecific(
                                CarTelemetryEvent.class, SCHEMA_REGISTRY_URL))
                .build();

        DataStream<CarTelemetryEvent> telemetryStream = env.fromSource(
                kafkaSource,
                // Watermark: allow up to 5s of late events
                WatermarkStrategy.<CarTelemetryEvent>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                        .withTimestampAssigner((event, ts) -> event.getEventTime()),
                "KafkaCarTelemetrySource"
        );

        // ── Sink 1: Raw telemetry → ClickHouse ────────────────────────────────

        telemetryStream
                .sinkTo(ClickHouseSinkFactory.createRawSink(
                        CH_HOST, CH_PORT, CH_USER, CH_PASSWORD, CH_DATABASE, CH_TABLE_RAW))
                .name("ClickHouseRawSink");

        // ── Windowed rollup aggregation ────────────────────────────────────────

        DataStream<TelemetryRollup> rollupStream = telemetryStream
                .keyBy(CarTelemetryEvent::getDriverNumber)
                .window(TumblingEventTimeWindows.of(Duration.ofSeconds(10)))
                .aggregate(new TelemetryAggregator(), new TelemetryWindowFunction())
                .name("10sRollupAggregation");

        // ── Sink 2: Rollups → ClickHouse ──────────────────────────────────────

        rollupStream
                .sinkTo(ClickHouseSinkFactory.createRollupSink(
                        CH_HOST, CH_PORT, CH_USER, CH_PASSWORD, CH_DATABASE, CH_TABLE_ROLLUP))
                .name("ClickHouseRollupSink");

        env.execute("F1 Telemetry Pipeline");
    }

    // -------------------------------------------------------------------------
    // Aggregation accumulator
    // -------------------------------------------------------------------------

    private static class TelemetryAccumulator {
        double sumSpeed    = 0;
        double maxSpeed    = 0;
        double sumThrottle = 0;
        int    hardBrakes  = 0;
        int    count       = 0;
    }

    // -------------------------------------------------------------------------
    // AggregateFunction — incremental, no state explosion
    // -------------------------------------------------------------------------

    private static class TelemetryAggregator
            implements AggregateFunction<CarTelemetryEvent, TelemetryAccumulator, TelemetryAccumulator> {

        private static final double HARD_BRAKE_THRESHOLD = 0.8;

        @Override
        public TelemetryAccumulator createAccumulator() {
            return new TelemetryAccumulator();
        }

        @Override
        public TelemetryAccumulator add(CarTelemetryEvent event, TelemetryAccumulator acc) {
            double speed = event.getSpeed();
            acc.sumSpeed    += speed;
            acc.maxSpeed     = Math.max(acc.maxSpeed, speed);
            acc.sumThrottle += event.getThrottle() != null ? event.getThrottle() : 0.0;

            if (event.getBrake() != null && event.getBrake() >= HARD_BRAKE_THRESHOLD) {
                acc.hardBrakes++;
            }

            acc.count++;
            return acc;
        }

        @Override
        public TelemetryAccumulator getResult(TelemetryAccumulator acc) {
            return acc;
        }

        @Override
        public TelemetryAccumulator merge(TelemetryAccumulator a, TelemetryAccumulator b) {
            a.sumSpeed    += b.sumSpeed;
            a.maxSpeed     = Math.max(a.maxSpeed, b.maxSpeed);
            a.sumThrottle += b.sumThrottle;
            a.hardBrakes  += b.hardBrakes;
            a.count       += b.count;
            return a;
        }
    }

    // -------------------------------------------------------------------------
    // ProcessWindowFunction — adds window metadata to the accumulator result
    // -------------------------------------------------------------------------

    private static class TelemetryWindowFunction
            extends ProcessWindowFunction<TelemetryAccumulator, TelemetryRollup, Integer, TimeWindow> {

        @Override
        public void process(Integer driverNumber,
                            Context context,
                            Iterable<TelemetryAccumulator> elements,
                            Collector<TelemetryRollup> out) {
            TelemetryAccumulator acc = elements.iterator().next();
            if (acc.count == 0) return;

            out.collect(new TelemetryRollup(
                    context.window().getStart(),
                    driverNumber,
                    acc.sumSpeed    / acc.count,
                    acc.maxSpeed,
                    acc.sumThrottle / acc.count,
                    acc.hardBrakes,
                    acc.count
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }
}
