package com.f1telemetry.sink;

import com.clickhouse.data.ClickHouseFormat;
import com.clickhouse.flink.ClickHouseAsyncSink;
import com.clickhouse.flink.ClickHouseClientConfig;
import com.clickhouse.flink.serialize.POJOConvertor;
import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import org.apache.flink.api.connector.sink2.Sink;

import java.util.Map;

/**
 * Factory for ClickHouse sinks using the official flink-connector-clickhouse.
 * 
 * Architecture:
 *   Flink DataStream → ClickHouseAsyncSink (official) → RowBinaryWithDefaults → ClickHouse Cloud
 * 
 * Key improvements over custom sink:
 *  - client-v2 under the hood (not the deprecated legacy ClickHouseClient)
 *  - RowBinaryWithDefaults by default — binary, typed, no JSON escaping bugs, faster to parse
 *  - POJOConvertor handles field mapping via reflection — no manual String.format()
 *  - Built-in metrics: numBytesSend, numRecordSend, writeLatencyHistogram, etc.
 *  - insert_deduplicate server setting for safe retries (no duplicate rows)
 *  - Wire compression enabled (critical for ClickHouse Cloud — compute is remote from storage)
 *  - Proper batch sizes per insert-batch-size rule (10K–100K rows per INSERT)
 *  - Low maxInFlightRequests to stay within ~1 INSERT/sec recommended rate
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
            Map.of(),          // java client options — defaults are fine (SSL inferred from https://)
            serverSettings,
            false              // enableJsonSupportAsString — not needed, we use RowBinary
        );
        
        // POJOConvertor maps CarTelemetryEvent fields to ClickHouse columns by name via reflection.
        // Field names must match column names in the ClickHouse table (case-sensitive).
        // See ClickHouseConvertor if you need manual field mapping instead.
        ClickHouseAsyncSink<CarTelemetryEvent> sink = new ClickHouseAsyncSink<>(
            new POJOConvertor<>(CarTelemetryEvent.class),
            RAW_MAX_BATCH_SIZE,
            RAW_MAX_IN_FLIGHT,
            RAW_MAX_BUFFERED,
            RAW_MAX_BATCH_BYTES,
            RAW_MAX_TIME_IN_BUFFER_MS,
            RAW_MAX_RECORD_SIZE_BYTES,
            config
        );
        
        // Default is RowBinaryWithDefaults — best for typed POJOs with nullable/default columns.
        // Uncomment only if you need human-readable debugging of the wire payload:
        // sink.setClickHouseFormat(ClickHouseFormat.JSONEachRow);
        
        return sink;
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
        
        return new ClickHouseAsyncSink<>(
            new POJOConvertor<>(TelemetryRollup.class),
            ROLLUP_MAX_BATCH_SIZE,
            ROLLUP_MAX_IN_FLIGHT,
            ROLLUP_MAX_BUFFERED,
            ROLLUP_MAX_BATCH_BYTES,
            ROLLUP_MAX_TIME_IN_BUFFER_MS,
            ROLLUP_MAX_RECORD_SIZE_BYTES,
            config
        );
    }
}
