# Resume Readiness Report

Audit date: 2026-07-21.

## Verdict

`CODEBASE-READY: NOT AWARDED` because several local validation commands were
blocked by unavailable tooling or provider access. `DEPLOYMENT-VERIFIED: NOT
VERIFIED` because no Astra database was contacted.

## Safe Resume Bullets

- Built a real-time Taobao behavior pipeline using Python, Kafka, Avro, Java
  Flink DataStream, and ClickHouse for behavioral analytics.
- Implemented event-time processing, keyed active-cart state, Cassandra-backed
  CQL serving targeted at DataStax Astra DB Serverless, and CDC-driven rules
  with PostgreSQL, Debezium, and Flink Broadcast State.

These bullets describe implemented code and do not claim cloud deployment.

## Prohibited Claims

Do not claim Astra deployment, Cassandra cloud E2E verification, recovery or
teardown verification, exactly-once Cassandra delivery, exactly-once end to
end processing, production readiness, scale, latency, or benchmark results.

## Exact Astra Verification Steps

1. Provision an Astra DB Serverless non-vector database and keyspace using an
   approved token and the optional Terraform module; record plan/apply evidence
   separately.
2. Obtain the Secure Connect Bundle from Astra and place it under an ignored
   runtime secret path. Supply the token through secret management and set the
   documented `ASTRA_*` and `CASSANDRA_*` variables.
3. Run `scripts/apply_cassandra_schema.sh` against the provisioned keyspace and
   verify the idempotent `user_active_cart` schema.
4. Run the bounded fixture with Cassandra enabled. Verify that user 100 has
   item 501 only, and that the fixed lookup CLI returns no item 500.
5. Repeat cart/buy inputs, exercise stale ordering, restart from a checkpoint,
   and capture duplicate/retry and failure-window evidence without claiming
   exactly-once Cassandra delivery.
6. Perform approved cleanup or teardown and retain the evidence. Never commit
   the token, Secure Connect Bundle, state, or credential-bearing tfvars.

