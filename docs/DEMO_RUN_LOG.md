# Demo Run Log — 2026-05-13 (Phase 6 E2E runtime smoke)

> First fully wired end-to-end runtime test of the VES-Monitor stack
> (Kafka + Flink + PostgreSQL + Spring Boot REST API). Executed from
> the freshly-built `origin/main @ 16eb665` HEAD, all JARs already
> produced from Phase 4.5 + Phase 5 builds. No source changes.

## Environment

| Component | Value |
|---|---|
| OS / Shell | Windows 11 + WSL2 Ubuntu-22.04 (Docker), PowerShell 5.1 (build/curl) |
| JDK / Maven | OpenJDK 21 / Maven 3.9.6 (user-scope on PATH) |
| Docker | Client/Server v29.1.5, Docker Compose v5.0.2 |
| Repo HEAD | `16eb665` (`Phase 4.5 + 5 build verified: 62/62 desktop tests pass, JARs produced`) |
| Network | Home WiFi (off-VPN). Maven did not run during this smoke (all JARs pre-built). |
| Compose profile | **Lite** (`infra/docker-compose.yml`, ~2.85 GB total) |

## Pipeline topology verified

```
3 Java generators (host JVM, WSL2)
   │                                            ┌─ fuel_prices_raw / window_agg / alerts
   ├─► fuel-prices       ─┐                     │
   ├─► grid-load          │   Flink job        ─┼─ grid_load_raw
   ├─► renewable-output   │   18 vertices       │
   └─► emission           │   (JDBC sink)      ─┼─ renewable_output_raw / emission_raw
                          ▼                     │
                       Kafka                    └─ alerts, recommendations (auto-gen)
                                                              │
                                            ┌─────────────────┴─────────────────┐
                                            ▼                                   ▼
                                  PostgreSQL 15                       Spring Boot REST
                                  13 tables + 19 views               (port 8090, JWT)
                                            │
                                            └─► v_security_score (Light ESI 0-100)
```

## Bootstrap command sequence (copy-paste runbook)

Run from repo root, PowerShell — bash bits via WSL.

```powershell
# 1. Bring up Docker stack (Lite profile, ~85s to all-healthy)
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && bash scripts/run.sh --wait'

# 2. (optional) Verify health — script returns exit 0 when 4/4 OK
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && bash scripts/healthcheck.sh'

# 3. Submit Flink job (pre-built JAR via Phase 4 shade plugin)
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && docker cp flink-jobs/fuel-flink-job/target/fuel-flink-job-1.1.1.jar flink-jobmanager:/tmp/fuel-flink-job.jar && docker exec flink-jobmanager flink run -d /tmp/fuel-flink-job.jar'

# 4. Start 3 data generators in background (logs → /tmp/ves-logs/*.log)
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && bash scripts/start_generators.sh'

# 5. Launch Spring Boot REST API on :8090 (background process)
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/fuel_prices"
$env:SPRING_DATASOURCE_USERNAME = "postgres"
$env:SPRING_DATASOURCE_PASSWORD = "123456"
$env:SERVER_PORT = "8090"
$env:NO_PROXY = "localhost,127.0.0.1,::1"
Start-Process java -ArgumentList "-jar","backend-api/target/ves-backend-api.jar" `
    -RedirectStandardOutput "build-logs/backend-run.log" `
    -RedirectStandardError  "build-logs/backend-run.err" `
    -WindowStyle Hidden -PassThru
```

## Verification snapshot

### Docker `ps` + `stats --no-stream` (T+~3 min)

| Container | CPU | Mem | Mem % | State |
|---|---|---|---|---|
| zookeeper | 0.16 % | 141 MiB / 256 MiB | 55 % | healthy |
| kafka | 85 % * | 466 MiB / 640 MiB | 73 % | healthy |
| postgres-database | 7.2 % | 90 MiB / 512 MiB | 17 % | healthy |
| flink-jobmanager | 1.2 % | 405 MiB / 640 MiB | 63 % | healthy |
| flink-taskmanager | 4.2 % | 489 MiB / 800 MiB | 61 % | healthy |

\* Kafka spike is the broker-side `produce` path under load from 3 generators; steady-state idle ≈10 %.

Total container RAM ≈ 1.59 GB / 2.85 GB Lite budget — well within target.

### Kafka topics — partitions = 3, replication = 1

| Topic | Total offsets T0 | Total offsets T+~3 min | Δ msgs |
|---|---:|---:|---:|
| `fuel-prices` | 3 185 | 3 845 | +660 |
| `grid-load` | 638 | 770 | +132 |
| `renewable-output` | 891 | 1 089 | +198 |
| `emission` | 103 | 127 | +24 |

All 4 topics producing at the expected cadence (fuel 30/10s, grid 3/5s, renewable 9/10s, emission 3/30s).

### Flink job

* `jid` = `db7d0dd69f4e90c6ff35023827f1edcc`
* Name = **VES-Monitor Real-time Pipeline (4 pillars / 7 streams)**
* State = **RUNNING**
* Tasks = **18 / 18 running**, 0 failed / 0 canceled

### Postgres row counts (T+~3 min)

| Table | Rows |
|---|---:|
| `fuel_prices_raw` | 7 280 |
| `fuel_price_window_agg` | 447 |
| `fuel_price_alerts` | 1 132 |
| `grid_load_raw` | 1 345 |
| `renewable_output_raw` | 1 872 |
| `emission_raw` | 218 |
| `alerts` | 34 |
| `recommendations` | 8 |

(Tables persist across runs via the `pgdata` volume, so absolute counts include prior demos. **Growth** between T0 and T+3 min confirms current run is sinking rows: e.g. `fuel_prices_raw` +660, `alerts` +8.)

### `SELECT * FROM v_security_score`

```
pillar1_score | 68.52
pillar2_score | 92.46
pillar3_score | 21.02
pillar4_score | 100.00
overall_score | 70.50
status        | STABLE
computed_at   | 2026-05-12 18:09:12 UTC
```

Light ESI is computed live — `pillar2` (volatility) drifted from 95.98 → 92.46 between the two snapshots as σ-rolling caught more price ticks, and `overall_score` re-weighted accordingly. Pillar 3 score is intentionally low (~21) because the grid-load generator deliberately fires `peak=true` batches, lighting up `v_active_alerts`.

> **Phase 7.1 schema update (13 May 2026):** the formula changed to the IEA / APERC weighting `ESI = 0.30·P1 + 0.20·P2 + 0.30·P3 + 0.20·P4`, and the status ladder is now `SECURE / ELEVATED / STRESSED / CRITICAL` (the legacy `STABLE / AT_RISK` labels above were captured *before* `08_pillars_v2.sql` was applied). Re-run the same `SELECT * FROM v_security_score;` after Phase 7.1 to see the new column shape; the sample QA snapshot taken at 13 May ~03:00 ICT was `pillar1=64.83 / pillar2=59.59 / pillar3=90.11 / pillar4=72.09 / overall=72.82 ELEVATED`.

### `v_active_alerts` / `v_active_recommendations`

| View | Count |
|---|---:|
| `v_active_alerts` | 34 |
| `v_active_recommendations` | 8 |

Latest alert sample (most recent EMISSION_INTENSITY breach in `VN_SOUTH`):
* severity = WARNING, threshold = 600 kg/MWh, triggered = 677.84 kg/MWh.
* Auto-generated by Flink `AlertDetectionFunction` + 60s MapState cooldown.

### REST API smoke (port 8090, JWT, all 200 OK)

| # | Endpoint | Status | Key field |
|---|---|---|---|
| 1 | `POST /api/auth/login` | 200 | `accessToken` = `eyJhbGciOiJI…` (8h TTL), `user.role` = `ADMIN` |
| 2 | `GET /api/security/score` | 200 | `overallScore` = 71.17, `status` = `STABLE`, 4 pillar sub-scores present |
| 3 | `GET /api/alerts/active?limit=5` | 200 | 5 items, top = `EMISSION_INTENSITY` WARNING `VN_SOUTH` 677.84 > 600 |
| 4 | `GET /api/pillars/1/outlook` | 200 | 6 rows, top = `VN_CENTRAL` GASOLINE 75 days (BELOW_TARGET), donor `VN_SOUTH`, recommendation_text populated |
| 5 | `GET /api/recommendations?limit=5` | 200 | 5 items (PENDING) |
| 6 | `GET /api/fuel-prices/latest?limit=3` | 200 | 3 rows |
| 7 | `GET /api/health` | 200 | `db = UP`, `status = UP` |

Full raw payloads saved to `build-logs/api_smoke.json`.

> **⚠ Phase 7.1 regression (caught by Phase 7.5 QA pass, 13 May 2026):** the four `/api/pillars/{1..4}/...` endpoints and `/api/security/cascade-risks` now return **HTTP 500** because Phase 7.1 dropped the legacy views (`v_pillar1_supply_outlook`, `v_pillar2_volatility_signal`, `v_pillar3_load_shedding_plan`, `v_pillar4_net_zero_progress`, `v_cascade_risks`) but the backend `PillarDao` / `SecurityDao` / matching DTOs were not migrated to the new `v_pillar*_supply_security|market_resilience|grid_reliability|energy_transition` shape. **Workaround for the demo:** drive the dashboard from JavaFX (which *was* migrated in Phase 7.1 and reads the new views directly via JDBC), or stick to the 6 still-green endpoints (`/api/auth/*`, `/api/security/score`, `/api/alerts/active`, `/api/recommendations*`, `/api/fuel-prices/latest`, `/api/grid-load/latest`, `/api/health`). Backend pillar migration is tracked as the only post-demo blocker.

### Backend log

* `build-logs/backend-run.log` (Spring Boot banner + tomcat lifecycle)
* `build-logs/backend-run.err` (only SLF4J binding-order noise; non-fatal)
* PID stored in `build-logs/backend-run.pid` (this run = **18220**)

### Acceptance criteria (per smoke plan)

* [x] All 4 Kafka topics receiving messages (delta > 0 across 90s).
* [x] Flink job RUNNING, 0 failed vertices.
* [x] All 4 raw tables count > 0 + growing.
* [x] `v_security_score` returns 1 row, score ∈ (0, 100), status ∈ `STABLE`.
* [x] `v_active_alerts` count > 0 (34).
* [x] `v_active_recommendations` count ≥ 0 (8).
* [x] `POST /api/auth/login` returns JWT for `admin` / `admin`.
* [x] 4 protected endpoints return 200 + non-empty body.

## How to launch JavaFX UI (manual — needs a display)

Open a new PowerShell window in the repo root:

```powershell
cd C:\Users\GOT4HC\Real-time-processing-with-Kafka-Flink-Postgres
mvn -pl desktop-admin javafx:run
```

* Login: `admin` / `admin` (also `manager` / `manager`, `user` / `user`).
* Dashboard refresh cadence (post-Phase 7.2): live ticker every **3 s**, table reload every **10 s** (was 30 s).
* 5 tabs (post-Phase 7.3): **Pillar 1-4 + Maps · Bản đồ** (3-zone Vietnam SVG + Leaflet world hubs) + Security Score gauge in the top bar + recommendations sidebar.
* JDBC goes directly to `localhost:5432/fuel_prices` — does **not** require backend-api.

## How to shut everything down

```powershell
# 1. Stop the backend Java process (PID written to file at startup)
$pidFile = "build-logs/backend-run.pid"
if (Test-Path $pidFile) { $p = (Get-Content $pidFile) -replace '\D',''; if ($p) { Stop-Process -Id $p -Force } }

# 2. Kill the 3 generators (running on WSL host)
wsl -d Ubuntu-22.04 bash -lc "pkill -f 'fuel-price-producer-1.0.0.jar' ; pkill -f 'grid-load-generator-1.0.0.jar' ; pkill -f 'renewable-generator-1.0.0.jar' ; true"

# 3. Bring down Docker stack (keeps volumes)
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && bash scripts/stop.sh'

# Or wipe data too:
# wsl -d Ubuntu-22.04 bash -lc '... bash scripts/stop.sh --volumes'
```

## Issues encountered & workarounds

| # | Issue | Resolution |
|---|---|---|
| 1 | `scripts/*.sh` originally had CRLF line endings → `set: pipefail\r: invalid option name` in WSL. | Ran `sed -i 's/\r$//' scripts/*.sh` once at the start of the WSL session (also applied to the helper script `build-logs/verify_e2e.sh`). |
| 2 | First `Invoke-RestMethod` to localhost would go via the corporate proxy and fail. | Added `$env:NO_PROXY = "localhost,127.0.0.1,::1"` and used `-NoProxy` switch consistently. |
| 3 | None of the demo prompts mention this — but the user prompt itself listed `fuel_db` / port 8080 / password `postgres`. The repo's actual config is `fuel_prices` / port 8090 / password `123456` (per `.env` and `backend-api/application.yml`). Followed the project's real config. | Used real values everywhere; documented above. |

No source code was modified during this run.

## Next manual step for the user

```powershell
# (Services left running — see "Final state" above.)
# Launch JavaFX desktop:
cd C:\Users\GOT4HC\Real-time-processing-with-Kafka-Flink-Postgres
mvn -pl desktop-admin javafx:run
# Login admin / admin → Dashboard refreshes every 30s.
```
