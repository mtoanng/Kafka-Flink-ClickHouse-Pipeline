# Repository Cleanup Audit

## Removed

- `data-generators/f1-replay-engine/`: obsolete Java F1 replay module.
- `flink-jobs/f1-telemetry-job/`: obsolete second Flink job and F1 Avro contract.
- `infra/grafana/`: F1 dashboards and datasource provisioning with no active consumer.
- F1-specific Maven reactor modules, parent metadata, dependency properties, and
  the old dataset ignore rule.

## Archived

- `docs/archive/f1-baseline/` remains as historical migration evidence only.
- `docs/PHASE_0_MIGRATION_INVENTORY.md` and prior phase reports remain immutable
  evidence of the migration and are not active implementation instructions.

## Kept

- `infra/docker-compose.yml` and its lifecycle scripts remain because they provide
  the bounded Kafka/Schema Registry integration profile on a disposable remote
  host. They do not start PostgreSQL, Debezium, Flink, ClickHouse, or Scylla on
  the laptop.
- `scripts/create_topic.sh` and `scripts/create_behavior_rules_topic.sh` remain
  because they create different Kafka topics.
- `scripts/run.sh`, `run_fixture_demo.sh`, and `run_phase6_demo.sh` remain because
  they represent infrastructure startup, data-plane verification, and control
  plane verification respectively.
- The current Taobao Java classes, Python package, schemas, infrastructure
  contracts, fixtures, and tests remain referenced by the active README/Makefile.

## Verification

- `mvn -B test`: 20 tests passed.
- `mvn -B -pl flink-jobs/taobao-stream-job -am package`: passed.
- Python tests: 32 passed; Ruff and compileall passed.
- All shell scripts parsed with Git Bash.
- Docker Compose config, Terraform format, Terraform validation, and
  `git diff --check` passed.
- No notebooks, diagrams, F1 source modules, or unused active dashboard files
  remain outside the documented archive/evidence areas.
