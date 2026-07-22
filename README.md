# Taobao Real-Time Customer Behavior Platform

A junior-friendly streaming portfolio project built from the raw Alibaba/Taobao
`UserBehavior.csv` event dataset.

```text
UserBehavior.csv -> deterministic Python replay -> Kafka + Avro/Schema Registry
                 -> one Java Flink DataStream job
                    |-> ClickHouse history, minute metrics, and durable audits
                    |-> Apache Cassandra per-user active cart

PostgreSQL -> Debezium -> compacted rules topic -> Flink Broadcast State
```

ClickHouse and Cassandra are both core serving systems. ClickHouse owns
analytical history and rollups; Cassandra owns only the current active cart,
partitioned by `user_id`. PostgreSQL/Debezium and Grafana belong to the `full`
profile. The repository does not add ML, a frontend, Kubernetes, S3 event
storage, or a second Flink job.

## Status

| Area | Status |
| --- | --- |
| Python replay, Avro contracts, Java topology, DDL/CQL, tests, and packaging | **implemented and statically verified** |
| Local or managed Kafka/Flink/ClickHouse/Cassandra runtime | **requires live deployment verification** |
| PostgreSQL/Debezium rule updates, alert timers, and Grafana | **implemented and statically verified; requires live deployment verification** |
| Checkpoint recovery and connector retry behavior | **requires live deployment verification** |
| End-to-end exactly once or production readiness | **not claimed** |

See [Implementation Status](docs/CURRENT_IMPLEMENTATION_STATUS.md) for the
complete verification boundary.

## Architecture and delivery model

The Flink job consumes checkpointed Kafka input with at-least-once checkpoints.
External sinks are not transactional with Kafka:

- ClickHouse receives at-least-once writes. Stable logical keys and
  `ReplacingMergeTree(record_version)` support effectively-once results through
  the committed `*_deduplicated` views.
- Cassandra uses idempotent prepared upsert/delete statements on
  `PRIMARY KEY ((user_id), item_id)`.
- This is not global transactional exactly-once delivery.

Semantic invalid Avro events, late events, and full-profile abandonment alerts
are written to durable ClickHouse audit tables. CSV rows that cannot be encoded
as the Avro contract are rejected by the producer before Kafka.

Detailed design: [Architecture](docs/ARCHITECTURE.md).

## Prerequisites

- Python 3.11+
- Java 11 and Maven
- Apache Flink 1.20.2 for runtime execution
- Docker Compose for local runtime dependencies
- Bash for operational scripts
- Terraform 1.6+ only for validation or an explicitly approved deployment

The full raw dataset, `.env`, credentials, Secure Connect Bundles, and Terraform
state must remain outside Git. Credential-independent tests use
`tests/fixtures/user_behavior_fixture.csv`.

## Checks profile

The `checks` profile starts no services:

```bash
python -m venv .venv
# Activate the environment using your shell's convention.
pip install -e '.[kafka]'
pip install ruff

make test
make package
make infra-config
make terraform-validate
```

Equivalent focused commands:

```bash
PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
ruff check producer scripts
ruff format --check producer scripts
mvn -B -pl flink-jobs/taobao-stream-job -am test
mvn -B -pl flink-jobs/taobao-stream-job -am package
docker compose -f infra/docker-compose.yml --profile core config --quiet
docker compose -f infra/docker-compose.yml --profile full config --quiet
```

## Runtime profiles

| Profile | Services and behavior |
| --- | --- |
| `checks` | Tests, lint, package, Compose rendering, and Terraform validation; no external services |
| `core` | Kafka, Schema Registry, one Flink job, ClickHouse, and mandatory Cassandra active cart |
| `full` | `core` plus PostgreSQL, Debezium, compacted behavior rules, timers, alerts, and Grafana |

The local Compose model provides Kafka, Schema Registry, ClickHouse, and one
Cassandra node for `core`; `full` adds PostgreSQL, Debezium, and Grafana. Flink
is installed and submitted separately so the same JAR can run on a disposable
host or an existing Flink cluster.

Copy `.env.example` to ignored `.env`, export its values, then follow the
[Local Runbook](docs/LOCAL_RUNBOOK.md). Cassandra supports both:

- `CASSANDRA_MODE=local`: contact points, port, datacenter, and optional
  username/password;
- `CASSANDRA_MODE=astra`: ignored Secure Connect Bundle path plus application
  token.

Configuration reference: [Runtime Configuration](docs/RUNTIME_CONFIGURATION.md).

## Bounded runtime workflow

On a disposable integration host:

```bash
bash scripts/run.sh
bash scripts/register_schemas.sh
bash scripts/apply_cassandra_schema.sh
mvn -B -pl flink-jobs/taobao-stream-job -am package
FLINK_DETACHED=true bash scripts/run_flink.sh
bash scripts/replay.sh
bash scripts/verify_bounded_pipeline.sh fixture-run
```

The bounded verifier independently checks produced, valid raw, invalid, late,
one-minute metric, and Cassandra active-cart results. It requires live services;
its presence is not evidence that those services have run.

## Deployment and recovery

- [Cloud Deployment Runbook](docs/CLOUD_DEPLOYMENT_RUNBOOK.md)
- [Recovery Verification Runbook](docs/RECOVERY_VERIFICATION_RUNBOOK.md)
- [Final E2E evidence boundary](docs/evidence/final-e2e/README.md)

Terraform checks and plans do not prove deployment. No repository command runs
`terraform apply` implicitly, and teardown requires an explicit confirmation.

## Project layout

```text
producer/                         deterministic Python replay and tests
flink-jobs/taobao-stream-job/     one Java Flink job and tests
schemas/                          Avro contracts
infra/clickhouse/                 analytical and audit DDL/query contracts
infra/cassandra/                  active-cart CQL contracts
infra/postgres/, infra/debezium/  full-profile control plane
infra/terraform/                  optional deployment definitions
scripts/                          checks, bootstrap, run, verify, recovery, teardown
docs/                             blueprint, status, runbooks, and evidence
tests/fixtures/                   bounded deterministic input
```

The authoritative implementation contract is
[PROJECT1_BLUEPRINT_FINAL.md](docs/PROJECT1_BLUEPRINT_FINAL.md).
