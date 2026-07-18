-- =============================================================================
-- ClickHouse Schema for F1 Telemetry Pipeline
-- =============================================================================
-- 
-- Design principles applied:
--   1. ORDER BY: low-cardinality first (driver_number), then time
--   2. Avoid Nullable: use DEFAULT values instead (better compression, faster queries)
--   3. Partition by day: bounded cardinality, efficient TTL cleanup
--   4. Use appropriate numeric types: UInt8 for drivers (max 20), Float32 for metrics
-- =============================================================================

CREATE DATABASE IF NOT EXISTS f1_telemetry;

-- -----------------------------------------------------------------------------
-- Raw telemetry table
-- -----------------------------------------------------------------------------
-- Stores every telemetry event (high volume: ~1000 events/driver/minute)
-- ORDER BY optimized for queries: "show me driver X's telemetry for time range Y"

CREATE TABLE IF NOT EXISTS f1_telemetry.raw_telemetry (
    event_time DateTime64(3) NOT NULL,
    driver_number UInt8 NOT NULL,          -- max 20 drivers in F1
    speed Float32 NOT NULL,
    throttle Float32 DEFAULT 0,            -- avoid Nullable per schema-types-avoid-nullable
    brake Float32 DEFAULT 0,
    rpm UInt16 DEFAULT 0,
    gear UInt8 DEFAULT 0,
    drs UInt8 DEFAULT 0
) 
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(event_time)       -- 1 partition per day; bounded cardinality
ORDER BY (driver_number, event_time)      -- low-cardinality first for better compression
TTL event_time + INTERVAL 90 DAY;         -- auto-expire old race data

-- Index for common query pattern: find events in time range for specific driver
-- Already covered by ORDER BY — no additional index needed

-- -----------------------------------------------------------------------------
-- Rollup aggregates table (10-second windows)
-- -----------------------------------------------------------------------------
-- Stores pre-aggregated metrics per driver per 10s window (low volume: 6 rows/driver/minute)
-- Used for dashboards and trend analysis

CREATE TABLE IF NOT EXISTS f1_telemetry.rollup_10s (
    window_start DateTime64(3) NOT NULL,
    driver_number UInt8 NOT NULL,
    avg_speed Float32 NOT NULL,
    max_speed Float32 NOT NULL,
    avg_throttle Float32 NOT NULL,
    hard_brake_count UInt16 DEFAULT 0,     -- count of samples where brake >= 0.8
    sample_count UInt32 NOT NULL           -- total samples in window
) 
ENGINE = MergeTree()
PARTITION BY toYYYYMMDD(window_start)
ORDER BY (driver_number, window_start);

-- Optional: Materialized view for real-time rollup from raw_telemetry
-- (Currently computed in Flink, but this provides ClickHouse-native alternative)
-- 
-- CREATE MATERIALIZED VIEW f1_telemetry.rollup_10s_mv TO f1_telemetry.rollup_10s AS
-- SELECT
--     toStartOfInterval(event_time, INTERVAL 10 second) AS window_start,
--     driver_number,
--     avg(speed) AS avg_speed,
--     max(speed) AS max_speed,
--     avg(throttle) AS avg_throttle,
--     countIf(brake >= 0.8) AS hard_brake_count,
--     count() AS sample_count
-- FROM f1_telemetry.raw_telemetry
-- GROUP BY window_start, driver_number;

-- =============================================================================
-- Verification queries
-- =============================================================================

-- Check data arrival
-- SELECT count(*) FROM f1_telemetry.raw_telemetry;
-- SELECT count(*) FROM f1_telemetry.rollup_10s;

-- Check latest events
-- SELECT * FROM f1_telemetry.raw_telemetry ORDER BY event_time DESC LIMIT 10;

-- Check driver distribution
-- SELECT driver_number, count(*) as cnt 
-- FROM f1_telemetry.raw_telemetry 
-- GROUP BY driver_number 
-- ORDER BY driver_number;

-- Check insert operations (system table)
-- SELECT 
--     event_time,
--     query_kind,
--     type,
--     written_rows,
--     written_bytes
-- FROM system.query_log
-- WHERE database = 'f1_telemetry' 
--   AND table = 'raw_telemetry'
-- ORDER BY event_time DESC
-- LIMIT 20;

-- Check table parts (actual data on disk)
-- SELECT 
--     partition,
--     name,
--     rows,
--     bytes_on_disk,
--     modification_time
-- FROM system.parts
-- WHERE database = 'f1_telemetry' 
--   AND table = 'raw_telemetry'
--   AND active = 1
-- ORDER BY modification_time DESC;
