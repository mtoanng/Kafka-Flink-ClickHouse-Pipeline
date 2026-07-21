# Active-Cart Serving Audit

Audit date: 2026-07-21. No Astra connection, Terraform apply/destroy, or
cloud execution was performed.

## Architecture Verdict

Flink materializes the same behavior stream into two query-optimized views:
ClickHouse for historical analytics and Apache Cassandra for low-latency
per-user active-cart serving.

ClickHouse is the only owner of raw behavioral history, item-level metrics,
behavior aggregations, and analytical SQL. Cassandra contains only bounded
active-cart rows in `user_active_cart`, with `PRIMARY KEY ((user_id), item_id)`.
The only supported Cassandra access pattern is returning all active items for
one `user_id`. Grafana is an optional visualization client over ClickHouse.
PostgreSQL, Debezium, and Flink Broadcast State remain an optional control
plane.

## Event Contract

| Behavior | Cassandra mutation |
| --- | --- |
| `cart` | Upsert one `(user_id, item_id)` row |
| `buy` | Delete one `(user_id, item_id)` row |
| `pv` | No mutation |
| `fav` | No mutation |

Flink keyed `MapState` retains item-level lifecycle and event ordering. A
newer buy leaves an inactive tombstone in Flink state, so a stale cart cannot
recreate the item. Repeated cart and buy events are safe at the materialized
row level. Cassandra delivery is not claimed to be exactly once.

## Classification

### IMPLEMENTED AND LOCALLY/STATICALLY VERIFIED

- Active-cart domain model, keyed projection, stale-event rejection, and
  explicit `UPSERT_CART_ITEM`/`DELETE_CART_ITEM` mutations.
- Prepared CQL INSERT and DELETE statements, user/item binding, session reuse,
  deterministic close, fixed lookup CLI, and disabled-core topology gating.
- Deterministic fixture: user 100 adds items 500 and 501, buys 500, and leaves
  item 501 as the expected active result.
- Configuration and secret-safe validation, CQL contract tests, unit tests,
  Python checks, and Maven checks listed in the completion report.

### IMPLEMENTED BUT REQUIRES ASTRA VERIFICATION

- Secure Connect Bundle loading, token authentication, Astra session behavior,
  table bootstrap, Flink writes, lookup, replay/retry, and restart behavior.

### TERRAFORM VALIDATED BUT NOT APPLIED

- The optional `datastax/astra` module is formatted and defined for a
  non-vector Astra DB Serverless database and initial keyspace. Provider
  initialization and `terraform validate` were not completed in this run.

### OPTIONAL AND NOT REQUIRED

- Grafana visualization, cart expiration, live CDC rule updates, and cloud
  recovery/teardown evidence.

### FAIL

- Final CODEBASE-READY gate: not awarded while the credential-independent
  Terraform, Compose, shell, and Markdown checks remain incomplete.

