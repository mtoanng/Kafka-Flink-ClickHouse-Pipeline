# Phase 1 Report

## 1. Phase Implemented

Implemented and audited Phase 1 only: a deterministic Taobao fixture, the `UserBehaviorEvent` contract, bounded-memory CSV parsing/profiling, paced local JSONL replay, a reproducible bounded-demo prefix tool, and an enforceable raw-source provenance audit. Kafka publishing, Schema Registry operations, Java Flink migration, watermarks, and sinks were not started.

Input grain is one five-column source behavior row. A valid row becomes one `UserBehaviorEvent`; an invalid row becomes one `ParseIssue` and is not emitted. Source order is preserved, including backward event-time jumps.

## 2. Files Changed

- Added `pyproject.toml` and `producer/src/taobao_replay/` package files for contracts, reading, profiling, replay, and CLI behavior.
- Added `producer/src/taobao_replay/source_audit.py` and the `source-audit` CLI command.
- Added eight test modules under `producer/tests/` with 22 unit tests.
- Added `schemas/user-behavior-event.avsc` as the Phase 1 contract scaffold.
- Added `scripts/generate_fixture.py` and `scripts/prepare_demo_subset.py`.
- Added the 1,000-row `tests/fixtures/user_behavior_fixture.csv`.
- Added `config/dataset-source.example.json` as a deliberately incomplete private-manifest template.
- Added `docs/DATASET_NOTES.md` and this report.
- Updated `.gitignore` for Python outputs and non-Git source/demo data.
- Updated `README.md` and `docs/DATASET_NOTES.md` with the audited raw-source status and honest Phase 2 boundary.
- Created ignored `data/UserBehavior.source.json` locally with the audited Tianchi source manifest; it is intentionally not a Git artifact.
- No F1 Java, Maven, Compose, SQL, Grafana, CI, or archived evidence file was changed in this phase.

## 3. Commands and Results

| Command | Exit | Concise result |
| --- | ---: | --- |
| `python scripts/generate_fixture.py --force` | 0 | Generated exactly 1,000 rows. Fixture is 28,980 bytes with SHA-256 `77224ac1f345ac4c50c417f8e82f6df796ae30ccb0949e4a7098c2726279b427`. |
| `$env:PYTHONPATH='producer/src'; python -m taobao_replay profile tests\fixtures\user_behavior_fixture.csv` | 0 | 1,000 source, 999 accepted, 1 invalid; `pv=251`, `cart=250`, `fav=250`, `buy=248`; one backward jump. |
| `$env:PYTHONPATH='producer/src'; python -m taobao_replay profile data\UserBehavior.csv` | 0 | Read 3,672,347,465 bytes in 1,275.9 s with bounded batches: 100,150,807 source rows, SHA-256 `46fdd7d389c1ddc7922eb7d9014af5573a4a3045da28c6c46197636873d8f1a9`, 100,150,489 accepted, 318 rejected for negative timestamps. |
| `$env:PYTHONPATH='producer/src'; python -m taobao_replay source-audit data\UserBehavior.csv --manifest data\UserBehavior.source.json` | 0 | `SATISFIED` in 1,038.1 s: filename, official Tianchi `dataId=649` manifest, SHA-256, and 100,150,807-row count matched. All rows have five columns; 318 raw rows retain negative timestamps and are explicitly rejected downstream. |
| `$env:PYTHONPATH='producer/src'; python -m unittest discover -s producer\tests -v` | 0 | 22 tests passed in 3.220 s. Source tests reject non-Tianchi URLs, derived columns, headers, hash mismatches, and renamed files; malformed semantic raw rows remain visible for replay rejection. |
| `ruff check producer scripts` | 0 | All checks passed. |
| `ruff format --check producer scripts` | 0 | All 18 Python files formatted. |
| `python -m compileall -q producer\src producer\tests scripts` | 0 | All Python sources compiled. |
| `python -m json.tool schemas\user-behavior-event.avsc` and manifest-template JSON check | 0 | Both files are valid JSON. Unit test verified Avro record fields and enum symbols. |
| Local fixture replay with `--batch-size 64` to temporary accepted/invalid JSONL files | 0 | 16 batches, 1,000 source rows, 999 emitted events, 1 rejected row. Temporary outputs were removed. |
| Generate a temporary fixture, then `python scripts\prepare_demo_subset.py <fixture> <subset> --limit 100` | 0 | Regenerated fixture hash matched the committed file; subset contained exactly 100 rows and reported SHA-256 `2c83f1aed5f7c6aa7902c0f9678c098bd8691e679bb869499bf688eb742f15e7`. |
| `mvn -B test` | 0 | Frozen three-project F1 reactor succeeded in 15.621 s after resolving its existing Avro plugin; both Java modules still report no tests. |
| High-confidence secret-pattern `rg` scan over Phase 1 paths | 0 | No AWS access key, private-key header, or GitHub token pattern found. |
| Missing raw-source audit experiment | 2 (expected) | CLI reported `input file does not exist: data\UserBehavior.csv`; no dataset claim or external connection was made. |
| `git -c safe.directory=... diff --check` plus documentation whitespace check | 0 | No tracked diff whitespace errors; README, dataset notes, and phase report have no trailing whitespace. |
| `git check-ignore -v data\UserBehavior.csv data\UserBehavior.source.json artifacts\events.jsonl` | 0 | Raw data, private acquisition manifest, and local replay artifacts are ignored. |
| Phase 2 path audit with `Test-Path` | 0 | Taobao Flink job, Kafka module, and Python Kafka client paths are absent. |

## 4. Verified Acceptance Criteria

- The committed fixture is deterministic and within the required 1,000-10,000 row range.
- It contains all four behavior types, a repeated raw row, one invalid row, one out-of-order event, cart-then-buy, and an unpurchased cart.
- Event IDs are stable across replay runs and change when stable fields or `source_sequence` change.
- Parsing, profiling, replay, hashing, and subset creation use iterators or fixed-size batches rather than full-file loading.
- Invalid rows are explicit, replay preserves source order, and paced replay does not sleep backward for out-of-order input.
- Local JSONL replay and deterministic subset smoke checks pass without credentials or infrastructure.
- The required raw dataset criterion in `AGENTS.md` is satisfied: ignored local `UserBehavior.csv` has the required filename, five-column shape, official Tianchi `dataId=649` manifest, SHA-256, and row count.
- The source checker validates exact filename, official Tianchi `dataId=649` provenance, raw-event manifest type, acquisition timestamp, SHA-256, row count, and headerless five-column rows using bounded reads.
- The real release's 318 negative-timestamp rows are preserved as raw evidence and will be rejected explicitly by replay rather than cleaned or emitted.
- Non-Tianchi/Kaggle provenance and cleaned or feature-derived shapes are rejected in tests.
- The previous Java baseline still compiles.

Phase 1 acceptance is satisfied: the required raw source, deterministic fixture, bounded replay implementation, and credential-independent checks have evidence.

## 5. NOT VERIFIED

- `NOT VERIFIED`: a bounded-demo output generated from the real raw dataset. Full-source audit passed, but no additional full-data derivative was created under the laptop resource limits.
- `NOT VERIFIED`: Avro binary serialization, compatibility evolution, subject registration, Kafka key serialization, and Kafka delivery. These belong to Phase 2.
- `NOT VERIFIED`: Java Flink consumption, timestamps/watermarks, invalid/late side outputs, aggregation, checkpoints, or recovery.
- `NOT VERIFIED`: every cloud service, connector, deployment, throughput, latency, cost, and teardown claim.

## 6. Blockers and Risks

- Provenance remains dependent on the private acquisition manifest and user-provided official download; the file shape and checksum cannot independently prove origin.
- The 318 negative-timestamp rows and extreme accepted timestamps are raw-data quality risks. Phase 1 keeps them visible and rejects invalid timestamps downstream; time-range policy belongs with later event-time processing.
- The student learning task remains required as a manual verification before the user begins Phase 2.
- A repeated raw row at a different source position has a different `event_id` because `source_sequence` is part of the locked deterministic input. Replaying the same file preserves IDs.
- Phase 1 emits local JSONL only. Treating it as a Kafka producer would incorrectly begin Phase 2.
- The real dataset may expose encoding, malformed-row, timestamp, or scale characteristics not represented by the fixture.
- The legacy Maven build still copies the ignored 156 MB F1 dataset from `src/main/resources`; Phase 1 intentionally did not alter that frozen path.

## 7. Student Learning Task

Explain in your own words: the source and event grain, why the future Kafka key is `user_id`, and why `event_id` includes the five stable source fields plus zero-based `source_sequence` but excludes `replay_run_id`.

## 8. Teach-Back Questions and Failure Experiment

1. Which source row fields define one event, and what happens when any ID, timestamp, column count, or behavior value is invalid?
2. What ordering does a `user_id` Kafka key preserve, and what ordering does it not provide across users?
3. Why does the same source file produce the same IDs across runs, while identical raw values on two different source lines produce different IDs?

Controlled failure experiment: the source-audit unit test supplies an otherwise valid five-column raw row with `user_id=0`. Predicted and observed behavior: the source remains auditable as unmodified raw input, profile reports one `IDs must be positive` rejection, and replay would not emit it as an event.

## 9. Cleanup Report

Removed:
- None in this closing audit run.

Archived:
- None in this closing audit run.

Kept:
- `data/UserBehavior.csv` and `data/UserBehavior.source.json` outside Git because they are required raw-source evidence.
- `tests/fixtures/user_behavior_fixture.csv` in Git because focused tests require deterministic, laptop-sized input.
- The frozen F1 Java baseline because Phase 2 has not started.

Reason:
- The raw file is now verified locally but must remain ignored; the fixture and baseline preserve repeatable Phase 1 checks without adding deployment or later-phase work.

## 10. Phase Boundary Confirmation

No Phase 2 Kafka registration, Avro serialization/compatibility integration, Java Flink migration, watermark, late-data handling, or item-window aggregation was started.
