# Project 1 Codebase Completion Audit

Audit date: 2026-07-21. This is a credential-independent repository audit only.
No feature work, Terraform apply, deployment, or cloud E2E run was performed.

## Gate Decision

`CODEBASE-READY: NOT READY`.

The local `core` profile is independently configurable, but deployment smoke
code still contradicts that contract: `scripts/cloud_preflight.sh` requires
ScyllaDB unconditionally, and `scripts/run_fixture_demo.sh` always applies the
Scylla schema and performs a Scylla lookup. A core-only cloud handover therefore
cannot follow the blueprint without optional serving credentials. This audit
does not change those scripts.

`DEPLOYMENT-VERIFIED: NOT VERIFIED`.

## Audit Matrix

| Area | Result | Evidence |
| --- | --- | --- |
| Blueprint versus active application | PASS | One Java DataStream job, provider-neutral Kafka settings, ClickHouse core, optional serving/CDC branches. |
| Core independence | PASS locally | `RuntimeProfileConfigTest`, Compose core rendering, and guarded Flink branches pass without Scylla/CDC values. |
| Kafka and Schema Registry | PASS statically | PLAINTEXT/SASL_SSL mapping, Avro contracts, local Compose broker/registry, Terraform topic/group/subject permissions. |
| Flink Java modules | PASS | Maven compilation and 28 Java tests pass; no second active F1 module is present. |
| ClickHouse contracts | PASS statically | DDL, mapping, verification-query tests and environment-driven sink configuration pass. |
| Scylla serving | PASS optionality | Serving configuration and CQL contracts pass; live driver/topology/TLS remain unverified. |
| CDC control plane | PASS statically | PostgreSQL table, Debezium route, compacted topic, Broadcast State branch, and writer/reader Avro resolution test pass. |
| Grafana | PASS statically | Taobao datasource/dashboard JSON and Compose provisioning render; live panels unverified. |
| Terraform/bootstrap | PASS statically, incomplete operationally | `fmt`/`validate` pass; bootstrap defines Java/Flink/JAR/env/start/health/stop contracts, but no host run occurred. |
| CI | PARTIAL | GitLab CI covers Maven, Python, Terraform and security templates; Compose, shell, schema and documentation checks are local-only. |
| Verification/teardown | BLOCKED for ready gate | Guards and scripts exist, but cloud smoke path has the unconditional Scylla dependency identified above. |
| README/runbooks/evidence | PARTIAL | README and active status docs are aligned; historical reports remain explicitly historical. Cloud runbook remains planning-only. |
| Dependency cleanliness | PASS WITH DOCUMENTED RISK | Guava/HdrHistogram transitive duplicates were removed; vendor fat-connector class/resource overlaps remain documented in `docs/SHADED_JAR_ANALYSIS.md`. |

## Repository-Wide Negative Checks

- No active F1 Java/replay implementation or F1 dashboard was found. Archived
  F1 material and migration reports are historical only.
- No active S3 event sink, uploader, archive dependency, or event-archive
  environment variable was found. S3 wording is limited to architecture
  exclusions or historical evidence.
- Core Flink startup does not construct Scylla clients or CDC sources unless
  their profile flags are enabled.
- No committed credentials, private keys, or high-confidence secret patterns
  were found. `gitleaks` is unavailable in the environment.

## Checks Run

- Python compilation, Ruff lint/format, and 35 unit tests: PASS.
- Maven tests and CDC Avro compatibility: PASS, 28 tests.
- Clean Maven shaded package: PASS; remaining vendor-bundle warnings documented.
- Avro/Debezium/Grafana JSON parsing: PASS.
- Compose `core`, `serving`, `cdc`, and `observability` config: PASS.
- Terraform `fmt -check` and `validate`: PASS.
- Git Bash shell syntax, documentation links, and `git diff --check`: PASS.
- High-confidence secret scan: PASS.

## Required Follow-Up Before CODEBASE-READY

Make cloud preflight and fixture/release orchestration honor `RUNTIME_PROFILE`
and skip Scylla schema/lookup checks when serving is disabled. Then rerun the
same static checks and perform a separate deployment verification phase.

Cloud Kafka permissions, Schema Registry operations, Flink execution, ClickHouse
results, Scylla serving, Grafana rendering, live CDC updates, recovery, scale,
and teardown remain `NOT VERIFIED`.
