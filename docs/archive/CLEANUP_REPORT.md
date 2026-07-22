# Cleanup Report

## Removed

- No valid Terraform, Docker, bootstrap, CI, verification, or teardown artifact was removed.

## Renamed

- The former Scylla schema directory to `infra/cassandra/`.
- Former generic-state Java classes, CQL contract test, lookup CLI, and schema script to Cassandra active-cart names.

## Kept

- Existing AWS, Confluent, Docker Compose, bootstrap, CI, verification, and teardown artifacts.
- PostgreSQL/Debezium control-plane files.
- ClickHouse history and analytical aggregate contracts.

## Reason

The migration changes only active-cart serving. Unexecuted deployment code
remains part of the complete Project 1 codebase and is retained for later
validation. Historical migration reports may retain the prior database name;
active architecture and implementation documents use Apache Cassandra and
DataStax Astra DB Serverless.
