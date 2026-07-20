# Taobao Real-Time Customer Behavior Platform

A streaming data platform for Alibaba/Taobao `UserBehavior.csv` events.

<img width="1468" height="716" alt="image" src="https://github.com/user-attachments/assets/53224a9a-0b78-4244-8ea6-db6a38acce8a" />


```text
Taobao UserBehavior.csv
        -> Python replay producer
        -> Kafka + Schema Registry (Avro)
        -> one Java Apache Flink DataStream job
        -> ClickHouse
             \-> ScyllaDB current user state (optional)

PostgreSQL -> Debezium -> compacted Kafka topic -> Flink Broadcast State (optional)
```

The core path processes raw events, writes analytical history and one-minute
item metrics to ClickHouse, and is designed to run without ScyllaDB or CDC.
The project does not use S3 as an event-data sink and does not include ML,
recommendation logic, or a frontend.

## Repository status

The application, schemas, tests, infrastructure definitions, and runtime
profiles are implemented and covered by credential-independent checks. Live
Kafka/Flink/ClickHouse execution, cloud deployment, connector compatibility,
recovery, and performance are not claimed until evidence is captured.

See [Implementation Status](docs/IMPLEMENTATION_STATUS.md), the
[completion audit](docs/CODEBASE_COMPLETION_REPORT.md), and the
[cloud E2E runbook](docs/CLOUD_E2E_RUNBOOK.md) for the current verification
boundary.

## Prerequisites

- Python 3.11+
- Java and Maven, matching the Flink job target
- Docker Compose for local infrastructure checks
- Bash for the repository scripts (Git Bash, WSL, or Linux)
- `ruff` for Python linting and formatting checks

The raw Alibaba dataset is intentionally not committed. Local tests use the
deterministic fixture at `tests/fixtures/user_behavior_fixture.csv`.

## Quick start

From the repository root:

```bash
python -m venv .venv
# activate .venv using your shell's convention
pip install -e '.[kafka]'
cp .env.example .env

# Credential-independent checks
make test
make package
make infra-config
```

The same checks can be run individually:

```bash
PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
ruff check producer scripts
ruff format --check producer scripts
mvn -B -pl flink-jobs/taobao-stream-job -am test
docker compose -f infra/docker-compose.yml config --quiet
```

## Local runtime profiles

Copy `.env.example` to an ignored `.env` and provide endpoints and credentials
only for the services you enable. Profiles are additive:

```bash
# Core: Kafka, Schema Registry, Flink, and ClickHouse path
docker compose --profile core -f infra/docker-compose.yml up -d

# Optional branches
SERVING_ENABLED=true docker compose --profile serving -f infra/docker-compose.yml up -d
CDC_ENABLED=true docker compose --profile cdc -f infra/docker-compose.yml up -d
OBSERVABILITY_ENABLED=true docker compose --profile observability -f infra/docker-compose.yml up -d
```

The `serving` profile adds the ScyllaDB current-state sink when
`SCYLLA_HOST` and `SCYLLA_LOCAL_DATACENTER` are configured. The `cdc` profile
adds PostgreSQL, Debezium, and Flink Broadcast State when its complete
configuration is present. The `observability` profile provisions Grafana for
ClickHouse. Optional services must not be required by the core path.

Stop the local runtime with:

```bash
bash scripts/stop.sh
```

## Running the pipeline

The replay producer and Flink job run separately from Compose. After the
infrastructure is reachable:

```bash
bash scripts/register_schemas.sh
mvn -B -pl flink-jobs/taobao-stream-job -am package
bash scripts/run_flink.sh
bash scripts/replay.sh
```

For a bounded fixture demonstration, inspect
`scripts/run_fixture_demo.sh` and `scripts/verify_clickhouse.py`. The replay
engine supports deterministic IDs, chunked CSV reading, schema validation, and
configurable replay speed. Kafka settings are provider-neutral: local runs use
`PLAINTEXT`; managed Kafka can use `SASL_SSL` through environment variables.

Useful Make targets:

```text
make test                 Python, Ruff, and Maven checks
make package              Build the shaded Flink job
make infra-config         Validate Compose rendering
make schema               Register Avro schemas
make publish-fixture      Publish the configured fixture
make run-job              Submit the Flink job
make lookup-user USER_ID=...  Read Scylla current state
make terraform-validate   Format-check and validate Terraform
make teardown             Run guarded demo teardown
```

## Data and processing contracts

The canonical source columns are `user_id`, `item_id`, `category_id`,
`behavior_type` (`pv`, `cart`, `fav`, or `buy`), and Unix `timestamp`.

The Avro event contract is in
[`schemas/user-behavior-event.avsc`](schemas/user-behavior-event.avsc). Kafka
uses `user_id` as the message key. Flink assigns event-time timestamps and
watermarks, handles invalid/late data explicitly, and produces item-level
one-minute aggregates. ClickHouse stores raw accepted events and rollups;
ScyllaDB stores only bounded latest-user state when serving is enabled.

## Project layout

```text
producer/                         Python replay package and tests
flink-jobs/taobao-stream-job/     Java Flink job and tests
schemas/                          Avro contracts
infra/docker-compose.yml          Local runtime services
infra/clickhouse/                 ClickHouse DDL and verification SQL
infra/scylladb/                   ScyllaDB schema and checks
infra/postgres/                   CDC control-plane schema
infra/debezium/                   Debezium configuration
infra/terraform/                  Cloud deployment definitions
scripts/                          Build, run, replay, verify, and teardown tools
docs/                             Blueprint, status, runbooks, and evidence
tests/fixtures/                   Deterministic bounded test data
```

## Documentation

- [Final architecture blueprint](docs/PROJECT1_BLUEPRINT_FINAL.md)
- [Dataset notes](docs/DATASET_NOTES.md)
- [Deployment code status](docs/DEPLOYMENT_CODE_STATUS.md)
- [Cloud E2E runbook](docs/CLOUD_E2E_RUNBOOK.md)
- [Evidence archive](docs/evidence/)

Do not commit `.env`, credentials, Terraform state, or the full raw dataset.
Cloud resources are never provisioned by the repository checks; deployment and
teardown require an explicit operator-controlled run.
