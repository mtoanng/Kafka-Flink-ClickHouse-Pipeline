package com.f1telemetry.sink;

import com.clickhouse.data.ClickHouseFormat;
import org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSink;
import org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSinkBuilder;
import org.apache.flink.connector.clickhouse.sink.ClickHouseClientConfig;
import org.apache.flink.connector.clickhouse.convertor.ClickHouseConvertor;
import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import org.apache.flink.api.connector.sink2.Sink;

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
            .setElementConverter(new ClickHouseConvertor<>(CarTelemetryEvent.class))
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
            .setElementConverter(new ClickHouseConvertor<>(TelemetryRollup.class))
            .setMaxBatchSize(ROLLUP_MAX_BATCH_SIZE)
            .setMaxInFlightRequests(ROLLUP_MAX_IN_FLIGHT)
            .setMaxBufferedRequests(ROLLUP_MAX_BUFFERED)
            .setMaxBatchSizeInBytes(ROLLUP_MAX_BATCH_BYTES)
            .setMaxTimeInBufferMS(ROLLUP_MAX_TIME_IN_BUFFER_MS)
            .setMaxRecordSizeInBytes(ROLLUP_MAX_RECORD_SIZE_BYTES)
            .build();
    }
}
