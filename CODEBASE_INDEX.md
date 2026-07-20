# Codebase Index

## Purpose

Taobao real-time customer behavior platform:

`UserBehavior.csv -> Python replay -> Kafka/Avro -> one Java Flink job -> ClickHouse`

The completed extensions add ScyllaDB current state and a PostgreSQL/Debezium
rule control plane.

## Main Entry Points

| Area | Location | Responsibility |
| --- | --- | --- |
| Python package | `producer/src/taobao_replay/` | Raw dataset validation, deterministic IDs, replay, and Kafka publishing |
| Python tests | `producer/tests/` | Reader, replay, source audit, and Kafka contract tests |
| Flink job | `flink-jobs/taobao-stream-job/` | Java DataStream topology, validation, watermarks, metrics, sinks, broadcast rules |
| Flink tests | `flink-jobs/taobao-stream-job/src/test/` | Avro, aggregation, watermark, state, sink and contract tests |
| Avro schemas | `schemas/` | `UserBehaviorEvent` and `BehaviorRule` contracts |
| ClickHouse | `infra/clickhouse/` | DDL and verification query pack |
| ScyllaDB | `infra/scylladb/` | Current-activity CQL schema and verification |
| PostgreSQL | `infra/postgres/` | Single `behavior_rules` control-plane table |
| Debezium | `infra/debezium/` | PostgreSQL connector template and CDC notes |
| Kafka contracts | `infra/kafka/` | Compacted behavior-rules topic contract |
| Terraform | `infra/terraform/` | Temporary EC2/networking and optional Confluent Cloud resources |
| Scripts | `scripts/` | Fixture generation, replay, schema application, demos, checks, teardown |
| Fixtures | `tests/fixtures/` | Deterministic bounded input for local tests |

## Important Documents

- `AGENTS.md`: repository rules and learning contract.
- `README.md`: setup, local checks, and remote demo instructions.
- `docs/PROJECT1_BLUEPRINT_FINAL.md`: authoritative architecture and phase plan.
- `docs/DATASET_NOTES.md`: source dataset audit and profile.
- `docs/evidence/phase-0/` through `docs/evidence/phase-6/`: phase reports.

## Common Checks

```bash
PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
ruff check producer scripts
mvn -B test
mvn -B -pl flink-jobs/taobao-stream-job -am package
terraform -chdir=infra/terraform fmt -check
terraform -chdir=infra/terraform validate
```

Cloud services, Kafka Connect, and full-stack integration are remote-only; do not
commit `.env`, raw datasets, credentials, or Terraform state.
