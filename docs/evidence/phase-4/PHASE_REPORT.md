# Phase 4 Report

## 1. Phase Implemented

Implemented Phase 4 only: ScyllaDB current-state projection, native CQL schema and sink, a fixed one-user lookup CLI, remote schema/fixture wiring, tests, and README/environment updates. The user's explicit Phase 4 instruction overrides the Phase 3 remote-integration `NOT VERIFIED` gate.

The implementation uses the Scylla Java driver and CQL, not the DynamoDB API. `user_current_activity` is keyed only by `user_id`. ClickHouse remains the history and rollup store; ScyllaDB receives only a user's newest accepted on-time activity.

## 2. Files Changed

- Added `infra/scylladb/schema.cql` and `verify.cql` for `user_current_activity`.
- Added `UserCurrentActivity`, keyed `CurrentActivityProjector`, native CQL `ScyllaCurrentActivitySink`, and `ScyllaLookupCli` to the one Java Flink job.
- Added the shaded Scylla CQL Java driver and its version property.
- Added current-activity projection, CQL DDL/insert, and lookup-input Java tests.
- Added remote CQL schema and lookup scripts; extended the remote fixture workflow and Flink submission labels.
- Replaced the obsolete F1 `.env.example`; updated `README.md`, `Makefile`, and this report.

## 3. Commands and Results

| Command | Exit | Result |
| --- | ---: | --- |
| `ruff check producer scripts` | 0 | All checks passed. |
| `ruff format --check producer scripts` | 0 | 24 files already formatted. |
| `PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v` | 0 | 29 tests passed. |
| `python -m compileall -q producer/src producer/tests scripts` | 0 | Python sources compiled. |
| Fixture `replay` to ignored `artifacts/` | 0 | 999 accepted events and 1 invalid row. |
| Git Bash `bash -n` over operational scripts | 0 | All scripts, including the new Scylla scripts, parsed. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am test` | 0 | Scylla CQL driver resolved; Taobao unit and contract tests passed. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am package -DskipTests` | 0 | Built a 45,758,113-byte shaded JAR with the Scylla projector, sink, and lookup CLI. |
| `mvn -B test` | 0 | Four-module reactor passed; Taobao ran 14 tests. |
| `git diff --check` | 0 | No whitespace errors. |
| `bash scripts/lookup_current_activity.sh 0` | 2 (expected) | Rejected the invalid fixed lookup ID before creating a CQL session. |

## 4. Verified Acceptance Criteria

- CQL DDL contract test verifies `user_id` as the sole partition key and all required latest-state fields.
- The prepared insert uses bound values and validates keyspace/table identifiers before interpolation.
- The Flink topology keys on `user_id`; the projector emits a new state only for a newer event-time record, breaking equal event-time ties with `source_sequence`.
- Unit tests verify newer events replace current activity, older events do not overwrite it, and invalid lookup IDs are rejected locally.
- The fixed lookup interface is `bash scripts/lookup_current_activity.sh <positive-user-id>` and is backed by the same native CQL driver as the sink.
- No DynamoDB API, extra serving database, S3 archive, checkpoint experiment, CI, or Terraform work was added.

## 5. NOT VERIFIED

- `NOT VERIFIED`: applying CQL DDL to real ScyllaDB, provider-specific replication/datacenter settings, TLS, authentication, and `cqlsh` options.
- `NOT VERIFIED`: the Scylla driver connecting from Flink, real sink writes, retries, backpressure, failures, duplicate handling, recovery, and shaded-JAR runtime behavior.
- `NOT VERIFIED`: remote fixture smoke completion, a real user `100` lookup, Kafka/Flink/ClickHouse/Scylla end-to-end counts, cloud cost, and teardown.
- `NOT VERIFIED`: checkpoint/restart semantics and whether a replay preserves state under real failures. These remain Phase 5 work.

## 6. Blockers and Risks

- A disposable remote host plus approved ScyllaDB credentials and local datacenter name are required before the CQL path can be verified.
- The committed CQL uses `SimpleStrategy` for a single-node demo. A managed multi-node deployment must use the provider's documented `NetworkTopologyStrategy` datacenter before applying the schema.
- `session_event_count` currently counts current-state transitions that advance the newest event-time record in Flink keyed state. It is not session-gap/timer logic; no session boundary was introduced in Phase 4.
- CQL last-write-wins, Flink sink delivery, and connector retries have not been proven. The shaded build emitted dependency-overlap warnings, so remote runtime compatibility remains a risk.

## 7. Student Learning Task

Explain why `user_id` is the only Scylla partition key and why raw event history and item rollups remain in ClickHouse instead of being added to `user_current_activity`.

## 8. Teach-Back Questions and Controlled Failure

1. What query does `PRIMARY KEY (user_id)` make efficient, and why would item/time analytics be a poor fit for this table?
2. Why does the projector compare event time and then `source_sequence` before emitting a newer user state?
3. Why is native CQL a better fit here than the DynamoDB API for the locked ScyllaDB architecture?

Controlled failure: run `bash scripts/lookup_current_activity.sh 0`. Predicted and observed result: exit 2 with usage text before a Java CQL session or network request is created.

## 9. Cleanup Report

Removed:
- The F1-only contents of `.env.example`.

Archived:
- None.

Kept:
- The ignored `.env` file, because it may contain user-managed secrets and was not read or modified.
- Frozen F1 modules and Grafana artifacts, outside the Taobao Phase 4 serving path.
- ClickHouse history/rollup files, because current state is additive and does not replace analytical storage.

Reason:
- The example configuration conflicted with the Taobao architecture and was replaced. Existing secret-bearing or unrelated baseline files were preserved.

## 10. Phase Boundary Confirmation

No Phase 5 work was started. In particular, no S3 archive, checkpoint/restart experiment, CI pipeline change, Terraform, managed resource provisioning, or teardown automation was added.
