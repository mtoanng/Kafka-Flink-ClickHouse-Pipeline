# Deployment Code Status

Status reflects static inspection and credential-independent checks on
2026-07-20. `VALID AND COMPLETE` means complete for its documented static role,
not deployed or production-verified.

## Infrastructure and Configuration

| Artifact | Classification | Finding |
| --- | --- | --- |
| `infra/docker-compose.yml` | VALID BUT INCOMPLETE | Core/serving/CDC/observability profiles render with local Kafka/Schema Registry; Flink and ClickHouse remain operator-managed services. |
| `.env.example` | VALID AND COMPLETE | Reconciled core, serving, CDC, observability and provider-neutral Kafka variables; no event archive variable. |
| `.env.cloud.example` | VALID BUT INCOMPLETE | Separates external sink credentials, but includes unused advertised-listener fields and omits rules/consumer/recovery variables. |
| `.gitignore` | VALID AND COMPLETE | Excludes secrets, raw data, runtime artifacts, build output and Terraform state/cache. |
| `infra/terraform/versions.tf` | VALID AND COMPLETE | Pins AWS and Confluent providers; initialization metadata is locked. |
| `infra/terraform/network.tf` | VALID AND COMPLETE | Defines isolated VPC, public subnet, route, SSH-only ingress, EIP and outbound access. |
| `infra/terraform/ec2.tf` | VALID BUT INCOMPLETE | Defines an encrypted temporary host and templated bootstrap; real host readiness remains unverified. |
| `infra/terraform/cloud_user_data.sh` | VALID BUT INCOMPLETE | Installs Java, Docker, Flink and optional versioned bundle/JAR; runtime credentials and real startup remain operator-supplied. |
| `infra/terraform/confluent.tf` | VALID BUT INCOMPLETE | Defines event/rules/Connect-internal topics, Flink/Debezium groups, topic/cluster operations and Schema Registry subject read role; live propagation and least-privilege review remain unverified. |
| `infra/terraform/variables.tf` | VALID BUT INCOMPLETE | Sensitive inputs are marked; Kafka API-key variables appear unused and cross-variable validation is absent. |
| `infra/terraform/outputs.tf` | VALID AND COMPLETE | Exposes only host/network and optional Kafka endpoint identifiers. |
| `infra/terraform/terraform.tfvars.example` | VALID BUT INCOMPLETE | Covers AWS host inputs but not the optional Confluent plan contract. |
| `infra/terraform/.terraform.lock.hcl` | VALID AND COMPLETE | Reproducibly locks both providers. |
| `infra/terraform/README.md` | VALID BUT INCOMPLETE | Documents Java/Flink/JAR/bootstrap and ACL contracts; real host setup and state handling remain operator responsibilities. |

## Data Stores and Control Plane

| Artifact | Classification | Finding |
| --- | --- | --- |
| `infra/clickhouse/schema.sql` | VALID AND COMPLETE | Defines Taobao raw and one-minute metric tables with tested ordering contracts. |
| `infra/clickhouse/verify.sql` | VALID AND COMPLETE | Provides bounded raw-count and item-window checks. |
| `infra/scylladb/schema.cql` | VALID BUT INCOMPLETE | Correct latest-state key; `SimpleStrategy` is demo-only and live topology/TLS remain unspecified. |
| `infra/scylladb/verify.cql` | VALID AND COMPLETE | Fixed point lookup matches the serving access pattern. |
| `infra/postgres/schema.sql` | VALID AND COMPLETE | Contains only the versioned `behavior_rules` control table. |
| `infra/postgres/README.md` | VALID BUT INCOMPLETE | Correct role, but assumes an external disposable database rather than the Compose `cdc` service. |
| `infra/debezium/postgres-behavior-rules.json` | VALID BUT INCOMPLETE | Correct source/unwrap/route intent; credentials are placeholders and live connector compatibility remains unverified. |
| `infra/debezium/README.md` | VALID BUT INCOMPLETE | Honest `NOT VERIFIED` status, but no exact converter/image compatibility procedure. |
| `infra/kafka/behavior-rules-topic.md` | VALID AND COMPLETE | Documents key, compaction policy and creation command. |
| `schemas/user-behavior-event.avsc` | VALID AND COMPLETE | Parses, generates Java and passes binary/field contract tests. |
| `schemas/behavior-rule.avsc` | VALID AND COMPLETE | Includes the Debezium record alias and timestamp logical type; reader/writer resolution is covered by the Java compatibility test. |
| `infra/grafana/` | VALID BUT INCOMPLETE | Taobao ClickHouse datasource and minimal dashboard are provisioned from environment values; live Grafana rendering is unverified. |

## Automation and Verification Scripts

| Artifact | Classification | Finding |
| --- | --- | --- |
| `scripts/generate_fixture.py`, `prepare_demo_subset.py` | VALID AND COMPLETE | Deterministic and bounded; covered by tests. |
| `scripts/replay.sh` | VALID AND COMPLETE | Publishes the bounded fixture using environment-driven endpoints. |
| `scripts/register_schemas.sh` | VALID BUT INCOMPLETE | Validates compatibility, but requires API credentials even for an unauthenticated local registry. |
| `scripts/create_behavior_rules_topic.sh` | VALID AND COMPLETE | Idempotent compacted-topic command with explicit bootstrap endpoint. |
| Core event-topic provisioning | VALID BUT INCOMPLETE | Terraform can create it only in one cloud profile; no provider-neutral CLI script exists. |
| `scripts/apply_clickhouse_schema.py`, `verify_clickhouse.py` | VALID AND COMPLETE | Fail fast without endpoints and use bounded deterministic assertions. |
| `scripts/apply_scylla_schema.sh`, `lookup_current_activity.sh` | VALID BUT INCOMPLETE | Inputs are guarded; TLS and provider-specific connection behavior are external. |
| `scripts/apply_behavior_rules_schema.sh` | VALID AND COMPLETE | Uses environment credentials without embedding them. |
| `scripts/run_flink.sh` | VALID BUT INCOMPLETE | Submits the one JAR with provider-neutral configuration; live optional-branch execution remains unverified. |
| `scripts/run.sh` | VALID BUT INCOMPLETE | Starts the local Compose profiles and validates only enabled optional configuration; managed-cloud startup is a separate operator path. |
| `scripts/stop.sh` | VALID BUT INCOMPLETE | Stops only CDC containers and cannot tear down a future core profile. |
| `scripts/healthcheck.sh`, `cloud_preflight.sh` | CONFLICTS WITH FROZEN ARCHITECTURE | Cloud preflight requires ScyllaDB unconditionally, so it cannot validate a core-only deployment. |
| `scripts/run_fixture_demo.sh` | VALID BUT INCOMPLETE | Orders the intended flow, but uses a fixed sleep and does not independently reconcile all outputs. |
| `scripts/run_phase6_demo.sh` | VALID BUT INCOMPLETE | Automates connector/rule submission; live schema compatibility and rule effect remain unverified. |
| `scripts/run_checkpoint_experiment.sh` | VALID BUT INCOMPLETE | Documents a guarded experiment but captures no evidence. |
| `scripts/run_flink_recovery_test.sh` | VALID BUT INCOMPLETE | Bounded by confirmation, but verifies only CLI listing after an operator-supplied restart command. |
| `scripts/reconcile_final_e2e.py` | VALID AND COMPLETE | Produces independent fixture expectations and passes Ruff formatting. |
| `scripts/run_cloud_smoke.sh`, `run_release_e2e.sh` | CONFLICTS WITH FROZEN ARCHITECTURE | Their preflight/fixture path requires ScyllaDB and applies the Scylla schema unconditionally even when the core profile is selected. |
| `scripts/collect_cloud_evidence.sh` | VALID BUT INCOMPLETE | Avoids credential values, but collects metadata/file names rather than sink results. |
| `scripts/teardown_demo.sh`, `verify_cloud_teardown.sh` | VALID BUT INCOMPLETE | Destruction is guarded and was not run; verification cannot prove provider-side deletion. |

## CI and Documentation

| Artifact | Classification | Finding |
| --- | --- | --- |
| `.gitlab-ci.yml` | VALID BUT INCOMPLETE | Maven/Python/Terraform/security coverage exists; no Compose, shell, schema, link or deployment-contract jobs. |
| `Makefile` | VALID BUT INCOMPLETE | Covers core tests, package, Compose config, runtime lifecycle and Terraform; cloud evidence remains explicit scripts. |
| `README.md` | VALID BUT INCOMPLETE | Matches provider-neutral profiles and clearly marks cloud/E2E work unverified. |
| `docs/CLOUD_E2E_ARCHITECTURE.md` | VALID BUT INCOMPLETE | Clearly labels planning, but external-service and local-CDC descriptions are inconsistent. |
| `docs/CLOUD_E2E_RUNBOOK.md` | VALID BUT INCOMPLETE | Commands exist, but successful execution cannot follow from current bootstrap and Compose alone. |
| `docs/evidence/final-e2e/` | VALID BUT INCOMPLETE | Contains expected reconciliation and a checklist only; no deployment evidence is claimed. |
| Historical phase reports and archive | STALE | Useful migration history, but several statements contradict the active implementation and frozen architecture. |

## Verification Boundary

Credential-independently verified: Python behavior, Java compilation/tests,
Avro/JSON parsing, SQL/CQL contract tests, shell syntax, Compose rendering,
Terraform format/validation, Markdown links and a high-confidence secret-pattern
scan.

Not verified: Kafka delivery, Schema Registry operations, Flink runtime,
ClickHouse/Scylla writes, Grafana dashboards, Debezium CDC, checkpoint recovery,
Terraform apply, teardown, cloud connectivity, performance, scale and
end-to-end delivery guarantees.
