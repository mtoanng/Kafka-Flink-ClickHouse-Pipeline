# Phase 5 Archive Terraform

This directory creates only the bounded S3 raw-archive bucket. It does not create Kafka, Flink, ScyllaDB, ClickHouse, IAM users, or credentials.

Validate without a backend or resource changes:

```bash
terraform init -backend=false
terraform fmt -check
terraform validate
```

Review a plan with a unique bucket name:

```bash
terraform plan -var="bucket_name=your-unique-bucket-name"
```

Apply and destroy only from an approved disposable AWS account. Use `force_destroy=true` only when intentionally removing a non-empty demo bucket. The archive uploader writes JSONL objects under `object_prefix` and records a SHA-256 manifest.
