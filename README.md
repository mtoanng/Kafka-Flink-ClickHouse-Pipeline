# Taobao Real-Time Customer Behavior Platform

This repository has completed **Phase 5: archive, checkpoint, CI, and Terraform code**. Validated Taobao events flow through the Phase 4 path, accepted JSONL can be archived to S3 with a manifest, checkpointing is opt-in through durable storage configuration, and CI validates Python, Maven, and Terraform contracts. Remote integration remains unverified until run on a disposable host with credentials.

## Authoritative Documents

- [Final implementation blueprint](docs/PROJECT1_BLUEPRINT_FINAL.md)
- [Phase 0 migration inventory](docs/PHASE_0_MIGRATION_INVENTORY.md)
- [Phase 0 baseline report](docs/evidence/phase-0/BASELINE_REPORT.md)
- [Dataset notes](docs/DATASET_NOTES.md)
- [Phase 1 report](docs/evidence/phase-1/PHASE_REPORT.md)
- [Phase 2 report](docs/evidence/phase-2/PHASE_REPORT.md)
- [Phase 3 report](docs/evidence/phase-3/PHASE_REPORT.md)
- [Phase 4 report](docs/evidence/phase-4/PHASE_REPORT.md)
- [Phase 5 report](docs/evidence/phase-5/PHASE_REPORT.md)
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

The ScyllaDB contract is in [infra/scylladb/schema.cql](infra/scylladb/schema.cql). `user_current_activity` has exactly one partition key, `user_id`; it stores only the newest accepted on-time event for that user. The CQL sink is intentionally not a history store. ClickHouse retains raw and aggregate history.

`session_event_count` currently counts state transitions that advance a user's newest event-time record within the running keyed state. It is not a sessionization feature: session-gap/timer logic is intentionally deferred beyond Phase 4.

## Phase 5 Operations

The archive tool validates each JSONL row, computes row count/bytes/SHA-256, and uploads only when `--s3-uri s3://bucket/key` is provided. Install its optional dependency with `pip install -e '.[archive]'`. Without an S3 URI it performs a local dry-run and can write a manifest under ignored `artifacts/`.

```bash
PYTHONPATH=producer/src python scripts/archive_raw_events.py \
  artifacts/events.jsonl \
  --s3-uri "s3://your-bucket/taobao/raw/run-1.events.jsonl" \
  --manifest artifacts/run-1.archive.json
```

Checkpointing is disabled by default on the laptop. On the disposable Flink host, set `FLINK_CHECKPOINTING_ENABLED=true`, `FLINK_CHECKPOINT_DIR` to durable remote storage, and optionally `FLINK_CHECKPOINT_INTERVAL_MS`; then use `bash scripts/run_checkpoint_experiment.sh` for the controlled restart procedure. The job uses at-least-once checkpoint mode and makes no exactly-once claim.

Terraform under [infra/terraform](infra/terraform) creates only the bounded S3 archive bucket, versioning, server-side encryption, and prefix lifecycle. Run `terraform fmt -check`, `init -backend=false`, and `validate` locally. Apply and destroy require explicit cloud authorization; `scripts/teardown_demo.sh` requires `CONFIRM_TEARDOWN=YES`.

## Remote Phase 5 Integration

Use a disposable remote machine for Kafka, Schema Registry, Flink, ClickHouse, and ScyllaDB. Do not start the complete stack on the laptop. The Scylla integration uses the native CQL driver, not the DynamoDB API. Keep `SCYLLA_*` and `CLICKHOUSE_*` credentials out of Git.

```bash
pip install -e '.[kafka]'
bash scripts/run.sh
mvn -B -pl flink-jobs/taobao-stream-job -am package
export CLICKHOUSE_ENDPOINT='https://your-host:8443'
export CLICKHOUSE_USER='default'
export CLICKHOUSE_PASSWORD='replace-me'
export SCYLLA_HOST='your-scylla-host'
export SCYLLA_LOCAL_DATACENTER='your-datacenter'
export SCYLLA_USER='replace-me'
export SCYLLA_PASSWORD='replace-me'
bash scripts/run_fixture_demo.sh
```

`run_fixture_demo.sh` applies idempotent ClickHouse and Scylla CQL schemas, submits Flink detached, replays the committed fixture with a unique run ID, verifies 999 valid ClickHouse raw events plus the item 500 first-minute rollup (`cart=1`, `buy=1`, `unique_users=1`), performs a fixed CQL lookup for user `100`, and produces an archive manifest. Set `S3_ARCHIVE_URI` to upload the accepted JSONL; omit it for a local archive dry-run.

Run the lookup independently after building the job:

```bash
bash scripts/lookup_current_activity.sh 100
```

The remote smoke and lookup are credential-dependent and are not evidence until they complete against real services. The Kafka topic is `user-behavior-events`, keyed by the UTF-8 representation of `user_id`; the value subject is `user-behavior-events-value` with `BACKWARD` compatibility.
