# Taobao Real-Time Customer Behavior Platform

```text
Taobao replay -> Kafka + Schema Registry -> Java Flink DataStream -> ClickHouse
                                                       \-> ScyllaDB (optional)
PostgreSQL -> Debezium -> compacted config topic -> Flink Broadcast State (optional)
```

Kafka configuration is provider-neutral: local development uses `PLAINTEXT`;
managed Kafka uses `SASL_SSL` entirely through environment variables. There is
no S3 event-data sink.

## Authoritative Documents

- [Final blueprint](docs/PROJECT1_BLUEPRINT_FINAL.md)
- [Codebase closure plan](docs/CODEBASE_CLOSURE_PLAN.md)
- [Deployment code status](docs/DEPLOYMENT_CODE_STATUS.md)
- [Proposed deletions](docs/PROPOSED_DELETIONS.md)
- [Dataset notes](docs/DATASET_NOTES.md)
- [Phase reports](docs/evidence/)

## Local Checks

```bash
pip install -e '.[kafka]'
PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
ruff check producer scripts
ruff format --check producer scripts
mvn -B -pl flink-jobs/taobao-stream-job -am test
docker compose --profile core -f infra/docker-compose.yml config --quiet
```

The ignored `data/UserBehavior.csv` is the audited Alibaba Tianchi raw source.
The committed fixture under `tests/fixtures/` is the only dataset used for local
bounded tests.

## Runtime Profiles

Copy `.env.example` to an ignored local `.env`, then select profiles with the
configuration flags below. `core` is fully independent of ScyllaDB, PostgreSQL,
Debezium, and Grafana credentials.

```bash
docker compose --profile core -f infra/docker-compose.yml up -d
SERVING_ENABLED=true docker compose --profile serving -f infra/docker-compose.yml up -d
CDC_ENABLED=true docker compose --profile cdc -f infra/docker-compose.yml up -d
OBSERVABILITY_ENABLED=true docker compose --profile observability -f infra/docker-compose.yml up -d
```

`serving` enables the ScyllaDB sink only when `SCYLLA_HOST` and
`SCYLLA_LOCAL_DATACENTER` are supplied. `cdc` enables PostgreSQL, Debezium
Connect, and Flink Broadcast State only when the complete control-plane
configuration is supplied. `observability` provisions Grafana with ClickHouse.

The replay engine and Flink job run separately from Compose. Use
`scripts/register_schemas.sh` to register schemas and `scripts/run_flink.sh` to
submit the built job after Kafka, Schema Registry, and ClickHouse are reachable.
Cloud deployment and end-to-end verification remain unverified and are not part
of this runtime closure phase.
