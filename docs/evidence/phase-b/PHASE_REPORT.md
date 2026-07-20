# Phase B Report

## Scope

Phase B completed the deployment-code closure work without provisioning or
deploying cloud resources. The active data path remains provider-neutral Kafka
and Schema Registry into one Java Flink job, with optional ScyllaDB, CDC, and
Grafana profiles.

## Implemented

- Terraform EC2 user-data installs Java 11, Docker, Python, pinned Flink, and
  optional checksum-verified runtime bundle/JAR inputs. Runtime start, bounded
  health check, and stop helpers load `/etc/taobao-runtime.env` without secrets.
- Confluent topic resources and ACLs cover event and compacted rules topics,
  Kafka Connect internal topics, Flink and Debezium consumer groups, and
  topic/cluster describe operations. Schema Registry subject read access is
  represented by a role binding; schema registration remains a provisioning
  identity operation.
- Added Debezium writer schema fixture and Avro reader/writer resolution test.
  `BehaviorRule` now carries the PostgreSQL timestamp logical type and a
  Debezium record-name alias.
- Preserved Taobao Grafana datasource/dashboard provisioning and corrected active
  cleanup/evidence documentation so S3 is never an event sink.
- Removed direct Guava/HdrHistogram duplicate dependency copies after inspecting
  the ClickHouse fat connector contents. Remaining shaded warnings are metadata
  resources only; see [SHADED_JAR_ANALYSIS.md](../../SHADED_JAR_ANALYSIS.md).

## Checks

| Check | Result |
| --- | --- |
| Python tests, compile, Ruff lint/format | PASS: 35 tests |
| Maven tests and Avro compatibility | PASS: 28 tests |
| Maven shaded package | PASS; targeted Guava/HdrHistogram duplicates removed; remaining vendor-bundle overlaps documented |
| Avro, Debezium, Grafana JSON parsing | PASS |
| Docker Compose profiles | PASS: core, serving, cdc, observability |
| Terraform fmt/validate | PASS |
| Shell syntax | PASS |
| Secret scan | PASS: high-confidence patterns; gitleaks unavailable |
| Documentation links and diff check | PASS |

## Not Verified

No Terraform apply, cloud connectivity, Kafka ACL propagation, Flink execution,
ClickHouse/Scylla writes, Grafana rendering, CDC live update, recovery, scale,
or teardown was run. These remain `NOT VERIFIED` deployment-gate work.
