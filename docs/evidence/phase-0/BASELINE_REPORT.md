# Phase 0 Baseline Report

## 1. Phase Implemented

Phase 0 only: the existing F1 baseline was inspected, built, inventoried, and frozen. Input grain is one driver telemetry sample at one event timestamp; outputs are one raw row per sample and one rollup per driver per 10-second window. Core learning objective: explain the current source, key, event-time, state, and sink flow before migration.

Codex-owned work was documentation organization, static checks, build evidence, and the migration map. Student-owned architecture decisions were not implemented.

## 2. Files Inspected

Inspected every tracked path, with detailed reads of all POMs, Java/Avro sources, ClickHouse DDL, Compose, Grafana provisioning, scripts, `Makefile`, CI, environment examples, active/legacy documentation, `AGENT.md`, and the final blueprint. Local ignored dataset contents and `.env` values were not read; only dataset size/count and environment key names were checked.

## 3. Files Changed, Created, Deleted, or Archived

- Created `AGENTS.md`, `README.md`, `docs/PHASE_0_MIGRATION_INVENTORY.md`, and this report.
- Moved `PROJECT1_BLUEPRINT_FINAL.md` to `docs/PROJECT1_BLUEPRINT_FINAL.md` to match the source-of-truth path.
- Archived the former README, six root implementation/setup guides, `docs/IMPLEMENTATION_GUIDE.md`, and the superseded `docs/PROJECT1_BLUEPRINT.md` under `docs/archive/f1-baseline/`.
- Deleted no useful evidence and changed no executable code, schema, SQL, infrastructure, script, CI, or environment behavior.
- Left the pre-existing untracked singular `AGENT.md` unchanged.

## 4. Commands Run and Exact Results

| Command | Exit | Result |
| --- | ---: | --- |
| `mvn -B clean test` | 0 | Reactor build success; both modules compiled; **no tests to run**. Initial sandboxed attempt exited 1 because dependency downloads were denied, then succeeded with approved Maven access. |
| `mvn -B clean package -DskipTests` | 0 | Reactor package success in 45.642 s. Replay copied 1,181 resources and both shaded JARs were built with overlap warnings. A prior non-clean package attempt hit a transient shaded-JAR `EOFException`; the isolated clean rerun passed. |
| `docker compose -f infra/docker-compose.yml config --quiet` | 0 | Compose model is syntactically valid. |
| `bash -n scripts/*.sh` using Git Bash | 0 | All seven shell scripts parsed. WSL `bash` was unavailable because no distribution is installed. |
| Python JSON/Avro parse and schema equality check | 0 | Parsed four tracked JSON/Avro files; module Avro schemas are byte-identical. |
| `.env` ignore and key-parity check | 0 | `.env` is ignored and untracked; `.env` and `.env.example` key sets match. No values were printed. |
| Makefile script-reference check | 0 | Found three missing referenced scripts: `start_generators.sh`, `stop_generators.sh`, `submit_flink_job.sh`. |
| Tracked-file secret heuristic | 1 | Six candidate assignments; manual key-only review found examples/placeholders or prose, not captured credentials. `gitleaks` is unavailable, so a definitive scan is not verified. |
| PyYAML CI/Compose/provisioning parse attempt | 2 | `PyYAML` is unavailable. Compose was independently validated by Docker; GitLab-specific CI lint was not verified. |
| Empty-dataset replay failure experiment | 1 (expected) | Failed before publishing with `drivers.json not found`; confirms clear fail-fast behavior. It also initializes a Kafka producer before validating input and does not explicitly close it on this error path. |
| `git diff --check` | 0 | No whitespace errors. |
| Phase 0 local Markdown link checker (`python -`) | 0 | All local links resolve in the four new Phase 0 documents. |
| `git status --short` and `git diff --name-status` | 0 | Only documentation renames/additions are present; pre-existing untracked `AGENT.md` remains untouched. |

The local source dataset contains 1,182 files totaling 156,497,494 bytes; the post-test replay target contained 1,187 files totaling 156,564,751 bytes. This is recorded as a resource risk, not modified in Phase 0.

## 5. Verified Acceptance Criteria

- Active runtime behavior is unchanged.
- Credential-independent Maven compile/test and package commands pass.
- Compose, shell, tracked JSON/Avro, duplicate schema, ignore, and environment-key checks were recorded.
- A current-flow/reuse inventory and file-by-file migration map exist.
- Conflicting F1 documents are archived without deleting evidence.
- `AGENTS.md` points contributors to the final blueprint and enforces phase/resource/learning constraints.

## 6. Items Marked NOT VERIFIED

- `NOT VERIFIED`: Kafka and Schema Registry runtime/topic/schema registration.
- `NOT VERIFIED`: Flink job execution, checkpoints/savepoints, watermarks under live input, and recovery.
- `NOT VERIFIED`: ClickHouse DDL execution, connector mappings, inserts, deduplication, queries, and Grafana dashboards.
- `NOT VERIFIED`: cloud services, credentials, deployment, teardown, cost controls, latency, throughput, and exactly-once semantics.
- `NOT VERIFIED`: GitLab CI lint/job execution and definitive `gitleaks` secret scanning.
- `NOT VERIFIED`: SQL container contract tests and fixture-based end-to-end smoke tests; no fixture or local ClickHouse service exists.

## 7. Remaining Risks or Blockers

- The student learning gate is incomplete.
- Zero automated tests exist despite successful compilation.
- Replay loads and sorts all events in memory and packages ignored source data, violating the target bounded-memory/resource model.
- The Makefile cannot execute `submit`, `generate`, or `demo` because three scripts are absent.
- Java 20 ran a Java 11-targeted build; CI/runtime compatibility on the intended Java environment is not verified.
- Shaded dependency overlap warnings, including duplicated connector classes, require later connector verification.

## 8. Student-Owned Task Still Pending

Explain the frozen F1 flow in your own words: source file grain, reconstructed event time, driver-number Kafka key, watermark behavior, 10-second keyed window state, raw/rollup sink grains, and expected behavior for late data, duplicates, producer failure, and ClickHouse failure. Phase 0 cannot pass its learning gate until this attempt is reviewed.

## 9. Teach-Back Questions and Failure Experiment

1. What is the input grain, and how does the replay engine reconstruct `event_time`?
2. Why is `driver_number` the current Kafka key, and what ordering does that provide?
3. Where does the 10-second aggregation state live, when is it released, and what bounds it?
4. What happens today to events later than the five-second watermark, duplicates, and invalid telemetry files?
5. Which command proves the code compiles, and which behaviors still require integration evidence?

Controlled failure experiment: point `F1_DATASET_PATH` at an empty directory and run the replay JAR. Predicted and observed result: exit 1 with `drivers.json not found` before any publish. Follow-up risk: producer construction occurs before dataset validation.

## 10. Phase Boundary Confirmation

No Phase 1 dataset acquisition, profiler, fixture, schema migration, or Taobao runtime work was started.
