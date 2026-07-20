# Cloud E2E Runbook

Use a disposable EC2 host and bounded fixture data only. Copy
`.env.cloud.example` to a private ignored file and inject credentials through the
environment or a secret manager.

## Prepare and Plan

```bash
terraform -chdir=infra/terraform init -backend=false -input=false
terraform -chdir=infra/terraform fmt -check
terraform -chdir=infra/terraform validate
terraform -chdir=infra/terraform plan -refresh=false -var-file=terraform.tfvars
```

The preparation gate stops after `validate` and `plan`; it does not run `apply`.

## Remote Profile

```bash
docker compose --profile core -f infra/docker-compose.yml config --quiet
bash scripts/cloud_preflight.sh
bash scripts/run_cloud_smoke.sh
```

The local `run.sh` launcher is for the plaintext Compose profile. A managed
cloud run must use the external Kafka and Schema Registry endpoints from the
cloud environment contract and submit the built JAR with `run_flink.sh`; it
must not treat the local broker as the managed Kafka service. ClickHouse,
ScyllaDB, and Grafana are external endpoints supplied through environment
variables. The CDC profile adds local PostgreSQL and Debezium only when enabled.

## Smoke, Release, Evidence, Teardown

```bash
bash scripts/run_cloud_smoke.sh
RELEASE_E2E_CONFIRM=YES bash scripts/run_release_e2e.sh
bash scripts/collect_cloud_evidence.sh
CONFIRM_TEARDOWN=YES bash scripts/teardown_demo.sh
bash scripts/verify_cloud_teardown.sh
```

These commands are remote/operator workflows and are not executed during this
preparation task.
