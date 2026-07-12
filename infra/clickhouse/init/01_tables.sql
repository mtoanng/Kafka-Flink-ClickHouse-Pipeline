-- F1 Live Telemetry Platform — ClickHouse DDL
-- 2 tables: raw telemetry + 10-second windowed rollup

CREATE TABLE IF NOT EXISTS raw_telemetry
(
    event_time    DateTime64(3),
    driver_number Int32,
    speed         Float64,
    throttle      Nullable(Float64),
    brake         Nullable(Float64),
    rpm           Nullable(Int32),
    gear          Nullable(Int32),
    drs           Nullable(Int32)
)
ENGINE = MergeTree()
ORDER BY (driver_number, event_time)
TTL event_time + INTERVAL 7 DAY;

CREATE TABLE IF NOT EXISTS rollup_10s
(
    window_start  DateTime64(3),
    driver_number Int32,
    avg_speed     Float64,
    max_speed     Float64,
    avg_throttle  Float64
)
ENGINE = MergeTree()
ORDER BY (driver_number, window_start)
TTL window_start + INTERVAL 7 DAY;
