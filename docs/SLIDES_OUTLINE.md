# 🎤 VES-Monitor — Slide Deck Outline

> **Đối tượng**: Giảng viên môn Java (IS402.P21) + reviewer kỹ thuật.
> **Thời lượng đề xuất**: ~18 phút (15 slide nội dung + 1 title + 1 Q&A) → trung bình **~1 phút/slide**, slide kỹ thuật có thể tăng lên 1 phút 30 giây.
> **Format đề xuất**: Google Slides hoặc PowerPoint 16:9 · font sans-serif (Inter / Roboto / SF Pro), accent màu IEA xanh `#1976d2`.
> **Convention**: mỗi slide có **heading** (≤ 10 chữ) + **3-5 bullet** ngắn cho presenter elaborate · các diagram nhúng từ [`docs/diagrams/`](./diagrams/) ở dạng PNG (xem [`diagrams/README.md`](./diagrams/README.md) cho lệnh export `mmdc`).

---

## Slide 1 · Title

**VES-Monitor — Vietnam Energy Security Real-time Platform**

- Sinh viên thực hiện: _<điền tên>_
- Lớp / MSSV: _<điền>_
- Giảng viên hướng dẫn: _<điền>_
- Ngày bảo vệ: **13 / 05 / 2026**
- Repo: [`github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres`](https://github.com/mtoanng/Real-time-processing-with-Kafka-Flink-Postgres) · tag `v1.0.0`

> Footer: "Java + Distributed Streaming · 4-pillar IEA / APERC framework"

---

## Slide 2 · Bối cảnh — Việt Nam & An ninh năng lượng

- 🛢️ **Phụ thuộc nhập khẩu**: VN nhập ~ 40 % nhu cầu xăng dầu (2024), oil import dependency ratio đang tăng.
- ⚡ **Bottleneck Bắc-Nam**: lưới 500 kV cao điểm chạy > 95 % công suất → SAIDI tăng mỗi mùa nóng.
- ☀️ **Curtailment miền Trung**: solar/wind dư công suất nhưng truyền tải không kịp → cắt giảm ~ 8 % sản lượng.
- 🌱 **Cam kết Net-Zero 2050** (COP26, Glasgow) + Quy hoạch điện 8: roadmap 47 % renewable 2030 / 70 % 2050.

> Speaker note: dẫn dắt từ "an ninh năng lượng là vấn đề chiến lược quốc gia" → "đo lường nó như thế nào?"

---

## Slide 3 · Vấn đề hiện tại

- 📄 **Reporting thủ công**: EVN/PVN gửi báo cáo tuần/tháng dạng Excel.
- ⏳ **Độ trễ ra quyết định**: từ "sự cố" → "lệnh điều hành" trung bình vài giờ tới vài ngày.
- 🪟 **Visibility phân mảnh**: mỗi pillar (giá nhiên liệu / phụ tải / phát thải) thuộc một cơ quan khác nhau, không có view tổng hợp.
- 🚨 **Missing piece**: **real-time monitoring + cross-pillar alerting** đang là khoảng trống công nghệ.

> Speaker note: nhấn mạnh "real-time" và "cross-pillar" — đây là 2 từ khoá phân biệt VES-Monitor với BI dashboard thông thường.

---

## Slide 4 · Mục tiêu đồ án

- 🎯 **Mục tiêu chính**: Xây dựng nền tảng phân tán **real-time** giám sát an ninh năng lượng Việt Nam theo chuẩn quốc tế **IEA / APERC**.
- 🧩 **4 năng lực cốt lõi**: `Ingest → Process → Alert → Recommend` (event-by-event, không batch).
- 🎓 **Mục tiêu môn Java**: chứng minh 10 design pattern · 3-layer + MVC · ≥ 10 JUnit test · JavaFX desktop hoàn chỉnh.
- 📊 **KPI thành công**: 1 composite score 0–100 (ESI) + 4 pillar score + alert tự sinh + recommendation actionable.

> Speaker note: nói rõ "mục tiêu môn Java" để giảng viên thấy đồ án đã tự đặt tiêu chuẩn cao hơn requirement.

---

## Slide 5 · Kiến trúc tổng quan

[Nhúng diagram `docs/diagrams/01_dataflow.png` — export từ `01_dataflow.md`]

- 5 tier: **Generators (4 JVM, WSL)** → **Kafka (4 topic)** → **Flink (18 vertex)** → **PostgreSQL (13 bảng + 17 view)** → **3 consumer (JavaFX · REST · Android)**.
- Throughput đo thực: **~ 1 000 events/sec** tổng hợp (verified live, xem `docs/DEMO_RUN_LOG.md`).
- Latency end-to-end: **~ 1.5 s** từ generator → JavaFX dashboard.
- Toàn bộ chạy qua Docker Compose, RAM cap **~ 2.85 GB**.

> Speaker note: chỉ vào diagram → kể câu chuyện dữ liệu chạy từ trái sang phải.

---

## Slide 6 · Tech stack

| Tầng | Công nghệ | Phiên bản | Lý do chọn |
|---|---|---|---|
| Message broker | Apache Kafka | 7.5.0 | Standard cho event streaming, partition để scale |
| Stream processor | Apache Flink | 1.17.0 | True streaming (vs Spark micro-batch); MapState cho cooldown |
| Storage | PostgreSQL | 15 | Trusted, hỗ trợ JSONB + GENERATED columns |
| Backend | Spring Boot | 2.7.18 | LTS Java 11, JdbcTemplate (không magic Hibernate) |
| Desktop UI | JavaFX | 17.0.10 LTS | Yêu cầu môn Java, JDBC trực tiếp giảm latency |
| Container | Docker Compose | v2 | One-click stack, lite profile cho RAM thấp |
| Auth | jjwt (JWT HS256) | 0.11.5 | Stateless, OK cho mobile + REST |
| Build | Maven multi-module | 3.9.6 | Standard, 4 module song song |

> Speaker note: nếu hỏi "tại sao Flink chứ không Spark?" → "true streaming event-by-event, latency thấp cho alert, MapState support".

---

## Slide 7 · 4-pillar Framework (IEA / APERC)

[Nhúng diagram `docs/diagrams/04_pillar_framework.png`]

- **P1 — Supply Security · Availability** (weight 0.30) — IEA/APERC indicators 1, 7
- **P2 — Market Resilience · Affordability** (weight 0.20) — IEA + IMF Volatility
- **P3 — Grid Reliability · Accessibility** (weight 0.30) — NERC + IEEE 1366
- **P4 — Energy Transition · Acceptability** (weight 0.20) — IPCC AR6 + Net-Zero 2050

> Speaker note: nhấn mạnh "tên gọi quốc tế, không bịa" — taxonomy thực sự dùng bởi IEA Energy Security Indicators 2014 + APERC APEC 100% framework.

---

## Slide 8 · 16 sub-indicators

| Pillar | Sub-indicators (4 mỗi pillar) |
|---|---|
| **P1** | `idr` Import Dep Ratio · `sfri` Reserve Days · `hhi_supply` Fuel Diversity · `n1_resilience` |
| **P2** | `sigma_30d` Volatility · `price_gap_pct` vs Brent · `beta_crude` · `affordability_idx` |
| **P3** | `reserve_margin_pct` · `peak_load_factor` · `shedding_prob` · `freq_stability_idx` |
| **P4** | `renewable_pct` · `co2_intensity` · `curtailment_rate` · `netzero_progress` |

- Tất cả tính bằng **SQL views** trên `infra/script/08_pillars_v2.sql` — không cần redeploy Flink khi đổi công thức.
- Mỗi sub-indicator có **reference standard** (xem [`docs/diagrams/04_pillar_framework.md`](./diagrams/04_pillar_framework.md)).

> Speaker note: nếu reviewer hỏi "công thức cụ thể" → mở file `08_pillars_v2.sql` line 134-208 cho P1.

---

## Slide 9 · ESI Composite — formula + status

- **Công thức**: `ESI = 0.30·P1 + 0.20·P2 + 0.30·P3 + 0.20·P4`
- IEA-standard weight: Availability + Accessibility = 60 % (vì supply disruption / blackout có tần suất + impact cao hơn slow drift).

| Score | Status | Hex | Ý nghĩa |
|---:|---|---|---|
| ≥ 80 | **SECURE** | 🟢 `#2E7D32` | Mọi pillar nominal |
| 60-79 | **ELEVATED** | 🟡 `#F9A825` | Một vài pillar stress, monitor |
| 40-59 | **STRESSED** | 🟠 `#EF6C00` | Multi-pillar degraded, contingency |
| < 40 | **CRITICAL** | 🔴 `#C62828` | Cross-cutting risk, emergency |

- **Verified live**: `overall = 72.82 ELEVATED` (P1=64.83, P2=59.59, P3=90.11, P4=72.09) — Phase 7.5 QA snapshot.

> Speaker note: chỉ vào status hex → "JavaFX dashboard dùng đúng palette này, không phải tô bừa".

---

## Slide 10 · Streaming pipeline highlights

- ⚡ **Flink job**: 18 vertex / 4 luồng pillar / **MapState cooldown 60 s** cho alert dedup.
- 🚫 **Anti-storm logic**: cùng 1 rule trên cùng 1 region chỉ alert 1 lần / phút, dù metric oscillate.
- 🧠 **Recommendation 30 min dedup**: SQL `WHERE NOT EXISTS (... suggested_at > NOW() - INTERVAL '30 minutes')` — không sinh trùng action_type cho cùng pillar.
- 📡 **Throughput**: ~ 1 000 events/sec verified · scale-out path = Kafka partitions × Flink parallelism (không đổi code).
- ✅ **18/18 vertex RUNNING** sustained (xem Flink UI screenshot trong demo).

> Speaker note: nếu hỏi "scale 1 triệu events/sec?" → "Kafka partitions 12× + Flink parallelism 12 + Postgres read replica, không đổi code".

---

## Slide 11 · Database schema

[Nhúng diagram `docs/diagrams/02_erd.png`]

- **13 tables**: 4 raw streaming sinks · 1 windowed agg · 1 inventory · 1 user · 1 region · 1 alert_rules · 1 alerts · 1 recommendations · 1 daily_briefings · 1 fuel_price_alerts (legacy).
- **17 computed views**: 11 helper/legacy + **4 IEA-APERC pillar views** + 1 composite ESI + 1 active recommendations.
- **Generated columns**: `stock_days`, `load_pct`, `utilization_pct`, `intensity_kg_per_mwh` — compute-at-write, không phải view recompute.
- **FK cascade**: `regions` → 4 raw tables `ON DELETE CASCADE` · `alert_rules` → `alerts` cascade.

> Speaker note: "view = không lưu row, tính khi đọc" → đổi công thức ESI chỉ cần `psql -f`.

---

## Slide 12 · Backend REST API

- **Spring Boot 2.7.18** trên Java 11 + Tomcat embedded · port `8090`.
- **13 endpoints** cover 4 pillar + auth + recommendations + alerts + raw data + health.
- **JWT HS256** stateless · token 8 h · 3 role (ADMIN / MANAGER / VIEWER).
- **Springdoc OpenAPI 1.7.0** → Swagger UI tại `/swagger-ui.html`.
- 📦 Postman collection: [`docs/VES-Monitor.postman_collection.json`](./VES-Monitor.postman_collection.json).
- 🟡 **Hiện trạng**: 8/13 endpoints `200 OK`; 5 endpoints `500` đang được migrate theo schema Phase 7.1 (sibling worker fix Phase 7.6).

> Speaker note: chuyển sang OpenAPI dropdown trong demo → cho thấy mọi endpoint đều có Swagger doc đầy đủ.

---

## Slide 13 · JavaFX Desktop

[Nhúng diagram `docs/diagrams/03_class_desktop.png`]

- **5 tab**: 4 pillar + **Maps · Bản đồ** (VN SVG 3 zone clickable + Leaflet world hub).
- **Live ticker** top bar: `⚡ N events/sec · Last tick HH:MM:SS` (Flink REST poll 3 s + EMA smoothing).
- **Sparkline canvas** mỗi pillar tab — 3 min trailing window @ 3 s sample.
- **Pulse animation** đỏ trên CRITICAL alert mới · **Toast popup** trượt từ top-right.
- Refresh cadence chia tier: **live 3 s** / **table 10 s**.
- **3-layer architecture** + 10 design pattern (Singleton, Strategy, DAO, MVC, …).

> Speaker note: chỉ vào sparkline + live ticker → "đây là điểm khác biệt so với dashboard tĩnh: app thực sự *live*".

---

## Slide 14 · Design patterns dùng

| # | Pattern | Vị trí | Mục đích |
|---|---|---|---|
| 1 | **Singleton** | `DatabaseConfig`, `SessionManager`, `MainApp` | Global state thread-safe |
| 2 | **DAO** | 4 DAO extends `BaseDao` | Tách JDBC khỏi business logic |
| 3 | **MVC** | 6 FXML × 6 Controller × 13 Model | JavaFX-idiomatic separation |
| 4 | **Strategy + Composite** | `Validator` interface · 4 strategy · `compose(...)` | Validation chain mở rộng vô hạn |
| 5 | **Factory** | `DatabaseConfig.openConnection()` | Fresh connection mỗi call |
| 6 | **Service Layer / Facade** | `*ServiceImpl` | Controller phụ thuộc interface, testable |
| 7 | **Observer** | `LiveMetricsService` + `ScheduledExecutorService` callback | Decouple Flink poll khỏi UI |
| 8 | **Adapter** | `FlinkClient` | Shield UI khỏi Flink REST drift |
| 9 | **Template Method** | `BaseDao.setStringOrNull(...)` | Loại bỏ boilerplate null-handling |
| 10 | **State machine** | `recommendations.status` (PENDING → ACK / DISMISSED / EXPIRED) | Audit trail explicit |

> Speaker note: nếu giảng viên chỉ hỏi 1 pattern → giải thích **Strategy + Composite** (validation chain) — dễ minh hoạ code nhất.

---

## Slide 15 · Testing & quality

- **77 / 77 desktop tests PASS** (`mvn -pl desktop-admin test` → 18 s)
  - 24 DAO integration (H2 in-memory MODE=PostgreSQL)
  - 22 service unit (Mockito mock DAO)
  - 16 util unit (PasswordUtil, SessionManager, Validator)
  - 15 widget / Flink client (embedded HttpServer)
- **Backend tests**: 6-10 đang được sibling worker bổ sung theo Phase 7.6.
- **Lint**: clean toàn bộ 4 module.
- **FXML XML parse**: 7/7 valid.
- **CI gate**: `mvn clean package -DskipTests` BUILD SUCCESS (`ves-desktop-admin.jar` thin 144 KB + `ves-backend-api.jar` fat 30.3 MB).

> Speaker note: nhấn vào "77/77 PASS" và "lint clean toàn module" — chỉ số minh hoạ kỷ luật code.

---

## Slide 16 · Demo live (runbook)

> Theo [`docs/DEMO_SCRIPT.md`](./DEMO_SCRIPT.md) — copy-paste runbook 18 min.

**Sequence**:
1. **JavaFX dashboard** — live ticker + sparklines + pulse + toast.
2. **Click qua 5 tab pillar** + Maps · Bản đồ (VN zones + Leaflet world).
3. **Postman** — `POST /api/auth/login` → `GET /api/security/score` 200.
4. **Trigger CRITICAL alert** — `UPDATE alert_rules SET threshold = 50 WHERE metric_type = 'FUEL_PRICE' AND severity = 'CRITICAL';` → wait 5-10 s → toast.
5. **Flink UI** `:8081` — 18 vertex RUNNING.
6. **Postgres** — `SELECT * FROM v_security_score;` trực tiếp psql.

> Speaker note: nếu thời gian dồn → skip step 5 (Flink UI) vì kém visual nhất.

---

## Slide 17 · Q&A + Tổng kết

**Tóm tắt thành quả**:
- 🎓 **Môn Java**: 10 design pattern · 3-layer + MVC · 77 JUnit · JavaFX 17 LTS · BCrypt + JWT.
- 🚀 **Engineering**: Kafka + Flink + Postgres + Spring Boot + Docker — full distributed stack live.
- 🌏 **Domain**: 4 pillar IEA/APERC + 16 sub-indicator + composite ESI có cơ sở academic.
- ✅ **End-to-end verified**: 18 Flink vertex RUNNING · 1 000 events/sec · ESI 72.82 ELEVATED live.

**Phase history** (xem [`docs/PROGRESS.md`](./PROGRESS.md)):
`0 → 1 → 2.x → 3 → 4 → 4.5 → 5.0-5.5 → 6 (split Android) → 7.0-7.5 QA → 7.7 docs final`.

**Câu hỏi mong nhận**:
- "Sao chọn Flink chứ không Spark Streaming?"
- "JavaFX có scale ra mobile được không?"
- "Khi nào nên dùng TimescaleDB thay Postgres?"
- "Backend regression Phase 7.6 đang fix gì?"

> Speaker note: kết thúc bằng "Cảm ơn thầy/cô + mời câu hỏi" — đừng quên đề cập tag `v1.0.0` để giảng viên có thể tự pull source.

---

## Phụ lục cho presenter

### Timing cheat-sheet (18 min)

| Block | Slides | Thời lượng |
|---|---|---:|
| Intro / Vấn đề | 1-3 | 2 min |
| Mục tiêu / Stack | 4-6 | 2 min 30 s |
| Domain (pillars) | 7-9 | 3 min |
| Tech (pipeline + DB + API + UI + pattern) | 10-14 | 5 min 30 s |
| Quality + Demo | 15-16 | 4 min |
| Q&A | 17 | 1 min (intro), 5+ min cho câu hỏi |

### Diagrams export checklist (trước hôm bảo vệ)

```bash
cd docs/diagrams
mmdc -i 01_dataflow.md         -o 01_dataflow.png         -w 1920 -H 1080 --scale 2
mmdc -i 02_erd.md              -o 02_erd.png              -w 1920 -H 1080 --scale 2
mmdc -i 03_class_desktop.md    -o 03_class_desktop.png    -w 1920 -H 1080 --scale 2
mmdc -i 04_pillar_framework.md -o 04_pillar_framework.png -w 1920 -H 1080 --scale 2
```

### Failure-mode handoff to demo script

Nếu live demo hỏng giữa chừng → chuyển sang Q&A slide ngay. **Backup commands** đã liệt kê trong [`docs/DEMO_SCRIPT.md`](./DEMO_SCRIPT.md) (mục "Backup commands").
