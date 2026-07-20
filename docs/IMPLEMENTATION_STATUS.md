# Implementation Status

## Implemented and Credential-Independently Verified

- Python replay contracts, tests, formatting, and compilation.
- Java Flink compilation/tests, profile gating, and Avro contracts.
- Core/serving/CDC/observability Compose rendering.
- Terraform formatting/validation and static bootstrap/ACL configuration.
- Grafana JSON provisioning, shell syntax, environment consistency, and
  documentation links.

## Implemented but Not Cloud-Verified

- EC2 bootstrap installation and runtime start/stop contract.
- Kafka topic/group ACL and Schema Registry role-binding definitions.
- Debezium writer-schema compatibility fixture and resolution test.
- ClickHouse-backed Grafana datasource/dashboard templates.
- Shaded JAR dependency selection and overlap analysis.

## Planned Deployment and E2E Work

- Terraform apply in an approved disposable account.
- Managed Kafka/Schema Registry permissions and compatibility checks.
- Flink job execution, ClickHouse/Scylla writes, Grafana rendering, CDC rule
  update, checkpoint/restart, bounded reconciliation, and teardown evidence.

No cloud deployment or production-readiness claim is made by this report.
