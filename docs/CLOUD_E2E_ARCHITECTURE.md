# Temporary Cloud E2E Architecture

This inventory is for deployment preparation only. It does not provision or claim
availability of any cloud resource.

```text
AWS VPC -> public subnet -> temporary EC2 host
                         |-> Confluent Cloud Kafka + Schema Registry clients
                         |-> Python replay + Flink CLI/job submission
                         |-> local encrypted Flink checkpoint directory

CDC profile adds local Docker Compose PostgreSQL + Debezium Connect containers.

External managed services, represented only by environment variables:
  ClickHouse Cloud, ScyllaDB Cloud, Grafana Cloud, PostgreSQL/Debezium
```

| Component | Owner | Terraform status |
| --- | --- | --- |
| VPC, subnet, route, internet gateway | AWS | Planned |
| EC2 demo host and restricted SSH group | AWS | Planned |
| Confluent environment, Basic cluster, topics, schemas, service accounts, API keys, ACLs | Confluent Cloud | Optional Terraform-managed resources |
| Kafka + Schema Registry | Confluent Cloud | Endpoint and credentials supplied through environment variables |
| Flink and Python replay | EC2 operator workflow | Not Terraform-managed |
| ClickHouse/Scylla/Grafana Cloud | External providers | Environment variables only |

The laptop runs validation and plan only. No secret, provider token, managed-service
password, private key, or Terraform backend is stored in this profile.
