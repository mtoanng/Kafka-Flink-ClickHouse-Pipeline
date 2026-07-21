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
Apache Cassandra/DataStax Astra DB Serverless, and Grafana are external endpoints supplied through environment
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

## Astra Serving Setup

Set `enable_astra_resources=true` only for a credentialed operator plan with
`astra_token` supplied privately. Terraform is not applied by this repository
workflow. After a database and initial keyspace exist, download its Secure
Connect Bundle to an ignored runtime secret path, set `CASSANDRA_ENABLED=true`,
`CASSANDRA_PROVIDER=astra`, `CASSANDRA_KEYSPACE`, `CASSANDRA_TABLE`,
`ASTRA_DB_SECURE_BUNDLE_PATH`, and `ASTRA_DB_APPLICATION_TOKEN`, then run the
idempotent table bootstrap. Do not commit the bundle or token. `ASTRA_DB_DATABASE_ID`
is recorded for provisioning and setup, not required by the Java CQL session.
