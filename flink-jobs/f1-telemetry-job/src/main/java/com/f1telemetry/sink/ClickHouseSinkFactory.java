package com.f1telemetry.sink;

import com.clickhouse.data.ClickHouseDataType;
import com.clickhouse.data.ClickHouseFormat;
import org.apache.flink.connector.clickhouse.convertor.ClickHouseConvertor;
import org.apache.flink.connector.clickhouse.convertor.ColumnBinding;
import org.apache.flink.connector.clickhouse.convertor.DataMapper;
import org.apache.flink.connector.clickhouse.sink.ClickHouseAsyncSink;
import org.apache.flink.connector.clickhouse.sink.ClickHouseClientConfig;
import com.f1telemetry.avro.CarTelemetryEvent;
import com.f1telemetry.model.TelemetryRollup;
import org.apache.flink.api.connector.sink2.Sink;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Factory cho ClickHouse sinks — dùng ĐÚNG API thật của flink-connector-clickhouse-1.17:0.2.0
 * (đã xác minh bằng javap, xem lịch sử trao đổi). API thật KHÔNG có POJOConvertor/DataWriter
 * (đó là API tưởng tượng ở bản trước) — mà dùng:
 *
 *   DataMapper<T>            — class trừu tượng người dùng viết: toMap() đổ field -> Map,
 *                               bindings() khai báo Map key -> tên cột ClickHouse -> kiểu.
 *   ColumnBinding             — factory tĩnh (scalar/dateTime64/...) để khai báo 1 cột.
 *   ClickHouseConvertor<T>    — bọc DataMapper, implement ElementConverter cho AsyncSinkBase.
 *   ClickHouseAsyncSink.builder() — builder pattern (KHÔNG phải constructor trực tiếp).
 *
 * Type mapping theo DDL thực tế (schema.sql — NOT NULL / DEFAULT, không Nullable):
 *   raw_telemetry : event_time DateTime64(3), driver_number UInt8, speed Float32,
 *                   throttle/brake Float32 DEFAULT 0, rpm UInt16 DEFAULT 0,
 *                   gear/drs UInt8 DEFAULT 0
 *   rollup_10s    : window_start DateTime64(3), driver_number UInt8,
 *                   avg/max_speed/avg_throttle Float32,
 *                   hard_brake_count UInt16, sample_count UInt32
 *
 * Vì cột không phải Nullable, giá trị null từ Avro (throttle/brake/rpm/gear/drs) được
 * default về 0 ngay trong toMap() — không dựa vào server-side default.
 */
public class ClickHouseSinkFactory {

    // ── Raw telemetry — high volume ──────────────────────────────────────────
    private static final int  RAW_MAX_BATCH_SIZE        = 100;
    private static final int  RAW_MAX_IN_FLIGHT         = 3;
    private static final int  RAW_MAX_BUFFERED          = 100_000;
    private static final long RAW_MAX_BATCH_BYTES       = 20_971_520L; // 20 MB
    private static final long RAW_MAX_TIME_IN_BUFFER_MS = 1000L;    // 10s
    private static final long RAW_MAX_RECORD_SIZE_BYTES = 1_048_576L; // 1 MB

    // ── Rollup aggregates — low volume ───────────────────────────────────────
    private static final int  ROLLUP_MAX_BATCH_SIZE        = 1_000;
    private static final int  ROLLUP_MAX_IN_FLIGHT         = 2;
    private static final int  ROLLUP_MAX_BUFFERED          = 10_000;
    private static final long ROLLUP_MAX_BATCH_BYTES       = 5_242_880L; // 5 MB
    private static final long ROLLUP_MAX_TIME_IN_BUFFER_MS = 10_000L;
    private static final long ROLLUP_MAX_RECORD_SIZE_BYTES = 524_288L;   // 512 KB

    // ────────────────────────────────────────────────────────────────────────
    // Raw telemetry sink
    // ────────────────────────────────────────────────────────────────────────

    /**
     * DataMapper cho CarTelemetryEvent -> f1_telemetry.raw_telemetry
     */
    private static class CarTelemetryEventMapper extends DataMapper<CarTelemetryEvent> {

        @Override
        public void toMap(CarTelemetryEvent event, Map<String, Object> out) {
            out.put("event_time",
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.getEventTime()), ZoneOffset.UTC));
            out.put("driver_number", event.getDriverNumber());
            out.put("speed", (float) event.getSpeed());

            // Float32 DEFAULT 0 (NOT NULL) <- Double nullable tu Avro: tu default null -> 0f
            out.put("throttle", event.getThrottle() != null ? event.getThrottle().floatValue() : 0f);
            out.put("brake", event.getBrake() != null ? event.getBrake().floatValue() : 0f);

            // UInt16/UInt8 DEFAULT 0 (NOT NULL) <- Integer nullable tu Avro
            out.put("rpm", event.getRpm() != null ? event.getRpm() : 0);
            out.put("gear", event.getGear() != null ? event.getGear() : 0);
            out.put("drs", event.getDrs() != null ? event.getDrs() : 0);
        }

        @Override
        public List<ColumnBinding> bindings() {
            return List.of(
                    ColumnBinding.dateTime64("event_time", "event_time", 3),
                    ColumnBinding.scalar("driver_number", "driver_number", ClickHouseDataType.UInt8),
                    ColumnBinding.scalar("speed", "speed", ClickHouseDataType.Float32),
                    ColumnBinding.scalar("throttle", "throttle", ClickHouseDataType.Float32),
                    ColumnBinding.scalar("brake", "brake", ClickHouseDataType.Float32),
                    ColumnBinding.scalar("rpm", "rpm", ClickHouseDataType.UInt16),
                    ColumnBinding.scalar("gear", "gear", ClickHouseDataType.UInt8),
                    ColumnBinding.scalar("drs", "drs", ClickHouseDataType.UInt8)
            );
        }
    }

    /**
     * Sink cho raw CarTelemetryEvent -> f1_telemetry.raw_telemetry
     */
    public static Sink<CarTelemetryEvent> createRawSink(
            String host, int port, String user, String password,
            String database, String table) {

        ClickHouseClientConfig config = new ClickHouseClientConfig(
                String.format("https://%s:%d", host, port),
                user,
                password,
                database,
                table,
                Map.of(
                    "connection_timeout", "5000",
                    "socket_timeout", "10000"
                ),                     // options
                Map.of("insert_deduplicate", "0",   // raw table: KHONG dedup, giu moi sample
                       "async_insert",       "0"),
                false                          // enableJsonSupportAsString
        );

        ClickHouseConvertor<CarTelemetryEvent> convertor =
                new ClickHouseConvertor<>(CarTelemetryEvent.class, new CarTelemetryEventMapper());

        // Da tu default null->0 trong toMap(), khong con phu thuoc RowBinaryWithDefaults
        // nua -> dung RowBinary thuong, nhe hon (khong co presence bitmask overhead).
        return ClickHouseAsyncSink.<CarTelemetryEvent>builder()
                .setElementConverter(convertor)
                .setClickHouseClientConfig(config)
                .setClickHouseFormat(ClickHouseFormat.JSONEachRow)
                .setMaxBatchSize(RAW_MAX_BATCH_SIZE)
                .setMaxInFlightRequests(RAW_MAX_IN_FLIGHT)
                .setMaxBufferedRequests(RAW_MAX_BUFFERED)
                .setMaxBatchSizeInBytes(RAW_MAX_BATCH_BYTES)
                .setMaxTimeInBufferMS(RAW_MAX_TIME_IN_BUFFER_MS)
                .setMaxRecordSizeInBytes(RAW_MAX_RECORD_SIZE_BYTES)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Rollup sink
    // ────────────────────────────────────────────────────────────────────────

    /**
     * DataMapper cho TelemetryRollup -> f1_telemetry.rollup_10s
     */
    private static class TelemetryRollupMapper extends DataMapper<TelemetryRollup> {

        @Override
        public void toMap(TelemetryRollup r, Map<String, Object> out) {
            out.put("window_start",
                    ZonedDateTime.ofInstant(Instant.ofEpochMilli(r.window_start), ZoneOffset.UTC));
            out.put("driver_number", r.driver_number);
            out.put("avg_speed", (float) r.avg_speed);
            out.put("max_speed", (float) r.max_speed);
            out.put("avg_throttle", (float) r.avg_throttle);
            // hard_brake_count la UInt16, sample_count la UInt32 trong schema.sql;
            // TelemetryRollup luu duoi dang long -> ep ve int truoc khi dua vao map
            // (khong tran vi so luong sample trong 1 cua so 10s khong the vuot UInt16/UInt32).
            out.put("hard_brake_count", (int) r.hard_brake_count);
            out.put("sample_count", (int) r.sample_count);
        }

        @Override
        public List<ColumnBinding> bindings() {
            return List.of(
                    ColumnBinding.dateTime64("window_start", "window_start", 3),
                    ColumnBinding.scalar("driver_number", "driver_number", ClickHouseDataType.UInt8),
                    ColumnBinding.scalar("avg_speed", "avg_speed", ClickHouseDataType.Float32),
                    ColumnBinding.scalar("max_speed", "max_speed", ClickHouseDataType.Float32),
                    ColumnBinding.scalar("avg_throttle", "avg_throttle", ClickHouseDataType.Float32),
                    ColumnBinding.scalar("hard_brake_count", "hard_brake_count", ClickHouseDataType.UInt16),
                    ColumnBinding.scalar("sample_count", "sample_count", ClickHouseDataType.UInt32)
            );
        }
    }

    /**
     * Sink cho TelemetryRollup -> f1_telemetry.rollup_10s
     */
    public static Sink<TelemetryRollup> createRollupSink(
            String host, int port, String user, String password,
            String database, String table) {

        ClickHouseClientConfig config = new ClickHouseClientConfig(
                String.format("https://%s:%d", host, port),
                user,
                password,
                database,
                table,
                Map.of(),
                Map.of("insert_deduplicate", "1"), // rollup: dedup dung, guard checkpoint replay
                false
        );

        ClickHouseConvertor<TelemetryRollup> convertor =
                new ClickHouseConvertor<>(TelemetryRollup.class, new TelemetryRollupMapper());

        return ClickHouseAsyncSink.<TelemetryRollup>builder()
                .setElementConverter(convertor)
                .setClickHouseClientConfig(config)
                .setClickHouseFormat(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                .setMaxBatchSize(ROLLUP_MAX_BATCH_SIZE)
                .setMaxInFlightRequests(ROLLUP_MAX_IN_FLIGHT)
                .setMaxBufferedRequests(ROLLUP_MAX_BUFFERED)
                .setMaxBatchSizeInBytes(ROLLUP_MAX_BATCH_BYTES)
                .setMaxTimeInBufferMS(ROLLUP_MAX_TIME_IN_BUFFER_MS)
                .setMaxRecordSizeInBytes(ROLLUP_MAX_RECORD_SIZE_BYTES)
                .build();
    }
}