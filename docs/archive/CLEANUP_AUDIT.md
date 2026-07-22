# Repository Cleanup Audit

## Historical Cleanup

The former F1 modules and S3 archive implementation were removed in earlier
approved cleanup work. Their archived reports remain historical evidence only;
neither F1 telemetry nor S3 event archival is part of the active codebase.

## Archived

- `docs/archive/f1-baseline/` remains as historical migration evidence only.
- `docs/PHASE_0_MIGRATION_INVENTORY.md` and prior phase reports remain immutable
  evidence of the migration and are not active implementation instructions.

## Kept

- `infra/docker-compose.yml` and its lifecycle scripts provide core, serving, CDC,
  and observability profiles on a disposable runtime host.
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
- No notebooks, diagrams, F1 source modules, S3 event sink, or unused active
  dashboard files remain outside the documented archive/evidence areas.
