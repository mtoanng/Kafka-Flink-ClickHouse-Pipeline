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
| 3 | Flink Alert Detection + Recommendation Gen | ⬜ | — | — | — | |
| 4 | 2 Generator (Pillar 3 + 4) | ⬜ | — | — | — | grid-load + renewable-output + emission |
| 4.5 | Spring Boot REST API (~12 endpoint) | ⬜ | — | — | — | 8 core + 4 pillar endpoint |
| 5 | JavaFX Admin Desktop ⭐ | ⬜ | — | — | — | TabPane 4-pillar dashboard |
| 6 | Android App | ⬜ | — | — | — | Bottom-nav 4 pillars |
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

### Next: Phase 3 — Flink Alert Detection (multi-pillar) + Recommendation Auto-Gen
