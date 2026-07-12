-- =============================================================
--  F1 Live Telemetry — ClickHouse Schema
--  2 tables: raw_telemetry (all events) + rollup_10s (aggregated)
--  Engine: MergeTree (optimized for time-series analytics)
-- =============================================================

CREATE DATABASE IF NOT EXISTS f1_telemetry;

USE f1_telemetry;

-- ─────────────────────────────────────────────────────────────
-- Table 1: raw_telemetry — All telemetry events (from Kafka)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS raw_telemetry (
    event_time DateTime64(3),           -- Millisecond precision timestamp
    driver_number UInt8,                 -- Driver number (1-99)
    speed Float64,                       -- Speed in km/h
    throttle Nullable(Float64),          -- Throttle position 0-100%
    brake Nullable(Float64),             -- Brake pressure 0-100%
    rpm Nullable(UInt16),                -- Engine RPM
    gear Nullable(Int8),                 -- Gear (-1=R, 0=N, 1-8=forward)
    drs Nullable(UInt8)                  -- DRS status (0=off, 1=on)
) ENGINE = MergeTree()
ORDER BY (driver_number, event_time)
PARTITION BY toYYYYMMDD(event_time)
TTL event_time + INTERVAL 7 DAY        -- Auto-delete data older than 7 days
SETTINGS index_granularity = 8192;

-- ─────────────────────────────────────────────────────────────
-- Table 2: rollup_10s — 10-second windowed aggregations
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rollup_10s (
    window_start DateTime64(3),         -- Window start timestamp
    driver_number UInt8,
    avg_speed Float64,                   -- Average speed in window
    max_speed Float64,                   -- Max speed in window
    avg_throttle Float64                 -- Average throttle position
) ENGINE = MergeTree()
ORDER BY (driver_number, window_start)
PARTITION BY toYYYYMMDD(window_start)
TTL window_start + INTERVAL 7 DAY
SETTINGS index_granularity = 8192;

-- ─────────────────────────────────────────────────────────────
-- Indexes for common query patterns
-- ─────────────────────────────────────────────────────────────
-- Skip index for time-range queries
ALTER TABLE raw_telemetry ADD INDEX idx_event_time event_time TYPE minmax GRANULARITY 4;
ALTER TABLE rollup_10s ADD INDEX idx_window_start window_start TYPE minmax GRANULARITY 4;

-- ─────────────────────────────────────────────────────────────
-- Sample queries for verification
-- ─────────────────────────────────────────────────────────────
-- SELECT count() FROM raw_telemetry;
-- SELECT count() FROM rollup_10s;
-- SELECT driver_number, max(speed) as top_speed FROM raw_telemetry GROUP BY driver_number ORDER BY top_speed DESC;
