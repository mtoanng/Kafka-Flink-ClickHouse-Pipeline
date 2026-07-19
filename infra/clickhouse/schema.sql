-- Taobao real-time customer behavior, Phase 3.
-- Apply with: PYTHONPATH=producer/src python scripts/apply_clickhouse_schema.py

CREATE DATABASE IF NOT EXISTS taobao_behavior;

CREATE TABLE IF NOT EXISTS taobao_behavior.raw_behavior_events (
    event_id String,
    user_id UInt64,
    item_id UInt64,
    category_id UInt64,
    behavior_type LowCardinality(String),
    event_time DateTime64(3, 'UTC'),
    replay_run_id String,
    ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(event_time)
ORDER BY (toDate(event_time), item_id, event_time, user_id);

CREATE TABLE IF NOT EXISTS taobao_behavior.item_metrics_1m (
    window_start DateTime64(3, 'UTC'),
    item_id UInt64,
    category_id UInt64,
    pv_count UInt64,
    cart_count UInt64,
    fav_count UInt64,
    buy_count UInt64,
    unique_users UInt64,
    replay_run_id String,
    ingested_at DateTime64(3, 'UTC') DEFAULT now64(3)
)
ENGINE = MergeTree
PARTITION BY toYYYYMM(window_start)
ORDER BY (toDate(window_start), item_id, window_start, replay_run_id);
