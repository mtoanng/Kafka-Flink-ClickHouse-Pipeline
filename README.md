# Taobao Real-Time Customer Behavior Platform

This repository has completed **Phase 3: ClickHouse end-to-end MVP code**. Validated Taobao events are published as Confluent Avro, consumed by one Java Flink job, and mapped to ClickHouse raw history plus one-minute item metrics. Remote Kafka, Flink, Schema Registry, and ClickHouse integration remains unverified until run on a disposable host with credentials.

## Authoritative Documents

- [Final implementation blueprint](docs/PROJECT1_BLUEPRINT_FINAL.md)
- [Phase 0 migration inventory](docs/PHASE_0_MIGRATION_INVENTORY.md)
- [Phase 0 baseline report](docs/evidence/phase-0/BASELINE_REPORT.md)
- [Dataset notes](docs/DATASET_NOTES.md)
- [Phase 1 report](docs/evidence/phase-1/PHASE_REPORT.md)
- [Phase 2 report](docs/evidence/phase-2/PHASE_REPORT.md)
- [Phase 3 report](docs/evidence/phase-3/PHASE_REPORT.md)
- [Archived F1 documentation](docs/archive/f1-baseline/)

## Laptop Checks

The ignored local Tianchi raw source and its private manifest have passed the Phase 1 source audit. See [Dataset notes](docs/DATASET_NOTES.md) for its recorded checksum, row count, and raw-row rejection behavior; cleaned Kaggle or derived tables are not accepted.

Install the project with its Kafka/Avro test dependencies:

```bash
pip install -e '.[kafka]'
```

Audit a manually acquired, ignored raw source and its private manifest:

```bash
PYTHONPATH=producer/src python -m taobao_replay source-audit data/UserBehavior.csv --manifest data/UserBehavior.source.json
```

Generate and inspect the deterministic fixture without infrastructure:

```bash
python scripts/generate_fixture.py --force
PYTHONPATH=producer/src python -m taobao_replay profile tests/fixtures/user_behavior_fixture.csv
mkdir -p artifacts
PYTHONPATH=producer/src python -m taobao_replay replay tests/fixtures/user_behavior_fixture.csv --output artifacts/events.jsonl --invalid-output artifacts/invalid.jsonl
```

Run focused checks:

```bash
PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
ruff check producer scripts
ruff format --check producer scripts
mvn -B test
```

The ClickHouse DDL contract is in [infra/clickhouse/schema.sql](infra/clickhouse/schema.sql) and its query pack is in [infra/clickhouse/verify.sql](infra/clickhouse/verify.sql). Raw history has one row per valid replay event; late events are excluded only from the item rollup. The metric table includes `replay_run_id` so fixture verification remains isolated across runs.

The raw table is ordered by `(toDate(event_time), item_id, event_time, user_id)`. The verification pack filters a date range, then an item, then event time; this order minimizes the data range for that primary query. `user_id` is last for deterministic drill-down within equal item/time values, not because Kafka partitioning makes ClickHouse user lookups primary.

## Remote Phase 3 Integration

Use a disposable remote machine for Kafka, Schema Registry, and Flink plus an explicitly approved ClickHouse service. Do not start the complete stack on the laptop. The scripts use `CLICKHOUSE_ENDPOINT`, `CLICKHOUSE_USER`, `CLICKHOUSE_PASSWORD`, and optionally `CLICKHOUSE_DATABASE=taobao_behavior`; do not commit these values.

```bash
pip install -e '.[kafka]'
bash scripts/run.sh
mvn -B -pl flink-jobs/taobao-stream-job -am package
export CLICKHOUSE_ENDPOINT='https://your-host:8443'
export CLICKHOUSE_USER='default'
export CLICKHOUSE_PASSWORD='replace-me'
bash scripts/run_fixture_demo.sh
```

`run_fixture_demo.sh` applies the idempotent DDL, submits Flink detached, replays the committed fixture with a unique run ID, and checks for 999 valid raw events plus the item 500 first-minute rollup (`cart=1`, `buy=1`, `unique_users=1`). It is a remote, credential-dependent smoke test and is not evidence until it completes against real services. The Kafka topic is `user-behavior-events`, keyed by the UTF-8 representation of `user_id`; the value subject is `user-behavior-events-value` with `BACKWARD` compatibility.
