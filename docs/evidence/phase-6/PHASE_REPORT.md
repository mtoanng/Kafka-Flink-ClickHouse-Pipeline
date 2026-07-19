# Phase 6 Report

## Phase Implemented

Phase 6 only: PostgreSQL/Debezium control plane and Flink broadcast-state rule
processing. No later phase was started.

## Changed Files

- `schemas/behavior-rule.avsc`
- `infra/postgres/schema.sql`, `infra/postgres/README.md`
- `infra/debezium/postgres-behavior-rules.json`, `infra/debezium/README.md`
- `infra/kafka/behavior-rules-topic.md`
- `flink-jobs/taobao-stream-job/src/main/java/com/taobao/behavior/TaobaoStreamJob.java`
- `.../model/BehaviorAlert.java`
- `.../processing/CartAbandonmentRuleProcessor.java`
- `.../processing/RuleVersionPolicy.java`
- `.../src/test/.../AvroContractTest.java`, `RuleVersionPolicyTest.java`
- `scripts/apply_behavior_rules_schema.sh`
- `scripts/create_behavior_rules_topic.sh`
- `scripts/run_phase6_demo.sh`, `scripts/run_flink.sh`
- `.env.example`, `README.md`

## Checks and Results

- `mvn -B test`: **PASS**, full four-module reactor; Taobao module ran 20 tests.
- `mvn -B -pl flink-jobs/taobao-stream-job -am package`: **PASS**, shaded jar built.
- `ruff check producer scripts`: **PASS**.
- `ruff format --check producer scripts`: **PASS**, 27 files.
- `PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v`: **PASS**, 32 tests.
- `python -m compileall -q producer/src producer/tests scripts`: **PASS**.
- `terraform -chdir=infra/terraform fmt -check`: **PASS**.
- `terraform -chdir=infra/terraform validate`: **PASS**.
- JSON/Avro/PostgreSQL static contract checks: **PASS**.
- Git Bash `bash -n` for all new Phase 6 scripts and `run_flink.sh`: **PASS**.
- `git diff --check`: **PASS**.

## Verified Acceptance

- PostgreSQL has exactly one project table, `behavior_rules`, with version and
  threshold constraints.
- Debezium config uses PostgreSQL `pgoutput`, unwraps the row, and routes to the
  compacted `behavior-rules` topic.
- The existing single Flink job consumes rule Avro records from a separate Kafka
  source and connects them to the event stream through broadcast state.
- Newer rule versions replace older versions; stale updates are ignored.
- Enabled `cart_abandonment` rules schedule event-time timers in keyed `MapState`,
  resolve on a matching buy, and emit `BehaviorAlert` on expiry.
- Missing remote prerequisites fail the topic/schema scripts before any service
  call.

## NOT VERIFIED

- **NOT VERIFIED:** credentialed PostgreSQL, Kafka Connect/Debezium, Kafka topic
  compaction, Schema Registry compatibility against a live Debezium writer, or a
  remote Flink alert.
- **NOT VERIFIED:** the required live rule update without redeploy; it requires a
  disposable remote stack and credentials.
- **NOT VERIFIED:** timer behavior under remote watermarks, restart/recovery,
  throughput, latency, cloud resources, or dashboards.

## Risks and Blockers

The Debezium value schema is expected to resolve by field name to the canonical
`BehaviorRule` Avro reader schema. This must be confirmed with a live connector;
the repository does not start Kafka Connect or PostgreSQL on the laptop. The
processor intentionally keeps the latest pending cart per user/item and emits
alerts to the Flink log; no ClickHouse alert sink was added in this phase.

## Cleanup Report

Removed: none.

Archived: none.

Kept: existing Phase 1-5 data-plane code, reports, and remote smoke scripts.

Reason: Phase 6 adds a control-plane branch without replacing the previously
verified Kafka -> Flink -> ClickHouse/Scylla/S3 path.

## Student Learning Task

Explain why PostgreSQL/Debezium is a control plane (versioned configuration) while
Kafka/Flink/ClickHouse/Scylla process and serve the data plane. Manually inspect
the compacted topic key and explain why `version` ordering is still enforced in
Flink even after compaction.

## Teach-Back Questions

1. What does the Kafka record key represent for `behavior-rules`, and why is the
   topic compacted?
2. Why can a rule update change timer behavior without redeploying the Flink job?
3. What state is cleared when a matching buy arrives, and what happens if the
   rule is disabled before the timer fires?

## Controlled Failure Experiment

Run `bash scripts/create_behavior_rules_topic.sh` without
`KAFKA_BOOTSTRAP_SERVERS`, and `bash scripts/apply_behavior_rules_schema.sh`
without `POSTGRES_HOST`. Both exit with code 1 and a required-variable message
before invoking Kafka or `psql`; no remote access was attempted.

## Phase Gate

Phase 6 implementation and credential-independent checks are complete. Cloud,
connector, live-update, recovery, and performance claims remain explicitly
`NOT VERIFIED`. No later phase was started.
