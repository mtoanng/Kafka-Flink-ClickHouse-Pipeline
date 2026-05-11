# ⚡ VES-Monitor — Vietnam Energy Security Real-time Monitor

> Nền tảng **giám sát an ninh năng lượng Việt Nam** thời gian thực qua **Kafka + Flink + PostgreSQL**, bao quát **4 pillars** (Nguồn cung, Biến động Kinh tế, Phụ tải, Chuyển đổi & Môi trường) — kèm **JavaFX Admin Desktop** (môn Java) và **Android App** (môn Mobile). Tận dụng 90% code stream-processing có sẵn cho Pillar 2 (giá nhiên liệu thế giới), bổ sung light coverage cho 3 pillars còn lại.

[![Status](https://img.shields.io/badge/status-WIP%20Phase%202.5-yellow)]() [![Java](https://img.shields.io/badge/Java-11%2B-orange)]() [![License](https://img.shields.io/badge/license-Educational-blue)]()

---

## 🛡️ 4 Pillars An ninh năng lượng — Actionable Features

| # | Pillar | Detect | Predict | Recommend | Score |
|---|--------|--------|---------|-----------|-------|
| **1** | **Nguồn cung** | `alert_rules` INVENTORY_DAYS < 30/60 | `v_pillar1_supply_outlook` (days_to_critical) | Tự sinh `recommendation_text` điều chuyển từ region thừa sang region thiếu | P1 = stock_days / 90 × 100 |
| **2** | **Biến động Kinh tế** | `alert_rules` FUEL_PRICE breaches | `v_pillar2_volatility_signal` (σ rolling 1h) + `v_pillar2_cross_exchange_divergence` (chênh sàn) | Action: hedge nhập khẩu, cảnh báo địa chính trị | P2 = 100 - σ% × 10 |
| **3** | **Phụ tải** | `alert_rules` GRID_LOAD_PCT > 80/90 | (load_pct trend by region) | `v_pillar3_load_shedding_plan` — ordered priority list + suggested MW to shed | P3 = 100 - max_load × 1.1 |
| **4** | **Chuyển đổi & MT** | `alert_rules` EMISSION_INTENSITY > 600 | `v_pillar4_net_zero_progress` (vs roadmap VN 2026/2030/2050) | Action: dispatch ưu tiên nguồn xanh | P4 = renewable_share × 2 |
| **0** | **Cross-pillar** | `v_cascade_risks` (Pillar 2 spike + Pillar 1 low = FUEL_SHORTAGE_RISK, v.v.) | — | `recommendations` audit trail (PENDING/ACK/DISMISSED) | **`v_security_score` 0-100** (4 status: SECURE / STABLE / AT_RISK / CRITICAL) |

> 💡 **Triết lý:** Không chỉ "vẽ số liệu" — mọi view trả về `*_text` cột recommendation human-readable, `*_action_type` machine-readable, và severity để UI render badge/button hành động.

---

## 📦 Stack tổng quan

| Tầng | Công nghệ | Vai trò |
|------|-----------|---------|
| **Data Source** | 3 Java Producer (Pillar 2/3/4) + SQL seed (Pillar 1) | Sinh dữ liệu real-time cho 4 pillars |
| **Message Broker** | Apache Kafka 7.5.0 | Topics: `fuel-prices`, `grid-load`, `renewable-output` |
| **Stream Processing** | Apache Flink 1.17.0 | 4 luồng song song: raw / window 1-phút / **alert rule-based** / cross-pillar |
| **Storage** | PostgreSQL 15 | **13 bảng + 19 views** (3 fuel + 4 ops + 4 pillar + 2 security) — toàn bộ logic actionable nằm trong SQL views |
| **BI** | Metabase 0.47 (overlay) | Dashboard 4-pillar |
| **REST API** | Spring Boot 3 (Phase 4) | JWT + ~12 endpoint cover 4 pillars cho Desktop + Mobile |
| **Desktop UI** | JavaFX 21 (Phase 5) | 3-layer, JDBC trực tiếp, **Dashboard 4-tab pillars**, 5 màn, JUnit |
| **Mobile UI** | Android Studio (Phase 6) | Retrofit + MPAndroidChart, **bottom nav 4 pillars**, 4 màn |
| **Orchestration** | Docker Compose | One-click stack Lite ~2.85 GB |

---

## 🚀 Quick Start (3 lệnh)

```bash
git clone <repo-url>
cd Real-time-processing-with-Kafka-Flink-Postgres
bash scripts/run.sh        # hoặc .\scripts\run.ps1 trên Windows
```

Sau ~60s tất cả service sẽ healthy:
- **Flink UI**: http://localhost:8081
- **Metabase**: http://localhost:3000
- **PostgreSQL**: `localhost:5432` (user=`postgres`, pass=`123456`, db=`fuel_prices`)
- **Kafka**: `localhost:9092`, topic `fuel-prices`

Verify health: `bash scripts/healthcheck.sh`
Dừng stack: `bash scripts/stop.sh`

---

## 🏗️ Kiến trúc

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       VES REAL-TIME FUEL PIPELINE                       │
│                                                                          │
│  [Producer]  ─► [Kafka]  ─► [Flink Job]  ─► [Postgres]  ─► [Metabase]  │
│  data-gens/    fuel-prices  fuel-flink-     fuel_prices       (BI)     │
│  fuel-price-                 job (3 luồng    DB                          │
│  producer                    + alert Phase3)                             │
│                                                                          │
│                                                  ▲                       │
│                                                  │ JDBC + REST           │
│                                                  │                       │
│                              ┌───────────────────┴────────────────┐     │
│                              │   Spring Boot API (Phase 4)        │     │
│                              │   8 endpoint + JWT + SSE           │     │
│                              └───────┬───────────────────┬────────┘     │
│                                      │ REST              │ REST          │
│                                ┌─────▼─────┐       ┌─────▼─────┐        │
│                                │  JavaFX   │       │  Android  │        │
│                                │  Desktop  │       │  App      │        │
│                                │ (Phase 5) │       │ (Phase 6) │        │
│                                └───────────┘       └───────────┘        │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📁 Cấu trúc thư mục

```
Real-time-processing-with-Kafka-Flink-Postgres/
├── infra/                              # Hạ tầng Docker
│   ├── docker-compose.yml              # Stack 6 service
│   └── script/                         # SQL init schema + seed
│       ├── init_fuel_schema.sql        # Schema hiện tại
│       └── PostgreSQL.sql              # SQL legacy (reference)
│
├── data-generators/                    # Module gửi data lên Kafka
│   └── fuel-price-producer/            # Java Producer (Mock + HTTP)
│       ├── pom.xml
│       └── src/main/java/org/fuel/     # FuelPriceProducer, MockDataGenerator, HttpApiSource
│
├── flink-jobs/                         # Module Flink stream processing
│   └── fuel-flink-job/                 # 3 luồng: raw / window agg / alert
│       ├── pom.xml
│       └── src/main/java/org/cloud/    # KafkaConsumerApplication + process/* + source/*
│
├── backend-api/                        # Phase 4 — Spring Boot REST API (chưa có)
├── desktop-admin/                      # Phase 5 — JavaFX Admin Desktop (chưa có)
├── android-app/                        # Phase 6 — Android App (chưa có)
│
├── scripts/                            # Bootstrap scripts
│   ├── run.sh / run.ps1                # One-click start
│   ├── stop.sh / stop.ps1
│   └── healthcheck.sh                  # Verify stack health
│
├── docs/                               # Tài liệu
│   ├── data-flow-samples/              # Mẫu data ở mỗi tầng (bronze/silver/gold)
│   └── (Phase 8 sẽ thêm: AI_USAGE_LOG, DEPLOY, DEMO_RUNBOOK, TROUBLESHOOTING)
│
├── pom.xml                             # Parent Maven (multi-module)
├── .env.example                        # Template biến môi trường
├── .gitignore
├── .gitattributes                      # Đảm bảo line endings nhất quán
├── README.md                           # File này
├── UPGRADE_PLAN.md                     # Kế hoạch nâng cấp chi tiết (§24 = plan thực thi)
└── JAVA_FINAL_PROJECT_REQUIREMENT.md   # Yêu cầu đồ án Java
```

---

## 📊 Dữ liệu

| Loại nhiên liệu | Đơn vị | Địa điểm |
|------------------|--------|----------|
| WTI Crude | USD/barrel | New York, Houston |
| Brent Crude | USD/barrel | London, Dubai, Singapore |
| Gasoline | USD/gallon | New York, Houston |
| Diesel | USD/gallon | New York, London |
| Natural Gas | USD/MMBtu | Houston |

---

## 🛣️ Lộ trình build (Minimalist + Light 4-pillars — §24 UPGRADE_PLAN.md)

| Phase | Mục tiêu | Status |
|-------|----------|--------|
| **0** | Foundation — restructure repo + verify code có sẵn | ✅ `v0.0-foundation` |
| **1** | Docker Lite — tối ưu RAM ~2.85 GB | ✅ `v0.1-docker-lite` |
| **2** | Schema users/regions/alert_rules/alerts (Pillar 2 ops) | ✅ `v0.2-schema` |
| **2.5** | Pillar 1/3/4 raw tables + seed (Light 4-pillar coverage) | ✅ `v0.2.5-pillars` |
| **2.6** | Security output features (recommendations + 8 action views + ESI score) | ✅ `v0.2.6-security-features` |
| **3** | Flink Alert PF — đọc alert_rules đa pillar → `alerts` + tự sinh `recommendations` | ⬜ |
| **4** | Spring Boot REST API + **2 generator mới** (grid-load + renewable) | ⬜ |
| **5** | JavaFX Admin Desktop ⭐ — 5 màn, 4-tab pillars dashboard | ⬜ |
| **6** | Android App — 4 activity, bottom nav 4 pillars | ⬜ |
| **7** | Deploy + Cloudflared Tunnel | ⬜ |
| **8** | Documentation + Demo prep | ⬜ |

Chi tiết: [`UPGRADE_PLAN.md`](./UPGRADE_PLAN.md) §24.

---

## 🐛 Troubleshooting

| Vấn đề | Giải pháp |
|--------|-----------|
| Kafka connection refused | `docker logs kafka` → chờ 30s thêm. Hoặc `bash scripts/stop.sh --volumes && bash scripts/run.sh` để reset |
| Flink job failed | Check pom.xml shade plugin, verify JAR classpath. Xem log: `docker logs flink-jobmanager` |
| Postgres table not found | Verify SQL init script đã chạy: `docker logs postgres-database | grep init_fuel_schema` |
| Metabase can't connect DB | Trong Metabase, **Host** phải là `postgresql` (tên service Docker), KHÔNG phải `localhost` |
| Port 5432/9092/8081/3000 bị chiếm | Đổi port trong `.env` (sau khi copy từ `.env.example`) |

---

## 📚 Tài liệu

| File | Mô tả |
|------|-------|
| [UPGRADE_PLAN.md](./UPGRADE_PLAN.md) | Kế hoạch nâng cấp chi tiết, §24 = Minimalist Plan (thực thi) |
| [JAVA_FINAL_PROJECT_REQUIREMENT.md](./JAVA_FINAL_PROJECT_REQUIREMENT.md) | Yêu cầu đồ án môn Java |
| [docs/PHAN_CONG_CONG_VIEC_legacy.md](./docs/PHAN_CONG_CONG_VIEC_legacy.md) | Phân công công việc (legacy, sẽ thay bằng §22 UPGRADE_PLAN) |

---

**Môn học:** IS402.P21 + Lập trình Java + Phát triển ứng dụng di động
**Tài liệu liên quan:** [UPGRADE_PLAN.md](./UPGRADE_PLAN.md)
