# Temporary Cloud E2E Terraform

This directory plans a disposable AWS VPC, public subnet, route, restricted SSH
security group, Elastic IP, and one EC2 host. The optional Confluent resources
create a Confluent Cloud environment, Basic Kafka cluster, topics, service account,
API key, ACL, and Avro schemas. No S3 resource, data sink, backend, secret, MSK,
managed Flink, Kubernetes, ClickHouse, ScyllaDB, or Grafana resource is defined.

## Host Bootstrap Contract

`cloud_user_data.sh` installs Java 11, Docker, Python, archive tools, and a
pinned Apache Flink distribution. It verifies optional SHA-256 values before
installing a versioned application JAR under
`/opt/taobao/runtime/flink-jobs/taobao-stream-job/target/`. A
`runtime_bundle_url` supplies the Compose file and scripts; no repository token
or application secret is embedded in user-data.

Bootstrap writes an operator-owned `/etc/taobao-runtime.env` with the selected
`runtime_profile` and paths. After adding profile-specific endpoints and
credentials, `/usr/local/bin/taobao-runtime-start` loads that file, starts the
selected Compose profile, optionally submits the Flink JAR, and runs bounded
health checks. `/usr/local/bin/taobao-runtime-stop` stops the profile. Set
`auto_start_runtime=true` only when the bundle, JAR, and environment are already
available; otherwise startup is an explicit operator command.

The runtime identity needs event-topic `WRITE` for replay, event-topic `READ`
and consumer-group `READ` for Flink, compacted rules-topic `READ` for Flink and
`WRITE` for Debezium, plus topic/cluster `DESCRIBE`. The Confluent role binding
grants Schema Registry subject read access; schema registration remains an
explicit provisioning identity operation.

Validate and plan without changing cloud resources:

```bash
terraform init -backend=false -input=false
terraform fmt -check
terraform validate
terraform plan -refresh=false -var-file=terraform.tfvars
```

`enable_confluent_resources=false` is the credential-independent default. A
credentialed operator may set it to `true` and provide Confluent credentials via
environment variables for a review-only plan. This preparation workflow never
runs `terraform apply`.
