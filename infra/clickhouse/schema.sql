-- =============================================================================
-- ClickHouse Schema for F1 Telemetry Pipeline (v2 — thêm compression codec)
-- =============================================================================
--
-- Thay đổi so với bản gốc: CHỈ thêm CODEC cho các cột. Engine (MergeTree),
-- PARTITION BY, ORDER BY, TTL giữ nguyên vì đã đúng.
--
-- Nếu bảng đã có data, dùng ALTER TABLE ... MODIFY COLUMN để áp codec mà
-- không cần recreate (xem phần cuối file). Nếu bảng còn trống, có thể
-- DROP TABLE rồi chạy lại CREATE TABLE bên dưới.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS f1_telemetry;

-- -----------------------------------------------------------------------------
-- Raw telemetry table
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS f1_telemetry.raw_telemetry (
    event_time    DateTime64(3) NOT NULL      CODEC(Delta, LZ4),
    driver_number UInt8 NOT NULL,             -- max 20 drivers; cardinality quá thấp, codec không cần thiết
    speed         Float32 NOT NULL            CODEC(Gorilla, LZ4),
    throttle      Float32 DEFAULT 0           CODEC(Gorilla, LZ4),
    brake         Float32 DEFAULT 0           CODEC(Gorilla, LZ4),
    rpm           UInt16 DEFAULT 0            CODEC(Delta, LZ4),
    gear          UInt8 DEFAULT 0             CODEC(T64, LZ4),
    drs           UInt8 DEFAULT 0             CODEC(T64, LZ4)
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(event_time)
ORDER BY (driver_number, event_time)
TTL event_time + INTERVAL 90 DAY;

-- -----------------------------------------------------------------------------
-- Rollup aggregates table (10-second windows)
-- -----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS f1_telemetry.rollup_10s (
    window_start     DateTime64(3) NOT NULL   CODEC(Delta, LZ4),
    driver_number    UInt8 NOT NULL,
    avg_speed        Float32 NOT NULL         CODEC(Gorilla, LZ4),
    max_speed        Float32 NOT NULL         CODEC(Gorilla, LZ4),
    avg_throttle     Float32 NOT NULL         CODEC(Gorilla, LZ4),
    hard_brake_count UInt16 DEFAULT 0,
    sample_count     UInt32 NOT NULL
)
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(window_start)
ORDER BY (driver_number, window_start);

-- =============================================================================
-- Nếu bảng ĐÃ có data — áp codec mà không cần recreate table:
-- =============================================================================
-- ALTER TABLE f1_telemetry.raw_telemetry MODIFY COLUMN event_time DateTime64(3) CODEC(Delta, LZ4);
-- ALTER TABLE f1_telemetry.raw_telemetry MODIFY COLUMN speed      Float32       CODEC(Gorilla, LZ4);
-- ALTER TABLE f1_telemetry.raw_telemetry MODIFY COLUMN throttle   Float32       CODEC(Gorilla, LZ4);
-- ALTER TABLE f1_telemetry.raw_telemetry MODIFY COLUMN brake      Float32       CODEC(Gorilla, LZ4);
-- ALTER TABLE f1_telemetry.raw_telemetry MODIFY COLUMN rpm        UInt16        CODEC(Delta, LZ4);
-- ALTER TABLE f1_telemetry.raw_telemetry MODIFY COLUMN gear       UInt8         CODEC(T64, LZ4);
-- ALTER TABLE f1_telemetry.raw_telemetry MODIFY COLUMN drs        UInt8         CODEC(T64, LZ4);
-- -- LƯU Ý: MODIFY COLUMN chỉ áp dụng codec cho các part MỚI ghi vào sau lệnh này.
-- -- Muốn nén lại toàn bộ data cũ: chạy thêm
-- -- ALTER TABLE f1_telemetry.raw_telemetry MATERIALIZE COLUMN <col>;
-- -- (cẩn thận: MATERIALIZE COLUMN rewrite toàn bộ part, tốn IO — nên chạy off-peak)

-- =============================================================================
-- Verification queries
-- =============================================================================

-- Check data arrival
-- SELECT count(*) FROM f1_telemetry.raw_telemetry;
-- SELECT count(*) FROM f1_telemetry.rollup_10s;

-- Check codec đã áp dụng đúng
-- SELECT name, type, compression_codec
-- FROM system.columns
-- WHERE database = 'f1_telemetry' AND table = 'raw_telemetry';

-- Check latest events
-- SELECT * FROM f1_telemetry.raw_telemetry ORDER BY event_time DESC LIMIT 10;

-- Check insert operations / lỗi insert (system table)
-- SELECT
--     event_time, query_kind, type, exception, written_rows, written_bytes
-- FROM system.query_log
-- WHERE database = 'f1_telemetry' AND table = 'raw_telemetry'
-- ORDER BY event_time DESC
-- LIMIT 20;

-- Check table parts (actual data on disk) — theo dõi TOO_MANY_PARTS
-- SELECT partition, count() AS num_parts, sum(rows) AS total_rows
-- FROM system.parts
-- WHERE database = 'f1_telemetry' AND table = 'raw_telemetry' AND active = 1
-- GROUP BY partition
-- ORDER BY partition DESC;