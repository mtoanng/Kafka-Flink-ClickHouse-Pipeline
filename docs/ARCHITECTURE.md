# Architecture

## Official topology

```text
Taobao UserBehavior.csv
        |
        v
Python deterministic replay
        |
        v
Kafka + Avro + Schema Registry
        |
        v
one Java Flink DataStream job
  |-- validation -------------> ClickHouse invalid_behavior_events
  |-- event time / watermark -> ClickHouse late_behavior_events
  |-- valid raw -------------> ClickHouse raw_behavior_events
  |-- one-minute windows ----> ClickHouse item_metrics_1m
  |-- keyed active cart -----> Cassandra user_active_cart
  `-- Broadcast State/timers -> ClickHouse behavior_alerts (`full` only)

PostgreSQL -> Debezium -> compacted behavior-rules topic ----^
```

There is one Flink job and one replay producer. PostgreSQL/Debezium is a
control plane, not a serving database.

## Storage responsibilities

| System | Owns | Does not own |
| --- | --- | --- |
| ClickHouse | Accepted event history, one-minute item metrics, invalid/late audits, behavior alerts, analytical queries | Current per-user operational state |
| Cassandra | Current active-cart rows queried by one `user_id` partition | Raw history, rollups, arbitrary analytics |
| PostgreSQL | Versioned behavior-rule source rows | User activity or active carts |

The Cassandra primary key is `PRIMARY KEY ((user_id), item_id)`. A `cart`
upserts one item; a `buy` deletes it; `pv` and `fav` do nothing. Flink keyed
state compares event time and then source sequence so a stale cart cannot
recreate an item after a newer buy.

## Event and time contracts

One CSV row becomes one `UserBehaviorEvent`. Its deterministic `event_id` is
derived from the five source values and zero-based source sequence; replay run
ID is deliberately excluded. Kafka is keyed by `user_id`.

Flink emits an immediate watermark at `maximum event time - 5,001 ms`. A valid
event at or behind the current watermark is late. Valid late events remain in
raw ClickHouse history but do not affect minute metrics, active cart, or rule
timers.

## Delivery semantics

| Boundary | Model | Consequence |
| --- | --- | --- |
| Kafka to Flink | Checkpointed at least once | Records may be processed again after failure. |
| Flink to ClickHouse | Non-transactional at-least-once sink | Physical duplicates can be inserted. |
| ClickHouse query contract | Stable logical keys plus `ReplacingMergeTree(record_version)` and deduplicated views | Business results are effectively once after explicit deduplication. |
| Flink to Cassandra | Idempotent prepared upsert/delete by primary key | Retrying the same cart mutation converges to the same row state. |

At-least-once transport describes delivery attempts. Idempotence describes the
effect of repeating a write. Effectively-once describes the committed query
result after deduplication. None of these creates a distributed transaction
across Kafka, ClickHouse, and Cassandra, so the platform does not claim global
exactly-once delivery.

## Runtime profiles

- `checks`: no services; unit, contract, format, package, Compose, Terraform,
  and secret checks.
- `core`: Kafka, Schema Registry, one Flink job, ClickHouse, and Cassandra.
- `full`: core plus PostgreSQL, Debezium, Broadcast State, event-time rule
  timers, durable alerts, and Grafana.

`core` and `full` fail before Flink execution if Cassandra configuration is
missing. Cassandra connection mode changes only the session builder; active-cart
business logic and prepared statements are shared.

