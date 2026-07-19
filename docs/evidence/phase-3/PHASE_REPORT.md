# Phase 3 Report

## 1. Phase Implemented

Implemented Phase 3 only: the Taobao ClickHouse DDL, Java sink mappings for raw events and one-minute item metrics, a remote fixture smoke workflow, verification queries, and README instructions. The user's explicit Phase 3 instruction overrides the prior report's remote-integration phase gate.

`raw_behavior_events` receives every valid event before late routing. `item_metrics_1m` receives only on-time events and includes `replay_run_id`, so a fixture assertion is isolated from other replays. No ScyllaDB, S3, PostgreSQL, Debezium, timer rules, or Phase 4 work was added.

## 2. Files Changed

- Replaced `infra/clickhouse/schema.sql` with the Taobao raw and metric tables; added `infra/clickhouse/verify.sql`.
- Added ClickHouse HTTP DDL/verification tools and `scripts/run_fixture_demo.sh`; updated the Flink submit script and Phase labels.
- Added the official ClickHouse Flink connector, typed row mappings, ClickHouse sinks, and replay-run-aware metric keys to `flink-jobs/taobao-stream-job/`.
- Added ClickHouse mapping/DDL Java tests and Python HTTP-tool contract tests.
- Updated `README.md` and added this report.

## 3. Commands and Results

| Command | Exit | Result |
| --- | ---: | --- |
| `ruff check producer scripts` | 0 | All checks passed. |
| `ruff format --check producer scripts` | 0 | 24 files already formatted. |
| `PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v` | 0 | 29 tests passed, including ClickHouse HTTP/DDL splitting contracts. |
| `python -m compileall -q producer/src producer/tests scripts` | 0 | Python sources compiled. |
| Fixture `replay` to ignored `artifacts/` | 0 | 999 accepted events and 1 invalid row. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am test` | 0 | 9 Taobao Java tests passed. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am package -DskipTests` | 0 | Produced a 30,146,356-byte shaded JAR containing `TaobaoStreamJob` and `ClickHouseSinkFactory`. |
| `mvn -B test` | 0 | Four-module reactor passed; Taobao ran 9 tests. |
| Git Bash `bash -n` over operational scripts | 0 | All scripts, including `run_fixture_demo.sh`, parsed. |
| `git diff --check` | 0 | No whitespace errors. |
| `verify_clickhouse.py --run-id phase3-control` with no endpoint | 2 (expected) | Stopped before a network request with a missing-endpoint error. |

## 4. Verified Acceptance Criteria

- DDL contract tests cover the required raw and one-minute metric columns, UTC timestamps, defaults, and ordering keys.
- Java mapping tests verify Avro event and metric values map to the ClickHouse column contract.
- The raw sink is attached to the valid branch; the metric sink is attached after watermark and late-event routing.
- The committed fixture replay has the expected 999 valid / 1 invalid split. Item 500's local aggregation contract is `cart=1`, `buy=1`, and `unique_users=1` for its first minute.
- The final raw ordering key is `(toDate(event_time), item_id, event_time, user_id)`: the query pack first bounds date, then item, then event time; `user_id` supports deterministic drill-down after those predicates.

## 5. NOT VERIFIED

- `NOT VERIFIED`: applying DDL to a real ClickHouse service, authenticated HTTPS behavior, and actual table engine acceptance.
- `NOT VERIFIED`: Flink ClickHouse connector runtime writes, batching, retries, duplicates, checkpoint recovery, and shaded-JAR behavior on a remote Flink cluster.
- `NOT VERIFIED`: Kafka/Schema Registry/Flink/ClickHouse fixture smoke completion, raw counts, rollup rows, late-event logs, cloud costs, performance, and teardown.
- `NOT VERIFIED`: all cloud-dependent configuration and credentials. No cloud resource or complete laptop stack was started.

## 6. Blockers and Risks

- A disposable remote host and approved ClickHouse credentials are required for the smoke script. Until then, Milestone A is not end-to-end verified.
- Raw history uses `MergeTree` and does not claim exactly-once delivery; connector retry or recovery behavior requires remote evidence.
- Exact `unique_users` retains a set per item/window; hot items remain a state-size risk.
- Maven Shade emitted overlapping-class/resource warnings from the existing ClickHouse all-in-one connector; the package completed, but remote runtime remains unverified.

## 7. Student Learning Task

Using `infra/clickhouse/verify.sql`, explain why the raw table is ordered by `(toDate(event_time), item_id, event_time, user_id)` rather than beginning with `user_id`, and identify which query would become less efficient with the alternative.

## 8. Teach-Back Questions and Controlled Failure

1. Why does the raw sink use the valid branch while the rollup uses the on-time branch?
2. Why is `replay_run_id` included in `item_metrics_1m` even though it is not an aggregation measure?
3. How does the verification query pack justify the ordering key column order?

Controlled failure: run `PYTHONPATH=producer/src python scripts/verify_clickhouse.py --run-id phase3-control` without `CLICKHOUSE_ENDPOINT`. Predicted and observed result: exit 2 before an HTTP request, preventing an accidental request to an implicit host.

## 9. Cleanup Report

Removed:
- `infra/clickhouse/01_tables.sql`, an obsolete commented F1 Cloud DDL duplicate.

Archived:
- None.

Kept:
- `flink-jobs/f1-telemetry-job/`, `data-generators/f1-replay-engine/`, and Grafana artifacts as the frozen pre-migration baseline; they are not used by the Taobao Phase 3 path.
- The ignored raw dataset and private manifest outside Git.

Reason:
- Phase 3 replaces the conflicting ClickHouse DDL while retaining unrelated frozen baseline code pending real replacement-path integration evidence. No raw data, credentials, or generated artifacts were added to Git.

## 10. Phase Boundary Confirmation

No Phase 4 work was started. In particular, no ScyllaDB schema, sink, latest-state projection, lookup interface, or related dependency was added.
