# PostgreSQL control plane

`schema.sql` is the complete project schema. It intentionally contains one table,
`behavior_rules`; PostgreSQL is not a serving store for events or user state.

Apply it only on a disposable remote demo PostgreSQL instance:

```bash
POSTGRES_HOST=... POSTGRES_DB=taobao_behavior \
  POSTGRES_USER=... PGPASSWORD=... \
  bash scripts/apply_behavior_rules_schema.sh
```

Operators publish a rule by inserting or updating a row. `version` must increase
for every logical update. Debezium snapshots existing rows and then streams future
updates to the compacted `behavior-rules` topic. Credentials are supplied through
the environment and are never committed.
