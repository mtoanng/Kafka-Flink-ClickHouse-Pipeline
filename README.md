# ⚡ VES-Monitor — Vietnam Energy Security Real-time Monitor

> Nền tảng **giám sát an ninh năng lượng Việt Nam** thời gian thực qua **Kafka + Flink + PostgreSQL**, bao quát **4 pillars** (Nguồn cung, Biến động Kinh tế, Phụ tải, Chuyển đổi & Môi trường) — kèm **JavaFX Admin Desktop** (môn Java) và **Android App** (môn Mobile). Tận dụng 90% code stream-processing có sẵn cho Pillar 2 (giá nhiên liệu thế giới), bổ sung light coverage cho 3 pillars còn lại.

[![Status](https://img.shields.io/badge/status-Phase%207%20demo--grade-brightgreen)]() [![Java](https://img.shields.io/badge/Java-11%2B-orange)]() [![Spring%20Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen)]() [![JavaFX](https://img.shields.io/badge/JavaFX-17.0.10%20LTS-purple)]() [![License](https://img.shields.io/badge/license-Educational-blue)]()

> 👥 **Thành viên đồ án Java**, đọc **[`docs/TEAM_TASKS.md`](./docs/TEAM_TASKS.md)** để biết task của mình (B/C/D/E × 5 PR step-by-step).
>
> 📱 **Android Developer** (đồ án Mobile **riêng biệt**), đọc **[`docs/ANDROID_ONBOARDING.md`](./docs/ANDROID_ONBOARDING.md)** — repo Android độc lập, chỉ consume 14 endpoint REST API của project này.

---

## 🆕 Phase 7 (13 May 2026) — Domain quality + Real-time UX + Interactive maps ✅

JavaFX desktop app upgraded from "Phase 5 functional" → "demo-grade":

- **🛡️ IEA/APERC pillar redesign** — 4 pillars renamed to international standard (Supply Security / Market Resilience / Grid Reliability / Energy Transition) with **16 sub-indicators** (IDR, SFRI, HHI, N-1, σ30d, price gap, β-crude, affordability, reserve margin, peak factor, shed prob, freq stability, renewable %, CO2 intensity, curtailment, net-zero progress). Composite ESI uses IEA weights `0.30·P1 + 0.20·P2 + 0.30·P3 + 0.20·P4`. New status ladder: `SECURE / ELEVATED / STRESSED / CRITICAL`. Implemented entirely as SQL views in `infra/script/08_pillars_v2.sql` — **Flink job untouched**.
- **⚡ Real-time visibility** — live events/sec ticker (Flink REST + EMA smoothing), pillar sparkline canvases (3-min trailing window), pulse drop-shadow on new `CRITICAL` alerts, slide-in toast popups, fade-in for new recommendations. Refresh cadence split: live 3 s / tables 10 s.
- **🗺️ Interactive maps** — new "Maps · Bản đồ" tab with **VN SVG (3 zones)** coloured by Pillar 3 score + clickable drill-down filter on the dashboard, and a **Leaflet World Map** (`+javafx-web`) with 7 hubs (WTI Houston, Brent London, Singapore, Vũng Tàu, Hải Phòng, …) showing live price/delta popups. Graceful offline overlay if OSM tiles can't load.

> Test suite **77/77 PASS**, `mvn package` clean, no Lombok / Spring / extra deps on the desktop module. See [`UPGRADE_PLAN.md` §24.5 Phase 7](./UPGRADE_PLAN.md) and [`docs/PROGRESS.md` Phase 7 sections](./docs/PROGRESS.md) for the full log.

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
| **Message Broker** | Apache Kafka 7.5.0 | 4 topic: `fuel-prices`, `grid-load`, `renewable-output`, `emission` (Phase 4) |
| **Stream Processing** | Apache Flink 1.17.0 | **9 vertex / 4 luồng pillar** song song: 4 RAW sink + Window 1-phút + Price-change + 3 RULE detector (FUEL_PRICE / GRID_LOAD_PCT / EMISSION_INTENSITY) |
| **Storage** | PostgreSQL 15 | **13 bảng + 19 views** (3 fuel + 4 ops + 4 pillar + 2 security) — toàn bộ logic actionable nằm trong SQL views. `alerts.metric_type` đa pillar (Phase 4). |
| **BI** | Metabase 0.47 (overlay) | Dashboard 4-pillar |
| **REST API** | Spring Boot 2.7 (Phase 4.5 — code ready, build pending) | JWT + **14 endpoint** cover 4 pillars cho Desktop + Mobile, port `8090` |
| **Desktop UI** | JavaFX 17.0.10 LTS (Phase 5, code-complete) | 3-layer, JDBC trực tiếp Postgres, **5 màn** (Login + Dashboard 4-pillar TabPane + Region CRUD + AlertRule CRUD + User CRUD admin-only), **62 JUnit 4 test** (H2 in-memory DAO + Mockito service + util) |
| **Mobile UI** | Android Studio (Phase 6) | Retrofit + MPAndroidChart, **bottom nav 4 pillars**, 4 màn |
| **Orchestration** | Docker Compose | One-click stack Lite ~2.85 GB |

---

## 🚀 Quick Start (3 lệnh)

```bash
git clone <repo-url>
cd Real-time-processing-with-Kafka-Flink-Postgres
bash scripts/run.sh        # khởi động Docker + tự pre-create 4 Kafka topic
mvn -q clean package -DskipTests    # build 4 JAR (3 generator + 1 Flink job)
# Upload + submit Flink job qua Flink UI http://localhost:8081 (entry class: org.cloud.KafkaConsumerApplication)
bash scripts/start_generators.sh    # chạy 3 generator song song (Pillar 2/3/4)
```

Sau ~60s tất cả service sẽ healthy + raw data đang chảy 4 pillar:
- **Flink UI**: http://localhost:8081  (xem 9 vertex đều RUNNING)
- **Metabase**: http://localhost:3000  (BI overlay, optional)
- **PostgreSQL**: `localhost:5432` (user=`postgres`, pass=`123456`, db=`fuel_prices`)
- **Kafka**: `localhost:9092`, 4 topic: `fuel-prices`, `grid-load`, `renewable-output`, `emission`

Verify health: `bash scripts/healthcheck.sh`
Smoke test 4 pillar: `docker exec -i postgres-database psql -U postgres -d fuel_prices < scripts/phase4_smoke.sql`
Stress alert demo: `bash scripts/phase4_stress_alerts.sh`
Dừng generator (giữ stack): `bash scripts/stop_generators.sh`
Dừng toàn bộ: `bash scripts/stop.sh`

---

## 🏗️ Kiến trúc

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                  VES-MONITOR REAL-TIME 4-PILLAR PIPELINE  (Phase 4)             │
│                                                                                  │
│  ┌─────────────────────────┐                                                    │
│  │  3 Java Producer        │       4 topic Kafka                                │
│  │  (P2 fuel-price-prod)   │ ───►  fuel-prices                                  │
│  │  (P3 grid-load-gen)     │ ───►  grid-load                                    │
│  │  (P4 renewable-gen)     │ ───►  renewable-output                             │
│  │                         │ ───►  emission                                     │
│  └─────────────────────────┘                                                    │
│              ▼                                                                   │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │              Flink Job — 9 vertex / 4 source x N branch                  │  │
│  │  P2: raw + window(1m) + price-change + FUEL_PRICE rule detector          │  │
│  │  P3: raw + GRID_LOAD_PCT rule detector  (60s cooldown / region)          │  │
│  │  P4: raw renewable + raw emission + EMISSION_INTENSITY rule detector     │  │
│  │  → CRITICAL alerts ➜ auto-recommendations (SQL NOT EXISTS 30m dedup)     │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│              ▼                                                                   │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │  PostgreSQL — 13 table + 19 view                                         │  │
│  │  - 4 raw: fuel_prices_raw / grid_load_raw / renewable_output_raw / ...   │  │
│  │  - alerts (multi-pillar metric_type) / recommendations / daily_briefings  │  │
│  │  - v_security_score (Light ESI) + v_cascade_risks (compound multi-pillar) │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│              ▼ JDBC + REST                                                       │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │   Spring Boot API (Phase 4.5)  — 12 endpoint cover 4 pillar + JWT + SSE  │  │
│  └──────┬──────────────────────────────────────────────────────────┬────────┘  │
│         │ REST                                                       │ REST     │
│  ┌──────▼─────────┐                                            ┌─────▼────────┐ │
│  │ JavaFX Desktop │ (Phase 5) ★ TabPane 4-pillar dashboard     │ Android      │ │
│  │ Admin Console  │           ★ ESI gauge + recommendations UI │ App (Phase 6)│ │
│  └────────────────┘                                            └──────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
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
├── data-generators/                    # 3 module gửi data lên Kafka (Pillar 2/3/4)
│   ├── fuel-price-producer/            # Pillar 2 — 30 record/10s vào topic fuel-prices
│   │   ├── pom.xml
│   │   └── src/main/java/org/fuel/     # FuelPriceProducer, MockDataGenerator, HttpApiSource
│   ├── grid-load-generator/            # Pillar 3 — 3 region/5s vào topic grid-load
│   │   ├── pom.xml
│   │   └── src/main/java/vn/edu/ves/grid/   # GridLoadGenerator + mock source + GridLoadEvent
│   └── renewable-generator/            # Pillar 4 — 9 record/10s + 3 emission/30s
│       ├── pom.xml
│       └── src/main/java/vn/edu/ves/renewable/  # 2 topic producer: renewable-output + emission
│
├── flink-jobs/                         # Module Flink stream processing
│   └── fuel-flink-job/                 # 9 vertex / 4 source: P2 (4 luồng) + P3 (raw+alert) + P4 (raw+raw+alert)
│       ├── pom.xml
│       └── src/main/java/org/cloud/    # KafkaConsumerApplication + 3 detector + 7 model POJO
│
├── backend-api/                        # Phase 4.5 — Spring Boot 2.7 REST API (14 endpoint, JWT, JdbcTemplate)
│   ├── pom.xml
│   ├── README.md                       # Build/run + endpoint reference + proxy workaround
│   └── src/main/
│       ├── java/vn/edu/ves/api/
│       │   ├── VesApiApplication.java
│       │   ├── config/                 # SecurityConfig, JwtTokenProvider, JwtAuthFilter, OpenApiConfig
│       │   ├── controller/             # 7 controller (Auth, Pillar, Security, Recommendation, Alert, RawData, Health)
│       │   ├── dao/                    # 5 DAO JdbcTemplate (User/Pillar/Security/Recommendation/Alert)
│       │   ├── dto/                    # 14 DTO POJO (Lombok) — pillar dashboards + ESI + alert + recommendation
│       │   └── exception/              # ApiException + GlobalExceptionHandler (JSON 4xx/5xx)
│       └── resources/application.yml
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
| **3** | Flink Alert Detection (FUEL_PRICE rules) + Auto Recommendation Gen (CRITICAL) | ✅ `v0.3-flink-alert` |
| **4** | **2 Generator mới (grid-load + renewable) + Flink multi-pillar (GRID_LOAD_PCT + EMISSION_INTENSITY)** | ✅ `v0.4-generators` |
| **4.5** | **Spring Boot 2.7 REST API — 14 endpoint cover 4 pillar + JWT + Swagger** | 🟡 `code-complete` (build pending — Bosch proxy NTLM block; run `mvn package` từ hotspot 4G) |
| **5** | **JavaFX Admin Desktop ⭐ — 5 màn, 4-tab pillars dashboard, JDBC trực tiếp, 62 JUnit test** | 🟡 `code-complete` Phase 5.1→5.5 (build pending cùng proxy block) |
| **6** | Android App — 4 activity, bottom nav 4 pillars | ⬜ |
| **7** | Deploy + Cloudflared Tunnel | ⬜ |
| **8** | Documentation + Demo prep | ⬜ |

Chi tiết: [`UPGRADE_PLAN.md`](./UPGRADE_PLAN.md) §24.

---

## 🌐 Phase 4.5 — Backend REST API (Spring Boot 2.7)

> **Trạng thái**: source đầy đủ ở `backend-api/` (24 file Java, lint-clean) — chờ build sau khi qua được proxy Bosch NTLM. Khi có hotspot 4G hoặc bridge, chỉ cần 2 lệnh:

```bash
mvn -pl backend-api -am clean package -DskipTests          # ~2-5 phút
java -jar backend-api/target/ves-backend-api.jar            # nghe 0.0.0.0:8090
```

Smoke 14 endpoint:
```bash
bash scripts/phase45_smoke_api.sh         # user mặc định admin/admin
```

| Endpoint | Pillar | Mô tả |
|----------|--------|-------|
| `POST /api/auth/login`             | -    | Lấy JWT (admin/admin, manager/manager, user/user) |
| `GET  /api/auth/me`                | -    | User hiện tại |
| `GET  /api/pillars/1/outlook`      | 1    | Stock days + recommendation điều chuyển |
| `GET  /api/pillars/2/volatility`   | 2    | σ rolling 1h + signal STABLE/ELEVATED/VOLATILE |
| `GET  /api/pillars/3/shedding-plan`| 3    | Priority load shed (load_pct ≥ 85%) |
| `GET  /api/pillars/4/net-zero`     | 4    | Renewable share vs roadmap 2026/2030 |
| `GET  /api/security/score`         | 0    | Light ESI 0-100 + status |
| `GET  /api/security/cascade-risks` | 0    | Compound multi-pillar risks |
| `GET  /api/recommendations`        | 0    | List PENDING + age |
| `POST /api/recommendations/{id}/acknowledge` | 0 | ACK/DISMISS + audit user/note |
| `GET  /api/alerts/active`          | all  | Cảnh báo chưa ACK |
| `GET  /api/fuel-prices/latest`     | 2    | N giá nhiên liệu mới nhất |
| `GET  /api/grid-load/latest`       | 3    | Phụ tải theo region |
| `GET  /api/health`                 | -    | DB ping (public, dùng cho tunnel probe) |

Swagger UI: `http://localhost:8090/swagger-ui.html`. Chi tiết build / proxy workaround / config: [`backend-api/README.md`](./backend-api/README.md).

---

## 🖥️ Phase 5 — JavaFX Admin Desktop (code-complete)

> **Trạng thái**: 5 màn code-complete, lint clean, **62 JUnit 4 test** (H2 + Mockito + pure unit). Build runtime defer cùng Phase 4.5 do proxy Bosch block JavaFX 17 deps lần đầu. Module nằm ở `desktop-admin/`, đường găng dùng JDBC trực tiếp Postgres (**không qua REST API Phase 4.5**) — tránh blocker proxy chéo phase.

```bash
mvn -pl desktop-admin -am clean compile     # ~30s pull JavaFX 17.0.10 deps lần đầu
mvn -pl desktop-admin javafx:run            # mở app, đăng nhập admin/admin
mvn -pl desktop-admin test                  # chạy 62 @Test (không cần network)
```

| Màn | Controller | Quyền |
|-----|-----------|-------|
| Login | `LoginController` (BCrypt verify, AuthServiceImpl) | Public |
| Dashboard | `DashboardController` (ProgressIndicator gauge + TabPane 4 pillar + auto-refresh 30s + recommendation sidebar) | ADMIN / MANAGER / VIEWER |
| Region CRUD | `RegionController` (form + table, validator chain) | ADMIN / MANAGER write, VIEWER read |
| AlertRule CRUD | `AlertRuleController` (multi-pillar metric_type, operator/severity enum, toggle enable) | ADMIN / MANAGER write, VIEWER read |
| User CRUD | `UserController` (ADMIN-only, self-delete guard) | ADMIN only |

### Design patterns checklist (môn Java)
- **Singleton**: `DatabaseConfig`, `SessionManager` (AtomicReference thread-safe)
- **DAO**: 4 DAO (`UserDao` / `RegionDao` / `AlertRuleDao` / `ViewsDao`) extends `BaseDao`
- **MVC**: 6 FXML view + 6 Controller + 13 Model POJO
- **Strategy + Composite**: `util/Validator` interface + 4 strategy implementations + `Validator.compose(...)` chain
- **Factory**: `DatabaseConfig.openConnection()` trả Connection mới mỗi call

Chi tiết: [`desktop-admin/README.md`](./desktop-admin/README.md) — bao gồm directory tree, test count từng file, override DB env.

---

## 🐛 Troubleshooting

| Vấn đề | Giải pháp |
|--------|-----------|
| Kafka connection refused | `docker logs kafka` → chờ 30s thêm. Hoặc `bash scripts/stop.sh --volumes && bash scripts/run.sh` để reset |
| Flink job failed | Check pom.xml shade plugin, verify JAR classpath. Xem log: `docker logs flink-jobmanager` |
| Postgres table not found | Verify SQL init script đã chạy: `docker logs postgres-database | grep init_fuel_schema` |
| Metabase can't connect DB | Trong Metabase, **Host** phải là `postgresql` (tên service Docker), KHÔNG phải `localhost` |
| Port 5432/9092/8081/3000/8090 bị chiếm | Đổi port trong `.env` (Docker) hoặc `SERVER_PORT=...` (backend-api) |
| `mvn package` báo `407 Proxy Authentication Required` ở Bosch | Proxy `rb-proxy-apac.bosch.com:8080` chỉ chấp nhận NTLM/Negotiate. Workaround: hotspot 4G để cache deps, hoặc `cntlm` local bridge — xem `backend-api/README.md` mục "Build". |

---

## 📚 Tài liệu

| File | Mô tả |
|------|-------|
| [UPGRADE_PLAN.md](./UPGRADE_PLAN.md) | Kế hoạch nâng cấp chi tiết, §24 = Minimalist Plan (thực thi) |
| [JAVA_FINAL_PROJECT_REQUIREMENT.md](./JAVA_FINAL_PROJECT_REQUIREMENT.md) | Yêu cầu đồ án môn Java |
| **[docs/TEAM_TASKS.md](./docs/TEAM_TASKS.md)** | **👥 Đồ án Java — phân công team 5 người (B/C/D/E × 5 PR step-by-step)** |
| **[docs/ANDROID_ONBOARDING.md](./docs/ANDROID_ONBOARDING.md)** | **📱 Đồ án Android (riêng biệt) — onboarding cho Android Dev: API contract + stack + first-day setup** |
| [backend-api/README.md](./backend-api/README.md) | Hướng dẫn build & deploy Spring Boot REST API (Phase 4.5) |
| [docs/PROGRESS.md](./docs/PROGRESS.md) | Sổ tay tiến độ phase + verification log |
| [docs/PHAN_CONG_CONG_VIEC_legacy.md](./docs/PHAN_CONG_CONG_VIEC_legacy.md) | Phân công công việc (legacy, đã thay bằng TEAM_TASKS.md) |

---

**Môn học:** IS402.P21 + Lập trình Java + Phát triển ứng dụng di động
**Tài liệu liên quan:** [UPGRADE_PLAN.md](./UPGRADE_PLAN.md)
