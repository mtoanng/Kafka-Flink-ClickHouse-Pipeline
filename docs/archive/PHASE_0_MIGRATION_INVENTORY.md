# Phase 0 Migration Inventory

## Frozen Baseline Flow

Input grain is one reconstructed car telemetry sample for one driver at one event timestamp. `F1ReplayEngine` reads driver metadata, lap start times, and per-lap telemetry JSON, loads all samples into memory, globally sorts by event time, and publishes Avro to `car-telemetry-events` keyed by driver number.

`F1TelemetryPipeline` consumes from earliest offsets, assigns a fixed five-second out-of-orderness watermark, enables 30-second checkpoints, and branches once. The raw branch writes one ClickHouse row per sample. The aggregate branch keys by driver and emits one row per driver per 10-second tumbling event-time window. Grafana reads the ClickHouse tables. Kafka KRaft and Schema Registry are the only services in Compose; ClickHouse is external.

## Reuse Assessment

- Reuse the Maven multi-module shape, one-job Flink boundary, environment-driven configuration pattern, Kafka source builder, stable operator UIDs, and remote Kafka/Schema Registry fallback.
- Reuse the official ClickHouse connector factory structure only after its version-specific API is rechecked. Replace all F1 mappings and avoid carrying forward unverified deduplication claims.
- Reuse the incremental aggregate pattern and MergeTree DDL mechanics, but leave the Taobao aggregate and ClickHouse ordering/partition decisions to the student-owned Phase 4 work.
- Reuse producer reliability and pacing ideas. Do not reuse the current full-memory load/global sort; the Taobao replay must stream with bounded memory.
- Reuse Grafana provisioning mechanics, lifecycle script patterns, and CI stages after their F1 names and obsolete assumptions are replaced.

## File-by-File Map

| Current path | Disposition | Planned phase |
| --- | --- | --- |
| `pom.xml` | Retain parent scaffold; rename coordinates and manage new modules/dependencies. | 2-3 |
| `data-generators/f1-replay-engine/pom.xml` | Replace Java replay module with Python packaging. | 2 |
| `data-generators/f1-replay-engine/src/main/avro/car-telemetry-event.avsc` | Replace with the canonical user behavior schema; remove duplicate schema ownership. | 2 |
| `data-generators/f1-replay-engine/src/main/java/com/f1telemetry/F1ReplayEngine.java` | Replace; pacing is reference only because loading/sorting is unbounded. | 2 |
| `data-generators/f1-replay-engine/src/main/resources/.gitignore` | Retain dataset exclusion intent; move raw data outside source resources. | 1-2 |
| `flink-jobs/f1-telemetry-job/pom.xml` | Retain module scaffold; rename and verify compatible connectors. | 3-8 |
| `flink-jobs/f1-telemetry-job/src/main/avro/car-telemetry-event.avsc` | Replace with generated/shared user behavior contract. | 2-3 |
| `flink-jobs/f1-telemetry-job/src/main/java/com/f1telemetry/F1TelemetryPipeline.java` | Migrate to the one Taobao job, then add branches phase by phase. | 3-8 |
| `flink-jobs/f1-telemetry-job/src/main/java/com/f1telemetry/InspectPOJOConvertor.java` | Archive/remove obsolete connector debugging helper if no longer needed. | 3-4 |
| `flink-jobs/f1-telemetry-job/src/main/java/com/f1telemetry/model/TelemetryRollup.java` | Replace with item/category output models. | 4 |
| `flink-jobs/f1-telemetry-job/src/main/java/com/f1telemetry/sink/ClickHouseSinkFactory.java` | Retain verified builder mechanics; replace mappings and semantics. | 4 |
| `infra/clickhouse/schema.sql` | Replace F1 tables with raw, metrics, funnel, and alert DDL. | 4-5 |
| `infra/clickhouse/01_tables.sql` | Consolidate or archive duplicate cloud DDL. | 4 |
| `infra/docker-compose.yml` | Retain as remote Kafka/Schema Registry fallback; extend only where the blueprint permits. | 2, 6, 11 |
| `infra/grafana/dashboards/f1-live-telemetry.json` | Replace with Taobao operational/business panels. | 10 |
| `infra/grafana/dashboards/f1-live-telemetry-cloud.json` | Replace after cloud evidence exists. | 10 |
| `infra/grafana/provisioning/datasources/clickhouse.yml` | Retain provisioning pattern; rename database/configuration. | 4, 10 |
| `scripts/create_topic.sh` | Replace topic contract and idempotent topic setup. | 2 |
| `scripts/register_schemas.sh` | Replace subject/schema paths and add compatibility evidence. | 2 |
| `scripts/replay.sh` | Replace Java launch with bounded Python replay. | 2 |
| `scripts/run_flink.sh` | Rename job/JAR and retain remote-oriented launch role. | 3 |
| `scripts/run.sh` | Retain fallback lifecycle structure; remove F1 assumptions. | 2-3 |
| `scripts/healthcheck.sh` | Extend checks as integrations are introduced. | 2-10 |
| `scripts/stop.sh` | Retain teardown role and add cost/residual-resource evidence. | 11 |
| `Makefile` | Repair missing script targets and expose phase-appropriate commands. | 11 |
| `.gitlab-ci.yml` | Replace F1 build assumptions; add required credential-independent jobs. | 11 |
| `.env.example` | Evolve variable names without secrets as each service is added. | 2-10 |
| `.gitignore`, `.gitattributes` | Retain; extend for Python, fixtures, Terraform, and evidence as needed. | 1-11 |
| `README.md` | Keep as truthful phase index; complete setup/demo documentation last. | 0, 11 |
| `docs/archive/f1-baseline/*` | Preserve as historical evidence; never treat as active instructions. | Archived in 0 |
| `docs/PROJECT1_BLUEPRINT_FINAL.md`, `AGENTS.md` | Retain as implementation and contributor contracts. | All |

## Known Baseline Gaps

- There are no Java test sources, fixtures, Python modules, Terraform, CQL, API code, or Taobao contracts.
- `Makefile` references absent `submit_flink_job.sh`, `start_generators.sh`, and `stop_generators.sh` scripts.
- The ignored local F1 dataset is under `src/main/resources`; Maven copies about 156 MB into the replay artifact.
- Replay uses an unbounded list and global sort. Checkpoint, connector, ClickHouse, recovery, throughput, and CI execution behavior have no captured integration evidence.
