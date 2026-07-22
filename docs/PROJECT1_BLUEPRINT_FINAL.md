# PROJECT 1 — JUNIOR-FIRST IMPLEMENTATION BLUEPRINT

Repository: `mtoanng/Kafka-Flink-ClickHouse-Pipeline`

Target name: **Taobao Real-Time Customer Behavior Platform**

Status: **Final architecture, junior-first delivery plan, documentation-aligned**

This file is the source of truth for Project 1. The architecture is intentionally designed to remain useful for years, but implementation must begin with a small end-to-end system that can actually run, be explained, and be verified by a junior Data Engineer.

---

## 1. Goal

Build a real-time data platform for Alibaba/Taobao user-behavior events.

The project must first prove the independently runnable core streaming path:

```text
Taobao events
-> Python replay producer
-> Kafka + Schema Registry
-> one Java Flink job
-> ClickHouse
```

The complete junior platform extends that path with:

```text
Apache Cassandra active-cart serving (local development or DataStax Astra DB Serverless)
PostgreSQL + Debezium control-plane CDC
basic observability and the repository's deployment artifacts
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
  - keyed active-cart state in every runtime profile
  - cart timer logic when CDC/rules are enabled
  - checkpointing enabled by default in runtime profiles
        |
        +------------------> ClickHouse
        |                    - raw_behavior_events
        |                    - item_metrics_1m
        |                    - behavior_alerts
        |
        +------------------> Apache Cassandra (mandatory runtime serving layer)
        |                    - user_active_cart
        |
```

### 3.2 Runtime profiles

The repository exposes one service-free checks profile and two runtime profiles.
Both runtime profiles use the same single Java Flink job and always materialize
the active cart in Cassandra:

| Profile | Enabled path | Required external services |
| --- | --- | --- |
| `checks` | Unit/contract tests, formatting, package, Compose rendering, Terraform validation | None |
| `core` | Replay, Kafka/Schema Registry, one Flink job, ClickHouse, and Apache Cassandra active cart | Kafka, Schema Registry, ClickHouse, Cassandra local or Astra |
| `full` | `core` plus PostgreSQL, Debezium, compacted rules, Broadcast State, timers, and Grafana | Core services plus PostgreSQL, Kafka Connect, and Grafana |

The `core` and `full` profiles fail fast when Cassandra configuration is absent.
The `checks` profile never opens Kafka, ClickHouse, Cassandra, PostgreSQL, or
Grafana connections. The `full` profile is the only profile that creates the
CDC source, Broadcast State, timers, and alert sink. No profile creates a
second Flink job.

### 3.3 Kafka and Schema Registry portability

Kafka and Schema Registry endpoints, security protocol, SASL mechanism,
credentials, topic names, group names, and registry authentication are supplied
through environment variables. The same data-plane code must support local
plaintext (`PLAINTEXT`, no SASL) and managed `SASL_SSL` without source changes.
Provider-specific Terraform resources, image names, and cloud APIs must not
leak into the Java or Python application contract.

S3 is not an event-data sink. Checkpoints, Terraform state, or unrelated
deployment storage must never be represented as raw-event archival behavior or
as another branch of the data plane.

### 3.4 Control plane — advanced, not an MVP blocker

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
- The generated Debezium value schema must pass an automated Avro
  reader/writer compatibility test against `BehaviorRule` before a live
  connector is used.
- No CRUD application, user table, authentication, or admin frontend is required.

---

## 4. Technology boundaries

### Required core

- Python: dataset preparation and replay producer.
- Kafka and Schema Registry: event transport and contract.
- Java Flink DataStream API: stream processing.
- ClickHouse: raw and aggregated analytical serving.
- GitLab CI or equivalent repository CI.
- Provider-neutral Kafka configuration supporting local plaintext and managed
  `SASL_SSL` through environment variables.

### Required for the full junior project

- Apache Cassandra: mandatory per-user active-cart lookup in every runtime
  profile. Local development uses one Cassandra node; the initial managed
  target is non-vector DataStax Astra DB Serverless.
- Terraform, Docker Compose, bootstrap, CI, verification, and teardown code are
  part of the complete repository and must remain aligned with this blueprint.
- Basic operational evidence: logs, counts, checkpoint status, and query screenshots.

### Advanced extension

- PostgreSQL + Debezium + broadcast state.
- Cart-abandonment event-time timers.
- Savepoint upgrade demonstration.
- Managed observability dashboard.

### Deployment prerequisites

Any deployment artifact must document and validate installation of:

1. a supported Java runtime matching the Flink job target;
2. an Apache Flink distribution and CLI compatible with the compiled job;
3. the versioned application artifact, generated schemas, and required runtime
   configuration on the target host;
4. Kafka topic, consumer-group, producer, consumer, and Schema Registry
   read/write/describe permissions for every identity used by replay, Flink,
   and Debezium;
5. ClickHouse and Cassandra for every runtime profile, and PostgreSQL/Debezium
   plus Grafana only for `full`.

Bootstrap may install software and copy artifacts, but it must not embed
credentials. A successful Terraform plan or container start is not deployment
verification.

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

The codebase-ready tables use stable logical keys with `ReplacingMergeTree`
and deterministic version columns. Verification queries or views must use
deduplicated results (`FINAL` or an equivalent explicit query) so replay and
checkpoint recovery duplicates do not permanently change analytical answers.
This is effectively-once query behavior over at-least-once transport, not a
transactional exactly-once claim.

Required raw logical key:

```sql
ORDER BY (replay_run_id, event_id)
```

The query pack may add projections for item/time analysis without changing the
deduplication identity.

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

Required in `full` when timer/rule logic is enabled. Invalid and late events are
also durable ClickHouse audit tables:

```text
invalid_behavior_events
late_behavior_events
behavior_alerts
```

Each audit row carries a deterministic event identifier, replay run, available
user/item/category identifiers, event time, ingestion time, reason code, and a
concise reason message.

### 5.3 Apache Cassandra active-cart table

```text
user_active_cart
partition key: user_id
clustering key: item_id
```

Minimum fields:

```text
user_id
item_id
category_id
added_at
last_updated_at
```

Flink materializes the same behavior stream into two query-optimized views: ClickHouse for historical analytics and Apache Cassandra for low-latency per-user active-cart serving. Apache Cassandra stores bounded active-cart items only, not full event history. ClickHouse owns event history and analytical aggregates. Grafana is an optional ClickHouse visualization client, not part of the critical data path.

The logical database technology is Apache Cassandra. The initial managed deployment target is DataStax Astra DB Serverless, non-vector. The application uses CQL through the Apache Cassandra Java Driver; it must not use the Astra Data API for this sink.

Connection configuration contract:

```text
CASSANDRA_MODE             # local or astra
CASSANDRA_HOSTS            # local, comma-separated
CASSANDRA_PORT             # local
CASSANDRA_DATACENTER       # local
CASSANDRA_KEYSPACE
CASSANDRA_TABLE
CASSANDRA_USERNAME         # optional local pair
CASSANDRA_PASSWORD         # optional local pair
ASTRA_DB_SECURE_BUNDLE_PATH
ASTRA_DB_APPLICATION_TOKEN
CASSANDRA_CONNECT_TIMEOUT_MS
CASSANDRA_REQUEST_TIMEOUT_MS
```

Tokens and Secure Connect Bundles must never be committed. The Secure Connect
Bundle is supplied as a runtime secret file, and the token is supplied through
environment variables or secret management. Keyspace provisioning is separate
from application startup. Application code must not attempt `CREATE KEYSPACE`;
table bootstrap must be idempotent. Local and Astra sessions share the same cart
projection and prepared mutation code.

Active-cart semantics are event-time ordered in Flink keyed state before Cassandra mutation: `cart` upserts an item, `buy` deletes its matching item, and `pv`/`fav` produce no Cassandra mutation. Repeated cart or buy events are idempotent, and a stale cart cannot recreate an item after a newer buy. No TTL or timer-based cart expiration is required for the codebase-ready release; it is an optional later extension.

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
- the `core` profile fails fast without Cassandra configuration and runs without
  PostgreSQL/Debezium/Grafana configuration;
- the `checks` profile requires no external service configuration.

Milestone A remains the historical analytical foundation. It is not sufficient
for the current codebase-ready runtime contract, which also requires the
Milestone B Cassandra serving layer.

## Milestone B — Complete junior platform

Implement only after Milestone A is stable.

Deliver:

1. Apache Cassandra `user_active_cart` sink;
2. fixed CLI lookup for a user's active cart;
3. checkpoint/restart experiment;
4. valid Terraform, Docker, bootstrap, verification and guarded teardown artifacts;
6. CI for Python, Maven, schemas, and Terraform;
7. bounded cloud demo and teardown guide.

Acceptance criteria:

- one user lookup returns only the unpurchased active-cart items;
- replaying the fixture does not corrupt active-cart state;
- Flink restart behavior is documented with real logs;
- teardown procedure is statically validated; real teardown remains a separate
  deployment gate.

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

The CDC deliverable also requires a compatibility test that serializes the
Debezium-generated `behavior_rules` value schema and resolves it with the
canonical `BehaviorRule` reader schema.

Milestone C must never block Milestone A or B.

---

## 7. Implementation phases for Codex

Codex must implement one phase per run.

### Phase 0 — Inventory and freeze baseline

- inspect superseded legacy code and tests;
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

### Phase 4 — Apache Cassandra and active cart

- materialize per-user cart and buy lifecycle state;
- write to Apache Cassandra through the Apache Cassandra Java Driver;
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

### Final codebase-hardening phase — Cassandra core and delivery semantics

- make Cassandra mandatory in `core` and `full`, supporting local and Astra;
- enable runtime checkpointing by default without claiming global exactly once;
- add stable ClickHouse replacement keys and deduplicated verification paths;
- persist invalid, late, and alert audit streams in ClickHouse;
- align checks/core/full profiles, CI, bounded verification, and runbooks.

Student learning task: explain at-least-once transport, idempotent writes,
effectively-once queries, and why they are not transactional exactly once.

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
- Cassandra/PostgreSQL/Debezium only in their specific phase.

### Managed trials

Activate only after the client code and smoke scripts are ready. Provider choice
is a deployment concern and must not alter application code:

- ClickHouse Cloud;
- DataStax Astra DB Serverless, non-vector;
- Confluent Cloud when payment activation works;
- optional Grafana Cloud.

Use bounded-demo data, capture evidence, and destroy resources.

### Deployment handover contract

The deployment package must install Java, Apache Flink, the built application
artifact, generated Avro/schema assets, and profile-specific configuration
before submitting a job. It must document complete Kafka permissions for
topics, consumer groups, schema subjects, and registry operations. Missing
full-only credentials must not be required by core startup. Cassandra is not
full-only: missing Cassandra configuration must fail both runtime profiles.

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
- Apache Cassandra latest-state test for Milestone B;
- CDC `BehaviorRule` Avro reader/writer compatibility test;
- checks/core/full startup-configuration tests;
- provider-neutral Kafka plaintext and `SASL_SSL` configuration tests;
- Terraform `fmt -check` and `validate` when IaC is added;
- Docker Compose rendering, bootstrap prerequisite, verification and teardown
  contract checks.

One controlled failure experiment is required per phase after Phase 1.

---

## 10. Junior Definition of Done

The project is complete for the junior stage when:

1. Milestone A and B pass.
2. A fresh user can follow the README and run the fixture/bounded demo.
3. Kafka, Flink, ClickHouse, and Apache Cassandra each have one clear responsibility.
4. Raw counts, rollups, and active-cart lookup are verified using deterministic data.
5. At least one checkpoint/restart experiment has captured evidence.
6. Cloud resources can be destroyed safely.
7. README claims only implemented and verified features.
8. The user can explain event time, watermark, state, checkpoint, ClickHouse ordering, and Cassandra partitioning.

PostgreSQL/Debezium is not required for the junior Definition of Done. It remains the first advanced extension.

---

## 11. Release gates

### CODEBASE-READY

This gate is credential-independent. It passes only when Cassandra integration code
and Astra infrastructure/configuration code are complete, application modules,
schemas, Terraform, Docker Compose, bootstrap, CI, verification, teardown
scripts and documentation are internally consistent; checks/core/full profiles
are defined; full-only components are absent from core startup;
all unit, contract, formatting, compilation and static configuration checks
pass; and no active S3 event-data sink is present. Real Astra connectivity remains pending.

`CODEBASE-READY` does not mean Kafka, Flink, sinks, CDC, Grafana, cloud
resources or recovery have run.

### DEPLOYMENT-VERIFIED

This separate gate requires real bounded evidence from the target environment:
Kafka delivery and permissions, Schema Registry compatibility, Flink execution,
ClickHouse results, Astra database provisioning, table initialization, Flink writes,
active-cart lookup, replay and restart behavior, CDC `BehaviorRule` compatibility
and live rule update, observability output, and teardown or resource cleanup.
Until that evidence exists, the status is `NOT VERIFIED`; Terraform plan,
Compose rendering, or a process start is insufficient.

## 12. Target repository structure

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
  cassandra/
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

## 13. Codex rules

- Read `AGENTS.md` and this blueprint before editing.
- Implement exactly one phase.
- Prefer a small working diff over a broad rewrite.
- Keep the previous runnable path until its replacement passes.
- Never provision cloud resources without explicit approval.
- Never claim a cloud, recovery, performance, or connector result without evidence.
- End each run with changed files, commands, results, blockers, one student learning task, and three teach-back questions.

---

## 14. Blueprint change log

### Database migration changelog

```text
ScyllaDB
-> Apache Cassandra
-> DataStax Astra DB Serverless deployment target
```

This is an architecture-contract migration only. It does not claim a real
Astra deployment.

This change log records the documentation-only alignment performed on
2026-07-21. It distinguishes architecture-contract changes from implementation
defects that still require a later code phase.

| Audit finding | Classification | Blueprint consequence |
| --- | --- | --- |
| Cassandra was optional in core | Superseded architecture decision | Cassandra is mandatory in core/full; only CDC, timers, and Grafana remain full-only. |
| Compose `core` does not contain the broker/registry path it documents | Implementation defect | Runtime profiles and the required core path are explicit; Compose must be corrected in a code phase. |
| Kafka security differs between local and managed examples | Architecture clarification | One provider-neutral contract now covers plaintext and `SASL_SSL`. |
| S3 archive language survived in configuration and historical evidence | Architecture clarification plus cleanup defect | S3 is explicitly excluded from event data; stale active references remain proposed cleanup. |
| Terraform, bootstrap, CI, verification and teardown were treated as release add-ons | Scope clarification | They are part of the complete codebase and the `CODEBASE-READY` gate. |
| EC2 bootstrap lacks Java, Flink and application artifact installation | Implementation defect | Deployment prerequisites now require all three before job submission. |
| Kafka ACL coverage is incomplete | Implementation defect | Complete topic, group and Schema Registry permissions are now required. |
| Debezium writer-schema compatibility was assumed | Implementation defect | A `BehaviorRule` Avro reader/writer compatibility test is mandatory. |
| Grafana is optional and backed by ClickHouse | Architecture clarification | The dashboard and datasource are visualization artifacts, not part of the critical path. |
| Static plan/rendering was conflated with deployment | Governance clarification | `CODEBASE-READY` and `DEPLOYMENT-VERIFIED` are separate gates. |

Application, test, Terraform, Compose, CI, deployment, and report files are
aligned with the active-cart contract. No deployment result is claimed.
