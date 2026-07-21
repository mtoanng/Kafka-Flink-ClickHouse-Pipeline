# Project 1 Interview Guide

## Architecture Explanation

Flink materializes the same behavior stream into two query-optimized views:
ClickHouse for historical analytics and Apache Cassandra for low-latency
per-user active-cart serving. Grafana is an optional ClickHouse client, and
PostgreSQL/Debezium is an optional control plane.

## Teach-Back Questions

1. Why does ClickHouse own event history while Cassandra owns active-cart rows?
2. What does `PRIMARY KEY ((user_id), item_id)` guarantee for the lookup?
3. How do Flink keyed state and event timestamps prevent a stale cart from
   recreating an item after a buy?
4. Why must the core topology remain runnable with `CASSANDRA_ENABLED=false`?
5. What failure window remains when Cassandra writes are idempotent but are not
   claimed to be exactly once?

