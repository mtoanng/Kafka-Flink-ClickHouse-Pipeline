# Codebase Closure Plan

Audit date: 2026-07-20. This plan is based on the current branch, `AGENTS.md`,
the final Project 1 blueprint, all phase reports, and all repository files.
No deployment was performed and no existing artifact was changed or removed.

## Frozen Architecture

```text
UserBehavior.csv -> Python replay -> Kafka + Schema Registry
                 -> one Java Flink DataStream job -> ClickHouse -> Grafana
                                                  -> ScyllaDB current state

PostgreSQL -> Debezium -> compacted rules topic -> Flink Broadcast State
```

S3 is not part of the event-data path. The codebase must not claim verified
cloud deployment, production readiness, end-to-end exactly-once delivery, or
verified scale.

## Audit Summary

| Area | Classification | Evidence and gap |
| --- | --- | --- |
| Python replay package and tests | VALID AND COMPLETE | Bounded reader, deterministic IDs, validation, replay and Kafka mapping; 29 tests pass. |
| Java Flink module and tests | VALID BUT INCOMPLETE | Compiles and 20 tests pass, but Scylla and control-plane branches cannot be disabled for a core-only run. |
| Avro schemas | VALID BUT INCOMPLETE | Both parse and generate Java records; live registry compatibility and Debezium writer/reader compatibility are unverified. |
| ClickHouse SQL and mappings | VALID AND COMPLETE | Static DDL/mapping tests pass; live inserts and retry behavior are unverified. |
| ScyllaDB CQL and client | VALID BUT INCOMPLETE | Static contracts pass; TLS/provider topology and live driver behavior are unverified. |
| PostgreSQL/Debezium control plane | VALID BUT INCOMPLETE | Table, topic contract and broadcast logic exist; generated Debezium Avro compatibility is not proven. |
| Grafana | VALID BUT INCOMPLETE | Architecture and environment mention Grafana, but no Taobao dashboard or provisioning artifact exists. |
| Docker Compose profiles | CONFLICTS WITH FROZEN ARCHITECTURE | `core` contains no Kafka or Schema Registry service; only CDC containers are defined. |
| Terraform | VALID BUT INCOMPLETE | Formatting and validation pass; EC2 bootstrap omits Java/Flink/application delivery and Kafka ACL coverage is incomplete. |
| CI | VALID BUT INCOMPLETE | Maven, Python, Terraform and security jobs exist; Compose, shell, schema and documentation validation are absent. |
| Runbooks and scripts | STALE | Several scripts still describe a final cloud release and disagree with Compose and environment templates. |

## Closure Sequence

1. Make the single Flink job profile-aware. Keep ClickHouse as core; gate
   Scylla and the rules source/broadcast branch with explicit environment flags.
2. Repair the deployment contract. Make Compose `core` represent Kafka and
   Schema Registry, make `cdc` add PostgreSQL and Debezium, and align advertised
   listeners, connector converters and topic creation.
3. Complete provisioning. Install a supported Java runtime and Flink on the
   temporary host, define application artifact transfer, and add the minimum
   Kafka topic ACLs needed by replay, Flink and Connect identities.
4. Reconcile `.env.example`, `.env.cloud.example`, Java, Python, Compose,
   Terraform and scripts from one documented variable matrix. Remove the stale
   event-archive variable while retaining legitimate infrastructure storage
   variables if introduced later.
5. Add a Taobao Grafana dashboard and datasource template for ClickHouse raw and
   one-minute metrics. Keep credentials environment-driven.
6. Prove the CDC contract locally at schema level: capture the actual unwrapped
   Debezium writer schema and add an Avro resolution test against `BehaviorRule`.
7. Resolve build hygiene: format `scripts/reconcile_final_e2e.py`, review the
   ClickHouse all-in-one connector's duplicate classes, and make JUnit
   dependencies explicit if `dependency:analyze` remains a CI gate.
8. Extend CI with shell syntax, JSON/Avro parse, Compose profile rendering,
   Markdown links and a high-confidence secret scan.
9. Rewrite README and cloud runbooks so commands match the profiles exactly and
   all runtime-dependent outcomes remain marked `NOT VERIFIED`.
10. Only in a later deployment phase, run bounded smoke, recovery,
    reconciliation and teardown verification and store real evidence.

## Resume-Ready Exit Criteria

- A reviewer can trace each frozen-architecture arrow to code or configuration.
- Core starts without requiring ScyllaDB, PostgreSQL or Debezium.
- Both deployment profiles render from documented environment templates.
- Grafana has a Taobao-only dashboard artifact backed by ClickHouse queries.
- All credential-independent checks pass, including Ruff formatting.
- Documentation distinguishes static verification from live integration.
- No active S3 event sink, dependency, uploader, test, or event-sink variable remains.
- Cloud, recovery, connector and scale statements remain `NOT VERIFIED` until
  evidence from a real run exists.

## Current Check Results

| Check | Result |
| --- | --- |
| Python unit tests | PASS: 29 |
| Python compilation | PASS |
| Ruff lint | PASS |
| Ruff format | FAIL: `scripts/reconcile_final_e2e.py` would be reformatted |
| Maven clean test | PASS: 20 Java tests |
| Maven package | PASS with duplicate-class/resource shade warnings |
| Maven dependency analysis | PASS with used-undeclared and aggregate-JUnit warnings |
| Avro/JSON parsing | PASS |
| Shell syntax | PASS |
| Compose `core` and `cdc` rendering | PASS syntactically; semantic profile conflict remains |
| Terraform format and validate | PASS |
| Markdown links | PASS |
| High-confidence secret patterns | None found; `gitleaks` unavailable |

