# Phase 2 Report

## 1. Phase Implemented

Implemented Phase 2 only: Python Kafka publishing with Confluent Avro, topic and Schema Registry registration scripts, and one Java Flink DataStream job for validation, event-time watermarks, explicit late routing, and one-minute item aggregation. The user's direct Phase 2 instruction overrides the Phase 1 report's later-phase `NOT VERIFIED` gate items.

The topic is `user-behavior-events`; its key is the UTF-8 string form of `user_id`; its value subject is `user-behavior-events-value`. The producer disables automatic registration, so `scripts/register_schemas.sh` must set `BACKWARD` compatibility, check an existing latest version when present, and register the canonical schema before replay.

The Flink policy is deterministic: each valid event advances the watermark to `max_event_time_ms - 5,001`. An event at or behind the current watermark is routed to `late-events` and excluded from the rollup. The fixture's `+15s` event follows a `+30s` event, sees watermark `+24.999s`, and is therefore late. On-time events are keyed by `item_id` into one-minute tumbling event-time windows.

## 2. Files Changed

- Added `producer/src/taobao_replay/kafka.py`, the `publish` CLI command, Kafka optional dependencies, and four Kafka/Avro tests.
- Added `flink-jobs/taobao-stream-job/` with the generated-Avro build, Kafka source, validation and late side outputs, watermark generator, item metrics model/aggregation, and six Java tests.
- Added the Taobao module to `pom.xml` and upgraded Surefire so JUnit 5 tests execute.
- Replaced obsolete F1 behavior in `Makefile`, Kafka/schema/replay/Flink scripts, healthcheck labels, and the Compose network name.
- Updated `README.md` for laptop checks and disposable-host Phase 2 integration.
- Added this report. Existing user changes in `AGENTS.md`, `docs/DATASET_NOTES.md`, and the Phase 1 report were preserved.

## 3. Commands and Results

| Command | Exit | Result |
| --- | ---: | --- |
| `pip install -e ".[kafka]"` | 0 | Installed `confluent-kafka 2.15.0` plus Avro/Schema Registry extras. The first base-only declaration exposed missing `certifi`; the extra was corrected and reinstalled. |
| `$env:PYTHONPATH='producer/src'; python -m unittest discover -s producer\tests -v` | 0 | 26 tests passed in 5.277 s, including Confluent wire-format round trip and schema ID, Kafka key, delivery flush failure, and all Phase 1 tests. |
| `ruff check producer scripts` | 0 | All checks passed. |
| `ruff format --check producer scripts` | 0 | All 20 Python files formatted. |
| `python -m compileall -q producer\src producer\tests scripts` | 0 | Python sources compiled. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am test` | 0 | New module compiled; 6 tests passed. |
| `mvn -B test` | 0 | Four-project reactor passed in 18.400 s; the Taobao module ran 6 tests while both retained F1 modules still have no tests. |
| `mvn -B -pl flink-jobs/taobao-stream-job -am package -DskipTests` | 0 | Final build produced a 22,505,777-byte shaded JAR in 12.260 s; expected duplicate metadata warnings were emitted by Shade. |
| JAR ZIP entry check | 0 | Confirmed `TaobaoStreamJob.class` and generated `UserBehaviorEvent.class` are packaged. |
| Git Bash `bash -n` over seven operational scripts | 0 | All scripts passed syntax checking. |
| `docker compose -f infra/docker-compose.yml config --quiet` | 0 | Compose configuration parsed; no container was started. Docker warned that the current sandbox user could not read the host Docker config file. |
| `python -m taobao_replay publish ... --schema schemas\missing.avsc` | 2 (expected) | Controlled failure stopped on the missing schema before producer creation or any broker connection. |

## 4. Verified Acceptance Criteria

- Python maps all eight canonical fields into Avro and publishes with `user_id` as the Kafka key.
- Confluent Avro serialization/deserialization passed in memory and the wire header carried the registered schema ID.
- Java code generation uses the one canonical schema under `schemas/`; a SpecificRecord binary round trip and reader/writer compatibility check passed.
- The Flink job consumes SpecificRecord Avro from Kafka and has explicit valid, invalid, late, and item-metrics streams.
- Non-positive IDs and negative timestamps are rejected explicitly before watermark assignment.
- The five-second watermark and fixture late-event result were verified deterministically.
- A manually calculated window passed with `pv=2`, `cart=1`, `fav=1`, `buy=1`, and `unique_users=3`.
- No complete stack, broker, Flink cluster, or cloud resource was started on the laptop.

## 5. NOT VERIFIED

- `NOT VERIFIED`: registration and compatibility responses from a real Schema Registry.
- `NOT VERIFIED`: fixture delivery to a real Kafka topic, partition placement by `user_id`, offsets, retries, and broker acknowledgements.
- `NOT VERIFIED`: a running Flink job consuming Kafka, runtime side-output counts, window emission, parallel watermark behavior, restart, or checkpoint recovery.
- `NOT VERIFIED`: authenticated Confluent Cloud settings and every cloud, connector, deployment, throughput, latency, cost, and teardown claim.
- `NOT VERIFIED`: ClickHouse DDL, mappings, sinks, counts, and queries; these belong to Phase 3 and were not started.

## 6. Blockers and Risks

- No credential-independent blocker remains. Remote integration evidence is still required before claiming the Kafka-to-Flink path runs end to end.
- Producer auto-registration is deliberately disabled; replay fails if the registration script has not run successfully.
- Immediate watermarks make fixture behavior deterministic but classify records more than five seconds behind the observed maximum as late. Idle Kafka partitions are excluded after 30 seconds.
- Exact `unique_users` keeps a user-ID set per item/window. State is bounded by one minute but hot items can still require meaningful memory.
- The old F1 modules remain in the reactor and copy 1,181 resources. They are kept only because the Taobao replacement has not passed remote integration yet.

## 7. Student Learning Task

Manually calculate the first fixture minute for one chosen item, then explain why the row at `1511658015` is late after the earlier row at `1511658030` advances the five-second watermark to `1511658024999` ms.

## 8. Teach-Back Questions and Failure Experiment

1. Why is the Kafka key `user_id`, and which ordering guarantee does that provide or not provide?
2. Why does the `+15s` fixture event become late even though it belongs to the same one-minute window as `+30s`?
3. Why must schema registration finish before publishing when `auto.register.schemas=false`?

Controlled failure experiment: publish the fixture with `--schema schemas/missing.avsc`. Predicted and observed result: exit 2 with a missing-file error before producer creation, no Kafka connection, and no partial invalid-output artifact.

## 9. Cleanup Report

Removed:
- Obsolete F1 command paths and service labels from `Makefile`, `scripts/create_topic.sh`, `scripts/register_schemas.sh`, `scripts/replay.sh`, `scripts/run.sh`, `scripts/run_flink.sh`, `scripts/healthcheck.sh`, and `infra/docker-compose.yml`.

Archived:
- None. No new archive was needed in Phase 2.

Kept:
- `data-generators/f1-replay-engine/` and `flink-jobs/f1-telemetry-job/` as the frozen runnable baseline.
- `infra/clickhouse/` and Grafana artifacts unchanged as legacy evidence; they are not part of the Taobao Phase 2 path.
- `data/UserBehavior.csv` and its manifest ignored outside Git.

Reason:
- The F1 baseline cannot be removed until the Taobao Kafka-to-Flink replacement has real integration evidence. ClickHouse changes would begin Phase 3, so those files remain untouched.

## 10. Phase Boundary Confirmation

No Taobao ClickHouse DDL, sink mapping, verification query, or ClickHouse smoke script was implemented. Phase 3 was not started.
