# Debezium control-plane connector

`postgres-behavior-rules.json` is a Kafka Connect configuration for a PostgreSQL
`pgoutput` connector. It snapshots `behavior_rules`, unwraps the CDC envelope,
and routes records to the compacted `behavior-rules` topic.

Copy the template to a private path, replace the `replace-me` database values, and
pass that path as `DEBEZIUM_CONFIG_FILE` to `scripts/run_phase6_demo.sh`; never
commit the private copy. Connector execution, schema-registry compatibility, and
remote CDC delivery are not local checks and remain `NOT VERIFIED` until a
credentialed demo is run.
