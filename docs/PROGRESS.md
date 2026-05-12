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
| 5.1 | Login + BCrypt + SessionManager | ⬜ | — | — | — | Full login form, AuthService, UserDao |
| 5.2 | Dashboard 4 pillar TabPane | ⬜ | — | — | — | Đọc 5 views có sẵn |
| 5.3 | Region CRUD | ⬜ | — | — | — | |
| 5.4 | AlertRule + User CRUD | ⬜ | — | — | — | |
| 5.5 | ≥10 JUnit test + final commit | ⬜ | — | — | — | |
| 6 | Android App | 🔀 split | — | 2026-05-12 | — | Tách thành đồ án độc lập, repo riêng [`mtoanng/DataStream`](https://github.com/mtoanng/DataStream). Không còn nằm trong scope đồ án Java. |
| 7 | Deploy + Cloudflared | ⬜ | — | — | — | |
| 8 | Doc + Demo Prep | ⬜ | — | — | — | |

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
