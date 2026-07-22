# Final Codebase-Hardening Phase Report

Date: 2026-07-23

Status: **implemented and statically verified**. No cloud resources were
created, no complete service stack was started, and production readiness is not
claimed.

## 1. Phase implemented

One final codebase-hardening phase aligned the existing single Java Flink job
with the current blueprint: Cassandra is mandatory in `core` and `full`,
ClickHouse has deterministic replay/deduplication contracts, invalid/late/alert
records are durable, and operator profiles are `checks`, `core`, and `full`.
No later phase was started.

## 2. Files changed

- Runtime and Java topology: `RuntimeProfileConfig`, `TaobaoStreamJob`,
  `BehaviorAuditEvent`, `BehaviorAlert`, validation/timer processing, Cassandra
  configuration/session/sink, and ClickHouse mapping/sink classes.
- Java contracts: checkpoint, runtime-profile, Cassandra config/CQL/sink,
  ClickHouse DDL/delivery/mapping, and existing behavior tests.
- Data contracts: replay validation, deterministic fixture generator and CSV,
  independent reconciliation, and Python contract/profile/source tests.
- Storage/runtime: ClickHouse schema/verification, Cassandra local keyspace,
  Compose profiles, Grafana queries, and environment examples.
- Operations: schema, preflight, run, health, fixture, ClickHouse verification,
  bounded-pipeline verification, Terraform bootstrap/variables/examples/lockfile,
  Make targets, and GitLab CI.
- Documentation: blueprint, README, architecture, runtime configuration, local,
  cloud, recovery, implementation status, codebase index, learning material, and
  final E2E evidence contract.

The pre-existing user moves into `docs/archive/` were preserved. This phase did
not rewrite or reverse those moves.

## 3. Commands and results

- `PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v`:
  **38 passed**.
- `PYTHONPATH=producer/src pytest -q`: **38 passed, 3 subtests passed** using the
  host's Python 3.12 Kafka package and installed pytest package path.
- `ruff check .` and `ruff format --check .`: **passed**, 27 files formatted.
- `mvn -B -pl flink-jobs/taobao-stream-job -am package`: **BUILD SUCCESS**,
  **40 tests passed**, shaded JAR produced. Maven reported existing shade overlap
  warnings; no test or package failure occurred.
- `docker compose -f infra/docker-compose.yml --profile core config --quiet`:
  **passed**.
- `docker compose -f infra/docker-compose.yml --profile full config --quiet`:
  **passed**. Docker emitted a host config access warning only.
- `terraform -chdir=infra/terraform init -backend=false -input=false`:
  **passed**; providers initialized, no plan/apply performed.
- `terraform -chdir=infra/terraform fmt -check -recursive`: **passed**.
- `terraform -chdir=infra/terraform validate`: **passed**.
- JSON/Avro parsing and SQL/CQL assertions: **passed**.
- Active Markdown local-link check, excluding generated provider and archived
  material: **passed**.
- Tracked `.env` / Secure Connect Bundle check: **passed**, none tracked.
- `git diff --check`: **passed** with repository-local safe-directory override.
- `bash -n`: **NOT VERIFIED** because this Windows host has no WSL distribution.

## 4. Verified acceptance criteria

- Cassandra is a mandatory core/full runtime dependency and configuration fails
  before submission when required settings are absent.
- One Java job retains Kafka/Avro, validation, event time/watermarks, late routing,
  minute aggregation, ClickHouse, active cart, Broadcast State, and timers.
- `cart` upserts, `buy` deletes, and `pv`/`fav` do not mutate active-cart serving;
  stale event-time mutations remain protected.
- ClickHouse logical keys and versions support deduplicated query views; durable
  invalid, late, and alert tables replace console-only handling.
- Checkpointing defaults on for core/full and remains off/configurable for checks.
- The bounded fixture independently expects 1,000 produced, 999 accepted raw,
  1 invalid, 2 late, 997 on-time, and active item 501 for user 100.
- Credential-independent Java, Python, schema, Compose, and Terraform checks pass.

## 5. NOT VERIFIED

- Live Kafka/Schema Registry authentication, Avro registration, and delivery.
- Actual Flink submission, durable checkpoints/savepoints, failure recovery, and
  connector classloading on the selected runtime.
- ClickHouse DDL execution, connector writes, merge behavior, and deduplicated
  results on a live ClickHouse version.
- Local Cassandra/Astra connection, prepared mutations, lookup, timeout, and
  restart behavior.
- PostgreSQL/Debezium live rule propagation, timer alerts, and Grafana rendering.
- Linux/bash execution of operational scripts.
- Cloud permissions, teardown, costs, latency, throughput, and scale.
- CI is defined but its remote pipeline status was not queried in this run.

## 6. Blockers and risks

- Existing ClickHouse tables are not migrated by `CREATE IF NOT EXISTS`; use a
  fresh database or a separately reviewed migration before replay.
- The shaded JAR contains dependency overlap warnings, especially from the
  all-in-one ClickHouse connector. Packaging passes, but remote classloading is a
  live verification gate.
- `ReplacingMergeTree` convergence is not a cross-system transaction and merges
  are asynchronous; consumers must use the committed deduplicated views.
- Cassandra and ClickHouse can temporarily diverge after a partial external-sink
  failure. Stable replay identifiers and idempotent primary-key mutations provide
  bounded recovery, not global exactly once.
- Astra keyspace and Secure Connect Bundle lifecycle remain operator-managed.

## 7. Cleanup report

Removed:

- `flink-jobs/taobao-stream-job/src/main/java/com/taobao/behavior/model/InvalidBehaviorEvent.java`
  after replacement by the shared durable audit model.
- Console-only invalid, late, and alert sink wiring from the active topology.

Archived:

- None by this phase. Existing user-created archive moves were kept unchanged.

Kept:

- One Java DataStream job and the existing Kafka, Avro, ClickHouse, Cassandra,
  PostgreSQL/Debezium, Broadcast State, timer, Terraform, and deployment assets.
- Historical SQL/PyFlink feasibility documents with a superseded-snapshot banner.

Reason:

- These artifacts remain part of the locked, junior-friendly architecture or
  preserve prior audit context without changing the active implementation.

## 8. Student learning task

Explain why this design provides at-least-once transport, idempotent Cassandra
writes, and effectively-once ClickHouse query results, but cannot claim
transactional exactly-once across both databases.

## 9. Teach-back questions

1. Why must ClickHouse queries use the deduplicated views after a retry?
2. How do event-time ordering and Cassandra's primary key prevent a stale `cart`
   event from undoing a newer `buy`?
3. Which state must remain durable for a Flink checkpoint restart to reproduce
   the same active-cart and one-minute results?

## 10. Controlled failure experiment

On a disposable Linux host, unset `CASSANDRA_HOSTS` while
`RUNTIME_PROFILE=core` and `CASSANDRA_MODE=local`, then submit the job. Predict
that configuration validation fails before job execution with a named missing
variable. Restore the value, submit the same JAR, and record both outputs without
including secrets.

## 11. Phase boundary

Only the final codebase-hardening phase was implemented. No cloud deployment,
resource provisioning, live recovery test, or later performance phase was
started.
