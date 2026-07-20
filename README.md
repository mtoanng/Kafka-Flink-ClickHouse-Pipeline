# Taobao Real-Time Customer Behavior Platform

Final cloud topology:

```text
Python Replay -> Confluent Cloud Kafka + Schema Registry -> one Java Flink job
                                                        -> ClickHouse Cloud
                                                        -> ScyllaDB Cloud
```

The temporary AWS EC2 host runs the replay engine and Flink CLI. The optional
`cdc` Compose profile adds PostgreSQL and Debezium Kafka Connect on that host.
There is no S3 data sink, Amazon MSK, AWS Managed Service for Apache Flink, or
Kubernetes deployment.

## Authoritative Documents

- [Final blueprint](docs/PROJECT1_BLUEPRINT_FINAL.md)
- [Cloud architecture inventory](docs/CLOUD_E2E_ARCHITECTURE.md)
- [Cloud E2E runbook](docs/CLOUD_E2E_RUNBOOK.md)
- [Cloud cleanup audit](docs/CLEANUP_AUDIT.md)
- [Dataset notes](docs/DATASET_NOTES.md)
- [Phase reports](docs/evidence/)

## Local Checks

```bash
pip install -e '.[kafka]'
PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
ruff check producer scripts
ruff format --check producer scripts
mvn -B test
mvn -B -pl flink-jobs/taobao-stream-job -am package
terraform -chdir=infra/terraform fmt -check
terraform -chdir=infra/terraform validate
```

The ignored `data/UserBehavior.csv` is the audited Alibaba Tianchi raw source.
The committed fixture under `tests/fixtures/` is the only dataset used for local
bounded tests.

## Core And CDC Profiles

The Compose file is a disposable remote-host profile. `core` runs Kafka and
Schema Registry; ClickHouse Cloud and ScyllaDB Cloud are external services from
environment variables. `cdc` adds PostgreSQL and Debezium Connect locally on the
same EC2 host.

```bash
docker compose --profile core -f infra/docker-compose.yml up -d
docker compose --profile cdc -f infra/docker-compose.yml up -d
```

Copy `.env.cloud.example` to a private ignored `.env.cloud` and inject secrets at
runtime. Never commit credentials or provider state.

## Cloud Preparation And E2E

Terraform describes only the temporary EC2/VPC/network/security-group/Elastic IP
and optional Confluent Cloud resources. Existing ClickHouse, ScyllaDB, and Grafana
Cloud services are environment variables only.

```bash
terraform -chdir=infra/terraform init -backend=false -input=false
terraform -chdir=infra/terraform validate
terraform -chdir=infra/terraform plan -refresh=false -var-file=terraform.tfvars
```

Do not run `terraform apply` from this preparation workflow. On the disposable
host, use:

```bash
bash scripts/cloud_preflight.sh
bash scripts/run_cloud_smoke.sh
RELEASE_E2E_CONFIRM=YES bash scripts/run_release_e2e.sh
bash scripts/collect_cloud_evidence.sh
bash scripts/verify_cloud_teardown.sh
```

Evidence remains `NOT VERIFIED` until a real bounded run reconciles input rows,
ClickHouse raw/rollup results, Scylla current state, Flink recovery, and optional
CDC rule updates.
