# Project 1 Codebase Completion Report

Audit date: 2026-07-21. This is a credential-independent codebase report.
No Terraform apply, destroy, Astra connection, or cloud E2E run was performed.

## Gate Decision

`CODEBASE-READY: NOT YET SATISFIED`.

The active-cart implementation is Apache Cassandra using CQL through
the Apache Cassandra Java Driver. The core path has no Cassandra requirements
when `CASSANDRA_ENABLED=false`; Astra configuration is required only for the
enabled serving branch. The final status depends on the credential-independent
validation suite recorded for this migration. Terraform provider initialization
and validation remain pending.

`DEPLOYMENT-VERIFIED: NOT VERIFIED`.

This requires Astra database provisioning, table initialization, Flink writes,
fixed lookup, replay/restart evidence, and teardown or resource-cleanup proof.

## Active-Cart Contract

- `user_active_cart` is partitioned by `user_id` with `item_id` as its clustering key.
- ClickHouse owns history and analytical aggregates; Cassandra holds bounded active-cart items only.
- The sink relies on Flink keyed `MapState` to reject stale cart/buy mutations before idempotent Cassandra upserts or deletes.
- Flink checkpoints provide at-least-once processing when configured; duplicate/retry writes are row-level idempotent, while the remaining failure window is not exactly-once delivery.

## Verification Boundary

Static checks can prove configuration, CQL shape, compilation, tests, schema
parsing, Compose rendering, Terraform validation, shell syntax, links, and
secret scanning. They cannot prove Astra connectivity, live CQL behavior,
Flink writes, recovery, or teardown.
