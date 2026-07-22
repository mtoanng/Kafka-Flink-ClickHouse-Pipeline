-- Taobao real-time customer behavior tables.
-- Transport into these tables is at least once. Stable keys plus
-- ReplacingMergeTree make the *_deduplicated views effectively once for queries.

CREATE DATABASE IF NOT EXISTS taobao_behavior;

CREATE TABLE IF NOT EXISTS taobao_behavior.raw_behavior_events (
    event_id String,
    replay_run_id String,
    user_id UInt64,
    item_id UInt64,
    category_id UInt64,
    behavior_type LowCardinality(String),
    event_time DateTime64(3, 'UTC'),
    source_sequence UInt64,
    record_version UInt64,
    ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)
)
ENGINE = ReplacingMergeTree(record_version)
PARTITION BY toYYYYMM(event_time)
ORDER BY (replay_run_id, event_id);

CREATE VIEW IF NOT EXISTS taobao_behavior.raw_behavior_events_deduplicated AS
SELECT
    event_id,
    replay_run_id,
    user_id,
    item_id,
    category_id,
    behavior_type,
    event_time,
    source_sequence,
    ingested_at
FROM taobao_behavior.raw_behavior_events FINAL;

CREATE TABLE IF NOT EXISTS taobao_behavior.item_metrics_1m (
    window_start DateTime64(3, 'UTC'),
    replay_run_id String,
    item_id UInt64,
    category_id UInt64,
    pv_count UInt64,
    cart_count UInt64,
    fav_count UInt64,
    buy_count UInt64,
    unique_users UInt64,
    record_version UInt64,
    ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)
)
ENGINE = ReplacingMergeTree(record_version)
PARTITION BY toYYYYMM(window_start)
ORDER BY (replay_run_id, window_start, item_id);

CREATE VIEW IF NOT EXISTS taobao_behavior.item_metrics_1m_deduplicated AS
SELECT
    window_start,
    replay_run_id,
    item_id,
    category_id,
    pv_count,
    cart_count,
    fav_count,
    buy_count,
    unique_users,
    ingested_at
FROM taobao_behavior.item_metrics_1m FINAL;

CREATE TABLE IF NOT EXISTS taobao_behavior.invalid_behavior_events (
    event_id String,
    replay_run_id String,
    user_id Int64,
    item_id Int64,
    category_id Int64,
    event_time_ms Int64,
    source_sequence Int64,
    reason_code LowCardinality(String),
    reason_message String,
    record_version Int64,
    ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)
)
ENGINE = ReplacingMergeTree(record_version)
PARTITION BY tuple()
ORDER BY (replay_run_id, event_id, reason_code);

CREATE VIEW IF NOT EXISTS taobao_behavior.invalid_behavior_events_deduplicated AS
SELECT * EXCEPT record_version
FROM taobao_behavior.invalid_behavior_events FINAL;

CREATE TABLE IF NOT EXISTS taobao_behavior.late_behavior_events (
    event_id String,
    replay_run_id String,
    user_id Int64,
    item_id Int64,
    category_id Int64,
    event_time_ms Int64,
    source_sequence Int64,
    reason_code LowCardinality(String),
    reason_message String,
    record_version Int64,
    ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)
)
ENGINE = ReplacingMergeTree(record_version)
PARTITION BY tuple()
ORDER BY (replay_run_id, event_id, reason_code);

CREATE VIEW IF NOT EXISTS taobao_behavior.late_behavior_events_deduplicated AS
SELECT * EXCEPT record_version
FROM taobao_behavior.late_behavior_events FINAL;

CREATE TABLE IF NOT EXISTS taobao_behavior.behavior_alerts (
    event_id String,
    replay_run_id String,
    user_id UInt64,
    item_id UInt64,
    category_id UInt64,
    event_time DateTime64(3, 'UTC'),
    alert_time DateTime64(3, 'UTC'),
    rule_id String,
    rule_version UInt64,
    reason_code LowCardinality(String),
    reason_message String,
    record_version UInt64,
    ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)
)
ENGINE = ReplacingMergeTree(record_version)
PARTITION BY toYYYYMM(alert_time)
ORDER BY (replay_run_id, event_id);

CREATE VIEW IF NOT EXISTS taobao_behavior.behavior_alerts_deduplicated AS
SELECT * EXCEPT record_version
FROM taobao_behavior.behavior_alerts FINAL;
