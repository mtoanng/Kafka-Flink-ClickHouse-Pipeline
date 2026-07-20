# Temporary Cloud E2E Terraform

This directory plans a disposable AWS VPC, public subnet, route, restricted SSH
security group, Elastic IP, and one EC2 host. The optional Confluent resources
create a Confluent Cloud environment, Basic Kafka cluster, topics, service account,
API key, ACL, and Avro schemas. No S3 resource, data sink, backend, secret, MSK,
managed Flink, Kubernetes, ClickHouse, ScyllaDB, or Grafana resource is defined.

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
