# Taobao Real-Time Customer Behavior Platform

This repository has completed **Phase 1: dataset fixture and local Python replay**. The checked-in Java code still implements the frozen F1 baseline until the Java migration begins in Phase 2.

## Authoritative Documents

- [Final implementation blueprint](docs/PROJECT1_BLUEPRINT_FINAL.md)
- [Phase 0 migration inventory](docs/PHASE_0_MIGRATION_INVENTORY.md)
- [Phase 0 baseline report](docs/evidence/phase-0/BASELINE_REPORT.md)
- [Dataset notes](docs/DATASET_NOTES.md)
- [Phase 1 report](docs/evidence/phase-1/PHASE_REPORT.md)
- [Archived F1 documentation](docs/archive/f1-baseline/)

## Phase 1 Local Workflow

The required raw Tianchi dataset is not present, so the source contract is currently `NOT SATISFIED`. See [Dataset notes](docs/DATASET_NOTES.md) before supplying any data; cleaned Kaggle or derived tables are not accepted.

Audit a manually acquired, ignored raw source and its private manifest:

```bash
PYTHONPATH=producer/src python -m taobao_replay source-audit data/UserBehavior.csv --manifest data/UserBehavior.source.json
```

Generate and inspect the deterministic fixture:

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
mvn -B clean test
```

Kafka output, Schema Registry registration, and Java Flink consumption are Phase 2 and are not implemented yet. The full stack is not required on the development laptop, and cloud services must not be provisioned without explicit approval.
