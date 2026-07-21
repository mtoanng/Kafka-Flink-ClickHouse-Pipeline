# Codebase Closure Plan

Audit date: 2026-07-20. This plan is based on the current branch, `AGENTS.md`,
the final Project 1 blueprint, all phase reports, and all repository files.
No deployment was performed and no existing artifact was changed or removed.

## Frozen Architecture

```text
UserBehavior.csv -> Python replay -> Kafka + Schema Registry
                 -> one Java Flink DataStream job -> ClickHouse
                                                  -> optional Grafana visualization
                                                  -> Apache Cassandra active-cart lookup

PostgreSQL -> Debezium -> compacted rules topic -> Flink Broadcast State
```

S3 is not part of the event-data path. The codebase must not claim verified
cloud deployment, production readiness, end-to-end exactly-once delivery, or
verified scale.

## Audit Summary

| Area | Classification | Evidence and gap |
| --- | --- | --- |
| Python replay package and tests | VALID AND COMPLETE | Bounded reader, deterministic IDs, validation, replay and Kafka mapping; 29 tests pass. |
| Java Flink module and tests | VALID AND LOCALLY VERIFIED | Cassandra-disabled core and enabled active-cart branches are covered by the current test suite. |
| Avro schemas | VALID BUT INCOMPLETE | Both parse and generate Java records; live registry compatibility and Debezium writer/reader compatibility are unverified. |
| ClickHouse SQL and mappings | VALID AND COMPLETE | Static DDL/mapping tests pass; live inserts and retry behavior are unverified. |
| Apache Cassandra CQL and client | VALID AND LOCALLY/STATICALLY VERIFIED | Static contracts, lifecycle, mutation mapping, and lookup shape pass; Astra behavior remains unverified. |
| PostgreSQL/Debezium control plane | VALID BUT INCOMPLETE | Table, topic contract and broadcast logic exist; generated Debezium Avro compatibility is not proven. |
| Grafana | OPTIONAL AND NOT REQUIRED | A ClickHouse datasource and Taobao dashboard artifact exist; Grafana is not on the critical processing path. |
| Docker Compose profiles | IMPLEMENTED BUT LOCALLY UNVERIFIED | Cassandra is external and no local serving database is added; profile rendering requires a working Docker client. |
| Terraform | TERRAFORM VALIDATED BUT NOT APPLIED | Formatting passed; provider initialization and `validate` were not completed because the Astra provider was unavailable in this run. |
| CI | VALID BUT INCOMPLETE | Existing Maven, Python, Terraform and security jobs remain; this audit does not claim every CI job executed locally. |
| Runbooks and scripts | ALIGNED | Active-cart lookup, external Astra serving, optional Grafana, and secret handling are documented without deployment claims. |

## Closure Sequence

1. Preserve the profile-aware single Flink job. Keep ClickHouse as core; gate
   Cassandra and the rules source/broadcast branch with explicit environment flags.
2. Verify the deployment contract. Keep Cassandra as an external managed
   serving dependency and retain the documented Compose profiles.
3. Complete provisioning. Install a supported Java runtime and Flink on the
   temporary host, define application artifact transfer, and add the minimum
   Kafka topic ACLs needed by replay, Flink and Connect identities.
4. Reconcile `.env.example`, `.env.cloud.example`, Java, Python, Compose,
   Terraform and scripts from one documented variable matrix. Remove the stale
   event-archive variable while retaining legitimate infrastructure storage
   variables if introduced later.
5. Keep the Taobao Grafana dashboard and datasource optional and
   credentials-driven; they are not required by the critical path.
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
- Core starts without requiring Cassandra, PostgreSQL or Debezium.
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
| Python unit tests | PASS: 35 |
| Python compilation | PASS |
| Ruff lint | PASS |
| Ruff format | PASS |
| Maven tests | PASS: 35 tests |
| Maven package | PASS with duplicate-class/resource shade warnings |
| Maven dependency analysis | PASS with used-undeclared and aggregate-JUnit warnings |
| Avro/JSON parsing | PASS |
| Shell syntax | NOT VERIFIED: WSL has no installed distribution; Git Bash validation remains pending |
| Compose profile rendering | NOT VERIFIED: Docker config access was denied by the local Docker client |
| Terraform format | PASS: recursive formatting check |
| Terraform validate | NOT VERIFIED: provider initialization was unavailable/interrupted |
| Markdown links | NOT VERIFIED: local link scan remains pending |
| High-confidence secret patterns | PASS: no tracked credentials or Secure Connect Bundles; `gitleaks` unavailable |
