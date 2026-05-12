# 🚀 KẾ HOẠCH NÂNG CẤP ĐỀ TÀI — VES-Monitor

> **Tên đề tài mới (đề xuất):**
> **VES-Monitor — Vietnam Energy Security Real-time Monitoring & Insight Platform**
> *(Hệ thống giám sát an ninh năng lượng Việt Nam thời gian thực, có khả năng phân tích insight bằng LLM)*

> **Định vị:** Mở rộng từ project cũ (chỉ giám sát giá dầu thế giới) sang giám sát **toàn diện 4 nhóm chỉ số an ninh năng lượng**, có khả năng **tái sử dụng làm backend chung cho 2 môn**:
> - 📘 **Lập trình Java** → JavaFX Admin Desktop (3-layer + JDBC + JUnit)
> - 📱 **Phát triển ứng dụng di động (Android)** → Android app + LLM Insight

---

## MỤC LỤC

1. [Vision & Lý do nâng cấp](#1-vision--lý-do-nâng-cấp)
2. [Mô hình bài toán — 4 nhóm chỉ số An ninh năng lượng](#2-mô-hình-bài-toán--4-nhóm-chỉ-số-an-ninh-năng-lượng)
3. [Mô hình phân tán 3 cấp (Spatial × Value-chain × Entity)](#3-mô-hình-phân-tán-3-cấp)
4. [Kiến trúc tổng thể mới](#4-kiến-trúc-tổng-thể-mới)
5. [Mô hình dữ liệu (PostgreSQL Star Schema)](#5-mô-hình-dữ-liệu-postgresql-star-schema)
6. [Kafka Topics & Flink Jobs](#6-kafka-topics--flink-jobs)
7. [Spring Boot REST API (Backend chung)](#7-spring-boot-rest-api-backend-chung)
8. [JavaFX Admin Desktop (môn Java)](#8-javafx-admin-desktop-môn-java)
9. [Android App (môn Android) + LLM Insight](#9-android-app-môn-android--llm-insight)
10. [Phân công 5 thành viên](#10-phân-công-5-thành-viên)
11. [Lộ trình 10 tuần (sprint plan)](#11-lộ-trình-10-tuần-sprint-plan)
12. [Mapping yêu cầu môn Java (checklist)](#12-mapping-yêu-cầu-môn-java-checklist)
13. [Mapping cho môn Android](#13-mapping-cho-môn-android)
14. [Design Patterns áp dụng](#14-design-patterns-áp-dụng)
15. [Tiêu chí điểm cao (≥ 8.5)](#15-tiêu-chí-điểm-cao--85)
16. [Rủi ro & Phương án giảm thiểu](#16-rủi-ro--phương-án-giảm-thiểu)
17. [Phụ lục: Cấu trúc thư mục đề xuất](#17-phụ-lục-cấu-trúc-thư-mục-đề-xuất)
18. [Deployment & Demo Strategy ⭐ (chắc ăn, không cần CC)](#18-deployment--demo-strategy--chắc-ăn-không-cần-cc)
19. [Clone-and-Run Checklist (đảm bảo chạy ngay)](#19-clone-and-run-checklist-đảm-bảo-chạy-ngay)
20. [Demo Day Runbook (T-30' đến T-0)](#20-demo-day-runbook-t-30-đến-t-0)
21. [Lightweight Profile 🪶 — Chạy gọn trên laptop 8GB](#21-lightweight-profile--chạy-gọn-trên-laptop-8gb)
22. [Phân công thực tế (Leader-heavy) 👤 — Khi thành viên yếu](#22-phân-công-thực-tế-leader-heavy--khi-thành-viên-yếu)
23. [AI Execution Plan 🤖 — Lộ trình code mượt, ít lỗi (Full spec, tham khảo)](#23-ai-execution-plan--lộ-trình-code-mượt-ít-lỗi)
24. [⚡ Minimalist Execution Plan — KHUYẾN NGHỊ (đồ án gọn, tận dụng code có sẵn)](#24--minimalist-execution-plan--khuyến-nghị-đồ-án-gọn-tận-dụng-code-có-sẵn)

---

## 1. Vision & Lý do nâng cấp

### 1.1 Vấn đề của project hiện tại
- **Scope hẹp:** chỉ theo dõi giá 5 loại dầu × 6 sàn quốc tế → không bám sát chủ đề "An ninh năng lượng Việt Nam".
- **Thiếu UI Java thuần:** đồ án Java yêu cầu Swing/JavaFX + JDBC + 3-layer + Login + CRUD — hiện tại chưa có.
- **Không expose API:** Android không thể dùng project này làm backend.
- **Thiếu chiều phân tán:** dữ liệu chưa theo địa phương Việt Nam (3 miền, các trung tâm năng lượng).

### 1.2 Mục tiêu nâng cấp
| # | Mục tiêu | Output |
|---|----------|--------|
| 1 | Mở rộng domain từ "giá dầu" → "an ninh năng lượng" với 4 nhóm chỉ số | 5 dòng dữ liệu, 5 Kafka topics |
| 2 | Thêm chiều phân tán địa lý Việt Nam | 3 miền × 6 cụm năng lượng × ~30 facility |
| 3 | Bổ sung tầng Spring Boot REST API | Backend chung cho desktop + Android |
| 4 | Bổ sung JavaFX Admin Desktop | Thỏa mãn 100% checklist đồ án Java |
| 5 | Bổ sung Android app + LLM Insight | Tái sử dụng cho đồ án Android |
| 6 | Tính được **Energy Security Index (ESI)** | Chỉ số tổng hợp 0–100 |
| 7 | **Deploy "clone-and-run" 3 lệnh** trên máy bất kỳ thành viên + tunnel free | docker-compose + cloudflared + script bootstrap |

### 1.3 Triết lý thiết kế
- **"Không quá phức tạp":** giữ nguyên 80% project hiện có làm nền, chỉ **thêm chứ không thay**.
- **"Đủ để điểm cao":** thỏa mãn từng gạch đầu dòng trong hướng dẫn đồ án (phụ lục §12).
- **"Tái sử dụng":** Spring Boot là điểm chung — desktop & Android cùng gọi chung API.
- **"Chắc ăn & dễ deploy":** mọi thành viên `git clone && ./run.sh` là chạy được. Không cần credit card, không cần cloud trả phí, không phụ thuộc tài khoản cá nhân. Chi tiết §18-20.

---

## 2. Mô hình bài toán — 4 nhóm chỉ số An ninh năng lượng

> Mỗi nhóm chỉ số sẽ trở thành **một dòng dữ liệu (data stream)** trong hệ thống.

| Nhóm | Tên KPI | Đơn vị | Nguồn data (mock) | Tần suất |
|------|---------|--------|-------------------|----------|
| **1. Nguồn cung & Hạ tầng** | Sản lượng khai thác than/khí | kT, MMscm | Mỏ Quảng Ninh, cụm khí Vũng Tàu | 1 phút |
| | Tồn kho dự trữ xăng dầu | ngày nhập ròng | Kho Nhà Bè, Đình Vũ, Vân Phong | 5 phút |
| | Tình trạng trạm biến áp 500kV | OK/WARN/FAULT | 8 trạm trục Bắc–Nam | event-driven |
| **2. Biến động kinh tế** | Giá dầu thế giới WTI/Brent | USD/barrel | Producer cũ (giữ nguyên) | 10 giây |
| | Tỷ trọng nhập khẩu theo nguồn | % | Tính trong Flink | rolling 1h |
| **3. Phụ tải & Tiêu thụ** | Phụ tải điện giờ | MW | 6 trung tâm phụ tải (HN, HCM, KCN Bắc Ninh, Bình Dương, Hải Phòng, Đà Nẵng) | 1 phút |
| | Hiệu suất năng lượng | kWh/USD GDP | Tính derived | rolling 1h |
| **4. Chuyển đổi & Môi trường** | Sản lượng điện tái tạo | MW | Solar Ninh Thuận, Wind Bạc Liêu | 1 phút |
| | Phát thải CO₂ | kgCO₂/MWh | Tính từ mix nguồn | window 1 phút |

### 2.1 Energy Security Index (ESI) — sáng tạo cốt lõi
> **ESI = 0..100** tổng hợp từ 4 nhóm theo trọng số:
>
> ```
> ESI = 0.35 × Supply_Score      (nhóm 1)
>     + 0.25 × Economic_Score    (nhóm 2)
>     + 0.25 × Load_Score        (nhóm 3)
>     + 0.15 × Green_Score       (nhóm 4)
> ```
>
> Tính real-time trong Flink job riêng (`EnergySecurityIndexJob`) — đây là **đặc trưng học thuật** nâng đề tài lên cấp "có thuật toán độc lập" theo yêu cầu Chương 5 báo cáo.

---

## 3. Mô hình phân tán 3 cấp

### 3.1 Spatial Distribution (theo địa lý Việt Nam)
```
┌─────────── MIỀN BẮC ───────────┬────── MIỀN TRUNG ──────┬────────── MIỀN NAM ──────────┐
│  • Mỏ than Quảng Ninh (supply) │  • Trạm 500kV Đà Nẵng │  • Cụm khí Vũng Tàu (supply) │
│  • Trạm 500kV Hà Tĩnh          │  • Wind Quảng Trị     │  • Kho Nhà Bè (storage)      │
│  • Phụ tải Hà Nội (demand)     │  • Phụ tải Đà Nẵng    │  • Solar Ninh Thuận (green)  │
│  • KCN Bắc Ninh (demand)       │                       │  • Phụ tải HCM (demand)      │
│  • Kho Đình Vũ (storage)       │                       │  • KCN Bình Dương (demand)   │
└────────────────────────────────┴───────────────────────┴──────────────────────────────┘
```
→ Kafka **partition theo `region_code`**: `NORTH=0`, `CENTRAL=1`, `SOUTH=2`.

### 3.2 Value Chain Distribution
| Tầng | Mock entity | KPI chính |
|------|-------------|-----------|
| **Upstream** (khai thác) | Mỏ Cẩm Phả, giàn khoan Bạch Hổ | Sản lượng, downtime |
| **Midstream** (vận chuyển/lưu trữ) | Đường ống PM3, kho Nhà Bè | Áp suất, tồn kho |
| **Downstream** (phân phối) | Trạm biến áp 500kV, phụ tải KCN | Điện áp, tần số, MW |

→ Flink dùng **`keyBy(facility_id)`** để xử lý song song theo từng facility, **`keyBy(region)`** để rollup theo vùng.

### 3.3 Entity Distribution (loại nguồn dữ liệu)
| Entity type | Producer mô phỏng | Format |
|-------------|-------------------|--------|
| IoT/SCADA Sensor | `ScadaSensorGenerator` | JSON, push 1/phút |
| Sàn giao dịch quốc tế | `FuelPriceProducer` (giữ nguyên) | JSON, 10s |
| Trạm khí tượng | `WeatherStationGenerator` | JSON (gió/nắng → predict renewable) | 5 phút |
| Catalog (master data) | Spring Boot CRUD (do Admin nhập) | RESTful |

---

## 4. Kiến trúc tổng thể mới

```
╔══════════════════════════════════════════════════════════════════════════════╗
║                          VES-MONITOR ARCHITECTURE                            ║
╠══════════════════════════════════════════════════════════════════════════════╣
║                                                                              ║
║   ┌──────────────────── DATA GENERATION LAYER ─────────────────────────┐    ║
║   │  ScadaSensorGen │ FuelPriceGen │ GridLoadGen │ WeatherGen │ ...    │    ║
║   └─────────────────────────────┬─────────────────────────────────────┘    ║
║                                  ▼                                           ║
║   ┌──────────────────────── KAFKA CLUSTER ─────────────────────────────┐    ║
║   │  topic_fuel_price │ topic_grid_load │ topic_renewable │            │    ║
║   │  topic_inventory  │ topic_emission  │ (partition by region_code)   │    ║
║   └─────────────────────────────┬─────────────────────────────────────┘    ║
║                                  ▼                                           ║
║   ┌─────────────────────────── FLINK ──────────────────────────────────┐    ║
║   │  • RawSinkJob          → fact_*_raw                                │    ║
║   │  • WindowAggJob (1m)   → agg_kpi_1min                              │    ║
║   │  • AlertDetectionJob   → alerts                                    │    ║
║   │  • EnrichmentJob       → join with dim_facility                    │    ║
║   │  • ESICalculatorJob    → energy_security_index (1 phút)           │    ║
║   └─────────────────────────────┬─────────────────────────────────────┘    ║
║                                  ▼                                           ║
║   ┌────────────────────── POSTGRESQL (DW) ─────────────────────────────┐    ║
║   │  Dim:  region, facility, fuel_type, alert_rule, users              │    ║
║   │  Fact: fuel_price_raw, grid_load_raw, renewable_raw, inventory_raw │    ║
║   │  Agg:  agg_kpi_1min, agg_kpi_1hour, esi_score                      │    ║
║   │  Views: v_esi, v_supply_demand_balance, v_carbon_intensity, ...    │    ║
║   └─────────────────────────────┬─────────────────────────────────────┘    ║
║                                  ▼                                           ║
║   ┌─────────────── SPRING BOOT REST API (Port 8080) ──────────────────┐    ║
║   │  • /api/auth         (JWT, 3 role)                                 │    ║
║   │  • /api/regions      (CRUD)                                        │    ║
║   │  • /api/facilities   (CRUD + search)                               │    ║
║   │  • /api/alert-rules  (CRUD)                                        │    ║
║   │  • /api/users        (CRUD, admin only)                            │    ║
║   │  • /api/kpi/*        (read aggregated data)                        │    ║
║   │  • /api/alerts       (list + acknowledge)                          │    ║
║   │  • /api/reports      (PDF/Excel export)                            │    ║
║   │  • /api/stream/kpi   (SSE — Server Sent Events real-time)          │    ║
║   │  • /ws/alerts        (WebSocket push)                              │    ║
║   └────────────┬──────────────────────────────────┬───────────────────┘    ║
║                │                                   │                         ║
║                ▼                                   ▼                         ║
║   ┌──────────────────────────┐       ┌──────────────────────────────┐      ║
║   │  JAVAFX ADMIN DESKTOP    │       │      ANDROID APP             │      ║
║   │  (Môn Lập trình Java)    │       │  (Môn Android Studio)        │      ║
║   ├──────────────────────────┤       ├──────────────────────────────┤      ║
║   │  • Login + 3 role        │       │  • Login (JWT)               │      ║
║   │  • Dashboard charts (SSE)│       │  • Map view facilities       │      ║
║   │  • CRUD 5 entity (JDBC) │       │  • 4 KPI cards real-time     │      ║
║   │  • Alert manager         │       │  • Alert list + FCM push     │      ║
║   │  • Report PDF/Excel      │       │  • LLM Insight (Gemini API)  │      ║
║   │  • Settings, profile     │       │  • Profile, settings         │      ║
║   └──────────────────────────┘       └──────────────────────────────┘      ║
║                                                                              ║
║   ┌──────── METABASE (giữ nguyên, BI nội bộ - optional) ───────────────┐   ║
║   │  Auto-refresh dashboards, embed link gửi sếp                       │   ║
║   └────────────────────────────────────────────────────────────────────┘   ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

> **Lưu ý**: Spring Boot **không đọc trực tiếp từ Kafka** mà đọc từ Postgres (đã được Flink ghi vào). Điều này giữ ranh giới rõ ràng: Flink chịu trách nhiệm streaming + analytics, Spring Boot chỉ là **serving layer (RESTful read/write)**. Đơn giản hóa rất nhiều cho team.

---

## 5. Mô hình dữ liệu (PostgreSQL Star Schema)

### 5.1 Dimension tables (Admin CRUD qua JavaFX/Android)
```sql
-- Vùng địa lý Việt Nam
CREATE TABLE dim_region (
  region_id    SERIAL PRIMARY KEY,
  code         VARCHAR(20) UNIQUE NOT NULL,   -- NORTH, CENTRAL, SOUTH, HN, HCM, NINH_THUAN...
  name         VARCHAR(100) NOT NULL,
  parent_code  VARCHAR(20),                   -- nested region
  latitude     DECIMAL(9,6),
  longitude    DECIMAL(9,6),
  area_km2     INTEGER
);

-- Cơ sở năng lượng (mỏ, kho, trạm biến áp, NMD tái tạo, KCN, hộ tiêu thụ)
CREATE TABLE dim_facility (
  facility_id        SERIAL PRIMARY KEY,
  code               VARCHAR(50) UNIQUE NOT NULL,
  name               VARCHAR(150) NOT NULL,
  facility_type      VARCHAR(30) NOT NULL,    -- MINE, GAS_FIELD, REFINERY, STORAGE, SUBSTATION, SOLAR_FARM, WIND_FARM, IND_PARK, CITY
  value_chain_stage  VARCHAR(20) NOT NULL,    -- UPSTREAM, MIDSTREAM, DOWNSTREAM
  region_id          INTEGER REFERENCES dim_region(region_id),
  capacity_value     DECIMAL(12,2),
  capacity_unit      VARCHAR(20),             -- MW, kT, m3, days
  latitude           DECIMAL(9,6),
  longitude          DECIMAL(9,6),
  status             VARCHAR(20) DEFAULT 'ACTIVE',
  commissioned_date  DATE,
  updated_at         TIMESTAMP DEFAULT NOW()
);

-- Loại nhiên liệu / nguồn năng lượng
CREATE TABLE dim_fuel_type (
  code           VARCHAR(30) PRIMARY KEY,
  name           VARCHAR(80) NOT NULL,
  category       VARCHAR(30),                 -- FOSSIL, RENEWABLE, NUCLEAR
  unit           VARCHAR(20),                 -- USD/barrel, MW, kgCO2/MWh
  emission_factor DECIMAL(8,4)
);

-- Quy tắc cảnh báo (Admin cấu hình)
CREATE TABLE dim_alert_rule (
  rule_id      SERIAL PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  kpi_code     VARCHAR(50) NOT NULL,
  scope_region VARCHAR(20),                   -- null = all
  comparator   VARCHAR(5) NOT NULL,           -- '>', '<', '>=', '<='
  threshold    DECIMAL(12,4) NOT NULL,
  severity     VARCHAR(10) NOT NULL,          -- LOW, MEDIUM, HIGH, CRITICAL
  enabled      BOOLEAN DEFAULT TRUE,
  created_by   INTEGER,
  updated_at   TIMESTAMP DEFAULT NOW()
);

-- User + phân quyền 3 role
CREATE TABLE users (
  user_id       SERIAL PRIMARY KEY,
  username      VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(100) NOT NULL,        -- BCrypt
  full_name     VARCHAR(100),
  email         VARCHAR(100),
  role          VARCHAR(20) NOT NULL,         -- ADMIN, MANAGER, VIEWER
  active        BOOLEAN DEFAULT TRUE,
  last_login    TIMESTAMP,
  created_at    TIMESTAMP DEFAULT NOW()
);
```

### 5.2 Fact tables (Flink ghi)
```sql
CREATE TABLE fact_fuel_price_raw (...);       -- giữ từ project cũ
CREATE TABLE fact_grid_load_raw (
  id BIGSERIAL PRIMARY KEY,
  event_ts TIMESTAMP NOT NULL,
  facility_id INTEGER REFERENCES dim_facility,
  region_code VARCHAR(20),
  load_mw DECIMAL(12,2),
  voltage_kv DECIMAL(8,2),
  frequency_hz DECIMAL(5,2),
  ingested_at TIMESTAMP DEFAULT NOW()
);
CREATE TABLE fact_renewable_output_raw (...); -- gió + mặt trời
CREATE TABLE fact_fuel_inventory_raw (...);   -- tồn kho theo ngày
CREATE TABLE fact_emission_raw (...);         -- kgCO2/MWh
```

### 5.3 Aggregate & derived
```sql
CREATE TABLE agg_kpi_1min (
  window_start TIMESTAMP, window_end TIMESTAMP,
  kpi_code VARCHAR(50), region_code VARCHAR(20), facility_id INTEGER,
  avg_value DECIMAL, min_value DECIMAL, max_value DECIMAL, sum_value DECIMAL,
  record_count INTEGER,
  PRIMARY KEY (window_start, kpi_code, region_code, facility_id)
);

CREATE TABLE esi_score (
  ts TIMESTAMP PRIMARY KEY,
  supply_score   DECIMAL(5,2),
  economic_score DECIMAL(5,2),
  load_score     DECIMAL(5,2),
  green_score    DECIMAL(5,2),
  esi_total      DECIMAL(5,2)
);

CREATE TABLE alerts (
  alert_id BIGSERIAL PRIMARY KEY,
  alert_ts TIMESTAMP NOT NULL,
  rule_id INTEGER REFERENCES dim_alert_rule,
  facility_id INTEGER,
  region_code VARCHAR(20),
  kpi_code VARCHAR(50),
  current_value DECIMAL(12,4),
  threshold DECIMAL(12,4),
  severity VARCHAR(10),
  message TEXT,
  acknowledged BOOLEAN DEFAULT FALSE,
  acknowledged_by INTEGER REFERENCES users,
  acknowledged_at TIMESTAMP
);
```

### 5.4 Views chính
- `v_esi_now` — ESI hiện tại + breakdown 4 thành phần
- `v_supply_demand_balance` — cung-cầu theo vùng (MW)
- `v_renewable_penetration` — % tái tạo trên tổng phụ tải
- `v_strategic_reserve_days` — số ngày dự trữ xăng dầu
- `v_carbon_intensity_now` — kgCO₂/MWh hiện tại
- `v_regional_kpi_snapshot` — snapshot KPI theo region (dùng cho map view trên Android)

---

## 6. Kafka Topics & Flink Jobs

### 6.1 Topics mới (giữ topic cũ `fuel-prices`)
| Topic | Partition by | Generator |
|-------|--------------|-----------|
| `fuel-prices` | location | `FuelPriceProducer` (cũ) |
| `grid-load` | region_code (3 part) | `GridLoadGenerator` |
| `renewable-output` | region_code | `RenewableOutputGenerator` |
| `fuel-inventory` | facility_id | `InventoryGenerator` |
| `emission` | region_code | `EmissionGenerator` |

### 6.2 Flink Jobs (tách module trong cùng 1 application)
| Job | Logic | Output |
|-----|-------|--------|
| `RawSinkJob` | Đọc tất cả topic → ghi raw vào fact tables | 5 bảng fact |
| `WindowAggregationJob` | 1-phút tumbling window theo `(region, kpi)` & `(facility, kpi)` | `agg_kpi_1min` |
| `AlertDetectionJob` | Đọc `dim_alert_rule` (broadcast state) → compare với KPI → emit alert | `alerts` |
| `EnrichmentJob` | Join sensor stream với `dim_facility` lấy lat/lon + region | enriched stream |
| `ESICalculatorJob` | Pha trộn 4 luồng KPI → tính ESI score 0–100 | `esi_score` |

> **Mẹo:** ESICalculatorJob có thể dùng **Flink Table API** + windowed join để đơn giản hóa code.

---

## 7. Spring Boot REST API (Backend chung)

### 7.1 Stack
- Spring Boot 3.2 (Java 17 — nâng từ 11)
- Spring Web, Spring Security 6 (JWT), Spring Data JPA + Hibernate
- PostgreSQL driver, Lombok, MapStruct, Springdoc OpenAPI (Swagger UI)
- iText 7 + Apache POI (export PDF/Excel)
- Spring WebFlux/SSE cho streaming
- WebSocket cho push alert

### 7.2 Cấu trúc package
```
org.ves.api
├── config         (SecurityConfig, JwtFilter, CorsConfig, OpenApiConfig)
├── controller     (AuthCtrl, RegionCtrl, FacilityCtrl, KpiCtrl, AlertCtrl, ReportCtrl)
├── service        (interface + impl)
├── repository     (Spring Data JPA)
├── entity         (JPA @Entity)
├── dto            (Request/Response DTO + MapStruct mapper)
├── exception      (GlobalExceptionHandler)
├── security       (JwtTokenProvider, UserDetailsServiceImpl)
└── stream         (KpiSseEmitter, AlertWebSocketHandler)
```

### 7.3 Danh sách endpoint chính
| Method | Endpoint | Role | Mô tả |
|--------|----------|------|-------|
| POST | `/api/auth/login` | ALL | Login → JWT |
| POST | `/api/auth/register` | ADMIN | Tạo user |
| GET | `/api/regions` | ALL | List vùng |
| POST/PUT/DELETE | `/api/regions/{id}` | ADMIN | CRUD |
| GET | `/api/facilities?type=&region=` | ALL | Search + filter |
| POST/PUT/DELETE | `/api/facilities/{id}` | MANAGER+ | CRUD |
| GET | `/api/alert-rules` | MANAGER+ | List |
| POST/PUT/DELETE | `/api/alert-rules/{id}` | ADMIN | CRUD |
| GET | `/api/kpi/overview` | ALL | Snapshot 4 nhóm |
| GET | `/api/kpi/esi` | ALL | ESI hiện tại |
| GET | `/api/kpi/{kpi}?region=&from=&to=` | ALL | Time-series |
| GET | `/api/alerts?severity=&ack=` | ALL | List alerts |
| POST | `/api/alerts/{id}/acknowledge` | MANAGER+ | Confirm alert |
| GET | `/api/reports/generate?type=PDF&...` | MANAGER+ | Export PDF/Excel |
| GET | `/api/stream/kpi` | ALL | SSE real-time |
| WS | `/ws/alerts` | ALL | WebSocket alert push |

### 7.4 Bảo mật
- BCrypt cho password
- JWT (HS256) — token expire 1h, refresh 7 ngày
- Spring Security `@PreAuthorize("hasRole('ADMIN')")`
- CORS whitelist (cho phép JavaFX desktop + Android)
- Validation: `@Valid` + `@NotNull/@Size/@Pattern` trên DTO
- Rate limit: Bucket4j (optional)

---

## 8. JavaFX Admin Desktop (môn Java)

### 8.1 Tại sao JavaFX (không phải Swing)?
- JavaFX **được hướng dẫn cho phép** (Swing/JavaFX) và hỗ trợ FXML, CSS, charts đẹp hơn.
- Có sẵn `LineChart`, `AreaChart`, `PieChart` → khớp với bài toán dashboard.

### 8.2 Stack
- Java 17 + JavaFX 21 + Maven
- Direct JDBC (để **bằng chứng dùng JDBC** trong checklist)
- **Hoặc** gọi REST qua `HttpClient` (Java 11+) → dùng JSON-B/Jackson
- Bcrypt-jbcrypt cho hash (nếu CRUD trực tiếp DB)
- Hibernate Validator (jakarta.validation)
- iText 7 (PDF), Apache POI (Excel)
- JUnit 5 + Mockito + AssertJ
- ControlsFX (notification, dialog đẹp)

### 8.3 Kiến trúc 3-layer (đúng yêu cầu)
```
src/main/java/org/ves/desktop/
├── Application.java                  ← entry
├── presentation/                     ← LAYER 1: UI
│   ├── controller/                   (FXML controllers)
│   │   ├── LoginController.java
│   │   ├── DashboardController.java
│   │   ├── FacilityController.java
│   │   ├── AlertRuleController.java
│   │   ├── UserController.java
│   │   ├── ReportController.java
│   │   └── ProfileController.java
│   ├── view/                         (.fxml files)
│   └── util/                         (FxmlLoader, AlertDialog)
├── service/                          ← LAYER 2: Business
│   ├── AuthService.java + Impl
│   ├── FacilityService.java + Impl
│   ├── KpiService.java + Impl        (gọi REST API)
│   ├── AlertService.java + Impl
│   ├── ReportService.java + Impl     (PDF/Excel)
│   └── ValidationService.java
├── dao/                              ← LAYER 3: Data Access (JDBC)
│   ├── DatabaseConnection.java       (Singleton)
│   ├── BaseDao.java                  (Generic)
│   ├── UserDao.java + Impl
│   ├── FacilityDao.java + Impl
│   ├── RegionDao.java + Impl
│   └── AlertRuleDao.java + Impl
├── model/                            (POJO entity)
└── util/
    ├── PasswordUtils.java            (BCrypt)
    ├── JwtClient.java                (gọi /api/auth)
    ├── SseClient.java                (real-time KPI)
    ├── Validator.java
    ├── ChartHelper.java
    └── ConfigLoader.java
```

### 8.4 Tính năng (mapping checklist)
| Tính năng | UI | Service | DAO |
|-----------|-----|---------|-----|
| Đăng nhập + lưu JWT | LoginCtrl | AuthService | UserDao (BCrypt verify) |
| 3 role: ADMIN/MANAGER/VIEWER | Menu filter | RoleGuard | — |
| Dashboard 4 KPI + chart real-time | DashboardCtrl + SSE | KpiService | (REST) |
| CRUD Facility (search đa tiêu chí) | FacilityCtrl + TableView | FacilityService | FacilityDao |
| CRUD Alert Rule | AlertRuleCtrl | AlertService | AlertRuleDao |
| CRUD User (chỉ ADMIN) | UserCtrl | AuthService | UserDao |
| Acknowledge Alert | AlertListCtrl | AlertService | AlertDao |
| Export PDF (báo cáo ngày) | ReportCtrl | ReportService (iText) | — |
| Export Excel | ReportCtrl | ReportService (POI) | — |
| Profile (đổi mật khẩu) | ProfileCtrl | AuthService | UserDao |
| Validation form | tất cả Ctrl | Validator | — |
| Anti SQL injection | — | — | dùng `PreparedStatement` |
| JUnit test ≥ 30 cases | — | test service | test DAO với H2 |

### 8.5 Charts trong Dashboard
- **LineChart** — phụ tải MW theo giờ (6 vùng, multi-series)
- **AreaChart** — sản lượng tái tạo
- **PieChart** — tỷ trọng nguồn năng lượng (than/khí/dầu/tái tạo)
- **Gauge (ControlsFX)** — ESI score real-time

---

## 9. Android App (môn Android) + LLM Insight

### 9.1 Stack
- Android Studio Iguana, Java 17 (hoặc Kotlin nếu nhóm muốn)
- Retrofit 2 + OkHttp (REST)
- OkHttp-SSE / Retrofit Streaming (real-time)
- Google Maps SDK
- MPAndroidChart
- Firebase Cloud Messaging (push alert)
- Material Design 3
- **Google Gemini API** hoặc **OpenAI API** cho LLM Insight

### 9.2 Màn hình chính
1. **Login** — JWT.
2. **Home Dashboard** — 4 KPI cards (Supply / Economic / Load / Green) + ESI gauge.
3. **Map View** — Google Map hiển thị toàn bộ facility, marker đổi màu theo trạng thái.
4. **Region Detail** — chọn 1 vùng → xem chart + danh sách facility.
5. **Alerts** — list, filter severity, mark as read.
6. **LLM Insight Tab** ⭐:
   - User chọn khoảng thời gian + nhóm KPI.
   - App gọi `/api/kpi/overview` → lấy JSON snapshot.
   - Gửi snapshot + system prompt sang Gemini API:
     ```
     "Bạn là chuyên gia an ninh năng lượng. Phân tích snapshot KPI dưới đây
      và đưa ra: (1) đánh giá ESI, (2) 3 rủi ro nổi bật, (3) khuyến nghị
      ngắn hạn cho Bộ Công Thương. Trả lời tiếng Việt, có heading."
     ```
   - Hiển thị markdown response.
   - Lưu lịch sử phân tích vào Room database.
7. **Settings & Profile**.

### 9.3 Vì sao LLM Insight là "đặc trưng đột phá"?
- **Khác biệt với 99% đồ án Android** (CRUD đơn thuần).
- Demo cực ấn tượng: real data + AI analysis live.
- Phù hợp xu hướng AI 2026 → điểm thực tế cao.

---

## 10. Phân công 5 thành viên

> ⚠️ **Đây là phân công LÝ TƯỞNG** (giả sử mọi thành viên đều có kỹ năng tương đương). Nếu thực tế có thành viên yếu — **xem §22 Phân công thực tế (Leader-heavy)** đảm bảo project vẫn về đích.

| Vai trò | Người | Phụ trách chính | Deliverable |
|---------|-------|----------------|-------------|
| **Leader** | A | Spring Boot REST API + ESI Job + Integration + Report | OpenAPI doc, JWT auth, ESI score table |
| **Data Engineer** | B | 5 data generator + 5 Kafka topic + 5 Flink job | 5 JAR producer, Flink uber-JAR |
| **DevOps & DB** | C | Docker compose v2 + PostgreSQL schema + index tuning + Metabase | docker-compose.yml, init.sql, seed.sql, 5 Metabase dashboards |
| **Java Desktop** | D | JavaFX Admin app (presentation + service + dao + JUnit) | .jar + screenshot 6 màn hình + test report |
| **Android** | E | Android app + LLM integration + FCM | .apk + Postman collection + demo video |

> **Phân công cùng người làm Java desktop + Android có thể trùng** nếu nhóm chỉ 5 người mà cùng làm 2 môn. Khi đó **D = E**.

### 10.1 Trách nhiệm chung (mọi người)
- Tham gia review PR ít nhất **2 PR/tuần** của thành viên khác.
- Tự viết **JUnit test** cho phần mình làm.
- Update **trang Notion / GitHub Pages** mỗi cuối tuần (yêu cầu §1 trong file hướng dẫn).
- Ghi nhật ký AI usage (yêu cầu §5).

---

## 11. Lộ trình 10 tuần (sprint plan)

> ⚠️ **Phiên bản này là Leader-heavy** (đồng bộ với §22). Team thực tế = Leader + 2 thành viên (B Doc/Media + C Data/QA). Mỗi thành viên 5 PR.

| Tuần | Mục tiêu chung | Leader A (~80%) | Người B — Doc/Media | Người C — Data/QA |
|------|---------------|------------------|----------------------|-------------------|
| 1 | Setup & Foundation | Repo + Spring Initializr skeleton + Docker compose Lite | — (chờ Leader xong skeleton) | — (chờ Leader có schema) |
| 2 | Skeleton chạy được | Spring Boot Login JWT + 1 CRUD; PostgreSQL schema mở rộng | **PR1: Vẽ ERD + Use-case + Architecture diagram** | — |
| 3 | **Báo cáo tiến độ 1 (Lab 3)** | 4 CRUD entity (Region/Facility/AlertRule/User) + 1 generator | (Leader dùng diagram của B cho slide) | **PR1: Seed data VN (30+ facility, 6 region, 10 rule)** |
| 4 | Generators + JavaFX | 3 generators + JavaFX Login + Dashboard skeleton | **PR2: Viết Chương 1 + 2 báo cáo** | — |
| 5 | **MVP working** | Flink RawSink + WindowAgg; JavaFX 3 CRUD; Android Home + KPI cards | — | **PR2: Postman collection** |
| 6 | Alert + Android | Flink AlertDetection; Spring Boot Alert endpoints; Android Map + Alert list | — | **PR3: Manual test report (API + JavaFX + Android)** |
| 7 | ESI + Report + LLM | Flink ESICalculator; Spring Boot Report PDF/Excel; Android LLM Insight (Gemini) | — | **PR5: Thêm màn About + Settings JavaFX (copy template)** |
| 8 | Polish + Integration | JavaFX final + Android final + cloudflared tunnel guide | **PR3: Viết Chương 4 + 6 báo cáo** | **PR4: Screenshot bộ 6 JavaFX + 5 Android** |
| 9 | Deploy + Test sạch | Pre-build JARs/APK release + test trên máy sạch + E2E test | **PR4: Tạo slide PowerPoint 20-25 slide** | (Hỗ trợ test lại sau bug fix) |
| 10 | **Demo & Báo cáo** | Diễn tập demo §20 + fix bug cuối + tunnel demo | **PR5: Video demo 5-7 phút + Notion page** | (Hỗ trợ chạy stack khi demo) |

### 11.1 Quy tắc đường găng (CRITICAL PATH)

**Đường găng dự án** (chỉ Leader, không trễ được):
```
Tuần 1: Foundation ─► Tuần 2: Skeleton ─► Tuần 5: MVP ─► Tuần 9: Deploy ─► Tuần 10: Demo
        (Leader)         (Leader)            (Leader)        (Leader)         (cả nhóm)
```

**Task thành viên KHÔNG nằm trên đường găng** — nếu trễ vẫn không chết dự án:
- Người B trễ doc → Leader có thể viết thay trong tuần 9 buffer.
- Người C trễ seed data → Leader dùng seed mẫu trong code Java (in-memory) tạm.

### 11.2 Báo cáo tiến độ 1 (Lab 3 — Tuần 3)

Theo yêu cầu §6 file hướng dẫn ĐA. Cần show:
- ✅ ERD + Use-case + Mockup (Người B)
- ✅ Login + 1 CRUD chạy được (Leader)
- ✅ Repo + Projects board (Người E)
- ✅ Plan timeline chi tiết
- ✅ Slide tiến độ 10-15 slide (Người E)
- ❌ Không cần slide chuyên nghiệp, không cần video

### 11.3 Buffer cuối kỳ

- **Tuần 9 = buffer week** — nếu bất kỳ ai trễ → tuần này hốt sạch. Leader đặt sẵn buffer này, không "vắt" feature mới vào.
- **Tuần 10 chỉ làm 2 việc**: (1) báo cáo & slide & video, (2) diễn tập demo. KHÔNG code feature mới.

---

## 12. Mapping yêu cầu môn Java (checklist)

| Yêu cầu trong [`JAVA_FINAL_PROJECT_REQUIREMENT.md`](./JAVA_FINAL_PROJECT_REQUIREMENT.md) | Thỏa mãn ở đâu | Status |
|---|---|---|
| Java Core OOP | Toàn bộ JavaFX + Spring Boot | ✅ |
| Swing/JavaFX | JavaFX Admin Desktop §8 | ✅ |
| JDBC | DAO layer trong desktop (§8.3) | ✅ |
| Maven (hoặc Gradle) | pom.xml multi-module | ✅ |
| Kiến trúc 3-layer / MVC | `presentation / service / dao` + FXML MVC | ✅ |
| Đăng ký / Đăng nhập | LoginCtrl + AuthService + BCrypt | ✅ |
| Phân quyền 3 role | ADMIN / MANAGER / VIEWER (§5.1) | ✅ |
| CRUD (Thêm/Xóa/Sửa/Đọc) | 5 entity: Region/Facility/AlertRule/User/FuelType | ✅ |
| Tìm kiếm đa tiêu chí | Filter form: type + region + status (Facility) | ✅ |
| Thống kê + xuất báo cáo | iText PDF + POI Excel (§8.4) | ✅ |
| Validation dữ liệu | Hibernate Validator + Validator.java | ✅ |
| Chống tấn công (SQLi, XSS) | `PreparedStatement`, BCrypt, JWT, CORS, input sanitization | ✅ |
| JUnit | ≥ 30 test cases service + DAO (H2 in-memory) | ✅ |
| Design Patterns | 5+ patterns (§14) | ✅ |
| Sáng tạo thực tế | Kafka/Flink streaming + ESI algorithm + LLM insight | ⭐ |
| Chương 5 (thuật toán) | ESI calculation (§2.1) | ✅ |
| Phụ lục Nhật ký AI | `AI_USAGE_LOG.md` ghi prompt + verify | ✅ |
| Trang giới thiệu nhóm | Notion / GitHub Pages | 📌 nhớ làm |
| Repo + PR + Code review | GitHub workflow | 📌 đặt rule branch protection |
| Tỷ lệ tham khảo < 15% | Tự code 85%+ | 📌 chú ý copy-paste |
| Video demo | 5–7 phút | 📌 tuần 10 |
| README.md cài đặt | Đã có sẵn, update phần mới | 📌 tuần 10 |

> **Tự tin:** Sau khi triển khai đầy đủ, đề tài thỏa mãn **gần 100% checklist môn Java**, vượt mức điểm 8.5 mà file hướng dẫn yêu cầu cho điểm cộng Leader.

---

## 13. Mapping cho môn Android

| Yêu cầu Android điển hình | Thỏa mãn ở đâu |
|---|---|
| Authentication | Login JWT (§9.2 màn 1) |
| Local DB | Room (lưu lịch sử LLM insight) |
| Remote API | Retrofit + OkHttp (gọi Spring Boot) |
| Real-time | SSE / WebSocket cho alert + KPI |
| Map | Google Maps SDK (§9.2 màn 3) |
| Charts | MPAndroidChart (§9.2 màn 2, 4) |
| Push notification | Firebase Cloud Messaging |
| Material Design | M3 components |
| **Sáng tạo** | **LLM Insight tab** (§9.3) — đặc trưng khác biệt |
| Permissions | Internet, Location (optional), Notification |
| Offline cache | Room cho KPI snapshot cuối |

→ Android app **dùng chung backend Spring Boot** — không phải làm lại logic — tiết kiệm công sức.

---

## 14. Design Patterns áp dụng

Trong file hướng dẫn yêu cầu "áp dụng các mẫu thiết kế". Đề xuất 5+ patterns:

| Pattern | Áp dụng tại |
|---------|------------|
| **Singleton** | `DatabaseConnection`, `ConfigLoader` (JavaFX); `JwtTokenProvider` (Spring) |
| **Factory** | `DaoFactory` tạo các DAO; `GeneratorFactory` chọn mock/real data |
| **Strategy** | `AlertRuleStrategy` (so sánh `>`, `<`, `>=`, `<=`); `ReportStrategy` (PDF vs Excel) |
| **Observer** | SSE client trong JavaFX nhận event từ server; FX `Property` binding |
| **Builder** | Đã dùng Lombok `@Builder` cho DTO; Flink job builder |
| **Repository** | Spring Data JPA repository + DAO layer JavaFX |
| **DTO / Mapper** | MapStruct trong Spring Boot |
| **Template Method** | `BaseDao<T>` generic CRUD |
| **Decorator** | JWT filter chain trong Spring Security |

→ **Chương 5 báo cáo** mô tả ít nhất 5 patterns + ví dụ code → đủ "điểm sáng tạo".

---

## 15. Tiêu chí điểm cao (≥ 8.5)

1. **Kiến trúc rõ ràng:** Sơ đồ §4 dán vào báo cáo Chương 5.
2. **Demo đầy đủ end-to-end:** producer → kafka → flink → postgres → API → JavaFX + Android (video 5–7 phút).
3. **Sáng tạo:** ESI algorithm + LLM Insight + multi-region distributed monitoring.
4. **Quản lý dự án chuyên nghiệp:**
   - GitHub repo public
   - Branch protection, PR template, code owner
   - Trello/Notion board công khai
   - Daily commit (tuần ≥ 5 commit/người)
5. **Tài liệu đầy đủ:**
   - README hướng dẫn cài đặt
   - Báo cáo PDF (Times New Roman 13, 1.5 line)
   - Slide PowerPoint
   - Video demo
   - Postman collection / Swagger UI link
   - Phụ lục Nhật ký AI (`AI_USAGE_LOG.md`)
6. **Trích dẫn chuẩn:** APA/IEEE — đặc biệt khi nêu số liệu an ninh năng lượng (Báo Công Thương, Năng lượng VN, NhanDan, TuoiTre như user đã đề cập).
7. **JUnit ≥ 30 test cases** + báo cáo coverage Jacoco.
8. **Sản phẩm chạy được trên máy người chấm** với 1 lệnh `docker-compose up`.

---

## 16. Rủi ro & Phương án giảm thiểu

| Rủi ro | Mức độ | Giảm thiểu |
|--------|--------|-----------|
| Quá nhiều thành phần (5 generator + Flink + Spring + JavaFX + Android) → trễ deadline | 🔴 High | Phase hóa: P1 = Backend + 1 generator; P2 = đủ 5 generator; P3 = Frontend. Mỗi phase phải chạy được end-to-end. |
| Flink alert + ESI logic phức tạp | 🟡 Medium | Có sẵn `PriceChangeDetector` làm template. ESI dùng Flink Table API. |
| Spring Boot mới → team chưa quen | 🟡 Medium | Dùng Spring Initializr generate sẵn; theo template chuẩn; có nhiều ví dụ. |
| LLM API rate limit / chi phí | 🟢 Low | Gemini free tier 60 req/min; cache response 5 phút trong Room. |
| Docker resource (RAM) | 🟡 Medium | Yêu cầu máy ≥ 8 GB; tài liệu khuyên Kafka heap = 512m, Flink TM heap = 1g. |
| Conflict merge nhiều thành viên | 🟡 Medium | Mỗi người 1 module riêng (folder riêng); PR review bắt buộc. |
| Quá lạc đề "Lập trình Java" | 🟡 Medium | **JavaFX Admin chiếm ≥ 30% scope báo cáo**; nhấn mạnh OOP + JDBC + 3-layer + JUnit. Coi Spring Boot là "thư viện thứ 3" (yêu cầu Chương 5). |
| Trùng đồ án các năm trước | 🟢 Low | ESI + LLM + đề tài An ninh năng lượng VN = unique. |
| **Không có CC → không lên được cloud free-tier** | 🟡 Medium | Strategy 2 cấp: Primary = máy thành viên + cloudflared (free vĩnh viễn, không CC). Backup = GitHub Codespaces (free 60h/tháng, chỉ cần GitHub account). Chi tiết §18. |
| **Máy thành viên crash giữa demo** | 🟡 Medium | (a) Backup person mở sẵn Codespace trước demo 10'; (b) **Firebase Remote Config cho Android BASE_URL** → switch URL không cần rebuild APK; (c) Quay video demo dự phòng. §20.4. |
| **Tunnel URL random đổi sau restart** | 🟡 Medium | (a) Build APK ngay trước demo 15' (mất 5'); (b) Hoặc named tunnel với DNS free (`*.is-a.dev`); (c) Hoặc **Firebase Remote Config** cập nhật URL real-time. §18.4. |
| **Mất Internet phòng demo** | 🟢 Low | Demo offline: Android Emulator + LAN IP `10.0.2.2:8080`. Hoặc đem theo Mi-Fi 4G dự phòng. §20.4. |
| **Người chấm clone về không chạy được** | 🔴 High | Tuân thủ §19 Clone-and-Run Checklist nghiêm ngặt. Test trên máy "sạch" trước khi nộp. Pre-build JAR đăng GitHub Releases để không phải build từ source. |
| **Máy chấm RAM thấp** | 🟡 Medium | Cung cấp 3 profile compose: `docker-compose.yml` (16GB), `.lite.yml` (8GB), `.minimal.yml` (4GB chỉ Postgres + Spring Boot + seed data). §19.8. |
| **Gemini API rate limit khi demo** | 🟢 Low | (a) Dùng `gemini-1.5-flash` (free tier rộng hơn); (b) Cache response 5' trong Room DB; (c) Có sẵn 2 API key dự phòng từ 2 Google account. |
| **Bị phát hiện expose máy công ty** | 🟡 Medium | **KHÔNG chạy tunnel từ máy công ty.** Chỉ dev local, demo thì chuyển sang máy thành viên hoặc Codespaces. |

---

## 17. Phụ lục: Cấu trúc thư mục đề xuất

```
Real-time-processing-with-Kafka-Flink-Postgres/
├── README.md                          (update)
├── UPGRADE_PLAN.md                    (file này)
├── AI_USAGE_LOG.md                    (nhật ký AI cho phụ lục)
├── docs/
│   ├── architecture.drawio
│   ├── erd.png
│   ├── usecase.png
│   ├── sequence.png
│   └── report/                        (báo cáo + slide)
│
├── infra/
│   ├── docker-compose.yml             (LITE - mặc định, ~2.7GB RAM, 8GB laptop OK)
│   ├── docker-compose.full.yml        (FULL stack - máy 16GB+ - khi cần show Metabase)
│   ├── docker-compose.bi.yml          (overlay - Metabase BI riêng, bật khi cần)
│   ├── docker-compose.dev.yml         (overlay - pgAdmin + Kafka UI cho dev)
│   ├── prometheus.yml                 (optional monitoring)
│   ├── .env.example                   (template env — copy thành .env)
│   └── script/
│       ├── 01_init_dim.sql
│       ├── 02_init_fact.sql
│       ├── 03_init_agg.sql
│       ├── 04_init_views.sql
│       └── 05_seed_vietnam_data.sql
│
├── scripts/                           (bootstrap "clone-and-run")
│   ├── run.sh                         (Linux/Mac one-click)
│   ├── run.ps1                        (Windows one-click)
│   ├── tunnel.sh                      (cloudflared launcher)
│   ├── healthcheck.sh                 (verify stack OK)
│   └── stop.sh                        (graceful shutdown)
│
├── .devcontainer/                     (GitHub Codespaces config)
│   ├── devcontainer.json
│   └── Dockerfile
│
├── data-generators/                   (Người B — Maven multi-module)
│   ├── pom.xml
│   ├── fuel-price-producer/           (giữ nguyên project cũ)
│   ├── grid-load-generator/
│   ├── renewable-output-generator/
│   ├── fuel-inventory-generator/
│   ├── emission-generator/
│   └── common-model/                  (FuelPrice, GridLoad, ...)
│
├── flink-jobs/                        (Người B — refactor từ KafkaConsumer)
│   ├── pom.xml
│   ├── raw-sink-job/
│   ├── window-aggregation-job/
│   ├── alert-detection-job/
│   ├── enrichment-job/
│   └── esi-calculator-job/
│
├── backend-api/                       (Người A — Spring Boot)
│   ├── pom.xml
│   ├── src/main/java/org/ves/api/
│   └── src/main/resources/
│       ├── application.yml
│       └── openapi.yml
│
├── desktop-admin/                     (Người D — JavaFX)
│   ├── pom.xml
│   └── src/main/java/org/ves/desktop/
│
├── android-app/                       (Người E — Android Studio)
│   ├── app/
│   ├── build.gradle.kts
│   └── google-services.json
│
└── tests/
    ├── e2e/                           (Postman + Newman)
    ├── load/                          (JMeter)
    └── ui/                            (JavaFX TestFX, Android Espresso)
```

---

## 18. Deployment & Demo Strategy ⭐ (chắc ăn, không cần CC)

> **Bối cảnh ràng buộc:**
> - 🔒 Laptop công ty có bảo mật cao, chỉ outbound qua proxy → không thể expose inbound.
> - 💳 Không có credit card → loại toàn bộ cloud free-tier yêu cầu xác minh thẻ (Oracle/AWS/GCP/Azure/Fly.io/Railway).
> - 💻 Máy cá nhân yếu → không thể chạy stack đầy đủ 24/7.
> - 👥 Có thể host trên máy thành viên khác → phải đảm bảo "clone là chạy".
> - 📱 Android dùng điện thoại cá nhân thật → cần URL public Internet thật.

### 18.1 Chiến lược 3 cấp (Self / Primary / Backup)

```
┌───────────────────────────────────────────────────────────────────────────┐
│                                                                           │
│   ┌──── SELF (KHUYẾN NGHỊ): Laptop của bạn (8GB RAM) + Lite Profile ─┐   │
│   │  • Chạy ngay trên laptop dev — không cần phụ thuộc ai            │   │
│   │  • docker-compose -f docker-compose.lite.yml up -d               │   │
│   │  • Stack tối ưu chỉ ~2.7GB RAM (xem §21)                         │   │
│   │  • cloudflared tunnel --url http://localhost:8080                │   │
│   │  • Android trỏ vào URL tunnel → demo bình thường                 │   │
│   │  • ✅ Đơn giản nhất, ✅ Không cần thành viên khác                 │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│   ┌──── PRIMARY (nếu muốn show full stack): Máy thành viên ≥16GB ───┐    │
│   │  • Khi cần show cả Metabase BI + parallelism cao                 │   │
│   │  • docker-compose.yml (full stack)                                │   │
│   │  • cloudflared tunnel                                             │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
│   ┌──── BACKUP: GitHub Codespaces (8GB cloud VM) ────────────────────┐   │
│   │  • Khi cả 2 phương án trên fail                                   │   │
│   │  • Dùng docker-compose.lite.yml (cùng config với Self)            │   │
│   │  • 60h/tháng free                                                 │   │
│   └──────────────────────────────────────────────────────────────────┘   │
│                                                                           │
└───────────────────────────────────────────────────────────────────────────┘
```

> 💡 **Tin vui:** Vì Lite Profile (§21) chỉ tốn ~2.7GB RAM, **chính laptop bạn (8GB) đã đủ host backend** mà không cần thành viên mạnh. Đây là phương án **đơn giản & độc lập nhất**. Máy thành viên và Codespaces giờ chỉ là "backup phòng xa".

### 18.2 Phương án theo từng môn

| Môn | Tình huống demo | Phương án khuyến nghị |
|-----|-----------------|-----------|
| **Java (live tại lớp)** | Mọi người ngồi chung phòng | Chạy Lite profile trên laptop trình bày — `bash scripts/run.sh` + chạy JavaFX. **Không cần tunnel**. |
| **Android (live tại lớp)** | Demo trên điện thoại cá nhân | **SELF**: Lite profile trên laptop bạn + cloudflared. **Backup**: Codespaces. |
| **Android (quay video)** | Quay sẵn rồi nộp | Lite profile local, dùng URL LAN `192.168.x.x:8080`, không cần Internet → đơn giản nhất. |
| **Cả 2 môn (demo từ xa)** | Online qua Zoom | SELF + cloudflared, share screen Zoom + demo điện thoại qua scrcpy. |

### 18.3 So sánh chi tiết 3 phương án

| Tiêu chí | **SELF** (Lite profile - laptop 8GB) | Primary (máy thành viên 16GB) | Backup (Codespaces) |
|---|---|---|---|
| Cần CC? | ❌ Không | ❌ Không | ❌ Không |
| Cần signup? | ❌ Không (TryCloudflare) | ❌ Không | ✅ GitHub free |
| RAM stack tiêu thụ | ~2.7 GB | ~5.7 GB (full) | ~2.7 GB (lite) |
| Phụ thuộc người khác? | ❌ Không — bạn tự kiểm soát | ✅ Phải nhờ thành viên | ❌ Không |
| Kafka + Flink + PG | ✅ (tối ưu) | ✅ (thoải mái) | ✅ (tối ưu) |
| Metabase | ⚠️ Tắt mặc định, bật khi cần (`--profile bi`) | ✅ Luôn bật | ⚠️ Tắt |
| URL public | ✅ TryCloudflare | ✅ TryCloudflare | ✅ `*.app.github.dev` cố định |
| Time-to-demo | ~3 phút | ~3 phút | ~2 phút resume |
| Giới hạn giờ | ❌ Vô hạn | ❌ Vô hạn | ✅ 60h/tháng |
| Demo offline cùng LAN | ✅ Dùng IP LAN, không cần Internet | ✅ | ❌ Phải Internet |
| Đơn giản nhất? | ⭐⭐⭐ | ⭐⭐ | ⭐ |
| **Khuyến nghị** | ✅ **MẶC ĐỊNH** | Khi cần demo Metabase BI | Khi cả hai trên fail |

### 18.4 Cloudflare Tunnel — Cách dùng chi tiết

#### A. Cách nhanh nhất: TryCloudflare (không signup)
```bash
# Cài cloudflared
# Windows:
winget install --id Cloudflare.cloudflared
# Mac:
brew install cloudflared
# Linux:
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64 -o cloudflared
chmod +x cloudflared

# (Tùy chọn) Nếu phải đi qua corp proxy:
# Windows PowerShell:
$env:HTTPS_PROXY = "http://corp-proxy:8080"
# Linux/Mac:
export HTTPS_PROXY=http://corp-proxy:8080

# Bật tunnel:
cloudflared tunnel --url http://localhost:8080
```

Output mẫu:
```
+--------------------------------------------------------------------------+
|  Your quick Tunnel has been created! Visit it at (it may take some time  |
|  to be reachable):                                                       |
|  https://crazy-blue-fox.trycloudflare.com                                |
+--------------------------------------------------------------------------+
```

→ Copy URL đó, paste vào Android `BASE_URL`, rebuild APK, test.

#### B. Named tunnel ổn định (cố định URL — nếu có domain free)
```bash
# 1 lần setup:
cloudflared tunnel login                          # mở browser → đăng nhập CF (free, không CC)
cloudflared tunnel create ves-demo                # tạo tunnel
cloudflared tunnel route dns ves-demo ves.example.com  # cần domain (free options: is-a.dev, js.org)

# Mỗi lần demo:
cloudflared tunnel run ves-demo                   # URL cố định mãi mãi
```

Nếu không có domain → cứ dùng TryCloudflare random URL, build APK lại 1 lần trước demo (mất 5 phút).

### 18.5 GitHub Codespaces — Cách setup

#### Bước 1: Tạo `.devcontainer/devcontainer.json` trong repo
```json
{
  "name": "VES-Monitor Dev",
  "image": "mcr.microsoft.com/devcontainers/java:17",
  "features": {
    "ghcr.io/devcontainers/features/docker-in-docker:2": {},
    "ghcr.io/devcontainers/features/node:1": {}
  },
  "forwardPorts": [8080, 8081, 3000, 5432, 9092],
  "portsAttributes": {
    "8080": { "label": "Spring Boot API", "visibility": "public" },
    "8081": { "label": "Flink UI",        "visibility": "private" },
    "3000": { "label": "Metabase",        "visibility": "public" }
  },
  "postCreateCommand": "bash scripts/setup-codespace.sh",
  "remoteUser": "vscode"
}
```

#### Bước 2: Mỗi lần demo
1. Vào GitHub repo → **Code → Codespaces → New codespace**.
2. Đợi build ~2 phút.
3. Terminal: `bash scripts/run.sh` (one-click bootstrap).
4. Tab **Ports** → port 8080 → chuột phải → **Port Visibility → Public**.
5. Copy URL `https://*-8080.app.github.dev`.
6. Cập nhật Android `BASE_URL` → build APK → cài điện thoại → test.

#### Bước 3: Tiết kiệm giờ
- Set Codespace **auto-stop 30 phút** (Settings → Codespaces).
- Tắt thủ công sau demo (`gh codespace stop` hoặc trên web).
- 60h/tháng = ~20 phiên 3h.

### 18.6 LLM Insight cho Android — KHÔNG cần CC

| Provider | Free tier | Cần CC? | Phù hợp Android? |
|---|---|---|---|
| **Google Gemini API** ⭐ | 60 req/phút, 1500 req/ngày (Gemini 1.5 Flash) | ❌ | ✅ REST từ Retrofit |
| **Groq API** (Llama 3.3, Mixtral) | Rất rộng | ❌ | ✅ |
| **OpenRouter** (free models) | Llama, Mistral free tier | ❌ | ✅ |
| OpenAI GPT | — | ✅ | — |
| Anthropic Claude | — | ✅ | — |

→ **Dùng Gemini API**: Chỉ cần Google account → vào https://aistudio.google.com → lấy API key → gọi `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent` từ Android.

### 18.7 Cây quyết định lựa chọn (đơn giản hóa)

```
START
  │
  ▼
Demo Android live cần Internet?
  ├─ KHÔNG (môn Java / video record) → SELF lite profile, không cần tunnel
  │
  └─ CÓ ──► Laptop bạn có 8GB+ RAM khả dụng?
              ├─ CÓ ──► ⭐ SELF lite profile + cloudflared (MẶC ĐỊNH)
              │
              └─ KHÔNG ──► Codespaces backup
```

**→ 95% trường hợp, SELF Lite Profile là đáp án.** Chỉ chuyển sang Primary/Codespaces khi gặp tình huống đặc biệt.

---

## 19. Clone-and-Run Checklist (đảm bảo chạy ngay)

Đây là **tiêu chí tối thượng** cho việc bàn giao project sang máy thành viên khác hoặc máy người chấm. Mỗi item phải ✅ trước khi báo cáo cuối kỳ.

### 19.1 Repository hygiene
- [ ] `README.md` ở root có **"Quick Start 3 lệnh"** ngay đầu file.
- [ ] `.gitignore` đầy đủ: `target/`, `*.class`, `node_modules/`, `.env`, `*.iml`, `.DS_Store`, `metabase.db/`.
- [ ] Không commit `.env` thật. **Commit `.env.example`** với placeholder.
- [ ] Không commit file binary lớn (>50MB). JAR build artifact → dùng **GitHub Releases**.
- [ ] Không commit file riêng IDE: `.idea/`, `.vscode/` (trừ `.vscode/settings.json` shared).
- [ ] Có `LICENSE` (MIT/Apache 2.0) để clean.

### 19.2 Configuration management
- [ ] Tất cả config đọc từ **environment variables** (Spring `application.yml` dùng `${VAR:default}`).
- [ ] Có file `.env.example` template với mọi biến cần thiết:
  ```env
  # Database
  POSTGRES_USER=postgres
  POSTGRES_PASSWORD=123456
  POSTGRES_DB=ves_security

  # Kafka
  KAFKA_BOOTSTRAP_SERVERS=kafka:29092

  # Spring Boot
  JWT_SECRET=change-me-in-production
  SERVER_PORT=8080

  # Gemini API (cho Android, không bắt buộc cho backend)
  GEMINI_API_KEY=
  ```
- [ ] **KHÔNG hard-code** `localhost`, `192.168.x.x` trong code Spring Boot/Flink.
- [ ] Android `BASE_URL` ở 1 vị trí duy nhất: `app/build.gradle` `buildConfigField` hoặc `gradle.properties` → dễ đổi.

### 19.3 Docker Compose
- [ ] `docker-compose.yml` chỉ pull image từ Docker Hub (không build local).
- [ ] Có `docker-compose.lite.yml` cho máy yếu/Codespaces (giảm Flink TM slots = 2, bỏ Metabase).
- [ ] Có **healthcheck** cho mọi service (Kafka, Postgres, Spring Boot).
- [ ] `depends_on` với `condition: service_healthy` để producer chỉ start khi Kafka up.
- [ ] Volumes có tên rõ ràng (`zk_data`, `kafka_data`, `pgdata`) — dễ `docker volume prune` khi cần reset.
- [ ] Network `realtime_network` để service gọi nhau qua service name.

### 19.4 Bootstrap scripts (giảm complexity cho user)
- [ ] `scripts/run.sh` (Linux/Mac) one-click:
  ```bash
  #!/usr/bin/env bash
  set -e
  echo "[1/5] Copy .env if needed..."
  [ ! -f .env ] && cp infra/.env.example .env
  echo "[2/5] Start Docker stack..."
  docker-compose -f infra/docker-compose.yml up -d
  echo "[3/5] Wait for services healthy..."
  bash scripts/healthcheck.sh
  echo "[4/5] Submit Flink job..."
  docker cp flink-jobs/target/flink-jobs-*.jar flink-jobmanager:/job.jar
  docker exec flink-jobmanager flink run /job.jar
  echo "[5/5] Start producers..."
  java -jar data-generators/target/all-generators.jar &
  echo "DONE. API: http://localhost:8080  Flink: 8081  Metabase: 3000"
  ```
- [ ] `scripts/run.ps1` (Windows) tương đương.
- [ ] `scripts/stop.sh` graceful shutdown.
- [ ] `scripts/tunnel.sh`:
  ```bash
  #!/usr/bin/env bash
  echo "Starting Cloudflare tunnel for Spring Boot (port 8080)..."
  cloudflared tunnel --url http://localhost:8080
  ```
- [ ] `scripts/healthcheck.sh` poll until tất cả service trả 200/ready.

### 19.5 Pre-built artifacts (tránh build trên máy người chấm)
- [ ] **GitHub Releases** chứa:
  - `flink-jobs-1.0.0.jar` (uber-JAR Flink consumer)
  - `data-generators-1.0.0.jar` (uber-JAR producer)
  - `backend-api-1.0.0.jar` (Spring Boot fat JAR)
  - `desktop-admin-1.0.0.jar` (JavaFX runnable)
  - `android-app-debug.apk`
- [ ] `README.md` chỉ cách download từ Releases nếu không muốn build từ source.
- [ ] Hoặc: GitHub Actions auto-build mỗi tag → Release.

### 19.6 Documentation
- [ ] `README.md` có section:
  - **Quick Start (3 lệnh)** — ưu tiên user mới
  - **Architecture diagram** (link đến `docs/architecture.png`)
  - **Endpoints summary** (link đến Swagger UI)
  - **Troubleshooting** (port conflict, low memory, Docker chưa cài)
  - **Demo URL hiện tại** (cập nhật trước demo)
- [ ] `docs/RUNBOOK.md` — checklist cho người host máy primary.
- [ ] `docs/ANDROID_SETUP.md` — cách build APK + đổi BASE_URL.
- [ ] `docs/AI_USAGE_LOG.md` — yêu cầu §5 file hướng dẫn (nhật ký prompt + verify).
- [ ] Video screencast 2 phút show từ `git clone` đến `localhost:8080/swagger-ui.html` chạy được → đính kèm README.

### 19.7 Testing trên máy "sạch"
- [ ] **Test ít nhất 1 lần trên máy thành viên khác** (không phải máy dev gốc):
  - Clone repo từ đầu
  - Cài Docker Desktop nếu chưa có
  - Chạy `bash scripts/run.sh`
  - Tất cả endpoint phản hồi đúng
- [ ] Test trên cả Windows + Linux/Mac (hoặc WSL2) → đảm bảo line endings (CRLF/LF) không phá script.
- [ ] Test trên Codespaces với `docker-compose.lite.yml`.

### 19.8 Resource sizing đảm bảo (3 profile)
| Profile | RAM stack | RAM máy tối thiểu | Compose file | Phù hợp |
|---------|-----------|-------------------|--------------|---------|
| **Lite** ⭐ | ~2.7 GB | 8 GB | `docker-compose.yml` (mặc định) | **Laptop dev cá nhân, Codespaces, demo chính** |
| **Lite + BI** | ~3.5 GB | 8 GB căng | `docker-compose.yml` + `docker-compose.bi.yml` | Khi cần show Metabase dashboard cho thầy |
| **Full** | ~5.7 GB | 16 GB | `docker-compose.full.yml` | Máy thành viên mạnh, demo production-like |

**Lệnh sử dụng:**
```bash
# Lite (mặc định, khuyến nghị):
docker-compose up -d

# Lite + Metabase BI (khi cần show dashboard):
docker-compose -f docker-compose.yml -f docker-compose.bi.yml up -d

# Full (cần 16GB+ RAM):
docker-compose -f docker-compose.full.yml up -d

# Dev mode (thêm pgAdmin + Kafka UI):
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

> **Chi tiết cấu hình từng service**: Xem §21 Lightweight Profile.

---

## 20. Demo Day Runbook (T-30' đến T-0)

Đây là **kịch bản chuẩn** cho ngày demo môn Android (môn cần tunnel). Người host primary phải thuộc lòng.

### 20.1 Vai trò trong demo
| Role | Người | Trách nhiệm |
|------|-------|-------------|
| **Host** | 1 thành viên (máy mạnh) | Bật stack + tunnel, monitor logs |
| **Presenter** | Leader hoặc trưởng nhóm | Trình bày, thao tác trên điện thoại |
| **Backup** | 1 thành viên khác | Cầm Codespace mở sẵn, ready takeover |
| **Recorder** | 1 thành viên | Quay video dự phòng nếu live failed |

### 20.2 Checklist T-1 ngày
- [ ] Sync code main branch ổn định, không có WIP.
- [ ] Build pre-release APK với URL placeholder, lưu sẵn trên Google Drive.
- [ ] Test full flow trên máy Host: clone fresh + run + tunnel + cài APK + chạy.
- [ ] Backup person mở thử Codespace, đảm bảo build pass.
- [ ] Ghi 1 video demo dự phòng 5 phút (offline play nếu mất Internet phòng demo).

### 20.3 Checklist T-30 phút (trước giờ demo)
```
T-30':  Host:    Khởi động máy, đảm bảo sạc + Wi-Fi ổn
         Host:    cd ves-monitor && git pull
         Host:    bash scripts/run.sh
         Host:    Đợi healthcheck PASS (xem output script)

T-25':  Host:    Submit Flink job (script tự làm) + verify Flink UI 8081
         Host:    Verify Spring Boot health: curl localhost:8080/actuator/health
         Host:    Verify Postgres có data: docker exec postgres-database psql ... \
                  -c "SELECT COUNT(*) FROM fuel_prices_raw;"

T-20':  Host:    bash scripts/tunnel.sh
         Host:    Copy URL https://*.trycloudflare.com
         Host:    Test: curl <URL>/actuator/health từ máy khác
                  → phải trả 200 {"status":"UP"}

T-15':  Presenter: Cập nhật BASE_URL trong Android (BuildConfig hoặc Remote Config)
         Presenter: ./gradlew assembleDebug
         Presenter: adb install app-debug.apk lên điện thoại
         Presenter: Smoke test trên điện thoại:
                    ✓ Login
                    ✓ Home dashboard (4 KPI cards có số)
                    ✓ Map view (markers hiện)
                    ✓ Alert list
                    ✓ LLM Insight tab (gọi Gemini OK)

T-10':  Backup:    Mở Codespace sẵn (chỉ resume, không stop)
                   → URL backup ready trong console
         Recorder:  Bật OBS record màn hình laptop + camera điện thoại

T-5':   Mọi người: Check lại điện thoại pin đầy, mạng OK
         Tắt mọi notification cá nhân để không bị che màn hình demo

T-0:    GO LIVE 🎬
```

### 20.4 Khi có sự cố — Playbook
| Sự cố | Ưu tiên xử lý |
|-------|--------------|
| Tunnel disconnect giữa demo | Host: `Ctrl+C` rồi chạy lại `tunnel.sh`. URL đổi → Backup tạm dùng URL trên Codespace đã prepared. |
| Spring Boot 500 error | Host: `docker-compose logs backend-api --tail 50` debug nhanh. Nếu DB chưa init: `docker exec postgres-database psql ... -f /docker-entrypoint-initdb.d/05_seed_vietnam_data.sql` |
| Android không lấy được data | Verify URL: mở browser trên điện thoại, vào `<URL>/api/kpi/overview` xem có JSON. Nếu OK → vấn đề ở App, dùng debug logs. |
| Mất Internet phòng demo | Switch sang **demo offline**: Android Emulator chạy trên laptop Host, dùng `10.0.2.2:8080`. Không cần tunnel. |
| Máy Host crash | Backup: chạy Codespace, lấy URL `*.app.github.dev`, sửa Remote Config Firebase → Android nhận URL mới mà không cần rebuild. **Đây là lý do nên dùng Firebase Remote Config cho `BASE_URL`!** |

### 20.5 Sau demo
- [ ] Tắt tunnel: `Ctrl+C` ở terminal cloudflared.
- [ ] Tắt stack: `bash scripts/stop.sh`.
- [ ] Tắt Codespace: `gh codespace stop` hoặc trên web → tiết kiệm 60h budget.
- [ ] Gửi video record cho nhóm + lưu trữ.
- [ ] Cập nhật `docs/DEMO_LOG.md`: ngày demo, sự cố, hướng cải thiện.

### 20.6 🪄 Mẹo "chắc ăn" thêm

1. **Dùng Firebase Remote Config cho `BASE_URL`** trong Android — đổi URL không cần build lại APK. Setup 10 phút, **cứu được 80% kịch bản tunnel chết giữa demo**.
2. **Pre-seed sẵn 1 ngày dữ liệu** trong Postgres → demo không cần đợi producer chạy 5 phút mới có data. Đã có sẵn `init_fuel_schema.sql` seed — giữ và mở rộng.
3. **Quay video full-flow 2 phút** ngay sau khi setup xong → có cái play nếu thầy cô không cần xem live (nhiều thầy chấp nhận video demo + Q&A).
4. **In QR code URL tunnel** dán lên slide → người chấm scan vào API thử ngay → ấn tượng pro.
5. **Để Metabase public qua tunnel riêng** (`cloudflared tunnel --url localhost:3000`) → thầy cô xem được dashboard BI online → impressive bonus.
6. **Limit gemini-1.5-flash** thay vì pro → free tier rộng hơn, nhanh hơn (~1s).
7. **Cache LLM response 5 phút** trong Android (Room DB) → tránh hit rate-limit khi demo nhiều lần.

---

## 21. Lightweight Profile 🪶 — Chạy gọn trên laptop 8GB

> **Mục tiêu:** Giảm stack từ ~5.7GB → **~2.7GB RAM**, vẫn giữ đầy đủ Kafka + Flink + Postgres + Spring Boot, chạy được trên laptop 8GB RAM thông thường (kể cả khi mở IDE + browser).

### 21.1 Memory Budget — Trước và Sau tối ưu

| Service | Mặc định | Lite tối ưu | Cách giảm |
|---------|----------|-------------|-----------|
| Zookeeper | ~512 MB | **256 MB** | `KAFKA_HEAP_OPTS=-Xmx256M` |
| Kafka | ~1024 MB | **512 MB** | Heap 512M + giảm partition + segment nhỏ |
| Flink JobManager | ~1024 MB | **600 MB** | `jobmanager.memory.process.size: 600m` |
| Flink TaskManager | ~1536 MB | **800 MB** | Process 800m + slots 2 + parallelism 1 |
| PostgreSQL | ~256 MB | **128 MB** | `shared_buffers=64MB` + `work_mem=4MB` |
| Spring Boot API | ~512 MB | **384 MB** | `-Xmx384m -XX:MaxMetaspaceSize=128m` |
| Metabase | ~1024 MB | **TẮT** mặc định | Chỉ bật khi cần (profile `bi`) |
| Data Generators (Java) | ~256 MB × 5 | **128 MB × 2** (chạy chỉ 2-3 gen) | Giới hạn heap mỗi gen |
| **TỔNG STACK** | **~6.1 GB** | **~2.7 GB** | -56% |

**Budget tổng cho 8GB laptop:**
- Windows OS + drivers: ~2.0 GB
- IDE (VS Code/IntelliJ): ~1.2 GB
- Browser (5-10 tab): ~1.5 GB
- **Stack Docker (Lite): ~2.7 GB**
- **Buffer dự phòng: ~0.6 GB**

→ Vừa đủ chạy được, có chỗ thở.

### 21.2 `docker-compose.yml` — Lite Profile (file mặc định)

```yaml
services:
  # ── Zookeeper (tối thiểu) ──────────────────────────────────────────
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
      KAFKA_HEAP_OPTS: "-Xmx256M -Xms128M"   # ⭐ giảm heap
    mem_limit: 384m                          # ⭐ hard cap
    ports: ["2181:2181"]
    networks: [realtime_network]
    healthcheck:
      test: ["CMD", "zookeeper-shell", "localhost:2181", "ls", "/"]
      interval: 30s
      retries: 10

  # ── Kafka (tối thiểu, single broker, 1 partition) ───────────────────
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: kafka
    depends_on:
      zookeeper: { condition: service_healthy }
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      KAFKA_NUM_PARTITIONS: 1                # ⭐ giảm từ 3 → 1
      KAFKA_LOG_RETENTION_HOURS: 24          # ⭐ giảm từ 168 → 24 (1 ngày)
      KAFKA_LOG_SEGMENT_BYTES: 134217728     # ⭐ 128MB thay 1GB
      KAFKA_LOG_RETENTION_BYTES: 268435456   # ⭐ giới hạn 256MB
      KAFKA_HEAP_OPTS: "-Xmx512M -Xms256M"   # ⭐ giảm heap
      KAFKA_JVM_PERFORMANCE_OPTS: "-XX:+UseG1GC -XX:MaxGCPauseMillis=20"
    mem_limit: 700m                          # ⭐ hard cap
    ports: ["9092:9092"]
    networks: [realtime_network]
    healthcheck:
      test: ["CMD", "kafka-broker-api-versions", "--bootstrap-server", "localhost:9092"]
      interval: 15s
      retries: 5

  # ── Flink JobManager (tối ưu memory) ────────────────────────────────
  flink-jobmanager:
    image: flink:1.17.0-scala_2.12-java11
    container_name: flink-jobmanager
    command: jobmanager
    environment:
      - |
        FLINK_PROPERTIES=
        jobmanager.rpc.address: flink-jobmanager
        jobmanager.memory.process.size: 600m
        jobmanager.memory.heap.size: 256m
        jobmanager.memory.off-heap.size: 64m
        jobmanager.memory.jvm-metaspace.size: 128m
        jobmanager.memory.jvm-overhead.fraction: 0.1
        parallelism.default: 1
    mem_limit: 700m
    ports: ["8081:8081"]
    networks: [realtime_network]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/overview"]
      interval: 30s
      retries: 5

  # ── Flink TaskManager (giảm slots + heap) ───────────────────────────
  flink-taskmanager:
    image: flink:1.17.0-scala_2.12-java11
    container_name: flink-taskmanager
    command: taskmanager
    depends_on:
      flink-jobmanager: { condition: service_healthy }
    environment:
      - |
        FLINK_PROPERTIES=
        jobmanager.rpc.address: flink-jobmanager
        taskmanager.memory.process.size: 800m
        taskmanager.memory.flink.size: 512m
        taskmanager.memory.framework.heap.size: 128m
        taskmanager.memory.task.heap.size: 256m
        taskmanager.memory.managed.size: 128m
        taskmanager.memory.network.min: 64m
        taskmanager.memory.network.max: 64m
        taskmanager.numberOfTaskSlots: 2   # ⭐ giảm từ 4 → 2
        parallelism.default: 1             # ⭐ giảm từ 2 → 1
    mem_limit: 900m
    networks: [realtime_network]

  # ── PostgreSQL (tối thiểu nhưng vẫn nhanh) ──────────────────────────
  postgresql:
    image: postgres:15-alpine             # ⭐ alpine nhẹ hơn ~50MB
    container_name: postgres-database
    restart: unless-stopped
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: 123456
      POSTGRES_DB: ves_security
    command: >
      postgres
        -c shared_buffers=64MB
        -c work_mem=4MB
        -c maintenance_work_mem=32MB
        -c effective_cache_size=128MB
        -c max_connections=20
        -c wal_buffers=4MB
        -c synchronous_commit=off
    mem_limit: 200m
    ports: ["5432:5432"]
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./script:/docker-entrypoint-initdb.d
    networks: [realtime_network]
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d ves_security"]
      interval: 10s
      retries: 5

  # ── Spring Boot Backend ─────────────────────────────────────────────
  backend-api:
    image: eclipse-temurin:17-jre-alpine  # ⭐ alpine nhẹ
    container_name: ves-backend-api
    depends_on:
      postgresql: { condition: service_healthy }
      kafka:      { condition: service_healthy }
    environment:
      JAVA_OPTS: "-Xmx384m -Xms128m -XX:+UseG1GC -XX:MaxMetaspaceSize=128m -XX:+UseContainerSupport"
      SPRING_PROFILES_ACTIVE: docker
      DB_URL: jdbc:postgresql://postgres-database:5432/ves_security
      DB_USER: postgres
      DB_PASS: 123456
      KAFKA_BROKERS: kafka:29092
    mem_limit: 500m
    ports: ["8080:8080"]
    volumes:
      - ./backend-api/target/backend-api.jar:/app.jar:ro
    command: ["sh", "-c", "java $$JAVA_OPTS -jar /app.jar"]
    networks: [realtime_network]
    healthcheck:
      test: ["CMD-SHELL", "wget -q -O- http://localhost:8080/actuator/health || exit 1"]
      interval: 20s
      retries: 5

volumes:
  pgdata:
  kafka_data:

networks:
  realtime_network:
    driver: bridge
```

→ **Tổng `mem_limit`: ~3.4 GB** (cap cứng, không thể vượt). Thực tế tiêu thụ ~2.7 GB do JVM overhead.

### 21.3 `docker-compose.bi.yml` — Overlay Metabase (bật khi cần)

```yaml
services:
  metabase:
    image: metabase/metabase:v0.47.7
    container_name: metabase
    environment:
      MB_DB_FILE: /metabase-data/metabase.db
      MB_JETTY_MAXTHREADS: 25                # ⭐ giảm pool
      JAVA_OPTS: "-Xmx512m -Xms256m"         # ⭐ giảm heap
    mem_limit: 700m
    ports: ["3000:3000"]
    volumes: [metabase_data:/metabase-data]
    networks: [realtime_network]
    depends_on:
      postgresql: { condition: service_healthy }
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/api/health"]
      interval: 30s
      start_period: 60s

volumes:
  metabase_data:

networks:
  realtime_network:
    external: true
    name: ves-monitor_realtime_network   # match với compose chính
```

**Sử dụng:**
```bash
# Bình thường (không Metabase):
docker-compose up -d

# Khi cần show Metabase cho thầy:
docker-compose -f docker-compose.yml -f docker-compose.bi.yml up -d

# Tắt Metabase nhưng giữ stack:
docker-compose -f docker-compose.bi.yml down
```

### 21.4 `docker-compose.dev.yml` — Overlay Dev Tools (cho dev cá nhân)

```yaml
services:
  pgadmin:
    image: dpage/pgadmin4:latest
    container_name: pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@ves.local
      PGADMIN_DEFAULT_PASSWORD: admin
      PGADMIN_CONFIG_SERVER_MODE: "False"
    mem_limit: 250m
    ports: ["5050:80"]
    networks: [realtime_network]

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092
    mem_limit: 300m
    ports: ["8090:8080"]
    networks: [realtime_network]

networks:
  realtime_network:
    external: true
    name: ves-monitor_realtime_network
```

**Sử dụng (chỉ khi dev cá nhân, không trong demo):**
```bash
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
# pgAdmin: http://localhost:5050
# Kafka UI: http://localhost:8090
```

### 21.5 Tối ưu code Java cho memory thấp

Áp dụng cho **mọi service Java** (Spring Boot API + Producers + Flink jobs):

#### 21.5.1 JVM flags chuẩn (dùng env `JAVA_OPTS`)
```
-XX:+UseG1GC
-XX:MaxGCPauseMillis=100
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
-XX:MaxMetaspaceSize=128m
-XX:+ExitOnOutOfMemoryError
-Djava.security.egd=file:/dev/./urandom
```

#### 21.5.2 Spring Boot — tắt features không cần
```yaml
# application.yml
spring:
  main:
    lazy-initialization: true             # lazy-load bean → giảm startup memory
  jpa:
    open-in-view: false                   # giảm overhead session
  datasource:
    hikari:
      maximum-pool-size: 5                # giảm từ 10 → 5
      minimum-idle: 1
      connection-timeout: 30000

server:
  tomcat:
    threads:
      max: 50                             # giảm từ 200 → 50
      min-spare: 5
    accept-count: 50
    max-connections: 100

logging:
  level:
    root: WARN                            # giảm log spam
    org.ves: INFO

management:
  endpoints:
    web:
      exposure:
        include: health,info               # chỉ expose minimal
```

#### 21.5.3 Flink — backpressure & state nhỏ
```java
// Trong KafkaConsumerApplication
env.setParallelism(1);                    // ⭐ giảm từ 2 → 1
env.enableCheckpointing(60_000);          // ⭐ tăng từ 30s → 60s (giảm I/O)
env.getCheckpointConfig().setMinPauseBetweenCheckpoints(30_000);
env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

// State backend: RocksDB → giảm heap, lưu state ra disk
env.setStateBackend(new EmbeddedRocksDBStateBackend(true));
```

#### 21.5.4 Generators — giảm tần suất + heap nhỏ
```bash
# Chạy producer với heap nhỏ + interval lớn hơn:
java -Xmx128m -Xms64m \
     -Dproducer.interval.ms=15000 \
     -jar fuel-price-producer.jar
```

### 21.6 Tối ưu OS-level

#### Windows
- **WSL2 backend cho Docker Desktop** (nhẹ hơn Hyper-V).
- Limit WSL2 memory trong `%USERPROFILE%\.wslconfig`:
  ```ini
  [wsl2]
  memory=4GB           # cap WSL2 ở 4GB → bảo vệ Windows
  processors=4
  swap=4GB
  ```
- Tăng pagefile lên 8GB (Settings → System → Advanced → Performance → Virtual memory) → nếu căng, OS fallback sang disk thay vì crash.
- Tắt Cortana, OneDrive sync, Windows Search Indexer khi demo.

#### Mac/Linux
- Docker Desktop → Settings → Resources → Memory: limit 4GB (đủ cho stack lite).
- Đóng các app không dùng (Slack, Discord, Teams).

### 21.7 Mẹo giảm tải khi runtime

| Tình huống | Action |
|------------|--------|
| Demo Java live, không cần Android | Tắt `backend-api` container, JavaFX dùng JDBC trực tiếp |
| Demo Android live, không cần Metabase | `docker-compose up -d` không kèm `bi.yml` |
| Chỉ test 1 luồng (vd: fuel price) | Chỉ chạy 1-2 generator thay vì cả 5 |
| Code/dev không cần Flink | `docker-compose stop flink-jobmanager flink-taskmanager` → tiết kiệm ~1.5GB |
| Test API endpoint thuần | Chạy Spring Boot từ IDE, chỉ giữ Docker = Postgres + Kafka |

### 21.8 Validate cấu hình — kiểm tra thực tế

Sau khi setup, chạy lệnh sau để verify RAM thực tế:

```bash
# Liệt kê RAM từng container:
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}"

# Output mẫu (Lite Profile, sau 5 phút chạy):
# NAME                MEM USAGE / LIMIT     MEM %
# zookeeper           198MiB / 384MiB       51.6%
# kafka               456MiB / 700MiB       65.1%
# flink-jobmanager    420MiB / 700MiB       60.0%
# flink-taskmanager   650MiB / 900MiB       72.2%
# postgres-database   85MiB / 200MiB        42.5%
# ves-backend-api     310MiB / 500MiB       62.0%
# ─────────────────────────────────────────────
# TỔNG                ~2.1 GB / 3.4 GB cap  (~62%)
```

→ **OK nếu tổng < 3GB.** Nếu vượt → giảm thêm `mem_limit` hoặc tắt service không dùng.

### 21.9 Khi nào KHÔNG dùng Lite Profile

Chuyển sang **Full Profile** (`docker-compose.full.yml`) khi:
- Máy ≥ 16 GB RAM rảnh → cứ dùng full để hiệu năng tốt nhất.
- Demo production-like cho thầy/cô (parallelism cao, throughput cao).
- Stress test / load test (cần Kafka 3 partitions, Flink 4 slots).

### 21.10 So sánh hiệu năng Lite vs Full

| Metric | Lite Profile | Full Profile |
|--------|--------------|--------------|
| RAM tiêu thụ | ~2.7 GB | ~5.7 GB |
| Throughput Kafka | ~500 msg/s | ~5000 msg/s |
| Flink parallelism | 1 | 2 |
| Latency end-to-end | ~5s | ~2s |
| Số producer chạy song song | 2-3 | 5 |
| Metabase | ❌ (tách overlay) | ✅ |
| Phù hợp | **Dev + demo đồ án** | Production |

> **Kết luận:** Với mục đích đồ án sinh viên + demo Android, **Lite Profile thừa sức**. Full Profile chỉ là "phòng khi cần show off".

### 21.11 Smoke test Lite Profile (5 phút verify)

```bash
# 1. Start stack lite
bash scripts/run.sh

# 2. Đợi 60s cho healthcheck pass

# 3. Verify từng service:
curl http://localhost:8080/actuator/health    # Spring Boot
# → {"status":"UP"}

curl http://localhost:8081/overview            # Flink
# → JSON dashboard info

docker exec postgres-database psql -U postgres -d ves_security \
  -c "SELECT COUNT(*) FROM fuel_prices_raw;"
# → count > 0 (có data từ producer)

# 4. Test latency end-to-end:
# Send 1 message qua producer → đo thời gian xuất hiện trong Postgres
# Kỳ vọng: < 10 giây

# 5. Check RAM:
docker stats --no-stream
# → Tổng < 3GB
```

✅ Nếu tất cả pass → Lite Profile sẵn sàng cho demo.

---

## 22. Phân công thực tế (Leader-heavy) 👤 — Khi thành viên yếu

> 📌 **CẬP NHẬT 2026-05-12 (chiều)**: **Đồ án Android tách thành đồ án độc lập** (môn Mobile, repo riêng, team chỉ Leader + 1 Android Dev). Phase 6 không còn nằm trong scope đồ án Java.
> Onboarding cho Android Dev: **[`docs/ANDROID_ONBOARDING.md`](docs/ANDROID_ONBOARDING.md)** — 14 endpoint API contract + stack recommendation + first-day setup.
>
> 📋 **Đồ án Java** giữ nguyên team 5 người (Leader + 4 helpers), phân công chi tiết tại **[`docs/TEAM_TASKS.md`](docs/TEAM_TASKS.md)**:
> - **B — Doc & PM** (non-coding, 5 PR ~30h): ERD/Use-case/Activity + báo cáo Chương 1/2/4/6 + AI Usage Log + Notion
> - **C — Media & QA** (non-coding, 5 PR ~25h): slide 22 trang + video demo 5-7' + bộ screenshot + Postman collection + test report
> - **D — JavaFX UI Junior** (simple coding, 5 PR ~20h): SQL seed VN + ValidatorUtils + JUnit + 2 màn JavaFX phụ (About/Settings) + CSS polish
> - **E — Backend & Test Helper** (simple coding, 5 PR ~18h): SQL stress seed + JUnit backend (JwtTokenProvider/ApiException) + Swagger @Schema examples + 14 curl example scripts + load test
>
> §22 dưới đây giữ lại bản tóm tắt 3 người cũ làm tham khảo lịch sử.

> **Bối cảnh:** Thành viên Java của nhóm chưa thạo Spring Boot / Flink / Android / JavaFX nâng cao. Leader sẽ đảm nhận phần lớn code, mỗi thành viên còn lại được giao **3-6 task nhỏ-đơn giản** đảm bảo có PR thật, có dấu ấn trong báo cáo, không block đường găng của Leader.

### 22.1 Triết lý phân công Leader-heavy

1. **Leader làm 80% workload** — toàn bộ code khó (Spring Boot, Flink, Android core, JavaFX core, integration, deploy).
2. **2 thành viên còn lại làm 20%** — mỗi người ~10% workload, chỉ 5 task gọn-nhẹ, tập trung vào **non-coding** (doc/media/test) hoặc **copy-paste có template**.
3. **Task giao thành viên phải có 3 đặc tính:**
   - 🪨 **Atomic**: làm xong trong 2-6 giờ, không phụ thuộc task khác đang dở.
   - 📖 **Template-able**: Leader đã có 1 ví dụ mẫu sẵn, thành viên copy-modify.
   - 🎯 **Visible**: tạo ra deliverable nhìn thấy được (file, ảnh, screenshot, video).
4. **Mỗi thành viên có ~5 PR** — đủ để chứng minh đóng góp trong báo cáo + GitHub history.
5. **Không bao giờ giao task block Leader.** Nếu thành viên trễ → Leader làm thay, ghi nhận trong commit.

### 22.2 Vai trò mới — Team 3 người (Leader + 2 thành viên)

> **Bối cảnh thực tế:** Team có Leader (bạn) + 2 thành viên yếu. Leader làm ~80% code, 2 thành viên gộp các task non-coding + simple-coding thành 2 cụm vai trò.

| Vai trò | Người | Workload | Skill required | PR mục tiêu |
|---------|-------|----------|----------------|-------------|
| **Leader / Full-stack** | A (bạn) | ~80% | Tất cả | 30-40 PR |
| **Người B — Doc & Media** | B | ~10% | draw.io, viết Word, làm slide, quay video | **~5 PR** |
| **Người C — Data & QA** | C | ~10% | SQL cơ bản, click & test, screenshot | **~5 PR** |

> **Tổng PR thành viên: ~10 PR (5+5).** Đủ để chứng minh đóng góp trong báo cáo Chương 6, không quá tải cho người yếu.

### 22.3 Task cụ thể cho 2 thành viên

#### 👤 NGƯỜI B — Doc & Media (~10%, 5 task / 5 PR)

> **Định vị:** Không cần code. Chỉ cần biết draw.io, viết Word, làm PowerPoint, quay video.

| # | Task | Mô tả chi tiết | Time | Deliverable |
|---|------|----------------|------|-------------|
| **PR1** | Vẽ **ERD + Use-case + Architecture** diagram | Dùng draw.io / dbdiagram.io. Leader cung cấp schema §5 + endpoint list §7 → người B vẽ thành ảnh đẹp. 3 diagram trong 1 PR. | 6h | `docs/erd.png`, `docs/usecase.png`, `docs/architecture.png` |
| **PR2** | Viết **Chương 1 + 2 báo cáo** | Chương 1: giới thiệu đề tài, bối cảnh an ninh năng lượng VN (~5 trang). Chương 2: phân tích thiết kế HTTT (dùng ERD/Use-case đã vẽ, ~8 trang). | 9h | `docs/report/chuong-1.docx`, `chuong-2.docx` |
| **PR3** | Viết **Chương 4 + 6 báo cáo** | Chương 4: quản lý dự án (charter, SOW, phân công). Chương 6: kết quả, đánh giá thành viên, ưu/nhược. | 8h | `chuong-4.docx`, `chuong-6.docx` |
| **PR4** | **Tạo slide PowerPoint** | 20-25 slide theo cấu trúc §12 hướng dẫn ĐA: intro / kiến trúc / công nghệ / demo / Q&A. Dùng diagram đã vẽ ở PR1. | 6h | `docs/slide.pptx` |
| **PR5** | **Quay video demo + setup Notion** | Quay 5-7 phút screen record demo end-to-end + voice over. Đồng thời setup Notion giới thiệu nhóm (yêu cầu §1 hướng dẫn ĐA). | 8h | `docs/demo-video.mp4`, link Notion |

**Tổng**: ~37h trong 10 tuần (~3.7h/tuần). Tất cả là task **non-coding** an toàn.

---

#### 👤 NGƯỜI C — Data & QA (~10%, 5 task / 5 PR)

> **Định vị:** Biết SQL cơ bản + chăm click test. Có thể copy-paste code đơn giản nhưng không cần hiểu sâu.

| # | Task | Mô tả chi tiết | Time | Deliverable |
|---|------|----------------|------|-------------|
| **PR1** | Viết **SQL seed data Việt Nam** | Mở rộng `infra/script/05_seed_vietnam_data.sql`: 30+ facility (mỏ Quảng Ninh, kho Nhà Bè, NMD Solar Ninh Thuận, KCN Bắc Ninh...) + 6 region + 10 alert rule + 8 fuel type. Leader cung cấp template 5 row, người C nhân rộng theo danh sách thực tế VN. | 5h | `infra/script/05_seed_vietnam_data.sql` (300+ dòng INSERT) |
| **PR2** | **Postman Collection** + import OpenAPI | Sau khi Leader xong Spring Boot, người C mở Swagger UI → import vào Postman → save collection 30+ request → export JSON. | 3h | `tests/postman/ves-api.postman_collection.json` |
| **PR3** | **Manual test report** (3 platform) | Click qua mọi endpoint (Postman), mọi màn JavaFX, mọi màn Android trên điện thoại. Ghi `docs/TEST_REPORT.md` với screenshot từng bước + log bug nếu có. | 6h | `docs/TEST_REPORT.md` (15-20 trang screenshot) |
| **PR4** | **Chụp screenshot bộ ảnh demo** | 6 màn JavaFX + 5 màn Android, chụp đẹp, crop, đặt tên rõ ràng. Dùng cho slide + báo cáo Chương 3. | 3h | `docs/screenshots/` (~15 PNG) |
| **PR5** | Thêm **2 màn JavaFX phụ** (About + Settings) | Leader cung cấp template `AboutController.java` + `about.fxml` đầy đủ → người C copy → đổi text (tên thành viên, version), thêm logo. Tương tự cho Settings screen (theme toggle). KHÔNG yêu cầu hiểu kiến trúc. | 4h | `desktop-admin/src/.../controller/AboutController.java`, `SettingsController.java` + 2 FXML |

**Tổng**: ~21h trong 10 tuần (~2.1h/tuần). 4 task non-coding + 1 task copy-paste an toàn.

---

#### 👤 LEADER A — Bạn (~80%)

**Phần KỸ THUẬT (Leader gộp luôn UI polish, media phụ, PM):**

| Hạng mục | Sub-task | Time tổng |
|----------|----------|-----------|
| **Spring Boot Backend API** | Setup + JWT + Security + CRUD 5 entity + KPI endpoints + SSE + Report PDF/Excel + Swagger | ~40h |
| **Flink Jobs** | 5 jobs: RawSink + WindowAgg + AlertDetect + Enrichment + ESI Calculator | ~25h |
| **Data Generators** | 3 generators: FuelPrice (giữ) + GridLoad + Renewable (Inventory/Emission stretch goal) | ~12h |
| **JavaFX Admin Core** | Setup + 3-layer + Login + Dashboard + CRUD 4 entity chính + JDBC layer + 15+ JUnit + Profile screen + CSS theme | ~32h |
| **Android Core** | Setup + Retrofit + Login + Home + Map + Alert list + LLM Insight (FCM stretch goal) | ~25h |
| **Database** | Schema mở rộng (dim + fact + agg + views) + index tuning | ~8h |
| **Docker & Deploy** | docker-compose Lite + scripts `run.sh/.ps1` + healthcheck + cloudflared tunnel guide | ~8h |
| **Integration & E2E test** | Test toàn pipeline + fix bug + smoke test trên máy sạch | ~10h |
| **Chương 3 & 5 báo cáo** | Mô tả UI/UX + Kiến trúc + Thuật toán ESI + Design Patterns | ~8h |
| **PM tasks** | GitHub repo setup (branch protection, PR template, Projects board, CODEOWNERS) + AI Usage Log | ~4h |
| **Tổng** | | **~172h** (~17h/tuần × 10 tuần) |

**PR ước tính Leader**: 30-40 PR.

> 💡 Leader gộp các task UI polish (3 màn phụ, validation, JUnit) — vì giờ chỉ 2 thành viên, không đủ chia hết các task này. Người C chỉ làm 2 màn copy-paste đơn giản nhất (About + Settings).

### 22.4 🛡️ Anti-burnout playbook cho Leader

Vì Leader chịu 70% workload, **cần kỷ luật để không kiệt sức**. 12 nguyên tắc:

#### A. Tận dụng tối đa AI (đúng tinh thần hướng dẫn ĐA cho phép)
1. **Cursor AI / Claude Code** generate code khung → Leader chỉ review + fix. Mỗi file/module bắt đầu bằng AI scaffold, không tự gõ từ scratch.
2. **Spring Initializr** — không tự tạo `pom.xml` từ đầu, dùng start.spring.io.
3. **JHipster** (tùy chọn) — generate Spring Boot + entity + CRUD + JWT auth tự động trong 5 phút.
4. **Scene Builder** cho JavaFX FXML — kéo thả thay vì viết tay.
5. **AI Usage Log** ghi rõ prompt → vừa tuân thủ quy định §5, vừa làm bằng chứng pháp lý nếu giáo viên hỏi.

#### B. Scope discipline (giữ MVP)
6. **MVP bắt buộc tuần 5** — chỉ những gì cần để **pass môn** (tham chiếu §12 checklist). Tính năng "đẹp" (i18n, dark mode, animation) cắt thẳng.
7. **3 generator thay vì 5** — FuelPrice + GridLoad + Renewable đủ minh họa. Inventory + Emission là "stretch goal" tuần 8 nếu kịp.
8. **Bỏ Metabase nếu trễ** — đã có overlay `bi.yml`, có thì show, không có cũng không sao.
9. **Bỏ WebSocket nếu trễ** — SSE là đủ, WebSocket chỉ là điểm cộng.
10. **Bỏ Firebase FCM nếu trễ** — alert hiển thị trong app là đủ, push notification là stretch goal.

#### C. Boundary với thành viên
11. **Daily standup 15 phút** — chỉ hỏi 3 câu: "Hôm qua làm gì? Hôm nay làm gì? Có bị block không?". KHÔNG dùng giờ Leader để giải thích kiến thức cơ bản cho thành viên — link tutorial cho họ tự đọc.
12. **Code review thành viên < 30 phút mỗi PR** — không re-write của họ. Nếu PR quá tệ → merge tạm rồi Leader refactor sau, không để PR pending lâu chặn flow.

### 22.5 Quy trình "không để thành viên block Leader"

Vấn đề kinh điển: thành viên yếu chậm trễ → Leader phải đợi để merge → blocking đường găng.

**Giải pháp 4 lớp:**

1. **Tách module độc lập** — code thành viên KHÔNG đụng vào core. VD: Người D thêm Profile screen ở folder riêng `controller/profile/`, không sửa `LoginController` của Leader.
2. **Feature flag** — code Leader có thể chạy độc lập kể cả khi feature của thành viên chưa xong. VD: menu "Profile" disable nếu module chưa merge.
3. **Deadline buffer** — task thành viên deadline T-3 ngày trước deadline thật của Leader. Trễ → Leader có 3 ngày làm thay.
4. **Pair với AI thay vì pair với Leader** — khuyến khích thành viên dùng Cursor/Copilot/Claude khi bí thay vì hỏi Leader (Leader không phải lúc nào cũng rảnh).

### 22.6 Bảng "Ai làm gì" tổng kết (cheat sheet 1 trang)

```
┌────────────────────────────────────────────────────────────────────┐
│  LEADER A (80%) — Bạn                                              │
│  ├─ Spring Boot API (full: auth + CRUD + KPI + SSE + Report)       │
│  ├─ Flink Jobs (5: RawSink + WindowAgg + Alert + Enrich + ESI)     │
│  ├─ Android Core (Login + Home + Map + Alert + LLM Insight)        │
│  ├─ JavaFX Core (Login + Dashboard + 4 CRUD + JDBC + 15 JUnit)     │
│  ├─ Data Generators (3: FuelPrice + GridLoad + Renewable)          │
│  ├─ Database schema + index + views                                │
│  ├─ Docker compose Lite + scripts run.sh/run.ps1                   │
│  ├─ Integration + Deploy + Cloudflared tunnel                      │
│  ├─ GitHub setup (branch protection, PR template, Projects)        │
│  ├─ Báo cáo Chương 3, 5 + AI Usage Log                             │
│  └─ ~172h / 10 tuần / 30-40 PR                                     │
│                                                                    │
│  NGƯỜI B (10%) — Doc & Media                                       │
│  ├─ PR1: ERD + Use-case + Architecture diagram                     │
│  ├─ PR2: Báo cáo Chương 1 + 2                                      │
│  ├─ PR3: Báo cáo Chương 4 + 6                                      │
│  ├─ PR4: Slide PowerPoint 20-25 slide                              │
│  └─ PR5: Video demo 5-7 phút + Notion page                         │
│      ~37h / 10 tuần / 5 PR                                         │
│                                                                    │
│  NGƯỜI C (10%) — Data & QA                                         │
│  ├─ PR1: SQL seed data VN (30+ facility, 6 region, 10 rule)        │
│  ├─ PR2: Postman collection (import OpenAPI từ Swagger)            │
│  ├─ PR3: Manual test report (3 platform) + bug log                 │
│  ├─ PR4: Screenshot bộ 6 màn JavaFX + 5 màn Android                │
│  └─ PR5: 2 màn JavaFX phụ (About + Settings) copy-template         │
│      ~21h / 10 tuần / 5 PR                                         │
└────────────────────────────────────────────────────────────────────┘
```

### 22.7 Bảo vệ uy tín cá nhân trong báo cáo

Đề thi/báo cáo có thể yêu cầu **"Đánh giá thành viên"** (Chương 6). Cách trình bày để bảo vệ công sức Leader mà KHÔNG hạ thấp thành viên:

- **Liệt kê PR + lines of code** từ GitHub Insights → con số khách quan tự nói.
- **Liệt kê deliverable** từng người (link đến file/screenshot) thay vì "anh/chị X làm tốt/kém".
- Đánh giá theo **5 tiêu chí**: (1) đúng deadline, (2) chất lượng PR, (3) tham gia review, (4) đóng góp họp nhóm, (5) tự học. Mỗi tiêu chí 1-5★.
- **Leader tự đánh giá khiêm tốn** — không cần nói "tôi làm hết", chỉ nói "tôi đảm nhận role Architect + Full-stack lead".
- Có **bằng chứng**: GitHub commit history, screenshot review comments, AI Usage Log.

### 22.8 Khi nào Leader nên "rút lui" khỏi task của thành viên?

Đôi khi muốn giúp thành viên nhưng phải kiềm chế. **Quy tắc 3P**:

- **Position**: Task đó có nằm trong scope thành viên hay không? Nếu trong → để họ làm.
- **Priority**: Có blocking đường găng dự án không? Nếu không → để họ học chậm.
- **Pain**: Họ có thực sự kẹt sau 2 giờ nỗ lực không? Nếu chưa → đẩy lại tutorial.

→ Chỉ can thiệp khi cả 3P đều "có vấn đề" hoặc deadline sát T-3 ngày.

### 22.9 Phương án dự phòng nếu thành viên drop-out giữa kỳ

| Tình huống | Action |
|------------|--------|
| 1 thành viên drop-out tuần 1-3 | Thông báo giáo viên. Người còn lại gánh thêm 5 task (chọn lọc task quan trọng nhất). |
| 1 thành viên drop-out tuần 4-7 | Leader làm thay technical task của họ, người còn lại tăng tải doc/media nếu cần. |
| 1 thành viên drop-out tuần 8-10 | Leader làm thay hết, ghi rõ trong báo cáo "do thành viên X không tham gia từ tuần Y". |
| **Cả 2 thành viên drop-out** | Leader báo cáo giáo viên, có thể hoàn thành 1 mình với scope cắt: bỏ Android, chỉ giữ Java desktop + REST API (vẫn pass môn Java). |

---

## 23. AI Execution Plan 🤖 — Lộ trình code mượt, ít lỗi

> ⚠️ **LƯU Ý:** Section này là **Full spec với 14 phase** cho phương án mở rộng tối đa (4 nhóm KPI + ESI + 3 generator + 5 Flink job + full stack). **Đây là TÀI LIỆU THAM KHẢO.** Phương án thực thi **CHÍNH THỨC** là **§24 — Minimalist Plan** (8 phase, tận dụng code có sẵn). Đọc §24 trước khi đọc §23.

> **Mục đích:** Đây là **playbook để AI agent (Cursor/Claude/Copilot) thực thi build dự án end-to-end**, với 14 phase atomic. Mỗi phase có **input rõ ràng / output đo được / smoke test verify / rollback strategy**. Tuân thủ nghiêm ngặt để **không bao giờ build trên nền móng lỗi**.

### 23.1 Nguyên tắc tổng quát "code không lỗi"

1. **Phase atomic** — mỗi phase là 1 đơn vị hoàn chỉnh, **chạy được standalone trước khi qua phase tiếp**.
2. **Smoke test sau mỗi phase** — không pass thì không tag/merge. Test = `curl` hoặc `docker stats` hoặc screenshot.
3. **1 phase = 1 branch + 1 PR** — `phase/01-foundation`, `phase/02-database`... → merge sau khi smoke test pass.
4. **Git tag mỗi phase complete** — `v0.1-foundation`, `v0.2-database`... → có thể `git reset --hard <tag>` rollback nhanh.
5. **Pre-flight check** — đầu mỗi phase: verify phase trước đó vẫn pass smoke test. Nếu fail → fix phase trước trước khi tiếp.
6. **Failure budget**: mỗi phase tối đa **3 lần retry**. Quá 3 → escalate (đổi approach hoặc tạm skip + ghi nợ technical debt).
7. **Build incrementally, test continuously** — KHÔNG build hết rồi mới test. Mỗi 30 phút phải có `curl health` để biết stack còn sống.
8. **AI agent rule** — không sửa file ngoài scope phase hiện tại. Tránh "drive-by refactor" gây vỡ phase khác.

### 23.2 Bản đồ 14 phase (dependency graph)

```
                         ┌─ Phase 0: Foundation
                         │
                         ▼
                    Phase 1: Database Schema
                         │
                         ▼
                    Phase 2: Docker Compose Lite
                         │
                         ▼
            ┌────────────┼────────────┐
            ▼            ▼            ▼
       Phase 3:      Phase 4:     Phase 5:
       Spring Boot   Data Gens    Flink Jobs
       Auth + CRUD   (3)          (Raw+Window)
            │            │            │
            └────────────┼────────────┘
                         ▼
                    Phase 6: Flink Alert + ESI
                         │
                         ▼
                    Phase 7: Spring Boot KPI endpoints
                         │
                         ▼
            ┌────────────┴────────────┐
            ▼                          ▼
       Phase 8:                  Phase 9:
       JavaFX Admin              Android App
       Desktop                   Core
            │                          │
            ▼                          ▼
       Phase 10:                 Phase 11:
       JavaFX Polish             Android LLM
       + JUnit                   Insight
            │                          │
            └────────────┬─────────────┘
                         ▼
                    Phase 12: Deploy Scripts + Tunnel
                         │
                         ▼
                    Phase 13: E2E Integration Test
                         │
                         ▼
                    Phase 14: Documentation Final
```

→ Phase 3, 4, 5 **có thể song song** sau Phase 2. Phase 8, 9 **có thể song song** sau Phase 7.

### 23.3 Chi tiết từng phase

---

#### 🏗️ Phase 0 — Foundation (Repo restructure)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Tạo cấu trúc multi-module Maven, dọn file legacy, setup .gitignore + .env.example |
| **Input** | Repo hiện tại với `Project/` cũ |
| **Output** | Repo theo cấu trúc §17 với `infra/`, `data-generators/`, `flink-jobs/`, `backend-api/`, `desktop-admin/`, `android-app/`, `scripts/`, `docs/`, `.devcontainer/` |
| **Files tạo** | Root `pom.xml` (parent), `.gitignore` updated, `.env.example`, `scripts/run.sh`, `scripts/run.ps1` (stub), `README.md` (skeleton mới), `docs/AI_USAGE_LOG.md` (skeleton) |
| **Files move** | `Project/KafkaProducer/FuelPriceProducer/` → `data-generators/fuel-price-producer/`; `Project/KafkaConsumer/` → `flink-jobs/` (sẽ split sau ở Phase 5/6); `Project/script/` → `infra/script/`; `Project/docker-compose.yml` → `infra/docker-compose.yml` |
| **Smoke test** | `mvn validate` từ root pass. `git log --oneline` shows clean restructure commit. |
| **Time estimate** | 1-2h |
| **Rollback** | `git reset --hard HEAD~1` |
| **Common pitfalls** | Mất history Git khi move folder → dùng `git mv` thay vì xóa+tạo mới |
| **Tag** | `v0.0-foundation` |

---

#### 🗄️ Phase 1 — Database Schema (Extended)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Schema PostgreSQL đầy đủ theo §5: dim + fact + agg + views |
| **Input** | `infra/script/init_fuel_schema.sql` (hiện tại) |
| **Output** | 5 file SQL theo thứ tự: `01_init_dim.sql`, `02_init_fact.sql`, `03_init_agg.sql`, `04_init_views.sql`, `05_seed_basic.sql` |
| **Files tạo** | 5 file SQL trên + cập nhật mount trong docker-compose |
| **Smoke test** | `docker-compose up -d postgresql && sleep 10 && docker exec postgres-database psql -U postgres -d ves_security -c '\dt'` → liệt kê 11 bảng (5 dim + 5 fact + 1 agg + 1 esi). `\dv` → 6 views. |
| **Time estimate** | 2-3h |
| **Rollback** | `docker-compose down -v` (xóa volume), `git revert` |
| **Common pitfalls** | Foreign key constraint khi bảng dim chưa được seed → bảng dim insert TRƯỚC fact |
| **Tag** | `v0.1-database` |

---

#### 🐳 Phase 2 — Docker Compose Lite

| Field | Value |
|-------|-------|
| **Mục tiêu** | docker-compose.yml Lite Profile ~2.7GB RAM theo §21.2 |
| **Input** | Schema từ Phase 1 + docker-compose hiện tại |
| **Output** | `infra/docker-compose.yml` (Lite mặc định), `infra/docker-compose.bi.yml` (overlay Metabase), `infra/docker-compose.dev.yml` (overlay pgAdmin + Kafka UI) |
| **Files tạo** | 3 file compose + `scripts/healthcheck.sh` + `scripts/stop.sh` |
| **Smoke test** | `bash scripts/run.sh` → đợi 60s → `docker stats --no-stream` cho thấy tổng < 3GB RAM. `curl http://localhost:8081/overview` → Flink UI OK. `docker exec postgres-database pg_isready` → ready. |
| **Time estimate** | 2h |
| **Rollback** | `docker-compose -f infra/docker-compose.yml down -v` |
| **Common pitfalls** | `mem_limit` quá thấp → container OOM kill ngay khi start. Test từng service tăng dần. |
| **Tag** | `v0.2-docker-lite` |

---

#### 🔐 Phase 3 — Spring Boot Auth + User CRUD

| Field | Value |
|-------|-------|
| **Mục tiêu** | Spring Boot 3 skeleton + JWT auth + User CRUD + Swagger UI |
| **Input** | Phase 1 schema (table `users`) |
| **Output** | `backend-api/` module hoàn chỉnh chạy được trên port 8080 |
| **Files tạo** | `pom.xml`, `Application.java`, `config/SecurityConfig.java`, `config/JwtFilter.java`, `config/OpenApiConfig.java`, `entity/User.java`, `repository/UserRepository.java`, `service/AuthService.java`, `service/UserService.java`, `controller/AuthController.java`, `controller/UserController.java`, `dto/*` (Login/Register/UserDto), `security/JwtTokenProvider.java`, `application.yml`, `application-docker.yml`, `Dockerfile` |
| **Smoke test** | `mvn spring-boot:run` → `curl -X POST localhost:8080/api/auth/login -d '{"username":"admin","password":"admin"}'` → trả JWT token. `curl -H "Authorization: Bearer <token>" localhost:8080/api/users` → list users. Swagger UI: `http://localhost:8080/swagger-ui.html` mở được. |
| **Time estimate** | 6-8h |
| **Rollback** | `git checkout v0.2-docker-lite -- backend-api/` |
| **Common pitfalls** | Spring Security cấu hình sai → mọi endpoint 401 hoặc 403. Test từng filter chain. BCrypt salt khác nhau giữa seed và login → seed bằng BCrypt 10 rounds chuẩn. |
| **Tag** | `v0.3-auth` |

---

#### 📦 Phase 4 — Data Generators (3 generators)

| Field | Value |
|-------|-------|
| **Mục tiêu** | 3 producer Java gửi data vào 3 Kafka topic |
| **Input** | Phase 2 stack chạy được (Kafka up) |
| **Output** | `data-generators/` multi-module: `common-model/`, `fuel-price-producer/` (refactor cũ), `grid-load-generator/`, `renewable-output-generator/` |
| **Files tạo** | 4 module pom.xml, `common-model/src/.../FuelPrice.java`, `GridLoad.java`, `RenewableOutput.java`, 3 main class (`*Producer.java`), 3 mock generator class |
| **Topics tạo** | `fuel-prices` (giữ), `grid-load` (mới), `renewable-output` (mới). Auto-create nhờ `KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`. |
| **Smoke test** | `java -jar fuel-price-producer.jar &` + `grid-load-generator.jar` + `renewable-output-generator.jar` → 30s sau: `docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic fuel-prices --max-messages 3` → trả 3 message JSON valid. Lặp lại cho 2 topic khác. |
| **Time estimate** | 4-5h |
| **Rollback** | Kill các process Java + `docker exec kafka kafka-topics --delete --topic <topic>` |
| **Common pitfalls** | Generator gửi với schema sai (nhầm field name) → Flink parse fail ở Phase 5. Validate JSON schema bằng Postman/jq trước khi qua Phase 5. |
| **Tag** | `v0.4-generators` |

---

#### ⚡ Phase 5 — Flink Jobs (RawSink + WindowAgg)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Flink job đọc 3 topic → ghi raw vào fact tables + window aggregate mỗi 1 phút |
| **Input** | Phase 1 (schema) + Phase 2 (Flink up) + Phase 4 (data trong Kafka) |
| **Output** | `flink-jobs/` multi-module với 2 job: `raw-sink-job/`, `window-aggregation-job/` |
| **Files tạo** | Refactor `KafkaConsumerApplication.java` thành 2 job riêng. Tạo `RawSinkJob.java`, `WindowAggregationJob.java`, mở rộng `FuelPriceAggregator` thành generic `KpiAggregator<T>` |
| **Smoke test** | `mvn package` → `docker cp` JAR vào flink-jobmanager → `flink run`. Đợi 90s. `docker exec postgres-database psql -U postgres -d ves_security -c 'SELECT COUNT(*) FROM fact_fuel_price_raw'` > 0. Sau 2 phút: `SELECT COUNT(*) FROM agg_kpi_1min` > 0. Flink UI port 8081 hiển thị 2 job RUNNING, không restart. |
| **Time estimate** | 6-8h |
| **Rollback** | `flink cancel <job-id>` cho cả 2 job, `git checkout v0.4-generators` |
| **Common pitfalls** | Composite key sai (kết với region thay vì location) → window agg sai. JDBC sink batch size lớn → latency cao. Set `withBatchSize(1)` để low-latency demo. Flink classloader conflict với Postgres driver → shade-relocate trong pom. |
| **Tag** | `v0.5-flink-basic` |

---

#### 🚨 Phase 6 — Flink Alert + ESI Jobs

| Field | Value |
|-------|-------|
| **Mục tiêu** | 2 job advanced: AlertDetection (broadcast state với dim_alert_rule) + ESICalculator |
| **Input** | Phase 5 (raw + window đang chạy) + Phase 1 seed `dim_alert_rule` |
| **Output** | `flink-jobs/alert-detection-job/`, `flink-jobs/esi-calculator-job/` |
| **Files tạo** | `AlertDetectionJob.java` với `BroadcastProcessFunction` (sub-stream dim_alert_rule from JDBC source định kỳ); `EnergySecurityIndexJob.java` với keyed state pha trộn 4 luồng |
| **Smoke test** | Trigger giá vượt threshold bằng cách tăng đột biến trong generator → check `SELECT * FROM alerts ORDER BY alert_ts DESC LIMIT 5` → có alert mới. `SELECT * FROM esi_score ORDER BY ts DESC LIMIT 5` → có row 0-100. |
| **Time estimate** | 8-10h |
| **Rollback** | Cancel job, revert tag v0.5 |
| **Common pitfalls** | Broadcast state không sync khi rule thay đổi → dùng `BroadcastStateDescriptor` với JDBC source connector chạy mỗi 60s. ESI tính khi thiếu 1 trong 4 input → fallback giá trị mặc định. |
| **Tag** | `v0.6-flink-advanced` |

---

#### 🌐 Phase 7 — Spring Boot KPI Endpoints + SSE

| Field | Value |
|-------|-------|
| **Mục tiêu** | API đọc dữ liệu từ Postgres (Flink đã ghi), trả về JSON cho client |
| **Input** | Phase 3 (auth + CRUD ready) + Phase 6 (data trong Postgres) |
| **Output** | KPI endpoints (§7.3): `/api/kpi/overview`, `/api/kpi/esi`, `/api/kpi/{kpi}`, `/api/alerts`, `/api/reports`, `/api/stream/kpi` (SSE) |
| **Files tạo** | `entity/Facility.java`, `Region.java`, `AlertRule.java`, `Alert.java`, `EsiScore.java` + repositories + services + `KpiController.java`, `AlertController.java`, `ReportController.java`, `controller/SseController.java` |
| **Smoke test** | `curl -H "Authorization: Bearer <token>" localhost:8080/api/kpi/overview` → JSON với 4 KPI nhóm. `curl localhost:8080/api/kpi/esi` → ESI score. `curl -N localhost:8080/api/stream/kpi` → SSE stream cập nhật mỗi 5s. |
| **Time estimate** | 6-8h |
| **Rollback** | Disable KPI endpoints, giữ auth |
| **Common pitfalls** | N+1 query khi list facilities với alerts → dùng `@EntityGraph` hoặc `JOIN FETCH`. SSE timeout 30s mặc định → set `setKeepAlive` hoặc dùng heartbeat. |
| **Tag** | `v0.7-kpi-api` |

---

#### 🖥️ Phase 8 — JavaFX Admin Desktop (Core)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Desktop app với Login + Dashboard + 3 CRUD chính (Region/Facility/AlertRule) |
| **Input** | Phase 7 API ready (JavaFX gọi REST) + Phase 1 schema (JavaFX cũng có JDBC trực tiếp cho thoả §12 checklist) |
| **Output** | `desktop-admin/` JavaFX 21 module hoàn chỉnh |
| **Files tạo** | `Application.java`, `presentation/controller/*` (LoginCtrl, DashboardCtrl, RegionCtrl, FacilityCtrl, AlertRuleCtrl), `presentation/view/*.fxml`, `service/*` (interface + impl), `dao/*` (BaseDao, RegionDao, FacilityDao, AlertRuleDao, UserDao), `model/*`, `util/*` (DatabaseConnection Singleton, PasswordUtils, Validator, JwtClient, SseClient, ChartHelper) |
| **Smoke test** | `mvn javafx:run` → cửa sổ Login mở. Login admin/admin → vào Dashboard. Tạo Region mới → thấy trong list. Chart hiển thị data realtime qua SSE. |
| **Time estimate** | 12-15h |
| **Rollback** | Disable từng controller, giữ Login |
| **Common pitfalls** | JavaFX module path issue trên JDK 17+ → cần `--module-path` + `--add-modules`. Dùng `javafx-maven-plugin` để config sẵn. FXML controller injection sai → check `fx:controller` attribute. |
| **Tag** | `v0.8-javafx-core` |

---

#### 📱 Phase 9 — Android App (Core)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Android app: Login + Home Dashboard + Map + Alert list |
| **Input** | Phase 7 API + Spring Boot chạy với CORS allow all (cho dev) |
| **Output** | `android-app/` Android Studio project |
| **Files tạo** | `MainActivity`, `LoginActivity`, `HomeActivity`, `MapActivity`, `AlertListActivity` + Retrofit `ApiService.java` + `dto/*` + Material Design layouts |
| **Smoke test** | Build APK debug → cài lên Android Emulator → Login → thấy 4 KPI cards trên Home, không crash. Tap Map → thấy markers facilities. Tap Alert → thấy list alerts. |
| **Time estimate** | 12-15h |
| **Rollback** | Disable activity, giữ MainActivity với "Hello World" |
| **Common pitfalls** | `localhost` trong emulator phải là `10.0.2.2`. CORS bị block → Spring Boot phải `allowedOrigins("*")` cho dev. HTTPS-only policy của Android → dev mode dùng `usesCleartextTraffic="true"` trong manifest. |
| **Tag** | `v0.9-android-core` |

---

#### 🎨 Phase 10 — JavaFX Polish + JUnit

| Field | Value |
|-------|-------|
| **Mục tiêu** | Hoàn thiện JavaFX: User CRUD + Alert manager + Report export + CSS theme + 15 JUnit |
| **Input** | Phase 8 core |
| **Output** | JavaFX đầy đủ tính năng theo §8 + test coverage > 50% service layer |
| **Files tạo** | `controller/UserController.java`, `AlertManagerController.java`, `ReportController.java`, `service/ReportService.java` (iText + POI), `style/material.css`, `test/.../FacilityServiceTest.java`, `RegionDaoTest.java` (H2 in-memory), v.v. |
| **Smoke test** | Tạo Alert Rule mới → producer gửi data vượt threshold → Alert tab hiển thị alert real-time qua SSE. Click Export PDF → file PDF tải về có biểu đồ. `mvn test` → ≥ 15 test pass. |
| **Time estimate** | 8-10h |
| **Rollback** | Disable Export, giữ core tính năng v0.8 |
| **Common pitfalls** | iText 7 license (free chỉ AGPL) → đính kèm vào báo cáo. JUnit với JavaFX UI → dùng TestFX. Service test mock DAO bằng Mockito. |
| **Tag** | `v0.10-javafx-polish` |

---

#### 🤖 Phase 11 — Android LLM Insight (Gemini)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Tab LLM Insight: gọi /api/kpi/overview → gửi sang Gemini → hiển thị markdown response |
| **Input** | Phase 9 core + Gemini API key |
| **Output** | Activity LLM Insight + Room DB cache |
| **Files tạo** | `LlmInsightActivity.java`, `service/GeminiApiClient.java` (Retrofit), `db/InsightDao.java` (Room), `db/AppDatabase.java`, markdown rendering library (`io.noties:markwon`) |
| **Smoke test** | Tap tab "AI Insight" → chọn khoảng thời gian → tap Generate → ~3s sau hiển thị markdown phân tích tiếng Việt từ Gemini. Tap lại với cùng input → trả từ cache < 100ms. |
| **Time estimate** | 4-6h |
| **Rollback** | Hide tab LLM Insight, giữ 4 màn core |
| **Common pitfalls** | Gemini API endpoint v1beta path khác v1 → check doc. Rate limit 60/phút → cache 5 phút. API key lộ → dùng BuildConfig hoặc fetch từ Spring Boot proxy endpoint. |
| **Tag** | `v0.11-llm-insight` |

---

#### 🚀 Phase 12 — Deploy Scripts + Cloudflared Tunnel

| Field | Value |
|-------|-------|
| **Mục tiêu** | Bootstrap scripts one-click + cloudflared tunnel setup |
| **Input** | Tất cả phase trước hoàn thành |
| **Output** | `scripts/run.sh`, `scripts/run.ps1`, `scripts/tunnel.sh`, `scripts/stop.sh`, `scripts/healthcheck.sh`, `.devcontainer/devcontainer.json`, `.env.example` |
| **Smoke test** | `git clean -xfd` (sạch hoàn toàn). `cp infra/.env.example .env`. `bash scripts/run.sh`. Đợi 90s. `curl http://localhost:8080/actuator/health` → UP. `bash scripts/tunnel.sh` → ra URL `https://*.trycloudflare.com`. `curl <url>/api/kpi/overview` từ máy khác → JSON. |
| **Time estimate** | 4-5h |
| **Rollback** | Xóa scripts, dùng `docker-compose` thủ công |
| **Common pitfalls** | CRLF vs LF trong script .sh trên Windows → đảm bảo `.gitattributes` set `*.sh text eol=lf`. cloudflared path khác giữa Windows/Mac → script detect OS. |
| **Tag** | `v0.12-deploy` |

---

#### 🧪 Phase 13 — E2E Integration Test

| Field | Value |
|-------|-------|
| **Mục tiêu** | Test toàn pipeline + test trên máy "sạch" (Codespaces hoặc máy khác) |
| **Input** | Phase 12 deploy ready |
| **Output** | `tests/e2e/run-e2e.sh` (Postman + Newman) + `docs/TEST_REPORT.md` |
| **Files tạo** | Newman collection + scripts E2E + test report markdown |
| **Smoke test** | Trên Codespaces: clone repo → `bash scripts/run.sh` → tất cả container UP → Newman run E2E suite → ≥ 95% test pass. JavaFX build trên Codespaces → mở được. APK build trên CI → cài được. |
| **Time estimate** | 6-8h |
| **Rollback** | Skip test, ghi nhận technical debt |
| **Common pitfalls** | Test phụ thuộc timing (data từ Flink chưa kịp ghi) → retry với exponential backoff 30s. Test order dependency → mỗi test phải atomic (cleanup sau test). |
| **Tag** | `v0.13-e2e` |

---

#### 📚 Phase 14 — Documentation Final

| Field | Value |
|-------|-------|
| **Mục tiêu** | README.md cuối + AI Usage Log + module README + comment quan trọng trong code |
| **Input** | Phase 13 stable |
| **Output** | `README.md` (rewrite), `docs/RUNBOOK.md`, `docs/ANDROID_SETUP.md`, `docs/AI_USAGE_LOG.md` (đầy đủ), module README cho từng folder |
| **Files tạo** | 4 doc chính + README cho mỗi module |
| **Smoke test** | Người chấm đọc README → biết cách clone + run trong 5 phút. AI Usage Log có ≥ 20 prompt với verify note. |
| **Time estimate** | 4-6h |
| **Rollback** | N/A |
| **Common pitfalls** | README quá dài → chia ra. Markdown render lỗi trên GitHub → preview trước khi commit. |
| **Tag** | `v1.0-final` 🎉 |

---

### 23.4 Tổng thời gian ước tính

| Cụm phase | Thời gian | Cộng dồn |
|-----------|-----------|----------|
| Phase 0-2 (Foundation) | 5-7h | Tuần 1 |
| Phase 3-4 (Auth + Gens) | 10-13h | Tuần 2 |
| Phase 5-6 (Flink) | 14-18h | Tuần 3-4 |
| Phase 7 (KPI API) | 6-8h | Tuần 4 |
| Phase 8-9 (JavaFX + Android core) | 24-30h | Tuần 5-7 |
| Phase 10-11 (Polish + LLM) | 12-16h | Tuần 7-8 |
| Phase 12-13 (Deploy + E2E) | 10-13h | Tuần 9 |
| Phase 14 (Doc) | 4-6h | Tuần 10 |
| **TỔNG AI execution** | **85-111h** | 10 tuần |

> 💡 Note: Đây là thời gian "AI + Leader review". Thực tế khi AI agent code, Leader chỉ cần review và verify smoke test → có thể giảm 30-50% wall-clock time.

### 23.5 Pre-flight checklist trước khi AI bắt đầu mỗi phase

Trước khi gõ "tiếp tục Phase X", Leader (bạn) verify:

```
☐ Phase X-1 đã tag (git tag --list | grep v0.X-1)
☐ Smoke test của Phase X-1 vẫn pass NOW (không chỉ lúc merge)
☐ Branch phase/0X-... chưa tồn tại (sẽ tạo mới)
☐ Disk space đủ (≥ 5GB free)
☐ Stack đang chạy ổn định (docker stats trong giới hạn)
☐ Đọc lại "Common pitfalls" của Phase X
☐ Có lệnh rollback sẵn (note giấy phòng khi cần)
```

→ Tích đủ 7 ô mới gõ command "AI proceed Phase X" cho agent.

### 23.6 Khi AI báo "phase fail" → debug protocol

1. **Đừng panic, đừng skip.** Phase fail là dấu hiệu nền móng yếu.
2. **AI in ra error log đầy đủ** — `docker logs <service> --tail 100`, stack trace, env vars.
3. **Check phase trước có thực sự pass smoke test không?** Nhiều khi phase X-1 "pass nháy mắt" rồi gây Phase X fail.
4. **3-strike rule**: AI thử fix tối đa 3 lần. Mỗi lần thay 1 thông số. Quá 3 → escalate.
5. **Escalate options**:
   - **(a)** Đổi approach: VD Phase 6 fail vì broadcast state phức tạp → đổi sang regular keyed state với poll DB mỗi 60s.
   - **(b)** Skip + ghi tech debt: VD Phase 11 LLM fail vì rate limit → skip, để Android dùng mock data cho LLM tab.
   - **(c)** Cắt scope: phase quá khó so với deadline → bỏ feature đó, không bỏ project.
6. **Document** mỗi fail trong `docs/AI_USAGE_LOG.md` để báo cáo Chương 5/6.

### 23.7 AI agent prompts mẫu (Leader gõ vào Cursor)

> Đây là **prompt template** Leader dùng khi yêu cầu AI thực thi từng phase:

```
@codebase
Implement Phase X (xem §23.3 trong UPGRADE_PLAN.md).

Requirements:
1. Đọc input/output/files tạo từ bảng Phase X
2. Tuân thủ "Common pitfalls" — không lặp lại lỗi đã ghi
3. Sau khi code xong, chạy smoke test (lệnh trong bảng)
4. Nếu pass → commit + tag v0.X-<name>
5. Nếu fail → 3-strike retry, sau đó ESCALATE

Constraints:
- Không sửa file ngoài scope Phase X (no drive-by refactor)
- Code follow conventions: lombok @Builder, Spring @Service, JavaFX FXML
- Comment chỉ khi non-obvious intent
- Tuân thủ Lite Profile §21 (mem_limit, JVM flags)

Start now.
```

### 23.8 Bảng tracking tiến độ (Leader maintain)

| Phase | Status | Tag | Date | Smoke test pass? | Notes |
|-------|--------|-----|------|-------------------|-------|
| 0 | ⬜ | — | — | — | |
| 1 | ⬜ | — | — | — | |
| 2 | ⬜ | — | — | — | |
| 3 | ⬜ | — | — | — | |
| 4 | ⬜ | — | — | — | |
| 5 | ⬜ | — | — | — | |
| 6 | ⬜ | — | — | — | |
| 7 | ⬜ | — | — | — | |
| 8 | ⬜ | — | — | — | |
| 9 | ⬜ | — | — | — | |
| 10 | ⬜ | — | — | — | |
| 11 | ⬜ | — | — | — | |
| 12 | ⬜ | — | — | — | |
| 13 | ⬜ | — | — | — | |
| 14 | ⬜ | — | — | — | |

> Mỗi lần phase hoàn thành: đánh ✅, ghi tag + ngày + checkmark smoke test. Bảng này paste vào `docs/PROGRESS.md` cũng tốt.

### 23.9 Quality gate — Standards cho mỗi PR

Mỗi PR Leader merge phải pass 5 gate:

| Gate | Check | Tool |
|------|-------|------|
| **Compile** | `mvn compile` không lỗi | Maven |
| **Test** | `mvn test` pass (nếu có test phase đó) | JUnit |
| **Run** | Smoke test trong §23.3 pass | Manual / curl |
| **Lint** | Không warning critical | Spotbugs / Checkstyle (optional) |
| **Doc** | Có comment public API + update README nếu cần | Manual review |

→ Fail bất kỳ gate nào → KHÔNG merge, fix trước.

### 23.10 Khi nào KHÔNG dùng plan này

- Nếu nhóm có thành viên giỏi tương đương → dùng §10 + §11 phân công đều.
- Nếu deadline gấp < 4 tuần → cắt scope: bỏ Android + Flink ESI, giữ JavaFX + REST API + Flink basic.
- Nếu giáo viên yêu cầu chỉ Swing (không JavaFX) → swap Phase 8 với Swing app, các phase khác không đổi.
- **Nếu muốn đồ án gọn, dễ kiểm soát, tận dụng code có sẵn** → **đọc §24 thay vì §23** (KHUYẾN NGHỊ).

---

## 24. ⚡ Minimalist Execution Plan — KHUYẾN NGHỊ (đồ án gọn, tận dụng code có sẵn)

> 🎯 **TRIẾT LÝ:** Không phải dự án nào điểm cao cũng phức tạp. **Đồ án gọn + hoàn thiện 100% > đồ án lớn + lỗi 30%**. Section này thiết kế cho team Leader + 2 thành viên yếu, deadline 10 tuần, **giữ nguyên 90% code có sẵn**, chỉ thêm phần thật sự cần thiết cho 2 môn (Java + Android).

### 24.1 Triết lý "Scope thật sự cần thiết"

| Nguyên tắc | Giải thích |
|------------|------------|
| **1. Code có sẵn = vàng** | Producer + Consumer + 3 stream processing + PostgreSQL schema fuel đã chạy ổn định → KHÔNG động vào, chỉ thêm thắt |
| **2. Mục tiêu = pass 2 môn** | Java cần JavaFX/Swing + JDBC + 3-layer + JUnit. Android cần REST API + UI. **Không cần ESI, không cần 4 nhóm KPI, không cần broadcast state Flink phức tạp** |
| **3. Demo cần 1 luồng end-to-end** | Producer → Kafka → Flink → Postgres → API → JavaFX (live chart) → Android (đọc API) là ĐỦ. Không cần demo 5 pipeline song song |
| **4. Sự khác biệt tạo điểm cao** | Stack Kafka+Flink+PostgreSQL+JavaFX+Android+Real-time đã VƯỢT 95% đồ án sinh viên. Không cần thêm độ phức tạp |
| **5. Stretch goal** | Nếu dư thời gian → thêm 1 thứ "wow": LLM Insight Gemini HOẶC Cloudflared tunnel demo HOẶC ESI dashboard Metabase đẹp |

### 24.2 Đề tài giản lược (giữ nguyên ý tưởng cốt lõi)

**Tên đề tài:** **"Vietnam Energy Security Real-time Monitor (VES-Monitor)"**

> ✅ Pivot từ "Fuel Price Monitoring" lên "Energy Security" theo **Light 4-pillar strategy** (xem §24.2.1) — chỉ tốn +15-18h Leader so với bản fuel-only mà cover được đủ 4 pillars an ninh năng lượng.

**Mô tả 3 dòng:**
- Nền tảng giám sát **4 pillars an ninh năng lượng Việt Nam**: Nguồn cung & Hạ tầng (dự trữ chiến lược), Biến động Kinh tế (giá NL thế giới), Phụ tải & Tiêu thụ (lưới điện), Chuyển đổi & Môi trường (NL tái tạo + CO2).
- Xử lý real-time bằng Kafka + Flink. **3 generator song song** đẩy data vào 3 topic riêng → 3 luồng Flink song song (raw + window + alert rule-based) → PostgreSQL (10 bảng + 8 views).
- Trực quan hóa qua **JavaFX Admin Desktop** (dashboard 4-tab pillars + CRUD) + **Android App** (bottom-nav 4 pillars) + **Metabase** (BI 4-pillar tổng quan).

### 24.2.1 Light 4-pillar Strategy — Coverage chiều rộng + Actionable Output Layer

> 💡 **Triết lý:** Show **đủ 4 pillars** + **mỗi pillar phải có output features hỗ trợ an ninh năng lượng, không chỉ vẽ số liệu**. Stack 4 lớp: **DETECT** (alert_rules đa pillar) → **PREDICT** (forecast views) → **RECOMMEND** (recommendations + recommendation_text trong views) → **SCORE** (Energy Security Score 0-100).
>
> Không làm Flink broadcast state ESI phức tạp (giữ ESI ở SQL view), không star schema. Pillar 2 (giá NL) là pillar chính (code có sẵn), 3 pillar còn lại là light coverage **với đầy đủ actionable views**.

| Pillar | KPIs chính | Implementation Light | Effort thêm |
|--------|-----------|----------------------|-------------|
| **1. Nguồn cung & Hạ tầng** | Dự trữ chiến lược (90 ngày), stock_days theo region | 1 bảng `fuel_inventory_raw` + seed tĩnh 6 row (3 region × 2 fuel) + 1 view `v_pillar1_inventory_status` 4 status (OK/BELOW_TARGET/WARNING/CRITICAL) | ~0h (chỉ SQL) |
| **2. Biến động Kinh tế** | Giá WTI/Brent/Gasoline/Diesel/Natural Gas × 5 sàn quốc tế | **Code có sẵn 100%** — `fuel_prices_raw`, `fuel_price_window_agg`, `fuel_price_alerts` | 0h |
| **3. Phụ tải & Tiêu thụ** | Phụ tải lưới điện theo region (MW), tỷ lệ load/capacity, giờ cao điểm | 1 generator `grid-load-generator` (~150 LOC) → topic `grid-load` → 1 bảng `grid_load_raw` + 1 view `v_pillar3_grid_load_latest` (4 status NORMAL/HIGH/WARNING/CRITICAL) | ~6h |
| **4. Chuyển đổi & Môi trường** | Sản lượng solar/wind/hydro, intensity CO2 (kg/MWh) | 1 generator `renewable-generator` (~180 LOC) → topic `renewable-output` → 2 bảng + 2 view (`v_pillar4_renewable_share`, `v_pillar4_emission_intensity`) | ~8h |

**Tổng thêm**: ~15-18h Leader so với Minimalist fuel-only ban đầu. **Không thêm Flink job mới** — 2 generator mới đẩy data trực tiếp Kafka, dùng chung Flink job alert (Phase 3) bằng cách thêm `KeyedProcessFunction` đọc cả 3 topic. **Không thêm endpoint API** quá nhiều — gộp vào `AdminController` với `/api/pillars/{1,2,3,4}/...` (4 endpoint mới).

### 24.3 Checklist code có sẵn — tận dụng KHÔNG đụng vào

> ✅ = giữ nguyên / sử dụng lại 100%, ⚠️ = chỉ tinh chỉnh nhẹ, ❌ = không cần

| Component | File hiện tại | Status | Ghi chú |
|-----------|---------------|--------|---------|
| Kafka Producer | `KafkaProducer/FuelPriceProducer/` | ✅ giữ nguyên | Move thành module `data-generators/fuel-price-producer/` |
| Mock generator | `MockDataGenerator.java` | ✅ giữ nguyên | Đã đủ data realistic |
| HTTP API source | `HttpApiSource.java` (Alpha Vantage) | ✅ giữ nguyên | Để demo "data thật" |
| Model FuelPrice | `model/FuelPrice.java` | ✅ giữ nguyên | DTO chuẩn |
| Flink Consumer App | `KafkaConsumerApplication.java` | ⚠️ thêm 1 sink alert | Giữ nguyên 3 stream gốc, chỉ thêm process function alert |
| Kafka source | `KafkaInvoiceSource.java` | ✅ giữ nguyên | OK |
| JDBC sink | `JdbcPostgresSink.java` | ✅ giữ nguyên | Đã handle batch + retry |
| Window aggregator | `process/FuelPriceAggregator.java` + `WindowFunction` | ✅ giữ nguyên | Đã có window 1 phút |
| Price change detector | `process/PriceChangeDetector.java` | ✅ giữ nguyên | Đã có CEP-like logic |
| PostgreSQL schema | `script/init_fuel_schema.sql` | ⚠️ thêm 4 bảng | `users`, `regions`, `alerts`, `alert_rules` — KHÔNG sửa bảng có sẵn |
| Docker Compose | `docker-compose.yml` | ⚠️ thêm `mem_limit` | Giữ 6 service, chỉ thêm memory cap theo §21 |
| Metabase config | (Metabase tự cấu hình) | ✅ giữ nguyên | Người dùng setup dashboard 1 lần |

> 💡 **Tổng kết:** ~90% code có sẵn giữ nguyên. Chỉ thêm: 1 alert process function trong Flink, 4 bảng SQL, và toàn bộ phần MỚI (Spring Boot + JavaFX + Android).

### 24.4 Phần BỔ SUNG tối thiểu (3 module mới)

#### 🌐 Module 1: Spring Boot REST API (~600 dòng code, GỌN)

| Đặc điểm | Quyết định |
|----------|------------|
| **Số module Maven** | **1 module duy nhất** (không multi-module phức tạp) |
| **Persistence** | **JdbcTemplate** (KHÔNG dùng JPA/Hibernate — nhanh, ít magic, dễ debug) |
| **Số entity** | **5**: `User`, `Region`, `AlertRule`, `Alert`, `FuelPriceDto` |
| **Số controller** | **3**: `AuthController`, `FuelPriceController`, `AdminController` (gộp Region + Rule + Alert) |
| **Endpoints** | **8 endpoint** (đủ demo, không thừa) |
| **Security** | JWT đơn giản (HS256, 24h expiry), không cần refresh token |
| **Realtime** | **SSE** `/api/stream/fuel-prices` (KHÔNG cần WebSocket) |
| **Doc** | Swagger UI tự sinh |

**Danh sách 8 endpoint:**
```
POST /api/auth/login                  # Trả JWT
GET  /api/fuel-prices                 # Latest 100 row từ fuel_prices
GET  /api/fuel-prices/aggregated      # Window 1 phút (table fuel_prices_aggregated)
GET  /api/fuel-prices/changes         # Price changes (table fuel_price_changes)
GET  /api/alerts                      # List alerts
GET  /api/regions                     # List regions
POST /api/admin/alert-rules           # Create alert rule (admin only)
GET  /api/stream/fuel-prices          # SSE realtime
```

#### 🖥️ Module 2: JavaFX Admin Desktop (~1200 dòng code, ĐỦ ĐIỂM)

| Đặc điểm | Quyết định |
|----------|------------|
| **Kiến trúc** | 3-layer chuẩn: `presentation/` + `service/` + `dao/` |
| **JDBC** | Trực tiếp `DriverManager` (KHÔNG dùng JPA, đúng yêu cầu môn Java) |
| **Số màn** | **5 màn** (đủ chấm điểm) |
| **CRUD** | 3 entity: `Region`, `AlertRule`, `User` (đủ checklist §12) |
| **Auth** | Login JDBC + BCrypt + session lưu trong Singleton |
| **Chart** | JavaFX LineChart cập nhật từ SSE/polling 5s |
| **JUnit** | 10-12 test (DAO + Service cơ bản) |
| **CSS** | 1 file `material.css` cho toàn app |

**5 màn:**
```
1. LoginView         → đăng nhập admin/admin
2. DashboardView     → 5 line chart fuel prices real-time + 4 KPI cards
3. RegionView        → CRUD region (form + table)
4. AlertRuleView     → CRUD alert rule (form + table)
5. UserView          → CRUD user (form + table) [hoặc Profile screen tùy]
```

> 💡 **Đủ thoả 100% checklist môn Java (§12)**: Swing/JavaFX ✅, JDBC trực tiếp ✅, 3-layer ✅, Login ✅, 3 CRUD ✅, JUnit ✅, Form validation ✅, CSS ✅.

#### 📱 Module 3: Android App (~800 dòng code, GỌN)

| Đặc điểm | Quyết định |
|----------|------------|
| **Số activity** | **4 activity** (đủ demo) |
| **Networking** | Retrofit + Gson |
| **Charting** | MPAndroidChart (đơn giản nhất) |
| **Local DB** | KHÔNG cần (data luôn từ API) — bỏ Room |
| **FCM** | KHÔNG cần (stretch goal nếu kịp) |
| **LLM Insight** | KHÔNG cần ban đầu (stretch goal tuần 8 nếu dư time) |

**4 activity:**
```
1. LoginActivity     → đăng nhập + lưu JWT vào SharedPreferences
2. HomeActivity      → 4 KPI cards (latest price 4 loại fuel chính) + danh sách alert mới nhất
3. ChartActivity     → Line chart history fuel price theo loại
4. AlertListActivity → List + filter alert theo severity
```

### 24.5 Lộ trình 11 phase Minimalist + Light 4-pillar (thay cho 14 phase §23)

> Mỗi phase vẫn theo nguyên tắc atomic + smoke test + rollback (§23.1) nhưng GỌN HỞN.
>
> **11 phase** = 8 phase gốc + **Phase 2.5** (schema 4-pillar) + **Phase 2.6** (security features layer: detect/predict/recommend/score) + **Phase 4** mới (2 generator Pillar 3/4). Phase 4 cũ (REST API) đẩy thành Phase 4.5.

---

#### 🏗️ Phase 0 — Foundation & Verify (1-2h)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Verify code có sẵn chạy, restructure repo gọn |
| **Action** | `docker compose up -d` → verify Kafka + Flink + Postgres + Metabase OK. `mvn package` producer + consumer. Submit consumer JAR lên Flink → thấy job RUNNING. |
| **Restructure** | Move `Project/KafkaProducer/FuelPriceProducer/` → `data-generators/fuel-price-producer/`. Move `Project/KafkaConsumer/` → `flink-jobs/fuel-flink-job/`. Move `Project/script/` → `infra/script/`. Move `Project/docker-compose.yml` → `infra/docker-compose.yml`. |
| **Smoke test** | `curl http://localhost:8081/jobs` → có 1 job RUNNING. `docker exec postgres-database psql -U postgres -d fuel_db -c 'SELECT COUNT(*) FROM fuel_prices'` > 0. Metabase mở `http://localhost:3000`. |
| **Tag** | `v0.0-verified` |

---

#### 🐳 Phase 1 — Docker Lite + Bootstrap Scripts (2h)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Tối ưu RAM ~2.7GB + script run.sh/run.ps1 one-click |
| **Action** | Apply `mem_limit` theo §21.2. Tạo `scripts/run.sh`, `scripts/run.ps1`, `scripts/stop.sh`, `.env.example`. |
| **Smoke test** | `bash scripts/run.sh` → 60s sau `docker stats --no-stream` < 3GB tổng. Tất cả service UP. |
| **Common pitfalls** | Kafka `KAFKA_HEAP_OPTS=-Xmx384m` quá thấp → OOM. Test từng service tăng dần. |
| **Tag** | `v0.1-docker-lite` |

---

#### 🗄️ Phase 2 — Schema mở rộng nhẹ (2-3h)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Thêm 4 bảng ops, KHÔNG sửa bảng có sẵn |
| **Files tạo** | `infra/script/02_init_users_regions.sql`, `03_init_alerts.sql`, `04_seed_basic.sql` |
| **Bảng mới** | `users` (id, username, password_bcrypt, role, created_at) — admin/manager/viewer seed; `regions` (id, code, name, vn_zone) — 6 row VN/Intl; `alert_rules` (id, fuel_type, op, threshold, severity, enabled) — 5 row mẫu; `alerts` (id, fuel_type, location, price, rule_id, severity, ts) |
| **Smoke test** | `psql -c '\dt'` → có thêm 4 bảng, các bảng cũ vẫn còn. `SELECT * FROM users WHERE username='admin'` → trả 1 row. |
| **Tag** | `v0.2-schema` |

---

#### 🛡️ Phase 2.5 — Schema 4-pillar (1-2h) — Light coverage

| Field | Value |
|-------|-------|
| **Mục tiêu** | Mở rộng schema cover Pillar 1/3/4 (Pillar 2 đã có sẵn ở Phase 0). Không cần generator ngay — chỉ chuẩn bị bảng để Phase 4 dùng. |
| **Files tạo** | `infra/script/05_init_pillars.sql` (4 bảng + 4 view), `06_seed_pillars.sql` (seed 6 row inventory cho Pillar 1) |
| **Bảng mới** | `fuel_inventory_raw` (Pillar 1, có generated column `stock_days`); `grid_load_raw` (Pillar 3, generated column `load_pct`); `renewable_output_raw` (Pillar 4, generated `utilization_pct`); `emission_raw` (Pillar 4, generated `intensity_kg_per_mwh`) — tất cả FK về `regions(code)` |
| **View mới** | `v_pillar1_inventory_status` (4 status: CRITICAL/WARNING/BELOW_TARGET/OK); `v_pillar3_grid_load_latest` (DISTINCT ON region, 4 status); `v_pillar4_renewable_share` (theo giờ, pivot solar/wind/hydro); `v_pillar4_emission_intensity` (24h aggregation) |
| **Seed Pillar 1** | 6 row inventory (3 region × 2 fuel) mix đủ 4 status để demo: VN_SOUTH DIESEL 25d=CRITICAL, VN_NORTH GASOLINE+DIESEL=WARNING, VN_CENTRAL GASOLINE=BELOW_TARGET, VN_CENTRAL DIESEL + VN_SOUTH GASOLINE=OK |
| **Smoke test** | Sau apply 6 SQL: `\dt` → **11 tables** (3 fuel + 4 phase2 + 4 phase2.5); `\dv` → **11 views**; `SELECT status, COUNT(*) FROM v_pillar1_inventory_status GROUP BY status` → đủ 4 status. Fresh boot `stop --volumes && run --wait` → tất cả load tự động. |
| **Common pitfalls** | Generated column phải có CASE bảo vệ chia 0 (`WHEN denominator > 0 THEN ...`). FK `regions(code)` đòi `regions` được seed trước (`04_seed_basic.sql` chạy trước `06_seed_pillars.sql`). |
| **Tag** | `v0.2.5-pillars` |

---

#### 🛡️ Phase 2.6 — Security Output Features Layer (2-3h) — Actionable, không chỉ vẽ số liệu

| Field | Value |
|-------|-------|
| **Mục tiêu** | Biến 4 pillars thành **platform an ninh thực sự**: detect → predict → recommend → score. Tất cả implement bằng SQL views + 2 bảng action audit (không cần Flink phức tạp). |
| **Files tạo** | `infra/script/07_init_security_features.sql` (extend alert_rules + 2 tables + 8 views), `08_seed_security_features.sql` (5 multi-pillar rules + 3 sample recommendations) |
| **Schema thay đổi** | `alert_rules`: ADD COLUMN `metric_type` (FUEL_PRICE/INVENTORY_DAYS/GRID_LOAD_PCT/RENEWABLE_PCT/EMISSION_INTENSITY) + `region_code` FK. Relax `fuel_type` NOT NULL. Backward-compat: rules cũ tự lấy `FUEL_PRICE` default. |
| **Tables mới** | `recommendations` (id, pillar 0-4, action_type, severity, title, message, suggested_data JSONB, status PENDING/ACK/DISMISSED, expires_at) — audit trail action; `daily_briefings` (security_score, headline, 4 pillar summaries, key_risks JSONB) — bản tin daily |
| **Action views (8)** | `v_pillar1_supply_outlook` (forecast days_to_critical + dynamic recommendation_text + suggested_donor_region); `v_pillar2_volatility_signal` (σ rolling 1h, signal STABLE/ELEVATED/VOLATILE); `v_pillar2_cross_exchange_divergence` (chênh giá giữa các sàn — tín hiệu địa chính trị); `v_pillar3_load_shedding_plan` (ordered priority list khi overload + suggested_shed_mw + recommendation_text); `v_pillar4_net_zero_progress` (% renewable vs roadmap VN 2026/2030/2050); `v_security_score` (**Light ESI 0-100** weighted 25%% mỗi pillar, status SECURE/STABLE/AT_RISK/CRITICAL); `v_cascade_risks` (compound risks multi-pillar: FUEL_SHORTAGE_RISK, GENERATION_DEFICIT_RISK, CARBON_COST_RISK); `v_active_recommendations` (PENDING list cho dashboard) |
| **Seed** | 5 alert_rules multi-pillar (Pillar 1: 2 rules INVENTORY_DAYS; Pillar 3: 2 rules GRID_LOAD_PCT; Pillar 4: 1 rule EMISSION_INTENSITY). 3 recommendations PENDING (TRANSFER_STOCK CRITICAL, INCREASE_RESERVE WARNING, PEAK_SHAVING_PREP INFO) — UI có data demo ngay. |
| **Smoke test** | Sau apply 8 SQL: 13 tables + 19 views. `SELECT * FROM v_security_score` trả 1 row score ≈ 76 (STABLE) khi data sparse. `SELECT * FROM v_pillar1_supply_outlook` trả 6 row có cột `recommendation_text` chứa tiếng Việt human-readable. `SELECT metric_type, COUNT(*) FROM alert_rules GROUP BY metric_type` trả 4 row đa pillar. Fresh boot `stop --volumes && run --wait` load đủ 8 SQL files thứ tự. |
| **Common pitfalls** | (1) `GREATEST(0, NULL)` trả 0 không NULL → COALESCE không fallback được; phải dùng `CASE WHEN max(col) IS NULL THEN baseline END`. (2) `ALTER TABLE ADD COLUMN IF NOT EXISTS` chỉ hoạt động PostgreSQL 9.6+. (3) Constraint `chk_alert_rules_metric_type` phải wrap trong `DO $$ ... IF NOT EXISTS` để idempotent. |
| **Tag** | `v0.2.6-security-features` |

---

#### 🚨 Phase 3 — Flink Alert Detection (multi-pillar) + Recommendation Generator (4-5h)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Thêm `AlertDetectionFunction` đa-metric vào Flink job. Tự sinh `recommendations` khi alert CRITICAL trigger. |
| **Multi-pillar logic** | Function đọc `alert_rules` lúc start, filter theo `metric_type`. Cho fuel-prices stream → filter rules `metric_type='FUEL_PRICE'`. Sau Phase 4, mở rộng tương tự cho `grid-load`, `renewable-output`, `emission` streams. |
| **Recommendation auto-gen** | Khi alert CRITICAL được tạo → insert kèm 1 row vào `recommendations` với `action_type` derive từ pillar: Pillar 1 → TRANSFER_STOCK; Pillar 2 → HEDGE_IMPORT; Pillar 3 → PEAK_SHAVING; Pillar 4 → DISPATCH_RENEWABLE. `suggested_data` JSONB chứa context (region, threshold, current_value). |
| **Files mới** | `process/AlertDetectionFunction.java`, `source/AlertRulesLoader.java`, `sink/AlertJdbcSink.java`, `sink/RecommendationJdbcSink.java` |
| **Smoke test** | Manually `UPDATE alert_rules SET threshold = 0.01 WHERE id = 1` → đợi 1 phút → `SELECT COUNT(*) FROM alerts WHERE rule_id=1` > 0 + `SELECT COUNT(*) FROM recommendations WHERE pillar=2` tăng. Sau đó reset threshold. |
| **Common pitfalls** | (1) Reload rules dynamic phức tạp → giữ static load lúc start, restart job khi đổi rule. (2) Cooldown 60s/key/rule qua MapState; recommendations dedup 30 phút SQL `WHERE NOT EXISTS`. (3) Maven build issue: `maven-surefire-plugin:3.2.2` chưa cache → corporate proxy fail. Downgrade về 2.12.4 trong parent pom (cached). (4) Kafka topic `fuel-prices` cần được tạo trước khi submit job (hoặc chạy producer trước) — Flink fail nếu topic chưa tồn tại. (5) Flink usrlib không có sẵn — upload JAR qua REST API `POST /jars/upload` + `POST /jars/{id}/run`. |
| **Tag** | `v0.3-flink-alert` |

---

#### 📡 Phase 4 — 2 Generator mới + Flink multi-pillar detection ✅ (HOÀN THÀNH 12 May 2026)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Pillar 3 + Pillar 4 có data **real-time**; Flink mở rộng từ 4 vertex → 9 vertex; AlertDetection multi-pillar (FUEL_PRICE + GRID_LOAD_PCT + EMISSION_INTENSITY); auto-recommendation cho mọi pillar. |
| **Module mới (Maven)** | ✅ `data-generators/grid-load-generator/` (~150 LOC) — Ornstein-Uhlenbeck random walk hướng về target = base + peak boost. ✅ `data-generators/renewable-generator/` (~280 LOC) đẩy 2 topic: `renewable-output` (SOLAR bell-curve 6h-18h + WIND/HYDRO 24/7) + `emission` (CO2 = (grid_load_75% - renewable) × 750 kg/MWh). |
| **Kafka topic mới** | ✅ `grid-load`, `renewable-output`, `emission` (mỗi 3 partition). Pre-created qua `scripts/create_kafka_topics.{sh,ps1}` (idempotent `--if-not-exists`). |
| **Flink mở rộng** | ✅ `KafkaInvoiceSource.createSourceForTopic(topic, groupId)` — generic factory. ✅ 3 POJO mới (`GridLoadEvent` với `getLoadPct()`, `RenewableOutputEvent`, `EmissionEvent` với `getIntensityKgPerMwh()`). ✅ 2 detector mới (`GridLoadAlertDetector`, `EmissionAlertDetector`) cùng pattern MapState 60s cooldown, key=region_code. ✅ `RuleAlertEvent` multi-pillar: thêm `metricType`, 2 constructor mới, dispatch `actionTypeForRecommendation()` theo metric (HEDGE_IMPORT / PEAK_SHAVING / DISPATCH_RENEWABLE / TRANSFER_STOCK) + `getPillar()` 1-4 cho SQL INSERT động. ✅ Helper `addAlertSinks(stream, label)` chung — gắn 2 sink (alerts + recommendations) cho mọi pillar. ✅ Job đổi tên: **"VES-Monitor Real-time Pipeline (4 pillars / 7 streams)"**, 9 vertex. |
| **Schema mở rộng** | ✅ `infra/script/09_alter_alerts_multi_pillar.sql`: thêm `metric_type` (default 'FUEL_PRICE' backward-compat) + relax `fuel_type`/`location` nullable + CHECK constraint 5 loại + re-create `v_active_alerts` view với cột `metric_type`. Idempotent. |
| **Smoke test (live)** | ✅ 30s sau khi start 3 generator: `grid_load_raw` 344 rows, `renewable_output_raw` 432, `emission_raw` 52. Stress test: GRID_LOAD_PCT 95% → 2 alert (CRITICAL+WARNING) + 1 auto-rec `PEAK_SHAVING` pillar=3. EMISSION_INTENSITY 700 → 1 alert WARNING. `v_security_score` rớt từ 70 → 0 cho Pillar 3 sau detection. `v_cascade_risks` thêm `CARBON_COST_RISK` (compound). |
| **Bug đã fix trong phase** | Renewable generator: `EMISSION_INTERVAL_MS / 60_000L = 0` (integer division khi 30s < 60s) → `co2_kg=0/energy_mwh=0`. Đổi sang `double emissionWindowMinutes = 0.5` → intensity hiển thị 267-640 kg/MWh đúng. |
| **Memory** | Docker stack 1.6 GB / 2.85 GB cap (+700 MB từ 4 source + 5 detector). 3 generator JVM ~1.2 GB ở host (ngoài cap docker). |
| **Files mới/sửa** | 2 module mới + 9 source file Flink + 1 SQL + 2 script topic + run.sh/ps1. Chi tiết: `docs/PROGRESS.md` Phase 4 log. |
| **Tag** | `v0.4-generators` |

---

#### 🌐 Phase 4.5 — Spring Boot REST API cover 4 pillars + Security Actions (6-8h) — 🟡 CODE COMPLETE, BUILD PENDING

| Field | Value |
|-------|-------|
| **Trạng thái** | 🟡 Source viết xong (24 file Java + pom + yml + README + smoke script), lint-clean. **Build & runtime test pending** do proxy Bosch dùng NTLM/Negotiate — Maven Wagon không gửi được creds. Khi có hotspot 4G hoặc `cntlm` bridge: `mvn -pl backend-api -am clean package -DskipTests && java -jar backend-api/target/ves-backend-api.jar` |
| **Quyết định stack** | Spring Boot **2.7.18** (không phải 3.x) — để giữ Java 11 đồng bộ với 4 module hiện hữu (3 generator + Flink job). Spring Boot 3.x yêu cầu Java 17 sẽ phá build đồng nhất. |
| **Mục tiêu** | 1 module Spring Boot 2.7, JdbcTemplate, JWT HS256, **14 endpoint** (3 auth + 4 pillar + 2 security + 2 recommendation + 1 alert + 2 raw + 1 health), Swagger UI tại `/swagger-ui.html`, port 8090 |
| **Files tạo (~24 file Java + 4 config):** | `pom.xml`, `application.yml`, `VesApiApplication.java`, `config/{SecurityConfig, JwtTokenProvider, JwtAuthFilter, OpenApiConfig}.java`, `controller/{Auth, Pillar, Security, Recommendation, Alert, RawData, Health}Controller.java` (7 controller), `dao/{User, Pillar, Security, Recommendation, Alert}Dao.java` (5 DAO JdbcTemplate), `dto/*Dto.java` × 14 (Lombok @Data @Builder), `exception/{ApiException, GlobalExceptionHandler}.java`. Tất cả file đã viết xong, ReadLints không có lỗi. |
| **Endpoint Auth (3)** | `POST /api/auth/login` (body `{username,password}` → JWT 8h + UserDto), `GET /api/auth/me` (user theo JWT), `GET /api/health` (DB ping, public, dùng cho cloudflared probe) |
| **Endpoint Pillar (4)** | `GET /api/pillars/1/outlook` (`v_pillar1_supply_outlook`), `GET /api/pillars/2/volatility` (`v_pillar2_volatility_signal`), `GET /api/pillars/3/shedding-plan` (`v_pillar3_load_shedding_plan`), `GET /api/pillars/4/net-zero` (`v_pillar4_net_zero_progress`) |
| **Endpoint Security (2) + Recommendation (2) + Alert (1) + Raw (2)** | `GET /api/security/score` (1 row `v_security_score`), `GET /api/security/cascade-risks` (`v_cascade_risks` — JSONB pass-through bằng `@JsonRawValue`), `GET /api/recommendations?limit=N` (`v_active_recommendations`), `POST /api/recommendations/{id}/acknowledge` (body `{status, note}` → update + audit `acknowledged_by`), `GET /api/alerts/active?limit=N`, `GET /api/fuel-prices/latest?fuel_type=&limit=`, `GET /api/grid-load/latest` |
| **Auth users** | Tận dụng 3 seed users từ Phase 2: admin/admin (ADMIN), manager/manager (MANAGER), user/user (VIEWER). `BCryptPasswordEncoder` match cả `$2a$` và `$2b$` hash đã seed. |
| **Smoke test (script `scripts/phase45_smoke_api.sh`)** | 14 endpoint × 1-click. Login admin → lưu token → curl 13 endpoint còn lại với `Authorization: Bearer`. Pass: tất cả trả 2xx. Khi data trống (vd Pillar 3 không có load ≥ 85%), endpoint trả `[]` (KHÔNG 500). |
| **Common pitfalls (đã handle sẵn)** | (1) JSONB `details`/`suggested_data` → `@JsonRawValue` + `details::text AS details` trong SQL. (2) CORS Android emulator (10.0.2.2) → `setAllowedOriginPatterns("*")`. (3) Endpoint dữ liệu trống → JdbcTemplate `query` tự trả empty list, controller forward thẳng. (4) `users.password_hash` dùng `$2b$10$...` → BCryptPasswordEncoder strength 10 native compatible. (5) JWT token có claim `uid` lưu vào `Authentication.details` để DAO `acknowledge` ghi đúng `acknowledged_by`. |
| **Smoke test command (sau khi build)** | `bash scripts/phase45_smoke_api.sh` (mặc định BASE=`http://localhost:8090`, USER=admin, PASS=admin). Xuất 14 dòng ✓/✗ + summary count. |
| **Memory budget** | JVM `-Xmx256M` đủ cho dev. Spring Boot 2.7 + Tomcat embedded ~150 MB heap thực dùng. Cộng với Docker stack 1.6 GB + 3 generator 1.2 GB = **~3 GB** trên máy 8 GB → vẫn an toàn. |
| **Files dependencies (download lần đầu, ~80 MB)** | Spring Boot 2.7.18, jjwt 0.11.5, springdoc-openapi-ui 1.7.0, Lombok 1.18.30 (PostgreSQL driver 42.7.3 đã cache từ Phase 0). |
| **Tag** | `v0.45-backend-api` (sẽ tag sau khi build + smoke test xanh — hiện đang code-complete) |
| **Tài liệu** | [`backend-api/README.md`](./backend-api/README.md) — full build/run/troubleshoot guide cho team |

---

#### 🖥️ Phase 5 — JavaFX Admin Desktop (12-15h) ⭐ **PHẦN CHÍNH**

| Field | Value |
|-------|-------|
| **Mục tiêu** | JavaFX 21 app 5 màn, 3-layer, JDBC trực tiếp, JUnit ≥ 10 test |
| **Files tạo (~30 file Java + 5 FXML + 1 CSS):** | `pom.xml` (javafx-maven-plugin), `module-info.java`, `MainApp.java`, `presentation/controller/{LoginCtrl, DashboardCtrl, RegionCtrl, AlertRuleCtrl, UserCtrl}.java`, `presentation/view/{login, dashboard, region, alertRule, user}.fxml`, `service/{AuthService, RegionService, AlertRuleService, UserService, FuelPriceService}.java` (interface + impl), `dao/{BaseDao, UserDao, RegionDao, AlertRuleDao, FuelPriceDao}.java`, `model/{User, Region, AlertRule, FuelPrice}.java`, `util/{DatabaseConnection, PasswordUtils, Validator, SessionManager, ChartHelper, SseClient}.java`, `style/material.css`, `test/{UserDaoTest, AuthServiceTest, RegionServiceTest, ValidatorTest}.java` |
| **Tính năng từng màn** | **Login**: form + validate + BCrypt check + lưu session. **Dashboard**: **Top bar Energy Security Score gauge 0-100** + **TabPane 4 tab** mỗi tab 1 pillar — Tab Pillar 1: TableView `v_pillar1_supply_outlook` (cột recommendation_text + button "Tạo recommendation"); Tab Pillar 2: LineChart giá NL realtime + panel volatility signal STABLE/ELEVATED/VOLATILE; Tab Pillar 3: BarChart load theo region + nếu critical hiển thị panel `v_pillar3_load_shedding_plan` với button "Acknowledge"; Tab Pillar 4: PieChart solar/wind/hydro + Label intensity CO2 + tracker Net Zero progress. **Sidebar Recommendations Panel** luôn hiển thị `v_active_recommendations` (3 row PENDING demo có ngay) với button ACK/DISMISS. **Region/AlertRule/User**: form bên trái + TableView bên phải, full CRUD + search + validation. |
| **Design patterns áp dụng** | Singleton (DatabaseConnection, SessionManager), DAO, MVC, Builder (cho Alert filter), Strategy (cho Validator). |
| **Smoke test** | `mvn javafx:run`. Login admin/admin → vào Dashboard → 5 chart vẽ data. Tạo region mới → table refresh. `mvn test` ≥ 10 test pass. |
| **Common pitfalls** | JavaFX module path JDK 17+ → dùng `javafx-maven-plugin` config sẵn `<mainClass>` + `<options>`. FXML không nhận controller → check `fx:controller="full.package.Path"`. JUnit không chạy với JavaFX UI test → tách test pure service/DAO + dùng H2 in-memory cho DAO test. |
| **Tag** | `v0.5-javafx` |

---

#### 📱 Phase 6 — Android App gọn (8-10h)

| Field | Value |
|-------|-------|
| **Mục tiêu** | 4 activity, Retrofit, JWT lưu SharedPreferences, MPAndroidChart |
| **Files tạo (~12 file Java/Kotlin + 4 layout):** | `MainActivity.java`, `LoginActivity.java`, `HomeActivity.java`, `ChartActivity.java`, `AlertListActivity.java`, `api/ApiService.java`, `api/ApiClient.java` (Retrofit + Auth Interceptor), `model/{LoginDto, FuelPrice, Alert, Region}.java`, `util/SessionManager.java`, `adapter/AlertAdapter.java`, `adapter/RegionSpinnerAdapter.java`, layout XML cho 4 activity + 1 item alert |
| **Smoke test** | Build APK debug → cài lên Android emulator → Login (server tại `10.0.2.2:8080`) → Home thấy 4 KPI cards. Tap Chart → thấy line chart. Tap Alert → list alert. |
| **Common pitfalls** | `usesCleartextTraffic="true"` trong manifest cho dev HTTP. `10.0.2.2` thay `localhost` trong emulator. Server thật trên LAN → dùng IP máy chủ + cấu hình `network_security_config.xml`. |
| **Tag** | `v0.6-android` |

---

#### 🚀 Phase 7 — Deploy + Cloudflared Tunnel (3-4h)

| Field | Value |
|-------|-------|
| **Mục tiêu** | Scripts hoàn chỉnh + cloudflared guide để demo Android trên điện thoại thật |
| **Action** | Hoàn thiện `scripts/run.sh`, `scripts/run.ps1` (chạy full stack + build JARs + apply migration). Viết `scripts/tunnel.sh` dùng `cloudflared tunnel --url http://localhost:8080`. Viết `docs/DEPLOY.md`. |
| **Stretch goal** | `.devcontainer/devcontainer.json` cho GitHub Codespaces backup. |
| **Smoke test** | Clone repo trên máy khác / Codespaces → `bash scripts/run.sh` → đợi 2 phút → mọi service UP. `bash scripts/tunnel.sh` → URL `https://*.trycloudflare.com` accessible từ điện thoại. |
| **Tag** | `v0.7-deploy` |

---

#### 📚 Phase 8 — Documentation + Demo Prep (4-5h)

| Field | Value |
|-------|-------|
| **Mục tiêu** | README final + AI Log + Demo runbook + Pre-build artifacts GitHub Release |
| **Files** | Rewrite `README.md` (~200 dòng có screenshot), `docs/AI_USAGE_LOG.md`, `docs/DEMO_RUNBOOK.md` (đã có template §20), `docs/TROUBLESHOOTING.md` |
| **GitHub Release** | Pre-build và upload: `backend-api-1.0.jar`, `fuel-flink-job-1.0.jar`, `fuel-price-producer-1.0.jar`, `desktop-admin-1.0.jar`, `app-debug.apk`, `compose-bundle.zip` |
| **Smoke test** | Người khác clone → đọc README → chạy `run.sh` trong 5 phút. Demo dry-run §20 không lỗi. |
| **Tag** | `v1.0-final` 🎉 |

---

### 24.6 Cắt scope: NÊN BỎ những gì (so với §23 Full)

| Tính năng §23 đề xuất | Quyết định Minimalist + Light 4-pillar | Lý do |
|------------------------|----------------------------------------|-------|
| 3 generator (Fuel + GridLoad + Renewable) | **Giữ cả 3** (Light 4-pillar §24.2.1) — Fuel có sẵn, GridLoad + Renewable là generator nhỏ ~150-180 LOC | Cần để cover Pillar 3 + Pillar 4 |
| 4 nhóm KPI + ESI Calculator | **Light coverage 4 pillars KHÔNG có ESI** — chỉ 4 view dashboard + 4 status thresholds đơn giản | ESI complex (broadcast state Flink), dễ sai. Coverage 4 pillars qua views là đủ ấn tượng cho báo cáo |
| Star schema 5 dim + 5 fact + agg | **Chỉ thêm 8 bảng phẳng**: `users`, `regions`, `alert_rules`, `alerts`, `fuel_inventory_raw`, `grid_load_raw`, `renewable_output_raw`, `emission_raw` | Không cần dim hoá vì OLAP query đủ nhanh trên schema phẳng (~10k row/ngày) |
| 5 Flink job phức tạp | **1 job duy nhất** (có sẵn) + 1 KeyedProcessFunction alert đọc cả 3 topic | Một job đa-source dễ vận hành hơn 5 job riêng |
| Spring Boot multi-module (api + service + dao module) | **1 module duy nhất** | Multi-module Maven phức tạp, không tăng điểm |
| JPA/Hibernate | **JdbcTemplate** | Nhanh, đúng triết lý "trực tiếp JDBC" phù hợp môn Java |
| WebSocket | **Chỉ SSE** | SSE đủ cho realtime 1 chiều server→client, đơn giản hơn |
| Report PDF/Excel export | **Không làm** ban đầu | Stretch goal — chỉ làm nếu dư time tuần 8 |
| LLM Insight Gemini | **Stretch goal** (không cam kết) | Risk: Gemini rate limit / network — chỉ làm khi mọi thứ khác xong |
| FCM push notification | **Bỏ** | Setup phức tạp, không tăng điểm Android nhiều |
| Room local DB Android | **Bỏ** | API luôn online trong demo, cache vô nghĩa |
| 5 màn JavaFX (Profile, Settings, About...) | **Chỉ 5 màn cốt lõi** | Đủ checklist, không cần thêm |
| Broadcast state Flink | **Hard-code rules load lúc start** | Đủ demo, không cần dynamic update |
| Multi-environment (Lite/Full/BI/Dev profile) | **Chỉ Lite + BI** | 2 profile đủ, 4 quá phức tạp |

### 24.7 So sánh Full (§23) vs Minimalist + Light 4-pillar (§24)

| Metric | §23 Full | §24 Minimalist + Light 4-pillar | Tiết kiệm |
|--------|----------|---------------------------------|-----------|
| Số phase | 14 | **11** (8 + 2.5 + 2.6 + 4.5) | -21% |
| Số module Maven | ~8 | **5** (3 generator + consumer + backend + javafx + android) | -38% |
| Số endpoint API | ~15 | **~14** (8 core + 4 pillar + 2 security: score + recommendations) | -7% |
| Số entity DB mới | 10+ | **10** (4 ops + 4 pillar + 2 security) | tương đương |
| Số Flink job | 5 | **1 multi-source** (4 stream song song trong 1 job) | -80% |
| Thời gian Leader | 172h | **~75-85h** | **-52%** |
| Khối lượng code mới (LOC) | ~8000 | **~4800** (thêm SQL views thay vì Flink code) | -40% |
| Số file cần đọc khi debug | ~150 | **~85** | -43% |
| Coverage 4 pillars an ninh NL | 4/4 deep + ESI Flink | **4/4 light + ESI SQL view** (light ESI) | ⭐ tương đương báo cáo |
| **Actionable features** (detect/predict/recommend/score) | Có (Flink) | **Có (SQL views)** — 8 action views, 2 audit tables | ⭐ |
| Risk lỗi/phase | Trung bình | **Thấp** | ⭐ |
| Khả năng pass deadline | 60-70% | **85-90%** | ⭐ |

> 💡 **Kết luận**: Minimalist + Light 4-pillar giảm 52% công sức Leader vẫn đạt:
> - **100% coverage 4 pillars** (chiều rộng) match đề tài "An ninh năng lượng VN".
> - **Actionable features đầy đủ** thay vì chỉ "vẽ số liệu": detect (alert_rules đa pillar), predict (supply_outlook, volatility_signal), recommend (recommendation_text trong views + bảng `recommendations`), score (Light ESI `v_security_score`).
> - Phần "wow" đến từ stack (Kafka+Flink hiếm SV dùng) + **multi-pillar architecture** + **Energy Security Score gauge** trên JavaFX dashboard.

### 24.8 Cấu trúc thư mục Minimalist (gọn)

```
Real-time-processing-with-Kafka-Flink-Postgres/
├── infra/
│   ├── docker-compose.yml              # Lite profile
│   ├── docker-compose.bi.yml           # Overlay Metabase
│   └── script/
│       ├── 01_init_fuel_schema.sql     # Code có sẵn (Pillar 2)
│       ├── 02_init_users_regions.sql   # Phase 2
│       ├── 03_init_alerts.sql          # Phase 2
│       ├── 04_seed_basic.sql           # Phase 2 (admin user, 6 region VN, 5 rule)
│       ├── 05_init_pillars.sql         # Phase 2.5 (Pillar 1/3/4 tables + 4 views)
│       ├── 06_seed_pillars.sql         # Phase 2.5 (6 row inventory cho Pillar 1)
│       ├── 07_init_security_features.sql # Phase 2.6 (extend rules + 2 tables + 8 action views)
│       └── 08_seed_security_features.sql  # Phase 2.6 (5 multi-pillar rules + 3 recommendations)
├── data-generators/
│   ├── fuel-price-producer/            # Move từ KafkaProducer/ (Pillar 2)
│   ├── grid-load-generator/            # Phase 4 - Pillar 3 (~150 LOC)
│   └── renewable-generator/            # Phase 4 - Pillar 4 (~180 LOC)
├── flink-jobs/
│   └── fuel-flink-job/                 # Move từ KafkaConsumer/
│       └── (chỉ thêm AlertDetectionFunction)
├── backend-api/                        # MỚI - 1 module duy nhất
│   ├── pom.xml
│   ├── src/main/java/com/ves/api/
│   │   ├── BackendApplication.java
│   │   ├── config/  (4 file)
│   │   ├── security/ (1 file)
│   │   ├── controller/ (4 file)
│   │   ├── service/ (2 file)
│   │   ├── dao/ (5 file)
│   │   ├── dto/ (6 file)
│   │   └── model/ (1 file User)
│   └── src/main/resources/application.yml
├── desktop-admin/                      # MỚI - JavaFX
│   ├── pom.xml
│   ├── src/main/java/com/ves/desktop/
│   │   ├── MainApp.java
│   │   ├── presentation/controller/ (5 file)
│   │   ├── service/ (5 file)
│   │   ├── dao/ (5 file)
│   │   ├── model/ (4 file)
│   │   └── util/ (6 file)
│   ├── src/main/resources/
│   │   ├── view/ (5 FXML)
│   │   └── style/material.css
│   └── src/test/java/ (4 test class, ~12 method)
├── android-app/                        # MỚI - Android Studio project
│   └── app/src/main/java/com/ves/mobile/
│       ├── (4 activity + 3 dto + 2 util + 2 adapter)
│       └── res/layout/ (4 XML)
├── scripts/
│   ├── run.sh / run.ps1
│   ├── stop.sh / stop.ps1
│   ├── tunnel.sh
│   └── healthcheck.sh
├── docs/
│   ├── AI_USAGE_LOG.md
│   ├── DEPLOY.md
│   ├── DEMO_RUNBOOK.md
│   └── TROUBLESHOOTING.md
├── pom.xml                             # Parent Maven (3 module child)
├── README.md
├── .env.example
└── .gitignore
```

> ✅ **Tổng cộng ~95 file Java/Kotlin mới + 20 file config/SQL/XML** — đủ "ra dáng" đồ án nhưng không bị ngộp.

### 24.9 Tổng thời gian Minimalist

| Phase | Time | Cộng dồn | Tuần |
|-------|------|----------|------|
| 0. Foundation & Verify | 1-2h | 2h | Tuần 1 |
| 1. Docker Lite + Scripts | 2h | 4h | Tuần 1 |
| 2. Schema mở rộng | 2-3h | 7h | Tuần 2 |
| 3. Flink Alert PF | 3-4h | 11h | Tuần 2 |
| 4. Spring Boot API | 5-6h | 17h | Tuần 3 |
| **5. JavaFX Desktop (chính)** | **12-15h** | **32h** | **Tuần 4-5** |
| 6. Android App | 8-10h | 42h | Tuần 6-7 |
| 7. Deploy + Tunnel | 3-4h | 46h | Tuần 8 |
| 8. Doc + Demo Prep | 4-5h | **51h** | Tuần 9-10 |
| **Tổng Leader** | | **~50-60h** | **10 tuần** |

> 🎯 So với §23 (172h), **Minimalist giảm 65% workload** mà vẫn pass cả 2 môn với điểm cao.
>
> **Phân bổ trong tuần**: Leader chỉ cần **~5-6h/tuần** (so với 17h/tuần phương án Full). Cho phép cân bằng công việc công ty + đồ án mà không kiệt sức.

### 24.10 Stretch goals (CHỈ làm nếu xong sớm)

> Hoàn thành Phase 0-8 ≥ tuần 8 → cân nhắc 1 trong 3 stretch goal (tối đa 1, không tham lam):

| # | Stretch goal | Time | Giá trị tăng |
|---|-------------|------|---------------|
| **S1** | LLM Insight tab Android (Gemini API) | +6-8h | Wow factor cho demo, viết được vào Chương 5 báo cáo "Tích hợp AI" |
| **S2** | Metabase BI dashboard đẹp + embed vào báo cáo | +4-5h | Tăng điểm môn Java (báo cáo có biểu đồ pro) |
| **S3** | Report PDF export trong JavaFX (iText) | +5-6h | Thoả checklist môn Java "xuất báo cáo" |

> ⚠️ **Quy tắc vàng**: Chỉ chọn 1. Không bao giờ làm 2 stretch goal cùng lúc. Phase 8 (Doc) là ưu tiên cao hơn stretch.

### 24.11 Khi nào dùng §23 Full vs §24 Minimalist?

| Tình huống | Plan nên chọn |
|------------|---------------|
| Team 1-2 người mạnh + deadline 10 tuần | **§24 Minimalist** ⭐ |
| Team 3 người (Leader + 2 yếu) + deadline 10 tuần | **§24 Minimalist** ⭐ (case của bạn) |
| Team 4-5 người mạnh + deadline 10 tuần + muốn điểm > 9.5 | §23 Full |
| Team đông + deadline 15+ tuần (đồ án năm cuối) | §23 Full |
| Đã làm xong §24 + còn 4+ tuần dư | Upgrade từng phần §23 (thêm ESI, thêm generator) |
| Lần đầu code Kafka/Flink, sợ phức tạp | **§24 Minimalist** ⭐ |
| Đã có kinh nghiệm Kafka/Flink, muốn show off | §23 Full |

> 🎯 **Khuyến nghị cho bạn (Leader-heavy, 2 thành viên yếu, deadline 10 tuần): §24 Minimalist**

### 24.12 Pre-flight checklist trước khi bắt đầu Phase 0

```
☐ Đã đọc xong §24 (section này)
☐ Đã đồng ý với scope cắt §24.6
☐ Đã thông báo team: "Đi theo §24, không §23"
☐ Đã xóa expectation về ESI / 4 nhóm KPI / 3 generator
☐ Đã commit "v0.0-start-minimalist" tag để mốc bắt đầu
☐ Đã backup code hiện tại branch `legacy/original`
☐ Đã đọc Phase 0 detailed §24.5
☐ Sẵn sàng dành 5-6h/tuần × 10 tuần
```

→ Tích đủ → ra lệnh AI **"Bắt đầu Phase 0 theo §24"**.

### 24.13 Cam kết "ít lỗi" của plan Minimalist

| Yếu tố | Tại sao ít lỗi |
|--------|-----------------|
| **Tận dụng code có sẵn** | Code đã chạy ổn định, không cần debug từ đầu |
| **1 module Spring Boot** | Không bị classpath conflict giữa nhiều module |
| **JdbcTemplate thay JPA** | Không bị magic của Hibernate (lazy loading, N+1, transaction boundary) |
| **Hard-code alert rules** | Không bị bug Flink broadcast state |
| **Không Room/FCM/LLM** | Tránh 3 nguồn lỗi phổ biến Android |
| **Smoke test mỗi phase** | Bug phát hiện sớm, không cộng dồn |
| **Rollback tag rõ ràng** | Khi sai có thể về điểm safe ngay |
| **Stretch goal tách biệt** | Không block deadline chính nếu stretch fail |

---

## 🎯 KẾT LUẬN

> ⭐ **PHƯƠNG ÁN CHÍNH THỨC: §24 Minimalist Plan** — đồ án gọn, tận dụng 90% code có sẵn, deadline 10 tuần khả thi, ~50-60h cho Leader.
>
> §23 Full là phương án mở rộng tối đa (4 nhóm KPI + ESI + 5 Flink job) — **chỉ tham khảo**, không thực thi.

### Triết lý cuối cùng

Đồ án này **KHÔNG hướng tới làm to** mà hướng tới **làm gọn + làm chuẩn + demo chạy được**. Stack Kafka + Flink + PostgreSQL + JavaFX + Android + Real-time đã là điểm sáng tạo lớn — không cần thêm độ phức tạp.

### Điểm mạnh của Minimalist Plan
- ✅ **Tận dụng 90% code hiện có** — Producer, Flink job, Postgres schema, Docker compose đều giữ nguyên.
- ✅ **Chỉ 3 module mới**: Spring Boot API (1 module gọn), JavaFX Desktop (phần chính cho môn Java), Android App (gọn 4 màn).
- ✅ **Phân công thực tế**: Leader 80% + 2 thành viên × 5 PR đơn giản (§22).
- ✅ **Đáp ứng đầy đủ checklist môn Java** (§12) và môn Android (§13).
- ✅ **Lộ trình 10 tuần với ~5-6h/tuần** cho Leader — không kiệt sức (§24.9).
- ✅ **Deploy chắc ăn KHÔNG cần credit card** (§18) — Cloudflared tunnel + Codespaces backup.
- ✅ **"Clone-and-run" 3 lệnh** (§19) — bất kỳ ai có Docker đều chạy được.
- ✅ **Demo Day Runbook chi tiết** (§20).
- ✅ **Lite Profile 2.7GB RAM** (§21) — chạy được trên laptop 8GB.

### Bước tiếp theo (đề xuất theo thứ tự)

#### Tuần này (Week 0 — chuẩn bị)
1. **Đọc kỹ §24** (Minimalist Plan) — chốt scope cắt giảm §24.6.
2. **Họp nhóm** thông báo phương án §24 + scope đã cắt. Đảm bảo 2 thành viên hiểu rõ 5 task của họ trong §22.3.
3. **Setup GitHub** cơ bản:
   - Branch protection cho `main` (require PR).
   - PR template + Issue template.
   - Tag `v0.0-start-minimalist` để mốc bắt đầu.
   - Branch `legacy/original` backup code hiện tại.
4. **Pre-flight checklist §24.12** — tích đủ 8 ô → bắt đầu Phase 0.

#### Tuần 1 — Phase 0 + 1 (Foundation)
5. **Phase 0**: Verify code có sẵn chạy được + restructure repo.
6. **Phase 1**: Docker Lite mem_limit + scripts run.sh/run.ps1.

#### Tuần 2 — Phase 2 + 3 (Backend nền)
7. **Phase 2**: Thêm 4 bảng SQL (`users`, `regions`, `alerts`, `alert_rules`).
8. **Phase 3**: Thêm `AlertDetectionFunction` vào Flink job có sẵn.

#### Tuần 3 — Phase 4 (Spring Boot API)
9. **Phase 4**: 1 module Spring Boot, 8 endpoint, SSE, Swagger.
10. **Báo cáo tiến độ 1 (Lab 3)**: show API chạy + Postman demo + Người B đã có ERD/Use-case.

#### Tuần 4-5 — Phase 5 (JavaFX — phần CHÍNH)
11. **Phase 5**: 5 màn JavaFX + 3-layer + JDBC + 10-12 JUnit test.

#### Tuần 6-7 — Phase 6 (Android)
12. **Phase 6**: 4 activity Android + Retrofit + MPAndroidChart.

#### Tuần 8 — Phase 7 (Deploy)
13. **Phase 7**: Cloudflared tunnel + .devcontainer Codespaces.
14. **Optional**: 1 stretch goal §24.10 (LLM hoặc PDF export hoặc Metabase polish).

#### Tuần 9-10 (Demo & Báo cáo)
15. **Phase 8**: Doc final + Pre-build JARs/APK + GitHub Releases.
16. **Test trên máy sạch** (§19.7) — clone fresh từ GitHub → chạy → verify.
17. **Diễn tập demo** theo runbook §20 ít nhất 2 lần.
18. **Quay video demo dự phòng** 5–7 phút (Người B làm).
19. **Hoàn thiện báo cáo PDF + slide** (Người B làm Chương 1,2,4,6 + slide).

### 🎁 Cam kết "chắc ăn" của kế hoạch này

| Cam kết | Đảm bảo bằng |
|---------|--------------|
| 💰 **0 đồng chi phí cloud** | TryCloudflare + Codespaces free + Gemini free + Docker free |
| 💳 **Không cần credit card** | Tránh hoàn toàn Oracle/AWS/GCP/Fly.io/Railway |
| 🪶 **Host trên laptop 8GB RAM** | Lite Profile §21 — stack chỉ ~2.7 GB |
| 🔌 **Không phụ thuộc máy công ty** | Host trên laptop dev cá nhân (Lite) hoặc Codespaces |
| 👤 **Độc lập, không cần nhờ thành viên khác host** | Lite Profile chạy ngay trên laptop bạn |
| 📦 **Clone là chạy** | §19 Checklist + Bootstrap scripts + Pre-built artifacts |
| 🎬 **Demo không bị lỗi bất ngờ** | §20 Runbook + Backup Codespace + Firebase Remote Config + Video dự phòng |
| 📱 **Android dùng điện thoại cá nhân** | Tunnel public URL https → mạng 4G/Wi-Fi nào cũng truy cập được |
| 🤖 **LLM Insight hoạt động ổn định** | Gemini 1.5 Flash + cache 5' + 2 API key dự phòng |
| 🔄 **Bật/tắt từng service tuỳ ý** | Compose overlay: `.bi.yml` cho Metabase, `.dev.yml` cho pgAdmin/Kafka UI |
| 🛡️ **Không bị thành viên yếu kéo trễ** | Phân công Leader-heavy §22 — task thành viên không nằm đường găng + buffer 3 ngày |
| 📈 **Mỗi thành viên có ~5 PR thật** | §22 cung cấp 5 task atomic/người (2 thành viên = 10 PR), task non-coding hoặc copy-paste |
| 🤖 **Code mượt, ít lỗi nhờ AI agent** | §24 Minimalist Plan 8 phase atomic + smoke test + 3-strike retry + rollback strategy |
| ⚡ **Scope kiểm soát được** | §24 cắt 65% workload so với §23 Full — tận dụng 90% code có sẵn, chỉ thêm 3 module mới |
| 🎯 **Đảm bảo finish ≥ 90%** | Stretch goal §24.10 tách riêng — không block deadline chính |

---

**Tác giả kế hoạch:** Cursor AI (Claude) — Tháng 5/2026
**Verify bởi:** Nhóm trưởng (cần review + đánh dấu các phần đồng ý/điều chỉnh)
