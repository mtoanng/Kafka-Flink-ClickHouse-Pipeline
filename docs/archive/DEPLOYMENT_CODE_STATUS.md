# Deployment Code Status

Status reflects credential-independent inspection and validation. It does not
claim that a cloud database or pipeline has run.

## Implemented and Locally/Statically Verified

| Artifact | Status | Finding |
| --- | --- | --- |
| `infra/cassandra/schema.cql` | VALID | Idempotently creates only `user_active_cart`; no keyspace creation, indexes, views, or history table. |
| Cassandra Java sink and lookup CLI | VALID | Uses CQL, prepared statements, a single reusable session per sink instance, and fixed lookup by `user_id`. |
| `.env.example`, `.env.cloud.example`, `.gitignore` | VALID | Define the Cassandra/Astra runtime contract and exclude tokens, Secure Connect Bundles, Terraform state, plans, and credential tfvars. |
| `infra/docker-compose.yml` | VALID | Does not run a local Cassandra container; serving is an external managed dependency. |
| `infra/terraform/modules/astra/` | VALID | Defines an optional non-vector serverless database with initial keyspace and temporary-demo deletion policy. |
| Core scripts | VALID | Core startup and smoke paths do not require Cassandra configuration when `CASSANDRA_ENABLED=false`. |

## Implemented but Requires Astra Cloud Verification

| Artifact | Status | Remaining evidence |
| --- | --- | --- |
| Astra Terraform module | NOT VERIFIED | Provider authentication, database/keyspace provisioning, outputs, and cleanup. |
| CQL table bootstrap | NOT VERIFIED | Astra-compatible `cqlsh` invocation against a real Secure Connect Bundle. |
| Flink Cassandra sink | NOT VERIFIED | Session connection, writes, retry/replay behavior, restart behavior, and deterministic lookup. |
| Lookup CLI | NOT VERIFIED | Read of real active-cart items through Astra CQL. |

## Terraform Validated But Not Applied

The AWS, Confluent, and Astra Terraform definitions are code only. `terraform
fmt -check` passed, but provider initialization and `validate` remain pending
because the Astra provider was not downloaded in this run. `terraform apply` and
`terraform destroy` are outside this phase.

## Not Implemented

- Real Astra deployment and full cloud E2E evidence.
- Recovery and teardown verification.
- Exactly-once Cassandra or end-to-end delivery claims.
