package com.f1telemetry.sink;

import com.clickhouse.data.ClickHouseFormat;
import com.clickhouse.data.ClickHouseColumn;
import org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSink;
import org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSinkBuilder;
import org.apache.flink.connector.clickhouse.sink.ClickHouseClientConfig;
import org.apache.flink.connector.clickhouse.convertor.ClickHouseConvertor;
import org.apache.flink.connector.clickhouse.convertor.ColumnBinding;
import org.apache.flink.connector.clickhouse.convertor.DataMapper;
import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import org.apache.flink.api.connector.sink2.Sink;

import java.util.List;
import java.util.Map;

/**
 * Factory for ClickHouse sinks using the official flink-connector-clickhouse.
 * 
 * Architecture:
 *   Flink DataStream → ClickHouseAsyncSink (official) → RowBinaryWithDefaults → ClickHouse Cloud
 */
public class ClickHouseSinkFactory {

    // -------------------------------------------------------------------------
    // Shared sink parameters
    // -------------------------------------------------------------------------

    // Raw telemetry: high volume — large batches, few in-flight to limit parts/sec
    private static final int    RAW_MAX_BATCH_SIZE          = 20_000;   // rows per INSERT
    private static final int    RAW_MAX_IN_FLIGHT           = 3;        // concurrent INSERTs
    private static final int    RAW_MAX_BUFFERED            = 100_000;  // backpressure limit
    private static final long   RAW_MAX_BATCH_BYTES         = 20_971_520L; // 20 MB
    private static final long   RAW_MAX_TIME_IN_BUFFER_MS   = 10_000L;  // flush every 10s max
    private static final long   RAW_MAX_RECORD_SIZE_BYTES   = 1_048_576L; // 1 MB per record (guard)

    // Rollup aggregates: low volume — smaller batches are fine
    private static final int    ROLLUP_MAX_BATCH_SIZE        = 1_000;
    private static final int    ROLLUP_MAX_IN_FLIGHT         = 2;
    private static final int    ROLLUP_MAX_BUFFERED          = 10_000;
    private static final long   ROLLUP_MAX_BATCH_BYTES       = 5_242_880L; // 5 MB
    private static final long   ROLLUP_MAX_TIME_IN_BUFFER_MS = 10_000L;
    private static final long   ROLLUP_MAX_RECORD_SIZE_BYTES = 524_288L;   // 512 KB

    private static final DataMapper<CarTelemetryEvent> RAW_EVENT_MAPPER = new DataMapper<>() {
        @Override
        public void toMap(CarTelemetryEvent event, Map<String, Object> values) {
            values.put("event_time", event.getEventTime());
            values.put("driver_number", event.getDriverNumber());
            values.put("speed", event.getSpeed());
            values.put("throttle", event.getThrottle());
            values.put("brake", event.getBrake());
            values.put("rpm", event.getRpm());
            values.put("gear", event.getGear());
            values.put("drs", event.getDrs());
        }

        @Override
        public List<ColumnBinding> bindings() {
            return List.of(
                    ColumnBinding.dateTime64("event_time", "event_time", 3),
                    ColumnBinding.of("driver_number", "driver_number", ClickHouseColumn.of("driver_number", "UInt8")),
                    ColumnBinding.of("speed", "speed", ClickHouseColumn.of("speed", "Float32")),
                    ColumnBinding.of("throttle", "throttle", ClickHouseColumn.of("throttle", "Nullable(Float32)")),
                    ColumnBinding.of("brake", "brake", ClickHouseColumn.of("brake", "Nullable(Float32)")),
                    ColumnBinding.of("rpm", "rpm", ClickHouseColumn.of("rpm", "Nullable(UInt16)")),
                    ColumnBinding.of("gear", "gear", ClickHouseColumn.of("gear", "Nullable(UInt8)")),
                    ColumnBinding.of("drs", "drs", ClickHouseColumn.of("drs", "Nullable(UInt8)")));
        }
    };

    private static final DataMapper<TelemetryRollup> ROLLUP_MAPPER = new DataMapper<>() {
        @Override
        public void toMap(TelemetryRollup rollup, Map<String, Object> values) {
            values.put("window_start", rollup.window_start);
            values.put("driver_number", rollup.driver_number);
            values.put("avg_speed", rollup.avg_speed);
            values.put("max_speed", rollup.max_speed);
            values.put("avg_throttle", rollup.avg_throttle);
            values.put("hard_brake_count", rollup.hard_brake_count);
            values.put("sample_count", rollup.sample_count);
        }

        @Override
        public List<ColumnBinding> bindings() {
            return List.of(
                    ColumnBinding.dateTime64("window_start", "window_start", 3),
                    ColumnBinding.of("driver_number", "driver_number", ClickHouseColumn.of("driver_number", "UInt8")),
                    ColumnBinding.of("avg_speed", "avg_speed", ClickHouseColumn.of("avg_speed", "Float32")),
                    ColumnBinding.of("max_speed", "max_speed", ClickHouseColumn.of("max_speed", "Float32")),
                    ColumnBinding.of("avg_throttle", "avg_throttle", ClickHouseColumn.of("avg_throttle", "Float32")),
                    ColumnBinding.of("hard_brake_count", "hard_brake_count", ClickHouseColumn.of("hard_brake_count", "UInt16")),
                    ColumnBinding.of("sample_count", "sample_count", ClickHouseColumn.of("sample_count", "UInt32")));
        }
    };

    // -------------------------------------------------------------------------
    // Raw telemetry sink
    // -------------------------------------------------------------------------

    /**
     * Creates a sink for raw CarTelemetryEvent records.
     * 
     * Default format: RowBinaryWithDefaults (fastest; binary, typed, null-safe).
     * Deduplication: insert_deduplicate=1 ensures retried batches don't create duplicate rows.
     */
    public static Sink<CarTelemetryEvent> createRawSink(
            String host, int port, String user, String password, 
            String database, String table) {
        
        String url = String.format("https://%s:%d", host, port);
        
        Map<String, String> serverSettings = Map.of(
            "insert_deduplicate",    "1",   // safe retries — no duplicate rows on failure
            "async_insert",          "0"    // we batch client-side; don't double-buffer server-side
        );
        
        ClickHouseClientConfig config = new ClickHouseClientConfig(
            url,
            user,
            password,
            database,
            table,
            Map.of(),          // client options
            serverSettings,
            false              // ignoreDelete
        );
        
        return ClickHouseAsyncSink.<CarTelemetryEvent>builder()
            .setClickHouseClientConfig(config)
            .setElementConverter(new ClickHouseConvertor<>(CarTelemetryEvent.class, RAW_EVENT_MAPPER))
            .setMaxBatchSize(RAW_MAX_BATCH_SIZE)
            .setMaxInFlightRequests(RAW_MAX_IN_FLIGHT)
            .setMaxBufferedRequests(RAW_MAX_BUFFERED)
            .setMaxBatchSizeInBytes(RAW_MAX_BATCH_BYTES)
            .setMaxTimeInBufferMS(RAW_MAX_TIME_IN_BUFFER_MS)
            .setMaxRecordSizeInBytes(RAW_MAX_RECORD_SIZE_BYTES)
            .build();
    }

    // -------------------------------------------------------------------------
    // Rollup sink
    // -------------------------------------------------------------------------

    /**
     * Creates a sink for TelemetryRollup aggregates.
     */
    public static Sink<TelemetryRollup> createRollupSink(
            String host, int port, String user, String password, 
            String database, String table) {
        
        String url = String.format("https://%s:%d", host, port);
        
        Map<String, String> serverSettings = Map.of("insert_deduplicate", "1");
        
        ClickHouseClientConfig config = new ClickHouseClientConfig(
            url,
            user,
            password,
            database,
            table,
            Map.of(),
            serverSettings,
            false
        );
        
        return ClickHouseAsyncSink.<TelemetryRollup>builder()
            .setClickHouseClientConfig(config)
            .setElementConverter(new ClickHouseConvertor<>(TelemetryRollup.class, ROLLUP_MAPPER))
            .setMaxBatchSize(ROLLUP_MAX_BATCH_SIZE)
            .setMaxInFlightRequests(ROLLUP_MAX_IN_FLIGHT)
            .setMaxBufferedRequests(ROLLUP_MAX_BUFFERED)
            .setMaxBatchSizeInBytes(ROLLUP_MAX_BATCH_BYTES)
            .setMaxTimeInBufferMS(ROLLUP_MAX_TIME_IN_BUFFER_MS)
            .setMaxRecordSizeInBytes(ROLLUP_MAX_RECORD_SIZE_BYTES)
            .build();
    }
}
