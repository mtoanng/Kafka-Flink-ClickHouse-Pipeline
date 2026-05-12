# 📊 Build Progress Tracker

> Theo dõi tiến độ build theo §23.8 (UPGRADE_PLAN.md). Mỗi phase cập nhật khi hoàn thành smoke test.

## Phase tracking

| # | Phase | Status | Tag | Date | Smoke test | Notes |
|---|-------|--------|-----|------|------------|-------|
| 0 | Foundation & Verify | ✅ | v0.0-foundation | 2026-05-11 | Pass (4/4 file struct) | 47 files restructured, rename 88-100% preserved |
| 1 | Docker Lite + Scripts | ✅ | v0.1-docker-lite | 2026-05-11 | Pass 4/4 + memory 918MB/2.85GB | Lite Profile working; BI overlay separated; --wait flag added |
| 2 | Schema users/regions/alerts | ✅ | v0.2-schema | 2026-05-12 | Pass 7 tables + 7 views + 3 bcrypt OK | users, regions, alert_rules, alerts + v_active_alerts view |
| **2.5** | **Schema 4-pillar (Light)** | ✅ | **v0.2.5-pillars** | 2026-05-12 | **Pass 11 tables + 11 views, 4-status Pillar 1** | **Pillar 1/3/4 raw tables + 4 dashboard views; fresh boot loads all 6 SQL files cleanly; memory 929MB no regression** |
| **2.6** | **Security Output Features (actionable)** | ✅ | **v0.2.6-security-features** | 2026-05-12 | **Pass 13 tables + 19 views; security_score 76.41 STABLE** | **8 action views + 2 audit tables (recommendations, daily_briefings); alert_rules extended với metric_type đa pillar; v_security_score Light ESI; v_pillar1_supply_outlook trả recommendation_text human-readable; fresh boot loads all 8 SQL files cleanly; memory 917MB no regression** |
| **3** | **Flink Alert Detection + Recommendation Auto-Gen** | ✅ | **v0.3-flink-alert** | 2026-05-12 | **6 alerts triggered (3W+3C), 1 auto-rec, cooldown verified, cascade risk auto-detect** | **4th branch wired; AlertDetectionFunction with MapState 60s cooldown; SQL NOT EXISTS 30min dedup for recommendations; auto-detected FUEL_SHORTAGE_RISK cascade after volatility kicks in** |
| **4** | **Generators Pillar 3 + 4 (multi-pillar Flink)** | ✅ | **v0.4-generators** | 2026-05-12 | **4 raw streams flowing; 4 alert types fired (CRITICAL grid + 2 WARNING grid + WARNING emission); auto-rec PEAK_SHAVING generated; cascade CARBON_COST_RISK detected** | **grid-load-generator (3 region/5s) + renewable-generator (9 record/10s + 3 emission/30s); Flink 9 vertices (4 source + 5 detector); generic KafkaInvoiceSource.createSourceForTopic; alerts.metric_type added; multi-pillar dispatch in RuleAlertEvent.actionTypeForRecommendation() + getPillar()** |
| **4.5** | **Spring Boot REST API (14 endpoint)** | 🟡 | — | 2026-05-12 | **Code-complete, lint-clean. Build pending (proxy Bosch NTLM block).** | **24 file Java + pom + yml + module README + 14-endpoint smoke script. Spring Boot 2.7.18 / JdbcTemplate / JWT HS256. Khi có hotspot 4G hoặc cntlm bridge: `mvn package` + `java -jar` + `bash scripts/phase45_smoke_api.sh`.** |
| **5.0** | **JavaFX Module Foundation** | 🟡 | — | 2026-05-12 | **Module declared trong parent + 9 file source/config + valid XML + lint clean** | **desktop-admin/ module: pom.xml (JavaFX 17.0.10 LTS + jbcrypt + h2 test + JUnit 4 native với surefire 2.12.4), MainApp.java loads /fxml/login.fxml + /css/material.css, DatabaseConfig singleton (resolve order: sysprop → env → properties), placeholder gradient blue login.fxml, Material CSS. Build runtime test pending cùng proxy block với Phase 4.5.** |
| **5.1** | **Login + BCrypt + SessionManager + UserDao** | 🟡 | — | 2026-05-12 | **18 file (+1 css/+1 login.fxml replaced); lint clean; 17 @Test (PasswordUtil/SessionManager/AuthService Mockito)** | **model/User + model/Role; dao/BaseDao helpers + dao/UserDao; service/AuthService + AuthServiceImpl (DAO → BCrypt → Session → updateLastLogin); util/PasswordUtil (jbcrypt wrapper) + util/SessionManager (AtomicReference) + util/AlertHelper; exception/AuthenticationException; controller/LoginController + DashboardPlaceholderController; fxml/login.fxml full form, fxml/dashboard_placeholder.fxml. Commit `08a97bd`.** |
| **5.2** | **Dashboard 4 pillar TabPane + Security Score gauge** | 🟡 | — | 2026-05-12 | **15 file mới; lint clean; 10 @Test (ViewsDao H2 + DashboardService Mockito)** | **6 model POJO (SecurityScore, Pillar1-4, Recommendation); dao/ViewsDao đọc 6 view (v_security_score + v_pillar1_supply_outlook + ... + v_active_recommendations); service/DashboardService delegation; controller/DashboardController với top-bar ProgressIndicator gauge + TabPane 4 tab (BarChart/LineChart/BarChart/PieChart) + recommendation sidebar + auto-refresh 30s ScheduledExecutorService + Task background thread; fxml/dashboard.fxml; H2TestSupport reflection override singleton. Commit `de7c4dd`.** |
| **5.3** | **Region CRUD + Validator strategy** | 🟡 | — | 2026-05-12 | **11 file mới; lint clean; 14 @Test (Validator 5 + RegionDao H2 6 + RegionService Mockito 4)** | **model/Region + dao/RegionDao (findAll/findById/findByCode/save/delete) + service/RegionService + RegionServiceImpl (auto-uppercase code) + controller/RegionController (form ↔ table sync, applyPermissions VIEWER read-only, confirm dialog delete) + fxml/region.fxml SplitPane. util/Validator (interface + NotBlank/LengthRange/Pattern/InSet strategies + compose Composite). Commit `7ced8e0`.** |
| **5.4** | **AlertRule CRUD + User CRUD (admin-only)** | 🟡 | — | 2026-05-12 | **16 file mới; lint clean; 13 @Test (AlertRuleDao H2 5 + AlertRuleService Mockito 3 + UserService Mockito 5)** | **model/AlertRule + MetricType/Operator/Severity enum; dao/AlertRuleDao full CRUD + findByMetricType + setEnabled; service/UserService delete-protected (admin-only, self-delete guard, sinh BCrypt hash trên save khi pwd non-blank); controller/AlertRuleController (toggle button) + controller/UserController (deny non-admin on initialize → back); fxml/alertRule.fxml + fxml/user.fxml. Commit `8caec9e`.** |
| **5.5** | **Test consolidation + final docs** | 🟡 | — | 2026-05-12 | **62 @Test total (mục tiêu ≥10); UserDaoTest H2 7 mới; README desktop-admin + root + PROGRESS + UPGRADE_PLAN cập nhật** | **Phase 5 code-complete. Bộ test bao phủ 100% DAO (Users/Region/AlertRule/Views) + 100% Service (Auth/Region/AlertRule/User/Dashboard) + util (PasswordUtil/SessionManager/Validator). H2 in-memory MODE=PostgreSQL cho DAO test, Mockito 5.7.0 cho service mock. Build runtime vẫn pending proxy block. Commit pending.** |
| 6 | Android App | 🔀 split | — | 2026-05-12 | — | Tách thành đồ án độc lập, repo riêng [`mtoanng/DataStream`](https://github.com/mtoanng/DataStream). Không còn nằm trong scope đồ án Java. |
| 7 | Deploy + Cloudflared | ⬜ | — | — | — | |
| 8 | Doc + Demo Prep | ⬜ | — | — | — | |
| **E2E** | **End-to-end runtime smoke (full stack)** | ✅ | — | 2026-05-13 | **18/18 Flink vertices RUNNING, 4 topics flowing (+660/+132/+198/+24 msgs in 90s), 34 active alerts, 8 active recommendations, v_security_score=70.50 STABLE, 7 REST endpoints 200 OK** | **Full pipeline verified live on HEAD `16eb665`: Lite Docker stack (~1.59 GB RAM), Flink job submitted from pre-built `fuel-flink-job-1.1.1.jar`, 3 generators on WSL host, backend `ves-backend-api.jar` on :8090. Detailed runbook + payload samples + shutdown procedure in [`docs/DEMO_RUN_LOG.md`](DEMO_RUN_LOG.md).** |
| **7.0** | **Phase 7.0 — DB config sync + visible nav errors** | ✅ | — | 2026-05-13 | 62/62 desktop tests pass | DB defaults match docker (`fuel_prices` / `123456`); `switchScene` now catches `Exception` so FXML load failures surface to the user. Commit `ad1f0db`. |
| **7.1** | **Phase 7.1 — Pillar redesign (IEA/APERC framework)** | ✅ | — | 2026-05-13 | 62/62 desktop tests pass + 6 SQL views live (ESI=73.97 ELEVATED) | 4 pillars renamed: Supply Security / Market Resilience / Grid Reliability / Energy Transition. 16 sub-indicators (IDR, SFRI, HHI, N-1, σ30d, price gap, β-crude, affordability, reserve margin, peak factor, shed prob, freq stability, renewable %, CO2 intensity, curtailment, netzero progress). Composite ESI = 0.30·P1 + 0.20·P2 + 0.30·P3 + 0.20·P4 (IEA weights). Status thresholds SECURE/ELEVATED/STRESSED/CRITICAL. `infra/script/08_pillars_v2.sql` applied live. Java models + DAO + UI rewired with bilingual EN · VI labels. Commit `6c48d1e`. |
| **7.2** | **Phase 7.2 — Real-time visibility** | ✅ | — | 2026-05-13 | 71/71 desktop tests (9 new) | Live event ticker (Flink REST `/jobs/.../metrics`, EMA-smoothed 3s cadence), Sparkline canvas widget (3-min trailing window per pillar), PulseEffect drop-shadow on new CRITICAL rows, Toast widget for CRITICAL alerts, fade-in for new recommendations, refresh cadence split (live 3s / tables 10s, down from 30s), `FlinkClient` HttpURLConnection wrapper. Commit `08fb6c1`. |
| **7.3** | **Phase 7.3 — Interactive maps** | ✅ | — | 2026-05-13 | 77/77 desktop tests (6 new) + `mvn package` clean | `VietnamMap` widget (3 SVGPath polygons coloured by Pillar 3 score, click-to-drill-down), `WorldMapView` (JavaFX WebView + Leaflet 1.9.4 + OpenStreetMap with offline overlay fallback), 7 hub markers tinted by Pillar 2 price delta, new "Maps · Bản đồ" 5th tab loaded via nested FXML. `pom.xml` adds `javafx-web` 17.0.10. Commit `837ddba`. |
| **7.5** | **Phase 7.5 — Final QA pass** | ✅ | — | 2026-05-13 | 77/77 desktop tests + 8/13 backend endpoints OK; 5 doc fixes; backend pillar regression flagged | Re-verified full stack on `c7ef8bd`: branch in sync, lint clean, all FXML parse, 5 docker containers up, Flink 18/18 vertices RUNNING, 4 Kafka topics live, ESI=72.82 ELEVATED. Caught Phase 7.1 backend regression (4 `/api/pillars/*` + 1 cascade-risks endpoint return 500 because legacy views were dropped without migrating `PillarDao`/`SecurityDao`). Documented in DEMO_RUN_LOG.md. JavaFX desktop unaffected. |

**Legend:** ⬜ Pending | 🟡 In progress | ✅ Done | ❌ Failed (rolled back)

---

## Phase 0 — Verification Log

### Pre-flight checks (§24.12)
- [x] Đã đọc xong §24 (UPGRADE_PLAN.md)
- [x] Đã đồng ý với scope cắt §24.6
- [ ] Đã thông báo team: "Đi theo §24, không §23" (offline action)
- [x] Đã backup code branch `legacy/original`
- [x] Đã tag `v0.0-start-minimalist` mốc bắt đầu
- [x] Đọc Phase 0 detailed §24.5

### Action log
1. ✅ Explored current codebase (Java, Maven, Docker available; Maven not on PATH — will add wrapper later if needed)
2. ✅ Created backup branch `legacy/original` and tag `v0.0-start-minimalist`
3. ✅ `git mv` 100% rename — history preserved
4. ✅ Created parent `pom.xml` (multi-module)
5. ✅ Updated 2 child POMs (`<parent>` block)
6. ✅ Created `.env.example`, `.gitignore`, `.gitattributes`
7. ✅ Created `scripts/run.{sh,ps1}`, `stop.{sh,ps1}`, `healthcheck.sh`
8. ✅ Rewrote `README.md` (minimalist, focus clone-and-run)
9. ✅ Created `docs/AI_USAGE_LOG.md`, `docs/PROGRESS.md`
10. ⏳ Phase 0 smoke test (next step)

---

## Phase 2.5 — Schema 4-pillar (Light Coverage) Log

### Decision
User chose **Option B: Light coverage 4 pillars** (vs. Option A "Stay Minimalist" or Option C "Full §23 with ESI"). Title đổi từ "Real-time Fuel Price Monitoring" → "Vietnam Energy Security Real-time Monitor (VES-Monitor)" để match đề tài An ninh năng lượng.

### Action log
1. ✅ Updated `README.md` với title VES-Monitor + bảng tổng quan 4 pillars
2. ✅ Created `infra/script/05_init_pillars.sql` — 4 raw tables:
   - `fuel_inventory_raw` (Pillar 1, generated `stock_days`)
   - `grid_load_raw` (Pillar 3, generated `load_pct`)
   - `renewable_output_raw` (Pillar 4, generated `utilization_pct`)
   - `emission_raw` (Pillar 4, generated `intensity_kg_per_mwh`)
3. ✅ Added 4 dashboard views: `v_pillar1_inventory_status`, `v_pillar3_grid_load_latest`, `v_pillar4_renewable_share`, `v_pillar4_emission_intensity`
4. ✅ Created `infra/script/06_seed_pillars.sql` — 6 row inventory (3 region × 2 fuel) đủ 4 status types
5. ✅ Applied to LIVE Postgres container, verify idempotent (2nd run `INSERT 0 0`)
6. ✅ Fresh boot test (`stop --volumes` + `run --wait`) — load đủ 6 SQL files theo thứ tự, kết quả: 11 tables + 11 views, 4 status types đầy đủ
7. ✅ Memory regression check: 929 MB / 2.85 GB cap (vs. 918 MB ở Phase 1, no regression)
8. ✅ Healthcheck regression: 4/4 OK
9. ✅ Updated `UPGRADE_PLAN.md §24.2.1` (Light 4-pillar strategy), §24.5 (10-phase plan), §24.6 (rewrite Cắt scope), §24.7 (new comparison), §24.8 (directory structure)
10. ✅ Updated `docs/PROGRESS.md` (this file)

### Verification snapshot (12 May 2026)
- **Tables**: 11 (3 fuel + 4 ops + 4 pillar)
- **Views**: 11 (6 fuel + 1 alerts + 4 pillar)
- **Pillar 1 status distribution**: 1 CRITICAL, 2 WARNING, 1 BELOW_TARGET, 2 OK
- **FK integrity**: 4 new tables FK to `regions(code)` ✓
- **Idempotency**: ✓ (re-run all 6 SQL → no duplicates)

### Next after 2.5: Phase 2.6 (Security Output Features)

---

## Phase 2.6 — Security Output Features Layer Log

### Decision
User feedback: *"đủ pillars nhưng đảm bảo có đầy đủ các output features hỗ trợ cho việc an ninh năng lượng chứ ko phải chỉ vẽ số liệu nhé"*. Phase 2.6 thiết kế output stack 4 lớp: **DETECT → PREDICT → RECOMMEND → SCORE**, tất cả implement bằng SQL views (không cần Flink phức tạp).

### Action log
1. ✅ Created `infra/script/07_init_security_features.sql`:
   - `ALTER alert_rules` ADD COLUMN `metric_type` (FUEL_PRICE/INVENTORY_DAYS/GRID_LOAD_PCT/RENEWABLE_PCT/EMISSION_INTENSITY) + `region_code`. Backward-compat default 'FUEL_PRICE'.
   - 2 audit tables: `recommendations` (PENDING/ACK/DISMISSED with JSONB context), `daily_briefings` (security_score + 4 pillar summaries + key_risks JSONB)
   - 8 action views: `v_pillar1_supply_outlook` (forecast + dynamic recommendation_text + donor_region), `v_pillar2_volatility_signal` (σ rolling 1h), `v_pillar2_cross_exchange_divergence` (chênh giá giữa sàn), `v_pillar3_load_shedding_plan` (priority list + suggested_shed_mw), `v_pillar4_net_zero_progress` (vs roadmap VN 2026/2030/2050), **`v_security_score`** (Light ESI 0-100 weighted 25% mỗi pillar, status SECURE/STABLE/AT_RISK/CRITICAL), `v_cascade_risks` (FUEL_SHORTAGE_RISK / GENERATION_DEFICIT_RISK / CARBON_COST_RISK), `v_active_recommendations`
2. ✅ Created `08_seed_security_features.sql`: 5 multi-pillar alert rules (Pillar 1: 2, Pillar 3: 2, Pillar 4: 1) + 3 sample recommendations PENDING (TRANSFER_STOCK CRITICAL, INCREASE_RESERVE WARNING, PEAK_SHAVING_PREP INFO)
3. ✅ Applied to LIVE Postgres. Initial bug: `GREATEST(0, NULL)` returns 0 not NULL → Pillar 3 score 0 instead of baseline 70 → overall_score wrong. **Fixed** với `CASE WHEN MAX(col) IS NULL THEN 70 ELSE ... END`.
4. ✅ Re-applied fix → security_score = 76.42 (STABLE). Idempotency verified: 2nd run `INSERT 0 0`.
5. ✅ Fresh boot test: stop --volumes + run --wait → all 8 SQL files load in order → **13 tables + 19 views**, 10 alert_rules across 4 metric_types, 3 active recommendations, security_score 76.41 STABLE.
6. ✅ Memory regression check: 917 MB / 2.85 GB cap (vs Phase 2.5: 929 MB → improved!)
7. ✅ Healthcheck regression: 4/4 OK
8. ✅ Updated `README.md` (4 pillars actionable features table), `UPGRADE_PLAN.md §24.2.1 + §24.5 Phase 2.6 + §24.7 + §24.8`, `docs/PROGRESS.md`

### Verification snapshot (12 May 2026, after Phase 2.6 fresh boot)
- **Tables**: 13 (3 fuel + 4 ops + 4 pillar + 2 security)
- **Views**: 19 (6 fuel + 1 alerts + 4 pillar dashboards + 8 security actions)
- **alert_rules by metric_type**: FUEL_PRICE=5, INVENTORY_DAYS=2, GRID_LOAD_PCT=2, EMISSION_INTENSITY=1
- **v_security_score**: pillar1=68.5, pillar2=100 (no recent data, baseline 100), pillar3=70 (baseline), pillar4=70 (baseline) → overall 76.4 STABLE
- **v_pillar1_supply_outlook**: 6 row với recommendation_text như *"URGENT: Còn 25.0 ngày. Đề xuất điều chuyển từ VN_CENTRAL (đang có 90.0 ngày)..."*
- **v_active_recommendations**: 3 row (1 CRITICAL, 1 WARNING, 1 INFO)
- **Idempotency**: ✓

### Output features delivered (actionable, không chỉ vẽ số liệu)

| Layer | Feature | Implementation |
|-------|---------|----------------|
| **DETECT** | Multi-pillar alert rules (FUEL_PRICE/INVENTORY_DAYS/GRID_LOAD_PCT/RENEWABLE_PCT/EMISSION_INTENSITY) | Extended `alert_rules` + 5 seed rules |
| **PREDICT** | days_to_critical (Pillar 1), σ rolling 1h (Pillar 2), divergence giữa sàn (Pillar 2), Net Zero progress (Pillar 4) | 4 views với forecast columns |
| **RECOMMEND** | Dynamic recommendation_text trong views + bảng `recommendations` audit trail | `v_pillar1_supply_outlook.recommendation_text`, `v_pillar3_load_shedding_plan.recommendation_text`, 3 seed recs |
| **SCORE** | Energy Security Score 0-100 (Light ESI weighted 25% mỗi pillar) + 4 status SECURE/STABLE/AT_RISK/CRITICAL | `v_security_score` |
| **CASCADE** | Compound risks multi-pillar (3 risk types) | `v_cascade_risks` |

### Next: Phase 4 — 2 Generators (grid-load + renewable) cho Pillar 3/4

---

## Phase 3 — Flink Alert Detection + Recommendation Auto-Gen Log

### Mục tiêu đạt được
Biến Flink job từ chỉ "ghi dữ liệu" thành **detection + recommendation engine** real-time. Khi giá vi phạm rule, hệ thống tự sinh alert (audit trail) và recommendation (actionable for admin). Multi-pillar cascade risk tự động kích hoạt khi volatility + low inventory cùng xảy ra.

### Action log
1. ✅ Explored existing Flink job (3 streams: raw / window / price-change-detector legacy)
2. ✅ Created `model/AlertRule.java` — POJO mapping `alert_rules` table với helper `appliesTo(FuelPrice)` + `isTriggered(double price)`
3. ✅ Created `model/RuleAlertEvent.java` — DTO emit từ AlertDetectionFunction với `actionTypeForRecommendation()` helper
4. ✅ Created `source/AlertRulesLoader.java` — JDBC load FUEL_PRICE rules lúc job startup (1 lần, static rules — restart job khi đổi rule per §24.5 design)
5. ✅ Created `process/AlertDetectionFunction.java` — KeyedProcessFunction với `MapState<ruleId, lastAlertTsMillis>` 60s cooldown
6. ✅ Wired LUỒNG 4 vào `KafkaConsumerApplication.java`:
   - keyBy(fuelType + "::" + location)
   - process(AlertDetectionFunction) → RuleAlertEvent stream
   - Sink #1: `alerts` table (mọi event qua cooldown)
   - Sink #2: filter(severity=CRITICAL) → `recommendations` table với SQL `WHERE NOT EXISTS (... suggested_at > NOW() - 30 min)` dedup
7. ✅ Maven build issue: parent POM pinned `maven-surefire-plugin:3.2.2` nhưng cache chỉ có .pom.lastUpdated (corporate proxy fail). Downgraded to **2.12.4** (cached). Build OK → 86MB shaded JAR.
8. ✅ Upload JAR qua Flink REST API + submit job: `b23dbef9f603a0c621b95f5a31c13fa8`
9. ✅ Initial fail: Kafka topic `fuel-prices` chưa tồn tại (no producer chạy). Created topic manually với 3 partitions. Job tự restart → RUNNING.
10. ✅ Verified AlertRulesLoader logs: nạp 5 rules đúng từ DB (WTI 90, WTI 100, Brent 95, Gasoline NY 3, Diesel London <2)
11. ✅ Smoke test #1 — WTI 105.50: → 2 alerts (rule 1 WARNING + rule 2 CRITICAL) + 1 auto-recommendation HEDGE_IMPORT
12. ✅ Smoke test #2 — Cooldown verification: pushed 3 rapid WTI breaches, only 1 (the one outside 60s cooldown) generated alerts → cooldown works
13. ✅ Smoke test #3 — Recommendation dedup: 3 CRITICAL alerts generated only 1 recommendation (SQL NOT EXISTS 30min check works)
14. ✅ Integration test: 4 streams all RUNNING (raw=125 rows, window=100 rows, legacy price-change=7 rows, rule-alerts=6 rows)
15. ✅ Bonus: `v_security_score` Pillar 2 dropped 100 → 32.70 due to detected VOLATILE signal (σ=6.73%). Overall 76.41 → 60.31.
16. ✅ Bonus: `v_cascade_risks` activated — 3 FUEL_SHORTAGE_RISK CRITICAL detected (WTI volatile × 3 low-inventory regions VN_SOUTH/VN_NORTH)
17. ✅ Updated PROGRESS.md + README + UPGRADE_PLAN.md

### Phase 3 Verification snapshot
- **alerts table**: 6 rows (3 WARNING from rule 1 WTI>90, 3 CRITICAL from rule 2 WTI>100)
- **recommendations**: 4 active (3 seed + 1 auto-gen HEDGE_IMPORT)
- **v_security_score**: 60.31 STABLE (P1=68.5, P2=32.7 ← volatility detected!, P3=70, P4=70)
- **v_pillar2_volatility_signal**: WTI_CRUDE NY = VOLATILE (σ=7.75, 6.73%)
- **v_cascade_risks**: 3 FUEL_SHORTAGE_RISK auto-detected
- **All 4 Flink streams**: RUNNING ✓
- **Cooldown 60s/key/rule**: ✓ (proved by rapid push test)
- **Recommendation 30min dedup SQL**: ✓ (3 CRITICAL alerts → 1 recommendation)

### Files created
```
flink-jobs/fuel-flink-job/src/main/java/org/cloud/
├── model/
│   ├── AlertRule.java          (NEW - POJO from alert_rules)
│   └── RuleAlertEvent.java     (NEW - emitted by AlertDetectionFunction)
├── process/
│   └── AlertDetectionFunction.java  (NEW - KeyedProcessFunction with MapState cooldown)
├── source/
│   └── AlertRulesLoader.java   (NEW - JDBC load at startup)
└── KafkaConsumerApplication.java   (MODIFIED - added LUỒNG 4)
```

### Operational notes
- **Khi đổi alert_rules → restart Flink job** (static load design). Quy trình: cancel job qua Flink UI hoặc REST API DELETE /jobs/{id}, sau đó POST /jars/{id}/run lại.
- **Khi muốn test alert nhanh**: lower threshold của 1 rule cho dễ trigger:
  ```sql
  UPDATE alert_rules SET threshold = 50 WHERE rule_name = 'WTI vượt 90 USD/barrel';
  -- Restart Flink job sau đó.
  ```
- **Monitoring**: Flink UI tại http://localhost:8081/#/job/{jobid}/overview xem 4 vertex (Source, Window, PriceChange, Rule-Alert) đều RUNNING.

### Next: Phase 4 — 2 Generators (grid-load + renewable) cho Pillar 3/4

---

## Phase 4 — Generators Pillar 3 + 4 + Multi-pillar Flink Alert Detection

### Mục tiêu đạt được
Hoàn thiện multi-pillar pipeline với data **LIVE** cho cả 4 pillars. Flink job mở rộng từ 4 vertex → **9 vertex**, consume 4 topic và xuất alerts/recommendations theo `metric_type` của từng pillar. Schema `alerts` được mở rộng để chứa cảnh báo non-fuel-price.

### Action log
1. ✅ Created `data-generators/grid-load-generator/` (~150 LOC) — Pillar 3 producer:
   - 3 vùng VN_NORTH/CENTRAL/SOUTH × interval 5s = 0.6 record/s
   - Ornstein-Uhlenbeck random walk hướng về target = base + peak boost(15-25%) khi 18-22h
   - Capacity tham chiếu EVN 2024 (12k / 6k / 18k MW)
2. ✅ Created `data-generators/renewable-generator/` (~280 LOC) — Pillar 4 producer (2 topic):
   - **renewable-output** (10s/tick): 9 record (3 region × SOLAR/WIND/HYDRO). SOLAR bell-curve quanh 12h trưa với noise mây ±15%. WIND/HYDRO 24/7 với biên độ khác nhau.
   - **emission** (30s/tick): 3 record CO2 dựa trên (assumed_grid_load_75% - total_renewable) × 750 kg/MWh + noise ±3%. Cao khi renewable thấp.
3. ✅ Parent `pom.xml` thêm 2 module mới (build chung mvn `clean package`).
4. ✅ Created `scripts/create_kafka_topics.{sh,ps1}` — pre-create 4 topic (fuel-prices, grid-load, renewable-output, emission) ngăn Flink crash khi consumer lên trước producer. Tự gọi từ `run.sh`/`run.ps1`.
5. ✅ Created **09_alter_alerts_multi_pillar.sql**: ADD `metric_type` (default 'FUEL_PRICE' để backward-compat) + relax `fuel_type`/`location` nullable + CHECK constraint 5 metric types + re-create `v_active_alerts` view với cột `metric_type`. Idempotent.
6. ✅ Flink — 3 POJO mới: `GridLoadEvent` (với helper `getLoadPct()`), `RenewableOutputEvent`, `EmissionEvent` (với helper `getIntensityKgPerMwh()`).
7. ✅ Refactor `KafkaInvoiceSource` thêm method `createSourceForTopic(topic, groupId)` (generic factory) — giữ method cũ `createKafkaConsumer()` cho Pillar 2.
8. ✅ Mở rộng `AlertRule.appliesToRegion(eventRegionCode)` để rule không có regionCode match all region (Pillar 3/4 pattern).
9. ✅ Mở rộng `RuleAlertEvent` với 2 constructor mới `(rule, GridLoadEvent)` + `(rule, EmissionEvent)`, thêm `metricType` field, methods `getPillar()` (1-4) và `actionTypeForRecommendation()` dispatch theo metric_type:
   - FUEL_PRICE → HEDGE_IMPORT / PRICE_MONITORING
   - GRID_LOAD_PCT → PEAK_SHAVING
   - EMISSION_INTENSITY → DISPATCH_RENEWABLE
   - INVENTORY_DAYS → TRANSFER_STOCK
10. ✅ Created `GridLoadAlertDetector` + `EmissionAlertDetector` — cùng pattern KeyedProcessFunction + MapState 60s cooldown, key=region_code.
11. ✅ Refactor `KafkaConsumerApplication` — extract helper `addAlertSinks(stream, label)` chung cho mọi pillar. Wire 3 luồng mới (grid-load → grid_load_raw + GridLoadAlertDetector; renewable-output → renewable_output_raw; emission → emission_raw + EmissionAlertDetector). Job được đặt lại tên: **"VES-Monitor Real-time Pipeline (4 pillars / 7 streams)"** với **9 vertex**.
12. ✅ Build `mvn -q clean package -DskipTests` ok (~80s). Output: 4 JAR (3 generator 17MB + 1 Flink job 86MB).
13. ✅ Apply migration `09_alter_alerts_multi_pillar.sql` vào LIVE Postgres → bảng `alerts` có `metric_type`, `fuel_type` đã nullable.
14. ✅ Pre-create 4 topic qua `scripts/create_kafka_topics.sh` (idempotent `--if-not-exists`).
15. ✅ Re-submit Flink JAR qua REST API → job `cabcf3c9f4fd62fd6b76c2ba4265c40b` RUNNING với **9 vertex** đều RUNNING.
16. ✅ Started 3 generator background (fuel-price + grid-load + renewable) → trong 30s đầu Pillar 2/3 cho 1500+/344+ rows tốt; Pillar 4 emission ban đầu `co2=0/energy=0` do bug **windowMinutes int division** (30s / 60_000L = 0).
17. ✅ Bug fix renewable-generator: chuyển sang `double emissionWindowMinutes` để giữ phần thập phân (30s = 0.5 phút). Rebuild + restart → intensity tính đúng (267-640 kg/MWh, gần ngưỡng 600 WARNING).
18. ✅ Stress test multi-pillar alert (script `_phase4_stress_alerts.sh`):
    - **GRID_LOAD_PCT 95% VN_NORTH** → 2 alert (CRITICAL rule>90 + WARNING rule>80) + 1 auto-rec `PEAK_SHAVING` (pillar=3)
    - **GRID_LOAD_PCT 83% VN_CENTRAL** → 1 alert WARNING
    - **EMISSION_INTENSITY 700 kg/MWh VN_SOUTH** → 1 alert WARNING (rule là WARNING-only nên không sinh rec)
19. ✅ `v_security_score` sau stress: P1=68.52, P2=86.06, P3=**0** (cascading drop từ 95% load!), P4=100 → overall=63.65 STABLE.
20. ✅ `v_cascade_risks` mới detect thêm `CARBON_COST_RISK WARNING` ("VN_SOUTH intensity 609 kg/MWh + WTI VOLATILE → chi phí xanh tăng") bên cạnh 3 FUEL_SHORTAGE_RISK.
21. ✅ Memory regression: docker stack **~1.6 GB / 2.85 GB cap** (vs Phase 3: 917 MB). Phần delta ~700 MB đến từ data flow nặng hơn, vẫn dưới ngưỡng cảnh báo. 3 generator JVM ~1.2 GB (host WSL, ngoài cap docker).
22. ✅ Updated `README.md`, `UPGRADE_PLAN.md`, `docs/PROGRESS.md`.

### Phase 4 Verification snapshot
- **Raw tables**: grid_load_raw=344, renewable_output_raw=432, emission_raw=52, fuel_prices_raw=2265
- **Alerts by metric_type**: FUEL_PRICE 5C+5W = 10; GRID_LOAD_PCT 1C+2W = 3; EMISSION_INTENSITY 1W = 1
- **Recommendations**: 6 PENDING (Pillar 1 TRANSFER_STOCK, Pillar 2 HEDGE_IMPORT × 2, Pillar 3 PEAK_SHAVING ← new, plus 2 seed)
- **v_security_score**: 63.65 STABLE; pillar3=0 (auto-reacted to 95% stress event)
- **v_cascade_risks**: 4 risks (3 FUEL_SHORTAGE_RISK + 1 CARBON_COST_RISK ← new multi-pillar compound)
- **All 9 Flink vertex**: RUNNING ✓
- **Memory**: 1.6 GB / 2.85 GB cap (no regression vs healthy budget)

### Files created/modified
```
data-generators/grid-load-generator/                       (NEW MODULE)
├── pom.xml
├── src/main/java/vn/edu/ves/grid/
│   ├── GridLoadGenerator.java     (Kafka producer main)
│   ├── GridLoadMockSource.java    (Ornstein-Uhlenbeck random walk)
│   └── model/GridLoadEvent.java
└── src/main/resources/application.properties

data-generators/renewable-generator/                       (NEW MODULE)
├── pom.xml
├── src/main/java/vn/edu/ves/renewable/
│   ├── RenewableGenerator.java    (2 topic producer)
│   ├── RenewableMockSource.java   (SOLAR bell-curve + WIND/HYDRO)
│   └── model/{RenewableOutputEvent, EmissionEvent}.java
└── src/main/resources/application.properties

flink-jobs/fuel-flink-job/src/main/java/org/cloud/
├── model/
│   ├── GridLoadEvent.java         (NEW)
│   ├── RenewableOutputEvent.java  (NEW)
│   ├── EmissionEvent.java         (NEW)
│   ├── RuleAlertEvent.java        (MOD - +2 constructor, getPillar, dispatch)
│   └── AlertRule.java             (MOD - +appliesToRegion)
├── process/
│   ├── GridLoadAlertDetector.java (NEW)
│   ├── EmissionAlertDetector.java (NEW)
│   └── AlertDetectionFunction.java (unchanged - backward compat)
├── source/
│   └── KafkaInvoiceSource.java    (MOD - +createSourceForTopic generic)
└── KafkaConsumerApplication.java  (MOD - +3 luồng, addAlertSinks helper)

infra/script/
└── 09_alter_alerts_multi_pillar.sql  (NEW - metric_type + relax nullable)

scripts/
├── create_kafka_topics.{sh,ps1}      (NEW - pre-create 4 topic)
├── run.{sh,ps1}                      (MOD - call create_kafka_topics after up)
└── _phase4_*.sh|*.sql                (dev helpers — smoke, stress, status)

pom.xml                                (MOD - +2 module)
```

### Operational notes
- **Khởi động đầy đủ 3 generator**: `bash scripts/_phase4_start_generators.sh` (cũng dùng được làm template cho deploy script Phase 7).
- **Test alert nhanh**: `bash scripts/_phase4_stress_alerts.sh` → đẩy event vượt ngưỡng cho 2 pillar mới.
- **Verify state**: `bash scripts/_phase4_verify.sql` qua psql → đếm raw + alerts + recommendations.
- **Khi đổi rule GRID_LOAD_PCT hoặc EMISSION_INTENSITY → restart Flink job** (cùng pattern Phase 3 — load static lúc startup).

### Next: Phase 4.5 — Spring Boot REST API (14 endpoint)

---

## Phase 4.5 — Spring Boot REST API (Backend) Log

### Mục tiêu đạt được
Đặt cầu nối REST giữa data tầng Postgres và 2 client phía trên (JavaFX Desktop + Android). 14 endpoint cover trọn 4 pillar + Energy Security Score + cascade risks + recommendation acknowledge audit. Stack chọn **Spring Boot 2.7.18** (không 3.x) để giữ Java 11 đồng bộ với 4 module hiện hữu.

### Decision points
1. **Spring Boot 2.7 thay vì 3.x**: Spring Boot 3 yêu cầu Java 17. Nếu bump Java sẽ phải rebuild + retest cả 3 generator + Flink job (vốn pin Java 11) → risk lớn. SB 2.7.18 là LTS cuối cho Java 11, vẫn nhận security patch tới 2025-08, an toàn cho đồ án.
2. **JdbcTemplate thay JPA**: Cố ý theo §24.6 ("không bị magic Hibernate") + tận dụng 19 view SQL phức tạp đã build (đặc biệt `v_security_score`, `v_cascade_risks`, `v_pillar1_supply_outlook`). DAO chỉ map 1:1 row → DTO.
3. **JSONB pass-through**: Cột `details` của `v_cascade_risks` và `suggested_data` của `v_active_recommendations` là JSONB. Dùng `details::text AS details` ở SQL + `@JsonRawValue` ở DTO → frontend nhận JSON nguyên gốc, không phải string-escaped.
4. **Port 8090 thay vì 8080**: Tránh đụng default Flink REST (8081) + Metabase (3000) + Postgres (5432) + Kafka (9092). 8090 là port duy nhất chưa dùng trong stack.
5. **Lombok**: Dùng `@Data @Builder` cho 14 DTO để giữ code ngắn. Lombok 1.18.30 đã cache (dùng ở 3 generator).

### Action log
1. ✅ Schema audit: chạy `\d+` từng view (`v_pillar1..4`, `v_security_score`, `v_cascade_risks`, `v_active_recommendations`, `v_active_alerts`, `users`) → có sẵn 9 view + 1 table cần map. Quyết định bố cục DTO theo cột thực tế.
2. ✅ Cập nhật parent `pom.xml`: thêm module `backend-api` + 3 property mới (`spring.boot.version=2.7.18`, `jjwt.version=0.11.5`, `springdoc.version=1.7.0`).
3. ✅ `backend-api/pom.xml`: Spring Boot starter-web + starter-jdbc + starter-security + starter-validation, jjwt 0.11.5 (api+impl+jackson), springdoc-openapi-ui 1.7.0, PostgreSQL 42.7.3, Lombok 1.18.30. Plugin `spring-boot-maven-plugin` với `<goal>repackage</goal>` → fat JAR ~30 MB.
4. ✅ `application.yml`: DataSource Hikari (max pool 5, min idle 1), JWT secret 256-bit base64 demo, expiration 8h, springdoc paths, port 8090.
5. ✅ `VesApiApplication.java`: entry-point, header comment liệt kê 14 endpoint.
6. ✅ `config/OpenApiConfig.java`: Swagger metadata + Bearer scheme. UI tại `/swagger-ui.html`.
7. ✅ Security stack:
    - `JwtTokenProvider.java` — HS256 sign/parse, claim {sub=username, uid, role}, `parseSilently()` swallow exception (filter friendly).
    - `JwtAuthFilter.java` — `OncePerRequestFilter`. Đọc `Authorization: Bearer`, populate `SecurityContext` với `ROLE_*`. Lưu uid vào `auth.details` cho controller dùng.
    - `SecurityConfig.java` — stateless, csrf disable, `/api/auth/login` + `/api/health` + Swagger paths public, còn lại `.authenticated()`. CORS `allowedOriginPatterns("*")` cho Android emulator dev. AuthenticationEntryPoint trả JSON `{error:"UNAUTHORIZED"}`.
8. ✅ Exception layer: `ApiException` (HttpStatus + code + message, factory methods unauthorized/forbidden/notFound/badRequest/conflict) + `GlobalExceptionHandler` (`@RestControllerAdvice`) → mapping `MethodArgumentNotValidException` → 400, `ApiException` → custom status, mọi `Exception` → 500 với log.
9. ✅ DTO layer (14 file): tất cả Lombok `@Data @Builder @AllArgsConstructor`. JSONB columns được khai báo là `String` + `@JsonRawValue` cho pass-through.
10. ✅ DAO layer (5 file):
    - `UserDao` — findByUsername / findById / touchLastLogin. Trả nội bộ `UserRecord` (gồm `passwordHash`), controller không expose.
    - `PillarDao` — 6 query (4 pillar views + `v_pillar3_grid_load_latest` + `fuel_prices_raw` latest với optional fuel_type filter).
    - `SecurityDao` — 2 query (`v_security_score` + `v_cascade_risks` với `details::text` cast).
    - `RecommendationDao` — list + acknowledge (UPDATE chỉ chạy nếu status='PENDING' để idempotent, trả số row updated).
    - `AlertDao` — query `v_active_alerts`.
11. ✅ Controller layer (7 file):
    - `AuthController` — POST /login (bcrypt verify + `touchLastLogin` + sign JWT), GET /me (đọc uid từ JWT.details, query DAO).
    - `PillarController` — 4 GET endpoint trả `List<DTO>` thẳng từ DAO.
    - `SecurityController` — score + cascade-risks. Score throw `notFound` nếu view rỗng (chỉ xảy ra khi DB chưa init).
    - `RecommendationController` — list (giới hạn 1-200) + ACK (whitelist status `{ACKNOWLEDGED, DISMISSED}`, return 404 nếu UPDATE đụng 0 row → đã ACK trước).
    - `AlertController` — list active.
    - `RawDataController` — `/api/fuel-prices/latest` + `/api/grid-load/latest`.
    - `HealthController` — DB `SELECT 1`, trả JSON `{status: UP|DEGRADED, db: UP|DOWN, timestamp}` (public).
12. ✅ Helper script `scripts/phase45_smoke_api.sh` (140 dòng bash): 14 endpoint × curl với màu sắc + summary count. Tự `NO_PROXY=localhost` để bypass corp proxy. Tham số `BASE`/`USER`/`PASS` qua env.
13. ✅ `backend-api/README.md` (~180 dòng): endpoint summary table, seed users, quick run, build with proxy workaround (hotspot / cntlm / px / mirror), application.yml override matrix, bố cục module, troubleshoot 6 lỗi thường gặp.
14. ✅ ReadLints toàn bộ `backend-api/` → **không có lỗi**.
15. ⚠️ Build verification PENDING: `mvn -pl backend-api -am clean package -DskipTests` thất bại với 407 Proxy Authentication Required khi tải Spring Boot 2.7.18. Diagnostic `curl -v -x http://rb-proxy-apac.bosch.com:8080` xác nhận proxy Bosch trả `Proxy-Authenticate: NEGOTIATE` + `NTLM` only — Maven Wagon stock không hỗ trợ. Người dùng đã chọn **Hướng Y (defer build)** thay vì cài `cntlm` hoặc bật hotspot 4G.
16. ✅ Updated `README.md` (status badge → "Phase 4.5 code-complete", thêm mục Backend REST API với 14-endpoint table, troubleshoot proxy NTLM, structure tree expand `backend-api/`), `UPGRADE_PLAN.md §24.5` (mark phase 🟡, decision points, smoke script reference, build dependencies, link `backend-api/README.md`), `docs/PROGRESS.md` (this file).

### Verification snapshot (12 May 2026 — code-complete state)
- **Files Phase 4.5 mới tạo**: 24 file Java + `pom.xml` + `application.yml` + `backend-api/README.md` + `scripts/phase45_smoke_api.sh` = **28 file**
- **LOC (Java)**: ~1100 dòng (DTO ~250 + DAO ~310 + Controller ~340 + Config ~180 + Exception ~50)
- **Linter status**: clean (ReadLints `backend-api/` → "No linter errors found.")
- **Maven module status**: declared trong parent `pom.xml`, sẽ build khi proxy giải quyết
- **Build/runtime test**: **PENDING** — proxy Bosch NTLM block. Dự kiến giải quyết:
   - Khi có hotspot 4G: 5 phút download deps → `mvn package` → `java -jar` → `phase45_smoke_api.sh` (kỳ vọng 14/14 ✓)
   - Hoặc trong Phase 5 (JavaFX dev cần API running) — sẽ ưu tiên fix proxy trước khi start JavaFX
- **Dependency footprint dự kiến**: ~80 MB cần tải (Spring Boot 2.7 + jjwt + springdoc + Tomcat embedded)
- **Memory dự kiến runtime**: ~150-256 MB heap. Tổng stack với 3 generator + Docker + API = ~3 GB / 8 GB budget

### Files created
```
backend-api/                                    (NEW MODULE)
├── pom.xml
├── README.md                                   (build/run/troubleshoot full guide cho team)
└── src/main/
    ├── java/vn/edu/ves/api/
    │   ├── VesApiApplication.java              (Spring Boot main, 14-endpoint header doc)
    │   ├── config/
    │   │   ├── SecurityConfig.java             (stateless JWT + CORS + public allowlist)
    │   │   ├── JwtTokenProvider.java           (HS256 sign/parse, parseSilently helper)
    │   │   ├── JwtAuthFilter.java              (OncePerRequestFilter, uid → auth.details)
    │   │   └── OpenApiConfig.java              (Swagger metadata + Bearer scheme)
    │   ├── controller/
    │   │   ├── AuthController.java             (POST /login, GET /me)
    │   │   ├── PillarController.java           (GET /api/pillars/{1..4}/...)
    │   │   ├── SecurityController.java         (score + cascade-risks)
    │   │   ├── RecommendationController.java   (list + acknowledge)
    │   │   ├── AlertController.java            (active alerts)
    │   │   ├── RawDataController.java          (fuel-prices + grid-load latest)
    │   │   └── HealthController.java           (public DB ping)
    │   ├── dao/
    │   │   ├── UserDao.java                    (UserRecord internal + touchLastLogin)
    │   │   ├── PillarDao.java                  (6 query: 4 pillar views + fuel/grid latest)
    │   │   ├── SecurityDao.java                (score + cascade-risks, JSONB cast)
    │   │   ├── RecommendationDao.java          (active + acknowledge UPDATE)
    │   │   └── AlertDao.java
    │   ├── dto/                                (14 DTO Lombok)
    │   │   ├── LoginRequest, LoginResponse, UserDto, AckRequest
    │   │   ├── Pillar1OutlookDto, Pillar2VolatilityDto, Pillar3SheddingDto, Pillar4NetZeroDto
    │   │   ├── SecurityScoreDto, CascadeRiskDto, RecommendationDto, AlertDto
    │   │   └── FuelPriceDto, GridLoadLatestDto
    │   └── exception/
    │       ├── ApiException.java               (4xx factory methods)
    │       └── GlobalExceptionHandler.java     (@RestControllerAdvice)
    └── resources/application.yml

scripts/
└── phase45_smoke_api.sh                        (14-endpoint curl smoke + summary)

pom.xml                                          (MOD - thêm module backend-api + 3 property version)
README.md                                        (MOD - badge + Phase 4.5 section + troubleshoot NTLM)
UPGRADE_PLAN.md                                  (MOD - §24.5 detail update)
docs/PROGRESS.md                                 (MOD - phase row + log section này)
```

### Operational notes
- **Khi proxy được giải quyết** (hotspot 4G / cntlm / mạng cá nhân):
  1. `mvn -pl backend-api -am clean package -DskipTests` (~2-5 phút lần đầu)
  2. `java -jar backend-api/target/ves-backend-api.jar` (hoặc `nohup ... > /tmp/ves-api.log 2>&1 &`)
  3. `bash scripts/phase45_smoke_api.sh` → kỳ vọng 14/14 ✓
  4. Sau khi xanh: tag `v0.45-backend-api`, cập nhật badge README sang green.
- **Auth seed users sẵn**: admin/admin (ADMIN), manager/manager (MANAGER), user/user (VIEWER). Token sống 8h.
- **Swagger UI**: `http://localhost:8090/swagger-ui.html` (có Authorize button dán Bearer token).
- **Phase 5 (JavaFX) sẽ dùng API**: `HttpURLConnection` hoặc OkHttp gọi 14 endpoint → render TabPane 4 pillar. Bắt buộc Phase 4.5 build trước.

### Decision update (12 May 2026, tối): Phase 5 đi đường **JDBC trực tiếp**, KHÔNG qua REST API

- Tách Phase 4.5 (REST API) khỏi đường găng Phase 5 — REST API chủ yếu phục vụ Android (đã tách ra repo riêng `mtoanng/DataStream`).
- §24.5 đã ghi rõ "3-layer, **JDBC trực tiếp**, JUnit ≥ 10 test" — JavaFX dùng JDBC qua `DriverManager`, đọc views có sẵn của Phase 2.6 (`v_security_score`, `v_pillar1_supply_outlook`, v.v.).
- Lợi ích: blocker proxy Bosch không cản đường Phase 5. Khi có hotspot/cntlm, build Phase 4.5 vẫn xong nhưng KHÔNG là đường găng môn Java.

---

## Phase 5.0 — JavaFX Module Foundation Log (12 May 2026, tối)

### Mục tiêu
Module `desktop-admin/` declared trong parent + scaffold đầy đủ để Phase 5.1 wire login. Foundation chạy được `mvn javafx:run` ra cửa sổ placeholder.

### Action log

1. ✅ Quyết định stack: JavaFX **17.0.10 LTS** (không phải 21) — cuối cùng support Java 11 đồng bộ với 5 module hiện hữu. javafx-maven-plugin 0.0.8. jbcrypt 0.4 cho password verify. h2 2.2.224 cho DAO test in-memory.
2. ✅ Test framework: **JUnit 4.13.2** (KHÔNG phải JUnit 5) để compat với surefire 2.12.4 đã cache trong WSL. Tránh phải pull surefire 3.x mới qua NTLM proxy.
3. ✅ Folder structure 11 directories (controller/service/dao/model/util/resources/fxml/css + test mirror).
4. ✅ `desktop-admin/pom.xml` (4.6 KB): inherit parent `vn.edu.ves:ves-monitor-parent:1.0.0-SNAPSHOT`, 4 main deps (javafx-controls/fxml + postgresql + jbcrypt + slf4j) + 4 test deps (junit + mockito + h2). javafx-maven-plugin config `mainClass=vn.edu.ves.desktop.MainApp` để chạy `mvn -pl desktop-admin javafx:run`.
5. ✅ `MainApp.java` (1.8 KB): Application class, load `/fxml/login.fxml` + `/css/material.css`, set stage 480×360 min, slf4j log. Dùng `Objects.requireNonNull` cho FXML resource.
6. ✅ `DatabaseConfig.java` (3.3 KB): **Singleton** eager init (design pattern checkpoint #1). `openConnection()` mở connection mới (try-with-resources caller-side). Resolve order: System property → env var (UPPER_SNAKE) → `application.properties` → default. Properties Postgres match `infra/docker-compose.yml`.
7. ✅ `application.properties` (0.6 KB): db.url/user/password defaults + query timeout + UI refresh interval.
8. ✅ `logback.xml` (0.5 KB): console appender, DEBUG cho `vn.edu.ves.desktop`, WARN cho javafx.
9. ✅ `fxml/login.fxml` (0.8 KB): VBox placeholder gradient blue với title "VES-Monitor" + subtitle "Admin Desktop" + hint "Phase 5.0 — Foundation OK". Sẽ replace ở Phase 5.1.
10. ✅ `css/material.css` (1.0 KB): Material-inspired theme — Segoe UI/Roboto font, root background `#fafafa`, login-pane gradient `#1f77b4 → #2196f3`, btn-primary `#1976d2`, error-text `#d32f2f`.
11. ✅ `desktop-admin/.gitignore` + `desktop-admin/README.md` (4.4 KB README chi tiết quick-run, override DB config, troubleshoot JavaFX module path).
12. ✅ Parent `pom.xml` add `<module>desktop-admin</module>` (uncomment dòng "sẽ thêm ở Phase 5").
13. ✅ ReadLints `desktop-admin/` → **No linter errors found**.
14. ✅ XML validation: 4/4 file XML (parent pom, module pom, login.fxml, logback.xml) parse OK qua PowerShell `[xml]`.

### Verification snapshot (Phase 5.0 code-complete state)
- **Files Phase 5.0 mới tạo**: 9 file (pom.xml + MainApp.java + DatabaseConfig.java + application.properties + logback.xml + login.fxml + material.css + .gitignore + README.md)
- **LOC (Java)**: ~150 dòng (MainApp 60 + DatabaseConfig 90)
- **Linter status**: clean
- **XML status**: 4/4 valid
- **Maven module status**: declared trong parent, sẽ build cùng `mvn package` khi proxy giải quyết
- **Build/runtime test**: **PENDING** — cùng blocker với Phase 4.5 (Bosch NTLM proxy block download JavaFX 17 deps lần đầu, ~20 MB)
- **Dependency footprint dự kiến**: ~30 MB (JavaFX 17 controls/fxml ~15 MB + jbcrypt ~30 KB + h2 ~2 MB + logback ~700 KB + slf4j ~50 KB + postgresql ~1 MB cached)

### Operational notes
- **Khi proxy giải quyết** (hotspot 4G / cntlm bridge / mạng cá nhân):
  1. `mvn -pl desktop-admin -am clean compile` (~30s pull JavaFX deps lần đầu)
  2. `mvn -pl desktop-admin javafx:run` → mở cửa sổ placeholder gradient blue
  3. Sau khi xanh: tiếp Phase 5.1 (Login full form)
- **JDBC trực tiếp** (không qua Phase 4.5 API): DAO sẽ gọi `DatabaseConfig.getInstance().openConnection()` rồi `PreparedStatement` thuần. Đọc 5 views có sẵn từ Phase 2.6 cho Dashboard.

### Next: Phase 5.1 — Login + AuthService + SessionManager + UserDao + PasswordUtil

---

## Phase 5.1 — Login + AuthService + SessionManager + UserDao Log (12 May 2026)

### Mục tiêu
Wire full login flow: form FXML → AuthService → UserDao + BCrypt → SessionManager → switch scene Dashboard placeholder. Verify hoàn toàn qua lint + JUnit (build runtime defer).

### Action log
1. ✅ `model/Role` enum (ADMIN/MANAGER/VIEWER) + `fromString` fallback VIEWER cho null/blank/invalid (defensive).
2. ✅ `model/User` POJO 10 field map đúng schema `users` (Phase 2): id, username, password_hash, full_name, email, role, enabled, created_at, updated_at, last_login_at. Equals + hashCode + toString manual (không Lombok).
3. ✅ `exception/AuthenticationException` extends RuntimeException + (msg) + (msg, cause).
4. ✅ `util/PasswordUtil` — wrapper jbcrypt:
   - `verify(plain, hash)`: null-safe, return false khi hash sai format (không throw)
   - `hash(plain)`: BCrypt.gensalt(10) đồng bộ seed Phase 2
5. ✅ `util/SessionManager` — Singleton eager init, `AtomicReference<User>` slot thread-safe. Methods: setCurrentUser, getCurrentUser, currentUser (Optional), clear, isLoggedIn, isAdmin, isManager, canWrite.
6. ✅ `util/AlertHelper` — static wrappers showError/showWarning/showInfo/showConfirm với JavaFX Alert + ButtonType.
7. ✅ `dao/BaseDao` — abstract base với logger + 6 helper static: setStringOrNull, setLongOrNull, setTimestampOrNull, getStringOrNull, getLongOrNull, getLocalDateTimeOrNull.
8. ✅ `dao/UserDao` — 7 method: findByUsername, findById, findAll, updateLastLogin, save (insert nếu id=0 else update, branch nếu newPasswordHash non-blank), delete, setEnabled. Full try-with-resources.
9. ✅ `service/AuthService` interface (login/logout/getCurrentUser) + `AuthServiceImpl` flow chuẩn 6 bước (validate → findByUsername → check enabled → BCrypt verify → SessionManager.set → updateLastLogin best-effort).
10. ✅ `controller/LoginController` — @FXML 4 field (txtUsername, txtPassword, btnLogin, lblError). `initialize()` focus username, Enter chuyển focus. `handleLogin` disable button khi auth, fallback dashboard_placeholder nếu dashboard.fxml chưa có. Scene switch 1280×800.
11. ✅ `controller/DashboardPlaceholderController` — hiển thị "Xin chào, X / Vai trò: Y" + nút logout quay về login.
12. ✅ `fxml/login.fxml` — REPLACE placeholder cũ. StackPane gradient → VBox card 380px max width với title/subtitle/2 input/btn/error label. fx:controller wire LoginController.
13. ✅ `fxml/dashboard_placeholder.fxml` — VBox center "VES-Monitor Dashboard (placeholder)" + welcome + role + logout button.
14. ✅ `css/material.css` — polish: gradient login-root, white login-card với drop-shadow, btn-primary blue 1976, btn-secondary outlined, error-text 12px bold red, dashboard-placeholder section.
15. ✅ 17 @Test:
    - `PasswordUtilTest` (5): verify correct/wrong/null/malformed hash + hash round-trip
    - `SessionManagerTest` (6): singleton identity, default empty, admin/manager/viewer role helpers, clear
    - `AuthServiceTest` (6, Mockito mock UserDao): login OK + session set, wrong pwd, user not found, disabled, empty input, logout

### Verification snapshot (Phase 5.1)
- **File mới**: 16 Java + 1 FXML mới + 1 FXML thay placeholder + 1 CSS update + 3 test file = 21 file changes
- **Test count**: 17 @Test mới (cumulative 17)
- **Lint**: clean
- **FXML XML parse**: 2/2 (login.fxml mới, dashboard_placeholder.fxml mới)
- **Commit**: `08a97bd`

### Next: Phase 5.2 — Dashboard 4 pillar TabPane + Security Score gauge

---

## Phase 5.2 — Dashboard 4 pillar TabPane + Security Score Log (12 May 2026)

### Mục tiêu
Full dashboard scene: top bar Security Score gauge (ProgressIndicator) + TabPane 4 pillar (mỗi tab có TableView + chart) + sidebar phải hiển thị recommendations active. Auto refresh 30s background thread.

### Action log
1. ✅ 6 model POJO: SecurityScore (overall + 4 sub-score + status + computed_at + ratio helper 0-1), Pillar1Outlook (14 col), Pillar2Volatility (9 col), Pillar3Shedding (11 col), Pillar4NetZero (9 col), Recommendation (11 col).
2. ✅ `dao/ViewsDao` — read-only DAO 6 method (fetchSecurityScore Optional, fetchPillar1Outlook/2/3/4 List, fetchActiveRecommendations List). SQL `suggested_data::text AS suggested_data_text` để cast JSONB → String.
3. ✅ `service/DashboardService` interface + `DashboardServiceImpl` pass-through.
4. ✅ `controller/DashboardController` — manage full Dashboard scene:
   - Top bar: ProgressIndicator gauge bind overallScore/100, lblOverallScore display 1-decimal, lblScoreStatus với styleClass theo SECURE/STABLE/AT_RISK/CRITICAL
   - TabPane 4 tab: Pillar 1 TableView (region/fuel/stockDays/target/status/recommendation) + BarChart, Pillar 2 TableView + LineChart, Pillar 3 TableView + BarChart bars colored theo load_pct (≥95 red, ≥85 orange, ≥70 yellow), Pillar 4 TableView + PieChart share %
   - Right sidebar: ListView<Recommendation> custom CellFactory styling theo severity (CRITICAL red bold, WARNING orange, INFO gray)
   - Auto refresh 30s: ScheduledExecutorService single-thread daemon + Task background → applySnapshot trong runLater
   - Nav buttons: Regions/AlertRules/Users (Users hidden if !admin), graceful fallback dialog nếu FXML chưa có (cho phase 5.3/5.4)
   - Logout: confirm dialog → shutdown scheduler → SessionManager.clear → load login.fxml
5. ✅ `fxml/dashboard.fxml` — BorderPane: top HBox (brand + score + nav + logout) + center SplitPane 0.78 (TabPane | recommendation sidebar). CategoryAxis + NumberAxis cho mọi chart.
6. ✅ `css/material.css` extend: .dashboard-root, .top-bar drop shadow, .brand/brand-sub typography, .score-value 22px bold, score-status states (status-secure/stable/at-risk/critical), pillar tints (P1 yellow #FFFBEA / P2 blue #E3F2FD / P3 orange #FFF3E0 / P4 green #E8F5E9), rec-sidebar.
7. ✅ 10 @Test mới:
    - `ViewsDaoTest` (6, H2 in-memory + MODE=PostgreSQL cho :: cast): fetchSecurityScore single row, P1 with recommendation_text, P2 with signal STABLE, P3 ordered by priority, P4 status ON_TRACK, recommendations non-expired
    - `DashboardServiceTest` (4, Mockito mock ViewsDao): delegation cho score + P1 + empty rec + all-6-methods called exactly once
8. ✅ `H2TestSupport` helper: reflection override singleton DatabaseConfig.url/user/password sang `jdbc:h2:mem:vesdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1`, exec/dropAll utilities.

### Verification snapshot (Phase 5.2)
- **File mới**: 11 Java + 1 FXML + 1 CSS update + 3 test file = 15 file changes
- **Test count**: 10 mới (cumulative 27)
- **Lint**: clean
- **FXML parse**: 3/3
- **Commit**: `de7c4dd`

### Next: Phase 5.3 — Region CRUD + Validator strategy pattern

---

## Phase 5.3 — Region CRUD + Validator Strategy Log (12 May 2026)

### Mục tiêu
Color screen quản lý 6 region seed (3 VN + 3 quốc tế). Form bên trái + table bên phải, validator chain compose. ADMIN/MANAGER được ghi, VIEWER read-only.

### Action log
1. ✅ `model/Region` POJO: id, code, name, vn_zone, country_code (default VN), description, created_at.
2. ✅ `dao/RegionDao` — 6 method: findAll, findById, findByCode (private findBy(whereClause, param) gộp 2), save (insert/update branch), delete. Full try-with-resources.
3. ✅ `service/RegionService` + `RegionServiceImpl` — auto trim + UPPERCASE code trước khi gọi DAO.
4. ✅ `util/Validator<T>` — Interface chính (`List<String> validate(T)`) + Strategy implementations:
   - `NotBlankValidator(fieldName)`
   - `LengthRangeValidator(fieldName, min, max)` — null skip
   - `PatternValidator(fieldName, regex, requirementMsg)` — null/blank skip
   - `InSetValidator(fieldName, allowed...)` — null/blank skip
   - `compose(Validator...)` static → Composite Validator chạy hết và collect all errors
5. ✅ `controller/RegionController` — 4 validator chain (code, name, country, zone). Form ↔ TableView selectedItemProperty listener. applyPermissions disable nếu !canWrite. Confirm dialog khi delete. Back button → dashboard.
6. ✅ `fxml/region.fxml` — BorderPane + top HBox (Refresh + Back) + center SplitPane 0.35 (form | table). ChoiceBox vn_zone "/BAC/TRUNG/NAM", TextArea description, btn-primary Save / btn-danger Delete.
7. ✅ CSS extend: .crud-root/.crud-form/.crud-table, .form-mode, .btn-danger red 1.
8. ✅ 14 @Test mới:
    - `ValidatorTest` (5): NotBlank reject null/blank, LengthRange bounds, Pattern regex match, InSet allowed values, compose collects all errors
    - `RegionDaoTest` (6, H2): insert auto-id + findable, findByCode existing, findByCode unknown empty, update changes fields, delete removes, findAll multiple
    - `RegionServiceTest` (4, Mockito): findAll delegation, save uppercase code BEFORE DAO, findByCode pass-through, delete delegation

### Verification snapshot (Phase 5.3)
- **File mới**: 7 Java + 1 FXML + 1 CSS update + 3 test file = 11 file changes
- **Test count**: 14 mới (cumulative 41)
- **Lint**: clean
- **FXML parse**: 4/4
- **Commit**: `7ced8e0`

### Next: Phase 5.4 — AlertRule CRUD + User CRUD (admin-only)

---

## Phase 5.4 — AlertRule CRUD + User CRUD (admin-only) Log (12 May 2026)

### Mục tiêu
2 screen CRUD song song: AlertRule (multi-pillar metric_type) cho ADMIN/MANAGER, và User management ADMIN-only enforced cả client + service side.

### Action log
1. ✅ `model/MetricType` enum (5 giá trị: FUEL_PRICE/INVENTORY_DAYS/GRID_LOAD_PCT/RENEWABLE_PCT/EMISSION_INTENSITY) + `fromString` fallback.
2. ✅ `model/Operator` enum (GT/GTE/LT/LTE/EQ) với `toSymbol()` và `fromSymbol(String)` bridge DB chars ('>', '<=' etc.). toString = symbol để ChoiceBox display đẹp.
3. ✅ `model/Severity` enum (INFO/WARNING/CRITICAL).
4. ✅ `model/AlertRule` POJO 13 field + getDisplayCondition helper "METRIC op threshold" cho TableView.
5. ✅ `dao/AlertRuleDao` — findAll, findByMetricType, findById, save (insert/update), delete, setEnabled.
6. ✅ `service/AlertRuleService` + `AlertRuleServiceImpl` — trim ruleName, UPPERCASE fuelType + regionCode trước khi save.
7. ✅ `service/UserService` + `UserServiceImpl` — save với option newPassword (hash via PasswordUtil chỉ khi non-blank), delete chỉ ADMIN + block self-delete (prevent system lockout), setEnabled toggle.
8. ✅ `controller/AlertRuleController`:
   - 3 ChoiceBox (MetricType/Operator/Severity) populate từ enum values()
   - Validator chain: ruleName NotBlank+Length, threshold pattern `^-?\\d+(\\.\\d+)?$`
   - Toggle button gọi setEnabled (fast enable/disable không cần full edit)
   - VIEWER form disabled
9. ✅ `controller/UserController`:
   - Initialize check !isAdmin() → Platform.runLater(denyAccessAndBack) → AlertHelper warning + auto back dashboard
   - Validator: username Pattern `^[a-zA-Z0-9_.-]+$`, optional email Pattern, password LengthRange 4-100 (required khi insert)
   - TableColumn last_login custom (string với "(chưa)" fallback)
   - Self-delete guard ở controller (UX) + service (defense in depth)
10. ✅ `fxml/alertRule.fxml` + `fxml/user.fxml` — pattern giống region.fxml: BorderPane + SplitPane form|table.
11. ✅ 13 @Test mới:
    - `AlertRuleDaoTest` (5, H2): insert with BigDecimal threshold + Operator/Severity roundtrip, update changes threshold + severity, delete, findByMetricType filter chính xác, setEnabled toggle
    - `AlertRuleServiceTest` (3, Mockito): save trim+uppercase, setEnabled passthrough, findByMetricType delegation
    - `UserServiceTest` (5, Mockito + real PasswordUtil verify): save sinh hash khi pwd non-blank, save không hash khi pwd blank/null, delete non-admin reject, delete admin OK, delete admin tự xóa chính mình bị reject

### Verification snapshot (Phase 5.4)
- **File mới**: 11 Java + 2 FXML + 3 test file = 16 file changes
- **Test count**: 13 mới (cumulative 54)
- **Lint**: clean
- **FXML parse**: 6/6 (login + dashboard + dashboard_placeholder + region + alertRule + user)
- **Commit**: `8caec9e`

### Next: Phase 5.5 — Test consolidation + final docs

---

## Phase 5.5 — Test Consolidation + Final Docs Log (12 May 2026)

### Mục tiêu
Đóng vòng Phase 5: bổ sung UserDaoTest H2 cho coverage cân bằng 4/4 DAO (User/Region/AlertRule/Views), cập nhật doc README desktop-admin + root + PROGRESS + UPGRADE_PLAN sang trạng thái "Phase 5 code-complete, build pending proxy".

### Action log
1. ✅ Đếm test: 55 @Test sau Phase 5.4 → bổ sung `UserDaoTest` 7 case (insert + findable, update without pwd giữ hash cũ, update with pwd đổi hash mới, updateLastLogin sets timestamp, setEnabled toggle, delete, findAll multiple). Tổng: **62 @Test** (≥10 yêu cầu, 6.2× target).
2. ✅ Coverage matrix sau 5.5:
   - DAO (4/4): UserDao 7 + RegionDao 6 + AlertRuleDao 5 + ViewsDao 6 = 24 H2 integration test
   - Service (5/5): Auth 6 + Region 4 + AlertRule 3 + User 5 + Dashboard 4 = 22 Mockito unit test
   - Util (3/3): PasswordUtil 5 + SessionManager 6 + Validator 5 = 16 pure unit test
3. ✅ Update `desktop-admin/README.md`: status badge → "Phase 5 code-complete", roadmap table 5.1-5.5 mark ✅ với commit hash, design pattern table (Singleton/DAO/MVC/Strategy/Composite/Factory), test directory tree với count từng file.
4. ✅ Update `docs/PROGRESS.md`: phase tracking row 5.1-5.5 set 🟡 với detail, append section log chi tiết cho từng phase.
5. ✅ Update root `README.md`: Phase 5 section (5 screen, design patterns, test count, build pending notice).
6. ✅ Update `UPGRADE_PLAN.md §24.5`: status Phase 5.1-5.5 sang 🟡 (code-complete, build pending) hoặc ✅ runtime verified nếu sau này build OK.

### Verification snapshot (Phase 5.5 — Phase 5 cumulative)
- **Module total**: ~50 Java source + 6 FXML + 1 CSS + 2 properties/xml resource + 12 test file = ~71 file
- **Test count**: 62 @Test methods (FAR ≥10 target)
- **LOC (estimate)**: ~4500 dòng Java + ~600 dòng FXML/CSS
- **Lint status**: clean toàn module
- **FXML parse**: 6/6 valid XML
- **Build/runtime**: PENDING (Bosch NTLM proxy block JavaFX 17 deps pull lần đầu — defer cùng Phase 4.5)
- **Commits Phase 5**: 5.0 `62c824c` → 5.1 `08a97bd` → 5.2 `de7c4dd` → 5.3 `7ced8e0` → 5.4 `8caec9e` → 5.5 (commit hiện tại)

### Operational notes
- Khi proxy giải quyết: `mvn -pl desktop-admin -am clean test` để chạy 62 test tự động, expect 62/62 PASS (H2 + Mockito + JUnit 4 không cần network sau khi cache).
- Sau đó `mvn -pl desktop-admin javafx:run` mở app, login admin/admin → Dashboard, click qua 4 tab pillar + 3 CRUD screen.
- Tag dự kiến khi runtime verified: `v0.5-javafx-desktop`.

### Phase 5 COMPLETE (code-level). Ready for demo offline qua source review + JavaDoc + test report dry-run.

---

## Phase 5 — Build runtime verification (13 May 2026)

### Outcome
- **Maven 3.9.6** portable installed user-scope at `C:\Users\GOT4HC\tools\apache-maven-3.9.6\`.
- **APAC proxy** `rb-proxy-apac.bosch.com:8080` in `~/.m2/settings.xml` (active) — works from home WiFi off-VPN. NTLM proxy block ✅ solved.
- 114+ Maven deps cached, build runs in <60s once warm.

### Build matrix
| Module | Phase | `mvn clean test` | Tests | Time | JAR |
|--------|-------|------------------|-------|------|-----|
| `desktop-admin` | 5.1-5.5 | ✅ BUILD SUCCESS | **62/62 PASS** | 17.6s | `ves-desktop-admin.jar` (112 KB, thin JAR — JavaFX deps stay in ~/.m2/) |
| `backend-api`  | 4.5     | ✅ BUILD SUCCESS | 0 (no @Test yet) | 37.0s | `ves-backend-api.jar` (30.3 MB, Spring Boot 2.7.18 fat JAR) |

### Test fixes applied (commit `1c41353`)
4 small fixes to make 62/62 desktop-admin tests pass:

1. **H2 IDENTITY syntax** — UserDaoTest, RegionDaoTest, AlertRuleDaoTest had `BIGINT AUTO_INCREMENT` in fixture SQL; H2 2.x in `MODE=PostgreSQL` rejects that MySQL-only syntax. Replaced with standard `BIGINT GENERATED BY DEFAULT AS IDENTITY`.
2. **ViewsDaoTest.dropAll** — wrapped `DROP VIEW IF EXISTS` in try/catch; H2 throws even with IF EXISTS if the object exists as a TABLE (which is how the fixture simulates views).
3. **PasswordUtil.verify** — jBCrypt 0.4 only accepts `2a$`/`2y$` prefixes; seed users from `04_seed_basic.sql` were hashed by Python bcrypt → `2b$`. Added prefix normalize (`2b$` → `2a$`); same algorithm for password ≤72 chars.
4. **Validator.LengthRangeValidator** — added blank-skip for null-safety parity with PatternValidator/InSetValidator (composite chain on blank input should report only NotBlank, not double-fire Length).

### Smoke test
- `backend-api` smoke skipped — no `spring-boot-starter-actuator` in pom (no `/actuator/health`) + strict Postgres requirement (no test profile). JAR manifest verified: `Spring-Boot-Version: 2.7.18`, `Start-Class: vn.edu.ves.api.VesApiApplication`, Spring Boot layered fat JAR format — confirmed bootable.
- `desktop-admin` `javafx:run` smoke skipped (blocks on UI, non-interactive subagent).

### Phase status changes
- Phase **4.5** (Backend REST API): 🟡 "code-complete, build PENDING" → ✅ **build VERIFIED** (compile + repackage). Pending end-to-end test with running Postgres + JWT login flow.
- Phase **5.1-5.5** (JavaFX desktop): 🟡 "code-complete, build PENDING" → ✅ **build + tests VERIFIED** (62/62 PASS). Pending JavaFX runtime smoke (needs display) + end-to-end with running Postgres.

### Next
- Bring up Postgres via Docker (Phase 1 `infra/` compose) → re-run backend smoke + JWT login from JavaFX.
- Tag `v0.5-javafx-desktop` once runtime smoke is green.
- Run `mvn javafx:run` locally (interactive) → final UI screenshot for demo.

---

## E2E runtime verified (13 May 2026)

### HEAD
`16eb665` (`Phase 4.5 + 5 build verified: 62/62 desktop tests pass, JARs produced`)

### Full stack live + smoke result
First end-to-end runtime test of the complete pipeline. **All acceptance criteria passed.** Detailed runbook in [`docs/DEMO_RUN_LOG.md`](DEMO_RUN_LOG.md).

| Layer | Verification |
|---|---|
| Docker Lite stack | 5 containers healthy in 15 s via `bash scripts/run.sh --wait`. Total RAM ≈ 1.59 GB / 2.85 GB budget. |
| Kafka | 4 topics auto-pre-created (`fuel-prices` / `grid-load` / `renewable-output` / `emission`), 3 partitions each. Δ messages over 90 s: +660 / +132 / +198 / +24. |
| Flink | Job `db7d0dd6…` (`VES-Monitor Real-time Pipeline (4 pillars / 7 streams)`) RUNNING, **18/18 tasks running**, 0 failed. |
| Postgres | Raw row counts growing across snapshots (fuel +660, alerts +8). `alerts` total = 34, `recommendations` = 8. |
| `v_security_score` | `overall=70.50 STABLE` (P1 68.52 / P2 92.46 / P3 21.02 / P4 100.00). Pillar 2 score drifted live as σ-rolling caught new ticks → views are live, not cached. |
| Backend REST (port 8090) | `POST /api/auth/login` → JWT 8h (ADMIN). 6 protected endpoints + `/api/health` all return 200 with non-empty bodies. Payloads saved to `build-logs/api_smoke.json`. |

### Row count snapshot (T+~3 min)

| Table | Rows | | View | Rows |
|---|---:|---|---|---:|
| `fuel_prices_raw` | 7 280 | | `v_active_alerts` | 34 |
| `fuel_price_window_agg` | 447 | | `v_active_recommendations` | 8 |
| `fuel_price_alerts` | 1 132 | | | |
| `grid_load_raw` | 1 345 | | | |
| `renewable_output_raw` | 1 872 | | | |
| `emission_raw` | 218 | | | |
| `alerts` | 34 | | | |
| `recommendations` | 8 | | | |

### Operational notes
- The 4 SQL fixes from commit `1c41353` (Phase 5 build verification) carry through cleanly — backend `/api/auth/login` with the seed `admin`/`admin` BCrypt `$2b$` hash works on first try (Spring `BCryptPasswordEncoder` accepts both `$2a$`/`$2b$`).
- One CRLF gotcha applied: `sed -i 's/\r$//' scripts/*.sh` once before any WSL invocation — same fix the repo already documents in `docs/PROXY_SETUP.md`.
- Existing `pgdata` volume preserved → tables include rows from prior demos. Growth between snapshots (not absolute counts) is what proves the live run is sinking data.
- Backend Java process recorded its PID in `build-logs/backend-run.pid` for clean shutdown.

### Next
- Tag `v0.6-e2e-verified` after this commit lands.
- Re-run snapshot with a fresh volume (`bash scripts/stop.sh --volumes && bash scripts/run.sh --wait`) before recording final demo video — that gives "clean canvas" counts for slides.
- User to launch JavaFX desktop (`mvn -pl desktop-admin javafx:run`) against this same running stack to capture the dashboard UI screenshot.

---

## Phase 7.0 — DB config sync + visible nav errors (13 May 2026)

Three local-modified files left over from the previous foreground session were promoted to a single self-contained commit before any Phase 7 work began.

| File | Change |
|---|---|
| `desktop-admin/src/main/resources/application.properties` | `db.url` → `jdbc:postgresql://localhost:5432/fuel_prices` (was `fuel_db`), `db.password` → `123456` (was `postgres`) so JDBC defaults match `infra/docker-compose.yml`. |
| `desktop-admin/.../util/DatabaseConfig.java` | Default fall-back constants updated to match the new docker user/database/password. |
| `desktop-admin/.../controller/DashboardController.java` | `switchScene` now catches `Exception` (was `IOException` only) so FXML load failures surface a user-visible alert instead of a console-only stack trace. |

- `mvn -pl desktop-admin -am clean test` → 62/62 PASS.
- Commit `ad1f0db`, pushed to `origin/main`.

---

## Phase 7.1 — Pillar redesign theo IEA / APERC framework (13 May 2026)

### Motivation
Phase 5 pillars were labelled with informal Vietnamese descriptors ("Nguồn cung", "Biến động giá", …). For the academic / demo audience this Phase swaps them out for the internationally recognised **IEA / APERC Energy Security** taxonomy (Availability / Affordability / Accessibility / Acceptability).

### New taxonomy

| # | Old | New (EN · VI) | Framework reference |
|---|---|---|---|
| 1 | Nguồn cung | **Supply Security · An ninh nguồn cung** | IEA + APERC indicators 1, 7 |
| 2 | Giá nhiên liệu | **Market Resilience · Khả năng chịu giá** | IEA + IMF Energy Price Volatility |
| 3 | Lưới điện | **Grid Reliability · Độ tin cậy lưới điện** | NERC + IEEE 1366 (SAIDI/SAIFI proxies) |
| 4 | Phát thải | **Energy Transition · Chuyển dịch năng lượng** | IPCC AR6 + Net-Zero 2050 pathway |

### 16 sub-indicators (4 per pillar)

| Pillar | Sub-indicators |
|---|---|
| P1 Supply Security | `idr` (Import Dependency Ratio, 0–1), `sfri` (Strategic Fuel Reserve Index = days of cover, IEA target ≥ 90), `hhi_supply` (Herfindahl-Hirschman Index of fuel-mix concentration 0–10000), `n1_resilience` (N-1 days of cover if largest source disrupted) |
| P2 Market Resilience | `sigma_30d` (rolling std-dev), `price_gap_pct` (VN vs Brent benchmark), `beta_crude` (OLS slope vs Brent returns), `affordability_idx` (price / income proxy 0–100) |
| P3 Grid Reliability | `reserve_margin_pct`, `peak_load_factor` (peak/avg), `shedding_prob` (P(load > 95%)), `freq_stability_idx` (100 − σ_load × 10) |
| P4 Energy Transition | `renewable_pct`, `co2_intensity` (kg/MWh), `curtailment_rate`, `netzero_progress` (current vs 2050 linear path to 70%) |

### Composite Energy Security Index (ESI)
`ESI = 0.30 · P1 + 0.20 · P2 + 0.30 · P3 + 0.20 · P4` — IEA-style weighting, availability + accessibility weighted higher because supply disruption and grid blackouts are higher-frequency / higher-impact than slow market drift or decarbonisation pace.

### Status thresholds
| Score | Status | Hex |
|---|---|---|
| ≥ 80 | SECURE | `#2E7D32` |
| 60–79 | ELEVATED | `#F9A825` |
| 40–59 | STRESSED | `#EF6C00` |
| < 40 | CRITICAL | `#C62828` |

### Action log
1. ✅ New SQL file `infra/script/08_pillars_v2.sql`:
   - Drops the 5 Phase 2.6 views with `CASCADE`.
   - Creates `v_pillar1_supply_security`, `v_pillar2_market_resilience`, `v_pillar3_grid_reliability`, `v_pillar4_energy_transition`, and a refreshed `v_security_score` with the IEA weight formula.
   - All views compute on-read from existing raw / agg tables — **no Flink job redeploy needed** so the live E2E pipeline keeps flowing.
2. ✅ Applied to live `postgres-database` container via `docker cp` + `psql -f`. Verified: 6 P1 rows, 5 P2, 3 P3, 3 P4, ESI single-row = `pillar1=64.83 / pillar2=64.06 / pillar3=90.93 / pillar4=72.15 / overall=73.97 ELEVATED`.
3. ✅ Renamed Java models: `Pillar1Outlook` → `Pillar1SupplySecurity`, `Pillar2Volatility` → `Pillar2MarketResilience`, `Pillar3Shedding` → `Pillar3GridReliability`, `Pillar4NetZero` → `Pillar4EnergyTransition`. Old files deleted.
4. ✅ Rewired `dao/ViewsDao` to new view column shape; `service/DashboardService` methods renamed (`getSupplySecurity`, `getMarketResilience`, `getGridReliability`, `getEnergyTransition`).
5. ✅ `controller/DashboardController` updated: new FXML fx:id wiring, new bar / line / pie chart values (now driven by SFRI / σ30d / reserve margin / renewable %), new status CSS class mapping (`status-elevated`, `status-stressed`).
6. ✅ `fxml/dashboard.fxml` — 4 Tab labels switched to bilingual EN · VI, 8 new TableColumns per pillar; `css/material.css` IEA palette added.
7. ✅ Test fixtures in `ViewsDaoTest` + `DashboardServiceTest` rewritten to mirror new view columns. 62 / 62 tests green.

### Verification snapshot (Phase 7.1)
- **Files changed**: 17 (4 new model + 4 deleted model + DAO + Service interface + impl + Controller + FXML + CSS + 2 tests + 08_pillars_v2.sql).
- **Tests**: 62 / 62 PASS.
- **Lint**: clean.
- **Live DB**: `v_security_score` = 73.97 ELEVATED at apply time.
- **Commit**: `6c48d1e`.

---

## Phase 7.2 — Real-time visibility (live ticker + sparklines + pulse + toast) (13 May 2026)

### Goal
Make the streaming nature of the pipeline tangible in the UI. Before this phase the dashboard refreshed every 30 s with no animation, so the audience had no signal that data was flowing live.

### Features delivered
1. **Live event ticker** in the top bar — `⚡ N events/sec · Last tick HH:MM:SS`. Source: Flink JobManager REST `/jobs/<id>/metrics?get=numRecordsInPerSecond`, summed across all RUNNING jobs and EMA-smoothed. Falls back to a silent "offline" state if the cluster is unreachable.
2. **Per-pillar sparklines** — 60 × 20 px `Canvas` widget inline in each pillar tab header. Stores a FIFO 60-sample window (3 minutes at 3 s cadence) of the pillar score; auto-fills + auto-strokes.
3. **Pulse effect** on freshly arrived CRITICAL alerts — `Timeline`-driven red drop-shadow on the recommendation `ListCell`, ~2 s, restores the original effect on completion.
4. **Toast popups** for new CRITICAL alerts — anchored top-right of the owner window, fade in / hold 5 s / fade out 250 ms, severity-tinted left border.
5. **Fade-in transition** on new ListView rows (700 ms 0 → 1 alpha).
6. **Refresh cadence split**: live ticker every **3 s**, table reload every **10 s** (was 30 s for everything). Both configurable via `ui.refresh.live.s` and `ui.refresh.table.s` in `application.properties`.

### New files
- `util/FlinkClient.java` — HttpURLConnection wrapper, regex-parsed JSON, short timeouts, returns 0 on any error.
- `service/LiveMetricsService.java` + `LiveMetricsServiceImpl.java` — daemon `ScheduledExecutorService`, EMA smoothing, callback to UI thread.
- `widget/Sparkline.java`, `widget/PulseEffect.java`, `widget/Toast.java`.

### Test additions
- `util/FlinkClientTest` (4 tests) — embedded `com.sun.net.httpserver.HttpServer` mocks 3 endpoints (`/jobs/overview`, `/jobs/<id>/metrics`) covering parse, sum-across-jobs, and unreachable-host paths.
- `widget/SparklineTest` (3 tests) — capacity honoured, clear resets, NaN / negative coercion to 0.
- `widget/PulseEffectTest` (2 tests) — drop-shadow attaches + activeCount tracking; `stopAll` clears state.

### Verification snapshot (Phase 7.2)
- **Files changed**: 13 (7 new src + 3 new test + DashboardController wiring + FXML + CSS + application.properties).
- **Tests**: 71 / 71 PASS (9 new on top of 62).
- **Lint**: clean.
- **Commit**: `08fb6c1`.

---

## Phase 7.3 — Interactive maps (VN SVG zones + Leaflet world hubs) (13 May 2026)

### Goal
Add geospatial context to the dashboard: a clickable 3-zone Vietnam map (Pillar 3 colour coded) plus a global map of fuel pricing hubs with live markers driven by Pillar 2 data.

### Sub-component breakdown

| | Tech | Purpose |
|---|---|---|
| `widget/VietnamMap` | Pure JavaFX, 3 `SVGPath` polygons in a `Group` | Click-to-filter pillar tables by VN_NORTH / VN_CENTRAL / VN_SOUTH. Legend with IEA palette. |
| `widget/WorldMapView` | JavaFX `WebView` + Leaflet 1.9.4 + OpenStreetMap | 7 marker hubs (Houston, NY, London, Dubai, Singapore, Hải Phòng, Vũng Tàu). Offline overlay if tile CDN unreachable. |
| `controller/MapsController` + `fxml/maps.fxml` | Nested FXML loaded into 5th tab "Maps · Bản đồ" | 10 s refresh, pushes scores → VN map, JSON → Leaflet via `WebEngine.executeScript("updateMarkers(...)")`. |

### Dependencies
`pom.xml` now requires `org.openjfx:javafx-web:17.0.10` (pulled successfully from Maven Central via the cached APAC proxy — 32 MB jar).

### Action log
1. ✅ `pom.xml` — added `javafx-web` dependency, verified resolves on first `mvn compile`.
2. ✅ `widget/VietnamMap` — 3 SVGPath polygons approximating VN silhouette, hover stroke widen, click handler emits region code. Static `colorForScore` / `statusForScore` helpers exposed for unit testing.
3. ✅ `widget/WorldMapView` — wraps `WebView`, queues `updateMarkers` calls until the JS page reports `Worker.State.SUCCEEDED`.
4. ✅ `resources/web/worldmap.html` + `worldmap.css` — Leaflet from `unpkg` CDN with SRI hashes, OpenStreetMap raster tiles. `tileerror` listener flips on an offline-card overlay listing the configured hubs even when the network is dead. JS exposes `window.updateMarkers(json)` for the Java side.
5. ✅ `controller/MapsController` — own scheduler (10 s cadence), `setOnZoneSelected` callback wired by parent dashboard, `buildMarkersJson` translates the Pillar 2 list into the marker payload.
6. ✅ `fxml/maps.fxml` — nested `TabPane` with "Vietnam · 3 zones" and "World · Fuel hubs".
7. ✅ `fxml/dashboard.fxml` — new 5th `Tab` ("Maps · Bản đồ") containing a `StackPane` placeholder; `DashboardController.configureMapsTab()` loads the nested FXML at startup.
8. ✅ VN-zone click drill-down — `DashboardController.onMapZoneSelected(regionCode)` filters `p1Data`, `p3Data`, `p4Data` to the selected region.
9. ✅ `widget/VietnamMapTest` (6 tests) — colour ladder thresholds at 80 / 60 / 40 (incl. null neutral grey) + status labels.

### Verification snapshot (Phase 7.3)
- **Files changed**: 11 (4 new src + 1 new test + 2 new resource (html/css) + 1 new FXML + DashboardController wiring + dashboard.fxml + css + pom).
- **Tests**: 77 / 77 PASS (6 new on top of 71).
- **`mvn package`**: BUILD SUCCESS, `ves-desktop-admin.jar` produced.
- **Lint**: clean.
- **Commit**: `837ddba`.

### Operational notes
- The world map requires internet access to load Leaflet from unpkg + OSM tile servers. If the host is behind the Bosch proxy without internet, the overlay shows a clear "Map tiles unavailable offline" message and falls back to a textual hub list — no crash.
- VN map is purely local (no network).
- Existing E2E pipeline (Docker stack, generators, backend) untouched.

---

## Phase 7.4 — Docs + final polish (13 May 2026)

- This file (`docs/PROGRESS.md`) updated with Phase 7.0 → 7.3 detailed sections + phase-tracking table rows.
- `UPGRADE_PLAN.md` Phase 7 status set to ✅ with sub-phase checklist.
- Root `README.md` gains a "Phase 7 features" callout.
- Final commit `docs: Phase 7 progress log + UPGRADE_PLAN status`, push to `origin/main`.

---

## Phase 7.5 — Final QA pass (13 May 2026, 03:00 ICT)

End-to-end re-verification on `origin/main @ c7ef8bd` while the Phase 6 stack was still up (5 docker containers, 3 generators in WSL, backend JVM PID 18220 on port 8090).

### Scorecard

| Check | Result |
|---|---|
| `git log origin/main..HEAD` / `HEAD..origin/main` | both empty — branch in sync |
| `mvn -pl desktop-admin -am clean test` | **77 / 77 PASS** in 23.8 s (Maven 3.9.6, JDK 21) |
| `mvn -pl backend-api test` | BUILD SUCCESS, 0 tests authored (no @Test in module — documented gap) |
| `mvn -pl desktop-admin -am clean package -DskipTests` | BUILD SUCCESS, `ves-desktop-admin.jar` = **144 KB** thin JAR |
| Backend JAR (in-use, not re-packaged) | `ves-backend-api.jar` = **30.30 MB** Spring Boot fat JAR |
| `ReadLints` over all 4 source modules | **clean** (no errors) |
| `[xml]` parse over 7 FXML files | **7 / 7 OK** |
| `web/worldmap.{html,css}` (Phase 7.3) | both present |
| Docker containers | **5 running** (zookeeper / kafka / postgres-database / flink-jobmanager healthy; flink-taskmanager `unhealthy` per docker healthcheck but Flink job 18 / 18 vertices `RUNNING` → false-positive healthcheck) |
| Flink REST `GET /jobs` | 1 job `db7d0dd6…` `RUNNING` |
| Kafka `--list` | `fuel-prices`, `grid-load`, `renewable-output`, `emission` (+ internal `__consumer_offsets`) |
| Postgres `\dt` / `\dv` | **13 tables / 17 views** (Phase 7.1 dropped 5 legacy views + added 5 IEA/APERC views = net 0; the README's older "19 views" claim was corrected to 17) |
| Live row-count snapshot | `fuel_prices_raw` = 26 930 (vs Phase 6 +19 650), `grid_load_raw` = 5 275, `renewable_output_raw` = 7 767, `emission_raw` = 872, `alerts` = 234, `recommendations` = 8, `users` = 3, `regions` = 6, `alert_rules` = 10 |
| `v_security_score` | `overall=72.82 ELEVATED` (P1 64.83 / P2 59.59 / P3 90.11 / P4 72.09) — IEA weighting active, status ladder = `SECURE/ELEVATED/STRESSED/CRITICAL` |
| Phase 7.1 view counts | P1=6, P2=5, P3=3, P4=3, `v_active_alerts`=234, `v_active_recommendations`=8 — all > 0 |

### Backend REST smoke (7 + 5 paths re-tested)

| # | Endpoint | Status |
|---|---|---|
| 1 | `POST /api/auth/login` (admin/admin) | **200**, JWT 8 h, role `ADMIN` |
| 2 | `GET /api/security/score` | **200** — overall 72.15 ELEVATED |
| 3 | `GET /api/alerts/active?limit=5` | **200** — 5 rows, top = EMISSION_INTENSITY 689 kg/MWh `VN_SOUTH` |
| 4 | `GET /api/pillars/1/outlook` | ❌ **500** — Phase 7.1 dropped `v_pillar1_supply_outlook` |
| 5 | `GET /api/recommendations?limit=5` | **200** — 5 PENDING |
| 6 | `GET /api/fuel-prices/latest?limit=3` | **200** — 3 rows |
| 7 | `GET /api/health` | **200** — `db=UP`, `status=UP` |
| + | `GET /api/auth/me` | **200** |
| + | `GET /api/grid-load/latest` | **200** |
| + | `GET /api/pillars/2/volatility` | ❌ **500** — view `v_pillar2_volatility_signal` dropped |
| + | `GET /api/pillars/3/shedding-plan` | ❌ **500** — view `v_pillar3_load_shedding_plan` dropped |
| + | `GET /api/pillars/4/net-zero` | ❌ **500** — view `v_pillar4_net_zero_progress` dropped |
| + | `GET /api/security/cascade-risks` | ❌ **500** — view `v_cascade_risks` dropped |

**8 / 13 endpoints OK, 5 broken** — all 5 failures stem from the same Phase 7.1 schema migration where SQL views were renamed/restructured to the IEA/APERC shape but `backend-api/.../dao/PillarDao.java` + `SecurityDao.java` + 4 corresponding DTOs were not migrated. Confirmed by reading `PillarDao.java` lines 46-127 — every query still references the dropped view names. Root cause is multi-file (5 backend Java files + matching DTO renames) and falls outside the QA pass's "no production code changes" guardrail, so it is reported here, not patched.

### Documentation fixes applied during this pass

| File | Change |
|---|---|
| `README.md` | "9 vertex" (Phase 3 era) → **18 vertex** (post-Phase 4); "13 bảng + 19 views" → **13 bảng + 17 views** with breakdown updated for Phase 7.1 v2 pillar views. |
| `desktop-admin/README.md` | Override-DB example URL `…/fuel_db` → `…/fuel_prices` (only example was misaligned with the real default). |
| `UPGRADE_PLAN.md` | Phase 0 smoke-test snippet referenced `fuel_db` + non-existent `fuel_prices` table; updated to `fuel_prices` DB + `fuel_prices_raw` table. |
| `docs/DEMO_RUN_LOG.md` | Added Phase 7.1 schema-update note next to the old `v_security_score` snapshot (status ladder + ESI weighting), refreshed the JavaFX runbook for the new 5-tab layout (Maps · Bản đồ) and the 3 s / 10 s refresh cadence, and inlined a Phase 7.1 backend regression callout next to the `/api/pillars/*` endpoint smoke table. |

### Hunt for the worker-mistake "admin / 123456" login string

Per the QA brief, both `README.md` and `docs/PROGRESS.md` (Phase 7.4 sections) were grep-searched (`admin.{1,8}123456`, `Login.{0,80}admin`, `đăng nhập.{0,30}admin`) — **the misquote is not present in any tracked doc**. Every login string found is correct: `admin/admin`, `manager/manager`, `user/user`, etc. The only `123456` references in the repo are the legitimate Postgres password documented across README / `Cloud.md` / `backend-api/README.md` / `docker-compose.yml` and one `TEAM_TASKS.md` test-case literal for weak-password validation. **No fix required for that item.**

### Verdict

* JavaFX desktop app: **demo ready** (build clean, tests green, JDBC reads new Phase 7.1 views directly, dashboard / maps / pillars / CRUD all functional).
* Backend REST API: **partial demo ready** — health / auth / security score / alerts / recommendations / fuel-prices / grid-load all 200; 4 pillar endpoints + cascade risks 500 until Phase 8 backend migration.
* Recommended demo path: showcase JavaFX as the primary surface; treat the backend as a smoke-tested sidecar (use the still-green endpoints in the API tour and skip pillar GETs).

