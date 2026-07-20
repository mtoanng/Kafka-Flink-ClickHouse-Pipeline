# PROJECT 1 — JUNIOR-FIRST IMPLEMENTATION BLUEPRINT

Repository: `mtoanng/Kafka-Flink-ClickHouse-Pipeline`

Target name: **Taobao Real-Time Customer Behavior Platform**

Status: **Final architecture, junior-first delivery plan**

This file is the source of truth for Project 1. The architecture is intentionally designed to remain useful for years, but implementation must begin with a small end-to-end system that can actually run, be explained, and be verified by a junior Data Engineer.

---

## 1. Goal

Build a real-time data platform for Alibaba/Taobao user-behavior events.

The project must first prove the core streaming path:

```text
Taobao events
-> Python replay producer
-> Kafka + Schema Registry
-> one Java Flink job
-> ClickHouse
```

After that path is stable, extend it with:

```text
ScyllaDB current-state serving
PostgreSQL + Debezium control-plane CDC
basic observability and Terraform
```

The project is about streaming infrastructure, not recommendation, machine learning, frontend development, or application authentication.

---

## 2. Dataset

Use the public Alibaba/Taobao `UserBehavior.csv` dataset.

Canonical columns:

```text
user_id
item_id
category_id
behavior_type   # pv, cart, fav, buy
timestamp       # Unix epoch seconds
```

The raw dataset must not be committed to Git.

Required data profiles:

- `fixture`: 1,000–10,000 deterministic rows committed under `tests/fixtures/`.
- `bounded-demo`: a reproducible subset created by script and used for integration runs.
- `full-scale`: optional; never required for the junior Definition of Done.

The fixture must include:

- all four behavior types;
- at least one duplicate event;
- at least one invalid row;
- at least one out-of-order event;
- one cart followed by buy;
- one cart that remains unpurchased.

Create `docs/DATASET_NOTES.md` with source, checksum, row count, timestamp range, and behavior counts.

---

## 3. Final architecture

### 3.1 Data plane

```text
Taobao UserBehavior.csv
        |
        v
Python Replay Engine
  - chunked CSV reading
  - schema validation
  - deterministic event_id
  - configurable replay speed
  - Kafka key = user_id
        |
        v
Kafka + Schema Registry (Avro)
        |
        v
ONE Java Flink DataStream job
  - event-time timestamps and watermarks
  - invalid/late event side outputs
  - item-level 1-minute aggregation
  - keyed user activity state
  - optional cart timer logic
  - checkpointing
        |
        +------------------> ClickHouse
        |                    - raw_behavior_events
        |                    - item_metrics_1m
        |                    - behavior_alerts
        |
        +------------------> ScyllaDB
        |                    - user_current_activity
        |
```

### 3.2 Control plane — advanced, not an MVP blocker

```text
PostgreSQL.behavior_rules
        |
        v
Debezium CDC
        |
        v
Kafka compacted topic
        |
        v
Flink Broadcast State
```

Rules:

- PostgreSQL contains exactly one project table: `behavior_rules`.
- PostgreSQL is not a serving database.
- Debezium is implemented only after the main data plane works.
- No CRUD application, user table, authentication, or admin frontend is required.

---

## 4. Technology boundaries

### Required core

- Python: dataset preparation and replay producer.
- Kafka and Schema Registry: event transport and contract.
- Java Flink DataStream API: stream processing.
- ClickHouse: raw and aggregated analytical serving.
- GitLab CI or equivalent repository CI.

### Required for the full junior project

- ScyllaDB: latest user state lookup.
- Terraform: minimal cloud resources used by the demo.
- Basic operational evidence: logs, counts, checkpoint status, and query screenshots.

### Advanced extension

- PostgreSQL + Debezium + broadcast state.
- Cart-abandonment event-time timers.
- Savepoint upgrade demonstration.
- Managed observability dashboard.

### Do not add

- Spark, Airflow, MongoDB, Redis, Elasticsearch, Druid, Pinot, Kubernetes, ML, recommendation, or arbitrary SQL API.
- Multiple Flink jobs for logic that belongs in the same stream topology.
- PyFlink replacement for the Java core.

---

## 5. Minimal contracts

### 5.1 Avro event

```text
UserBehaviorEvent
- event_id: string
- user_id: long
- item_id: long
- category_id: long
- behavior_type: enum[pv, cart, fav, buy]
- event_time_ms: long
- source_sequence: long
- replay_run_id: string
```

`event_id` must be deterministic from stable source fields and source sequence.

Kafka topic:

```text
user-behavior-events
key = user_id
value = UserBehaviorEvent Avro
```

### 5.2 ClickHouse tables

#### `raw_behavior_events`

Grain: one accepted source event.

Minimum columns:

```text
event_id
user_id
item_id
category_id
behavior_type
event_time
replay_run_id
ingested_at
```

Suggested first ordering key:

```sql
ORDER BY (toDate(event_time), item_id, event_time, user_id)
```

The final key must be justified using the actual query pack.

#### `item_metrics_1m`

Grain: one item per one-minute event-time window.

```text
window_start
item_id
category_id
pv_count
cart_count
fav_count
buy_count
unique_users
```

#### `behavior_alerts`

Only required when timer/rule logic is implemented.

### 5.3 ScyllaDB table

```text
user_current_activity
partition key: user_id
```

Minimum fields:

```text
user_id
last_event_time
last_behavior_type
last_item_id
last_category_id
session_event_count
updated_at
```

ScyllaDB stores latest bounded state, not full event history.

### 5.4 PostgreSQL control table

Advanced extension only:

```text
behavior_rules
- rule_id
- rule_type
- threshold_seconds
- enabled
- version
- updated_at
```

---

## 6. Junior-first milestones

## Milestone A — Runnable streaming core

This is the first mandatory target.

Deliver:

1. deterministic fixture and bounded-demo generator;
2. Python replay producer;
3. Avro schema and compatibility test;
4. Kafka + Schema Registry integration;
5. one Java Flink job with watermark, raw branch, and one-minute item aggregation;
6. ClickHouse raw and rollup tables;
7. end-to-end smoke script;
8. README run instructions.

Acceptance criteria:

- fixture replay reaches Kafka;
- Flink consumes valid events;
- invalid fixture rows are rejected or routed explicitly;
- ClickHouse raw count matches accepted fixture events;
- item rollups match a manually calculated expected result;
- one out-of-order event test documents whether it is accepted or late;
- all credential-independent tests pass.

Milestone A is sufficient for the first resume-ready release.

## Milestone B — Complete junior platform

Implement only after Milestone A is stable.

Deliver:

1. ScyllaDB `user_current_activity` sink;
2. fixed read endpoint or CLI lookup for current state;
3. checkpoint/restart experiment;
4. minimal Terraform for the temporary cloud host and approved managed resources;
6. CI for Python, Maven, schemas, and Terraform;
7. bounded cloud demo and teardown guide.

Acceptance criteria:

- one user lookup returns the expected latest event;
- replaying the fixture does not corrupt current state;
- Flink restart behavior is documented with real logs;
- teardown is verified.

Milestone B is the junior Definition of Done.

## Milestone C — Advanced immortal extension

Optional until the junior project is complete.

Deliver:

1. PostgreSQL `behavior_rules`;
2. Debezium connector and compacted Kafka topic;
3. Flink broadcast state;
4. one rule update that changes processing without redeploy;
5. cart `MapState` and event-time timer;
6. alert output and optional Grafana dashboard;
7. savepoint-based upgrade exercise.

Milestone C must never block Milestone A or B.

---

## 7. Implementation phases for Codex

Codex must implement one phase per run.

### Phase 0 — Inventory and freeze baseline

- inspect current F1 code and tests;
- record what still builds;
- create a migration map;
- do not delete working code yet.

### Phase 1 — Dataset fixture and Python replay

- add Taobao contracts and fixture;
- implement chunked replay;
- add unit tests;
- keep cloud disabled.

Student learning task: explain event grain, Kafka key, and deterministic `event_id`.

### Phase 2 — Kafka contract and Java Flink core

- register Avro schema;
- consume in Java Flink;
- assign timestamps/watermarks;
- add invalid and late-data handling;
- add item one-minute aggregation.

Student learning task: manually calculate one fixture window and predict the late-event result.

### Phase 3 — ClickHouse end-to-end MVP

- create DDL and sink mapping;
- write raw and item metrics;
- add verification queries and smoke script;
- update README.

Student learning task: justify the ClickHouse ordering key from required queries.

### Phase 4 — ScyllaDB and current state

- project latest user state;
- write to ScyllaDB;
- add fixed lookup interface and tests.

Student learning task: explain the partition key and why history stays in ClickHouse.

### Phase 5 — Checkpoint, CI, and Terraform

- verify checkpoint restart;
- add CI;
- add minimal Terraform and teardown.

Student learning task: explain what is and is not guaranteed after a restart.

### Phase 6 — Optional CDC control plane

- add PostgreSQL, Debezium, config topic, and broadcast state;
- prove one live rule update;
- optionally add cart timers and alerts.

Student learning task: explain data plane versus control plane.

---

## 8. Resource-aware deployment

The laptop is a thin client.

### Laptop

- edit code;
- run focused Python/Java tests;
- validate schemas and Terraform;
- never run the complete stack.

### Remote development

Use Codespaces or another temporary machine for:

- Kafka + Schema Registry;
- Flink integration;
- ScyllaDB/PostgreSQL/Debezium only in their specific phase.

### Managed trials

Activate only after the client code and smoke scripts are ready:

- ClickHouse Cloud;
- ScyllaDB Cloud;
- Confluent Cloud when payment activation works;
- optional Grafana Cloud.

Use bounded-demo data, capture evidence, and destroy resources.

---

## 9. Required tests

Minimum junior test set:

- Python fixture parsing and chunked replay;
- deterministic event ID;
- Avro serialization and compatibility;
- Java aggregation test;
- watermark/late-event test;
- ClickHouse mapping/DDL contract test;
- fixture end-to-end count and rollup test;
- ScyllaDB latest-state test for Milestone B;
- Terraform `fmt -check` and `validate` when IaC is added.

One controlled failure experiment is required per phase after Phase 1.

---

## 10. Junior Definition of Done

The project is complete for the junior stage when:

1. Milestone A and B pass.
2. A fresh user can follow the README and run the fixture/bounded demo.
3. Kafka, Flink, ClickHouse, and ScyllaDB each have one clear responsibility.
4. Raw counts, rollups, and current state are verified using deterministic data.
5. At least one checkpoint/restart experiment has captured evidence.
6. Cloud resources can be destroyed safely.
7. README claims only implemented and verified features.
8. The user can explain event time, watermark, state, checkpoint, ClickHouse ordering, and ScyllaDB partitioning.

PostgreSQL/Debezium is not required for the junior Definition of Done. It remains the first advanced extension.

---

## 11. Target repository structure

```text
AGENTS.md
README.md
pyproject.toml
producer/
  src/taobao_replay/
  tests/
flink-jobs/
  taobao-stream-job/
    pom.xml
    src/main/java/
    src/test/java/
schemas/
  user-behavior-event.avsc
infra/
  clickhouse/
  scylladb/
  kafka/
  terraform/
config/
  behavior-rules.example.yaml
scripts/
  prepare_demo_subset.py
  run_fixture_demo.sh
  verify_demo.py
  teardown_demo.sh
tests/fixtures/
docs/
  PROJECT1_BLUEPRINT_FINAL.md
  DATASET_NOTES.md
  learning/
legacy/
```

CDC-specific files may be added only in Phase 6.

---

## 12. Codex rules

- Read `AGENTS.md` and this blueprint before editing.
- Implement exactly one phase.
- Prefer a small working diff over a broad rewrite.
- Keep the previous runnable path until its replacement passes.
- Never provision cloud resources without explicit approval.
- Never claim a cloud, recovery, performance, or connector result without evidence.
- End each run with changed files, commands, results, blockers, one student learning task, and three teach-back questions.
