# 🚀 VES-Monitor — Release Notes

## v1.0.0 — Submission release · 13 May 2026

> **First production-grade release** of the Vietnam Energy Security Real-time Monitor. Marks the end of the 7-phase development cycle (Foundation → IEA/APERC redesign → submission docs). All four Maven modules are build-clean and the desktop module passes 77/77 tests.

**Git tag**: `v1.0.0` → commit `3d30b39` (`docs: Phase 7.7 — RELEASE_NOTES.md + v1.0.0 tag`). The tag is a snapshot in time; subsequent fixes on `origin/main` are documented in [§ Post-release fixes](#-post-release-fixes) below.
**Repo**: https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres
**Course**: IS402.P21 · Java Programming · UIT-VNUHCM 2026.

---

## 🌟 Highlights

1. **4-pillar IEA / APERC framework** — replaces informal "fuel-oriented" pillars with the internationally recognised Availability / Affordability / Accessibility / Acceptability taxonomy. **16 sub-indicators** (IDR · SFRI · HHI · N-1 · σ_30d · price_gap · β_crude · affordability · reserve_margin · peak_factor · shedding_prob · freq_stability · renewable_pct · CO₂_intensity · curtailment · netzero_progress), all computed as SQL views on read.
2. **Composite Energy Security Index** — `ESI = 0.30·P1 + 0.20·P2 + 0.30·P3 + 0.20·P4` with IEA-standard weighting; status ladder `SECURE ≥80 / ELEVATED 60-79 / STRESSED 40-59 / CRITICAL <40`.
3. **Real-time pipeline** — 4 generators (WSL JVMs) → 4 Kafka topics → Flink job with **18/18 vertices RUNNING**, MapState 60 s cooldown on alerts, SQL `NOT EXISTS` 30 min dedup on auto-recommendations. Verified throughput **~1 000 events/sec**.
4. **JavaFX 17 admin desktop** — 5 tabs (4 pillars + Maps · Bản đồ), live event ticker, per-pillar sparklines, pulse animation on CRITICAL alerts, slide-in toast popups, VN SVG zones + Leaflet world map. 10 design patterns demonstrated.
5. **Spring Boot 2.7 REST API** — 13 endpoints, JWT HS256, Springdoc OpenAPI (Swagger UI at `/swagger-ui.html`), Postman collection committed.
6. **77 / 77 desktop tests PASS** in ~18 s — 24 DAO integration (H2 in-memory `MODE=PostgreSQL`) + 22 service unit (Mockito) + 16 util unit + 15 widget / Flink client (embedded HttpServer).
7. **Comprehensive submission documentation** — 4 Mermaid diagrams (`docs/diagrams/`), 15-slide presenter outline (`docs/SLIDES_OUTLINE.md`), 18-minute demo runbook (`docs/DEMO_SCRIPT.md`), grader artifact manifest (`docs/SUBMISSION_PACKAGE.md`).
8. **Live E2E verified** — full stack ran end-to-end on `HEAD = 30265f1`: Docker Lite 5-container stack (~1.6 GB RAM), 4 Kafka topics flowing, 234 active alerts, 8 active recommendations, **ESI = 72.82 ELEVATED** (live, not seed).

---

## 📊 Phase summary

| Phase | Window | Deliverable | Tag / commit |
|---|---|---|---|
| 0 — Foundation | 11 May | Repo restructure · backup branch · scripts | `v0.0-foundation` |
| 1 — Docker Lite | 11 May | 5-container stack · 2.85 GB RAM budget · healthcheck | `v0.1-docker-lite` |
| 2 — Schema base | 12 May | `users` / `regions` / `alert_rules` / `alerts` | `v0.2-schema` |
| 2.5 — 4-pillar schema | 12 May | `fuel_inventory_raw` / `grid_load_raw` / `renewable_output_raw` / `emission_raw` | `v0.2.5-pillars` |
| 2.6 — Security output | 12 May | `recommendations` audit · `v_security_score` ESI v1 · 8 action views | `v0.2.6-security-features` |
| 3 — Flink alert engine | 12 May | `AlertDetectionFunction` + MapState cooldown + SQL dedup | `v0.3-flink-alert` |
| 4 — 4-pillar Flink | 12 May | 2 new generators · 9 → 18 Flink vertex · multi-pillar `metric_type` | `v0.4-generators` |
| 4.5 — REST API | 12 May | Spring Boot 2.7 · 13 endpoints · JWT · Springdoc | code-complete |
| 5.0 → 5.5 — JavaFX | 12 May | 5 screens · 62 → 77 tests · 10 design patterns | code-complete |
| **E2E** | 13 May | First full live runtime verification | — |
| 7.0 — DB config sync | 13 May | docker DB defaults · `switchScene` catch Exception | `ad1f0db` |
| 7.1 — IEA/APERC redesign | 13 May | `08_pillars_v2.sql` · composite ESI · 4 pillar Java models renamed | `6c48d1e` |
| 7.2 — Real-time visibility | 13 May | Live ticker · sparklines · pulse · toast · refresh cadence split | `08fb6c1` |
| 7.3 — Interactive maps | 13 May | VN SVG 3-zone + Leaflet world map (`javafx-web`) | `837ddba` |
| 7.5 — Final QA | 13 May | E2E re-verification · 5 doc fixes · backend regression flagged | `30265f1` |
| **7.7 — Submission docs** | **13 May** | **Diagrams · slides · demo script · release notes · v1.0.0 tag** | **`v1.0.0`** |

> Detailed phase log: [`docs/PROGRESS.md`](./PROGRESS.md). Original execution plan: [`UPGRADE_PLAN.md`](../UPGRADE_PLAN.md) §24.

---

## ✅ Post-release fixes

> The `v1.0.0` tag is a snapshot at commit `3d30b39`. Items below landed on `origin/main` **after** the tag and are part of the current `main` HEAD, but **not** retro-applied to the tag. No re-tag (`v1.0.1`) was issued — the tag is preserved as-is for archival fidelity. Reproduce the fixed state by checking out `origin/main` instead of `v1.0.0`.

### Phase 7.6 — Backend pillar migration · `e64d447` (post-tag)

**Status**: ✅ **Resolved**. The "Backend REST regression — 5 of 13 endpoints return 500" entry below (carried over from the `v1.0.0` snapshot) is now closed on `main`.

**What landed**:
- `PillarDao` + `SecurityDao` rewritten to read the IEA/APERC views (`v_pillar1_supply_security`, `v_pillar2_market_resilience`, `v_pillar3_grid_reliability`, `v_pillar4_energy_transition`).
- 4 pillar DTOs renamed and reshaped: `Pillar{1..4}{SupplySecurity,MarketResilience,GridReliability,EnergyTransition}Dto`.
- `PillarController` now exposes **both** legacy and new paths (legacy retained as backward-compat aliases — see backend taxonomy below).
- `GET /api/security/cascade-risks` returns `200 OK` with an empty list (`@Deprecated`; the old `v_cascade_risks` view was dropped in Phase 7.1 and cascade analysis is to be re-implemented in a later phase).
- **REST smoke**: 13 / 13 endpoints `200 OK` (`build-logs/p76-api-smoke.json`).
- **Tests**: 11 new `@WebMvcTest` smoke tests in `backend-api` (8 pillar + 3 security; JUnit 4 + `SpringRunner` to match the cached surefire 2.12.4 toolchain). `mvn -pl backend-api -am clean test` → 11 / 11 PASS.
- `docs/openapi.json` (20 paths) and `docs/VES-Monitor.postman_collection.json` (8 folders / 19 requests, with auto-`{{token}}` script on `Auth-Login`) regenerated.

**Backend pillar taxonomy** (post-Phase 7.6, on `main`):

| Pillar | New canonical path | Legacy alias (kept for backward-compat) |
|---|---|---|
| 1 — Availability | `GET /api/pillars/1/supply-security` | `GET /api/pillars/1/outlook` |
| 2 — Affordability | `GET /api/pillars/2/market-resilience` | `GET /api/pillars/2/volatility` |
| 3 — Accessibility | `GET /api/pillars/3/grid-reliability` | `GET /api/pillars/3/shedding` (and `/3/shedding-plan`) |
| 4 — Acceptability | `GET /api/pillars/4/energy-transition` | `GET /api/pillars/4/netzero` (and `/4/net-zero`) |

Both paths return the new IEA-shaped DTO; clients that pinned the old paths continue to work without code changes.

---

## 🐛 Known issues

> Status reflects the `v1.0.0` snapshot at `3d30b39`. Items already resolved on `main` are tagged **RESOLVED** with the fix commit.

### Backend REST regression — 5 of 13 endpoints return 500 · ✅ RESOLVED on `main` (Phase 7.6, `e64d447`)

**Endpoints affected** (in the `v1.0.0` snapshot):
- `GET /api/pillars/1/outlook`
- `GET /api/pillars/2/volatility`
- `GET /api/pillars/3/shedding-plan`
- `GET /api/pillars/4/net-zero`
- `GET /api/security/cascade-risks`

**Root cause**: Phase 7.1 IEA/APERC redesign (`infra/script/08_pillars_v2.sql`) **dropped the 5 legacy views** (`v_pillar1_supply_outlook`, `v_pillar2_volatility_signal`, `v_pillar3_load_shedding_plan`, `v_pillar4_net_zero_progress`, `v_cascade_risks`) and replaced them with the IEA-shaped views (`v_pillar1_supply_security`, `v_pillar2_market_resilience`, `v_pillar3_grid_reliability`, `v_pillar4_energy_transition`). `desktop-admin`'s `ViewsDao` was migrated in lock-step but `backend-api`'s `PillarDao` + `SecurityDao` were not, so any controller that hits a dropped view returned HTTP 500.

**Status as of `v1.0.0` (`3d30b39`)**: 🟡 **Migration in progress** at the time of tagging.
**Status on current `main`**: ✅ **Resolved at `e64d447`** (Phase 7.6 — Backend pillar migration). All 13 / 13 REST endpoints now return `200 OK`; 11 new `@WebMvcTest` smoke tests cover the migrated controllers. See [§ Post-release fixes](#-post-release-fixes) above for full details.

**Impact in the `v1.0.0` snapshot**:
- **JavaFX desktop**: ✅ unaffected — uses direct JDBC to read the new IEA views, not REST.
- **Working REST endpoints** at tag time (8 of 13): `POST /api/auth/login`, `GET /api/auth/me`, `GET /api/security/score`, `GET /api/recommendations`, `POST /api/recommendations/{id}/acknowledge`, `GET /api/alerts/active`, `GET /api/fuel-prices/latest`, `GET /api/grid-load/latest`, `GET /api/health`.

**Workaround when demoing the `v1.0.0` tag**: use the JavaFX dashboard as the primary surface (covers all 4 pillars via direct JDBC); only call the 8 working REST endpoints in the Postman tour (per [`docs/DEMO_SCRIPT.md`](./DEMO_SCRIPT.md) Step 4). When demoing `origin/main` (post-`e64d447`), all 13 REST endpoints are usable.

### Minor issues

- **Flink TaskManager** reports `unhealthy` in `docker compose ps` even though the job itself is 18/18 vertices RUNNING — false-positive healthcheck (the TaskManager service's `/taskmanagers/status` endpoint isn't probed correctly by the Lite docker-compose healthcheck stanza). Cosmetic; doesn't affect functionality.
- **Leaflet world map** requires internet access to `unpkg.com` + OpenStreetMap tile CDN. Behind a Bosch-style corporate firewall, the map falls back to an offline overlay with text hub list — no crash but no interactive markers. Documented in [`docs/PROGRESS.md`](./PROGRESS.md) Phase 7.3.

---

## 🔧 Migration guide

**Nothing to migrate** — `v1.0.0` is the first tagged release. The post-tag Phase 7.6 backend fix on `origin/main` (`e64d447`) inherits the `v1.0.0` schema verbatim; all changes are confined to the Java DAO / DTO / controller layer of `backend-api/`. No SQL schema migration is required to move from `v1.0.0` → `main`.

If you previously cloned the repo at `HEAD < 30265f1` (Phase 7.5 QA pass), you may have stale `Pillar*OutlookDto.java` files left in your working tree. Clean with:

```powershell
git fetch origin
git reset --hard origin/main
git clean -fdx -- backend-api/src/main/java/vn/edu/ves/api/dto/
```

---

## 🛠️ Build & run reproducibility

To reproduce the verified `v1.0.0` state:

```powershell
# 1) Clone @ v1.0.0
git clone --depth 1 --branch v1.0.0 https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres.git
cd Real-time-processing-with-Kafka-Flink-Postgres

# 2) Bring up Docker stack (WSL)
wsl -d Ubuntu-22.04 bash -lc 'bash scripts/run.sh --wait'

# 3) Build all Maven modules
mvn -q clean package -DskipTests

# 4) Run desktop tests — expect 77 / 77 PASS
mvn -pl desktop-admin test

# 5) Submit Flink job
#    → http://localhost:8081 → upload flink-jobs/fuel-flink-job/target/fuel-flink-job-1.1.1.jar
#    → entry: org.cloud.KafkaConsumerApplication

# 6) Start generators (WSL)
wsl -d Ubuntu-22.04 bash -lc 'bash scripts/_phase4_start_generators.sh'

# 7) Start backend + JavaFX (separate PowerShell windows)
java -jar backend-api\target\ves-backend-api.jar
mvn -pl desktop-admin javafx:run    # login admin / admin
```

Expected snapshot at T+3 min (per [`docs/DEMO_RUN_LOG.md`](./DEMO_RUN_LOG.md)):
- Docker stack: 5 containers Up, ~1.6 GB RAM
- Flink: 18 / 18 vertices RUNNING
- 4 Kafka topics flowing (~1000 events/sec aggregate)
- ESI: `72.82 ELEVATED` (P1 64.83 / P2 59.59 / P3 90.11 / P4 72.09)
- 234 alerts, 8 active recommendations

---

## 📦 What's included in `v1.0.0`

### Source code
- **4 Maven modules**: `backend-api/` (Spring Boot), `desktop-admin/` (JavaFX), `flink-jobs/fuel-flink-job/`, `data-generators/{fuel-price-producer,grid-load-generator,renewable-generator}/`
- **~ 6 500 LOC Java** across all modules
- **9 SQL init scripts** in `infra/script/` (loaded in order on first Postgres boot)
- **77 JUnit tests** in `desktop-admin/`

### Documentation
- [`README.md`](../README.md) — top-level introduction + quickstart
- [`UPGRADE_PLAN.md`](../UPGRADE_PLAN.md) — original 7-phase execution plan
- [`JAVA_FINAL_PROJECT_REQUIREMENT.md`](../JAVA_FINAL_PROJECT_REQUIREMENT.md) — course requirements
- [`docs/PROGRESS.md`](./PROGRESS.md) — phase-by-phase log
- [`docs/DEMO_RUN_LOG.md`](./DEMO_RUN_LOG.md) — verified E2E live snapshot
- [`docs/DEMO_SCRIPT.md`](./DEMO_SCRIPT.md) — 18-minute demo runbook
- [`docs/SLIDES_OUTLINE.md`](./SLIDES_OUTLINE.md) — 15-slide presenter outline
- [`docs/SUBMISSION_PACKAGE.md`](./SUBMISSION_PACKAGE.md) — grader artifact manifest
- [`docs/RELEASE_NOTES.md`](./RELEASE_NOTES.md) — this file
- [`docs/TEAM_TASKS.md`](./TEAM_TASKS.md) — 5-person team WBS
- [`docs/ANDROID_ONBOARDING.md`](./ANDROID_ONBOARDING.md) — Android sibling onboarding
- [`docs/PROXY_SETUP.md`](./PROXY_SETUP.md) · [`docs/MAVEN_SETUP.md`](./MAVEN_SETUP.md) — env workarounds
- [`docs/openapi.json`](./openapi.json) — OpenAPI 3 spec for the REST API
- [`docs/VES-Monitor.postman_collection.json`](./VES-Monitor.postman_collection.json) — Postman collection (8 folders / 19 requests + auto-`{{token}}` script on `Auth/Login`)
- [`docs/diagrams/`](./diagrams/) — 4 Mermaid diagrams + index README + export instructions

### Artifacts
- Pre-built JARs in `*/target/`:
  - `flink-jobs/fuel-flink-job/target/fuel-flink-job-1.1.1.jar` (~86 MB shaded)
  - `backend-api/target/ves-backend-api.jar` (~30.3 MB Spring Boot fat JAR)
  - `desktop-admin/target/ves-desktop-admin.jar` (~144 KB thin JAR; JavaFX deps stay in `~/.m2/`)
  - `data-generators/*/target/*.jar` (~17 MB each, with Kafka client deps)

---

## 🙏 Acknowledgments

- **IEA Energy Security Indicators (2014)** — taxonomy used for the 4-pillar redesign in Phase 7.1
- **APERC APEC 100 % framework** — sub-indicator definitions for Pillars 1 + 3
- **IPCC AR6 (2022)** — mitigation pathway for Pillar 4 netzero progress sub-indicator
- **NERC + IEEE 1366** — SAIDI / SAIFI proxy methodology for Pillar 3
- **Vietnam PDP-8 (Quy hoạch điện 8)** — renewable share targets (25 % 2026 / 47 % 2030 / 70 % 2050)
- **Apache Flink + Kafka + PostgreSQL** open-source communities
- **JavaFX OpenJDK team** — JavaFX 17 LTS
- **Spring Boot 2.7.x team** — LTS support window through 2025-08
- All teammates in IS402.P21 group — work breakdown in [`docs/TEAM_TASKS.md`](./TEAM_TASKS.md)

---

## 🔜 Next (post-`v1.0.0`)

These items are **out of scope for `v1.0.0`** but documented here for continuity:

- **Phase 7.6 backend regression patch** — ✅ **Landed post-tag at `e64d447`** (see [§ Post-release fixes](#-post-release-fixes) above). `PillarController` / `PillarDao` / `SecurityController` / `SecurityDao` + 4 pillar DTOs rewired to the IEA-APERC view schema; 13 / 13 endpoints `200 OK`; 11 new `@WebMvcTest` smoke tests added.
- **Phase 8 deploy** — Cloudflare Tunnel to expose the backend + JavaFX over the internet
- **Phase 9 hardening** — Patroni for Postgres HA · Kafka MirrorMaker for DR · OAuth2 + Keycloak replacing the demo JWT
- **TimescaleDB migration** — convert raw tables to hypertables for >1 year retention with auto-partitioning
- **k6 load test** — verify the documented "1 million events/sec scale-out path" with Kafka partitions=12 + Flink parallelism=12

---

**Released by**: VES-Monitor team, 13 May 2026 ICT.
**Tagged**: `v1.0.0` at commit `3d30b39`. **Current `main` HEAD**: `c1f833f` (Phase 7.6 backend pillar migration applied at `e64d447`; subsequent commits are docs-only — see [`AUDIT_REPORT.md`](./AUDIT_REPORT.md)).
