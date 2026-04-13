# 📋 PHÂN CÔNG CÔNG VIỆC — HỆ THỐNG GIÁM SÁT GIÁ NHIÊN LIỆU REALTIME

> **Dự án:** IS402.P21 — Real-time Fuel Price Analytics  
> **Stack:** Java Producer → Kafka → Flink → PostgreSQL → Metabase  
> **Repo:** `Real-time-processing-with-Kafka-Flink-Postgres`

---

## 🗺️ TỔNG QUAN KIẾN TRÚC

```
┌──────────────────────────────────────────────────────────────────┐
│                        LUỒNG DỮ LIỆU                             │
│                                                                  │
│  FuelPriceProducer.java                                          │
│  (sinh giá mỗi 10 giây)                                         │
│         │                                                        │
│         ▼  topic: fuel-prices                                    │
│  ┌─────────────┐                                                 │
│  │    KAFKA    │                                                 │
│  └──────┬──────┘                                                 │
│         │                                                        │
│         ▼                                                        │
│  ┌─────────────────────────┐                                     │
│  │     FLINK JOB           │                                     │
│  │  KafkaConsumerApplication│                                    │
│  │  ├─ Luồng 1: RAW        │                                     │
│  │  ├─ Luồng 2: WINDOW AGG │                                     │
│  │  └─ Luồng 3: ALERTS     │                                     │
│  └──────────┬──────────────┘                                     │
│             │                                                    │
│             ▼                                                    │
│  ┌──────────────────────────────┐                                │
│  │        POSTGRESQL            │                                │
│  │  ├─ fuel_prices_raw          │                                │
│  │  ├─ fuel_price_window_agg    │                                │
│  │  ├─ fuel_price_alerts        │                                │
│  │  └─ views (v_daily_summary…) │                                │
│  └──────────┬───────────────────┘                                │
│             │                                                    │
│             ▼                                                    │
│  ┌──────────────────────┐                                        │
│  │      METABASE        │  http://localhost:3000                 │
│  │  Dashboard & Charts  │                                        │
│  └──────────────────────┘                                        │
└──────────────────────────────────────────────────────────────────┘
```

---

## 📁 CẤU TRÚC THƯ MỤC SAU KHI HOÀN THÀNH

```
Project/
├── docker-compose.yml                  ← Người 2 phụ trách
├── script/
│   ├── PostgreSQL.sql                  (cũ, giữ lại)
│   └── init_fuel_schema.sql            ← ĐÃ TẠO SẴN (Người 3 đọc + verify)
│
├── KafkaProducer/
│   └── FuelPriceProducer/              ← Người 1 phụ trách
│       ├── pom.xml                     ← ĐÃ TẠO SẴN
│       └── src/main/java/org/fuel/
│           ├── FuelPriceProducer.java   ← ĐÃ TẠO SẴN (main class)
│           ├── MockDataGenerator.java   ← ĐÃ TẠO SẴN (sinh dữ liệu)
│           └── model/
│               └── FuelPrice.java      ← ĐÃ TẠO SẴN
│
└── KafkaConsumer/                      ← Lead phụ trách (ĐÃ REFACTOR)
    ├── pom.xml
    └── src/main/java/org/cloud/
        ├── KafkaConsumerApplication.java  ← ĐÃ REFACTOR
        ├── Constant.java                  ← ĐÃ REFACTOR
        ├── ConfigLoader.java              (không đổi)
        ├── model/
        │   ├── FuelPrice.java             ← ĐÃ TẠO SẴN
        │   ├── WindowedFuelPrice.java     ← ĐÃ TẠO SẴN
        │   └── PriceAlert.java            ← ĐÃ TẠO SẴN
        ├── process/
        │   ├── FuelPriceAggregator.java   ← ĐÃ TẠO SẴN
        │   └── PriceChangeDetector.java   ← ĐÃ TẠO SẴN
        ├── source/
        │   ├── KafkaInvoiceSource.java    ← ĐÃ REFACTOR
        │   └── JdbcPostgresSink.java      (không đổi)
        └── resources/
            └── application.properties     ← ĐÃ REFACTOR
```

---

## 🗓️ LỊCH LÀM VIỆC 10 NGÀY

| Ngày | Lead | Người 1 | Người 2 | Người 3 | Người 4 |
|------|------|---------|---------|---------|---------|
| 1    | Review code, giao task | Đọc boilerplate | Đọc docker-compose | Đọc SQL schema | Cài Metabase |
| 2    | Verify Flink build | Setup Maven | Chạy `docker-compose up` | Test SQL trên pgAdmin | Test Metabase UI |
| 3    | Test Flink pipeline | Chạy Producer test | Verify Kafka + Postgres | Insert sample data | Kết nối Metabase→DB |
| 4    | Fix integration bugs | Submit Producer | Submit Docker | Submit SQL | Tạo Questions |
| 5-6  | Flink tuning + monitoring | — | — | Tạo thêm views | Tạo Dashboards |
| 7-8  | End-to-end testing | Support test | Support test | Verify data | Finalize dashboard |
| 9    | Fix bugs | — | — | — | Viết báo cáo |
| 10   | Review + merge | — | — | — | Demo + slides |

---

---

# 👤 A. LEAD — Kiến trúc & Flink

## Trạng thái: ✅ Boilerplate đã tạo xong — cần verify & chạy thử

### Những gì đã được tạo sẵn

| File | Trạng thái | Ghi chú |
|------|-----------|---------|
| `KafkaConsumerApplication.java` | ✅ Đã refactor | 3 sink streams: raw, window agg, alerts |
| `model/FuelPrice.java` | ✅ Tạo mới | Flink model |
| `model/WindowedFuelPrice.java` | ✅ Tạo mới | Kết quả window |
| `model/PriceAlert.java` | ✅ Tạo mới | Cảnh báo giá |
| `process/FuelPriceAggregator.java` | ✅ Tạo mới | Tumbling 1-min window |
| `process/PriceChangeDetector.java` | ✅ Tạo mới | Keyed state, detect >3% change |
| `Constant.java` | ✅ Refactor | Fix DRIVER_NAME bug cũ |
| `application.properties` | ✅ Refactor | Topic, DB name mới |
| `KafkaInvoiceSource.java` | ✅ Refactor | Auto.commit fix |

### Checklist của Lead

#### Ngày 1–2: Verify & Build
- [ ] `cd Project/KafkaConsumer && mvn clean compile` → không lỗi
- [ ] Đọc lại `FuelPriceAggregator.java` — hiểu Accumulator pattern
- [ ] Đọc lại `PriceChangeDetector.java` — hiểu ValueState
- [ ] Đọc lại `KafkaConsumerApplication.java` — hiểu 3 luồng
- [ ] Chạy `docker-compose up -d` (chờ Người 2)
- [ ] Đổi `jdbc.url` trong `application.properties` khi test trong Docker:
  ```properties
  # Local test:
  jdbc.url=jdbc:postgresql://localhost:5432/fuel_prices
  # Khi chạy Flink trong Docker (nếu có):
  jdbc.url=jdbc:postgresql://postgres-database:5432/fuel_prices
  ```

#### Ngày 3–6: Test Pipeline
- [ ] Chạy Producer (Người 1 làm xong): verify message vào Kafka
  ```bash
  # Xem message trong topic
  docker exec kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic fuel-prices --from-beginning --max-messages 5
  ```
- [ ] Build Flink JAR:
  ```bash
  cd Project/KafkaConsumer
  mvn clean package -DskipTests
  ```
- [ ] Chạy Flink local:
  ```bash
  java -jar target/KafkaConsumer-v1.1.1.jar
  ```
- [ ] Verify 3 bảng PostgreSQL có dữ liệu:
  ```sql
  SELECT COUNT(*) FROM fuel_prices_raw;
  SELECT COUNT(*) FROM fuel_price_window_agg;
  SELECT COUNT(*) FROM fuel_price_alerts;
  ```
- [ ] Latency end-to-end < 30 giây

#### Ngày 7–8: Integration
- [ ] Producer + Kafka + Flink + Postgres chạy đồng thời 30 phút
- [ ] Không có exception trong log Flink
- [ ] Kiểm tra checkpoint: `env.enableCheckpointing(30_000)` hoạt động
- [ ] Metabase hiển thị data từ views (phối hợp Người 4)

### Điều chỉnh nếu cần

**Thay đổi window size:**
```java
// Trong KafkaConsumerApplication.java, dòng TumblingProcessingTimeWindows
// Hiện: 1 phút. Đổi thành 5 phút nếu data thưa:
Time.minutes(Constant.WindowConfig.TUMBLING_WINDOW_MINUTES)
// → Sửa TUMBLING_WINDOW_MINUTES = 5 trong Constant.java
```

**Thay đổi ngưỡng alert:**
```java
// Trong PriceAlert.java
public static final double ALERT_THRESHOLD_PERCENT = 3.0; // Đổi thành 5.0 nếu muốn ít alert hơn
```

---

---

# 👤 B. NGƯỜI 1 — Java Producer

## Mức độ: ⭐⭐ (Dễ — boilerplate đã tạo 90%)

## Trạng thái: ✅ Code đã viết sẵn — chỉ cần build và chạy

### Những gì đã được tạo sẵn

| File | Vị trí | Tóm tắt |
|------|--------|---------|
| `pom.xml` | `KafkaProducer/FuelPriceProducer/` | Kafka client 3.5, Jackson, Logback |
| `FuelPrice.java` | `src/.../org/fuel/model/` | POJO với @Data Lombok |
| `MockDataGenerator.java` | `src/.../org/fuel/` | Sinh giá VN realistic, random walk ±0.5%/tick |
| `FuelPriceProducer.java` | `src/.../org/fuel/` | Main class, Kafka producer loop |
| `application.properties` | `src/main/resources/` | Kafka config |

### Checklist Người 1

#### Phase 1: Setup (30 phút, Ngày 1)
- [ ] Clone/pull repo mới nhất
- [ ] Mở folder `Project/KafkaProducer/FuelPriceProducer/` trong IDE (IntelliJ hoặc VSCode)
- [ ] Đọc hiểu `MockDataGenerator.java` (chú ý comment trong code)
- [ ] Đọc hiểu `FuelPriceProducer.java` (chú ý main loop)

#### Phase 2: Build (30 phút, Ngày 2)
- [ ] Đảm bảo Docker đang chạy (Người 2 setup)
- [ ] Mở terminal tại `Project/KafkaProducer/FuelPriceProducer/`
- [ ] Build project:
  ```bash
  mvn clean package
  ```
- [ ] Kết quả: file JAR xuất hiện tại `target/FuelPriceProducer-1.0.0-shaded.jar`

#### Phase 3: Chạy thử (30 phút, Ngày 3)
- [ ] Chạy producer:
  ```bash
  java -jar target/FuelPriceProducer-1.0.0-shaded.jar
  ```
- [ ] Log output kỳ vọng:
  ```
  === Fuel Price Producer khởi động ===
  Kafka: localhost:9092 | Topic: fuel-prices | Interval: 10000ms
  [Batch #1] Gửi 20/20 records | topic=fuel-prices
  [Batch #2] Gửi 20/20 records | topic=fuel-prices
  ```
- [ ] Mở terminal mới, verify Kafka nhận message:
  ```bash
  docker exec kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic fuel-prices --from-beginning --max-messages 5
  ```
- [ ] Output kỳ vọng (JSON hợp lệ):
  ```json
  {"timestamp":"2025-04-12T10:30:00","fuel_type":"RON_95","price_per_liter":22150.0,"location":"Ho Chi Minh","region":"South","source":"mock-generator"}
  ```

#### Phase 4: Test 10 phút (Ngày 3)
- [ ] Chạy producer 10 phút, redirect log:
  ```bash
  java -jar target/FuelPriceProducer-1.0.0-shaded.jar 2>&1 | tee producer_test.log
  # Ctrl+C sau 10 phút
  ```
- [ ] Đếm số dòng "Gửi XX/20 records":
  ```bash
  grep "Gửi" producer_test.log | wc -l
  # Kỳ vọng: ~60 batch (10 phút × 6 batch/phút)
  ```
- [ ] **Không có dòng "Gửi thất bại"**

### Sản phẩm bàn giao (Ngày 4)
- [ ] `producer_test.log` (log 10 phút)
- [ ] Screenshot console output
- [ ] Confirm với Lead: "Producer chạy OK, topic có message"

### Troubleshooting

| Lỗi | Nguyên nhân | Fix |
|-----|------------|-----|
| `Connection refused localhost:9092` | Kafka chưa chạy | Báo Người 2 |
| `mvn: command not found` | Chưa cài Maven | [Tải Maven](https://maven.apache.org/download.cgi) |
| `java: command not found` | Chưa cài JDK | Cài JDK 11 |
| Không thấy file JAR | Build fail | Đọc lỗi mvn, báo Lead |

---

---

# 👤 C. NGƯỜI 2 — Docker / Infrastructure

## Mức độ: ⭐⭐ (Trung bình — docker-compose đã update)

## Trạng thái: ✅ docker-compose.yml đã cập nhật — cần chạy và verify

### Những gì đã được cập nhật

```yaml
# Trước (cũ):           # Sau (mới):
- postgres              - zookeeper   ← THÊM MỚI
- metabase              - kafka       ← THÊM MỚI
                        - postgres    (giữ, đổi DB: fuel_prices)
                        - metabase    (giữ, pin version v0.47.7)
                        + networks: realtime_network
                        + health checks cho tất cả services
                        + volumes cho tất cả services
```

### Checklist Người 2

#### Phase 1: Kiểm tra trước khi chạy (15 phút, Ngày 1)
- [ ] Docker Desktop đang chạy (icon trên taskbar)
- [ ] Không có process nào chiếm port: 9092, 5432, 3000, 2181
  ```powershell
  netstat -an | findstr "9092 5432 3000 2181"
  # Không có kết quả = OK
  ```
- [ ] Đọc file `Project/docker-compose.yml` — hiểu các services

#### Phase 2: Start toàn bộ hệ thống (30 phút, Ngày 2)
- [ ] Mở terminal tại `Project/`
- [ ] Pull images (lần đầu mất 5-10 phút):
  ```bash
  docker-compose pull
  ```
- [ ] Start tất cả services:
  ```bash
  docker-compose up -d
  ```
- [ ] Chờ khoảng 60 giây rồi kiểm tra:
  ```bash
  docker-compose ps
  ```
- [ ] Output kỳ vọng (tất cả `healthy`):
  ```
  NAME              STATUS
  zookeeper         Up (healthy)
  kafka             Up (healthy)
  postgres-database Up (healthy)
  metabase          Up (healthy)
  ```

#### Phase 3: Verify từng service (30 phút, Ngày 2)

**Kafka:**
```bash
# List topics (sẽ thấy __consumer_offsets và các internal topics)
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Tạo topic thủ công nếu cần (thường auto-create đã bật)
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --create --topic fuel-prices \
  --partitions 3 --replication-factor 1 --if-not-exists
```
- [ ] Kafka chạy: ✅

**PostgreSQL:**
```bash
# Kết nối và xem tables (init script đã chạy)
docker exec postgres-database psql -U postgres -d fuel_prices -c "\dt"
```
- [ ] Output kỳ vọng:
  ```
  fuel_price_alerts
  fuel_price_window_agg
  fuel_prices_raw
  ```
- [ ] Postgres chạy + schema OK: ✅

**Metabase:**
- [ ] Mở trình duyệt: `http://localhost:3000`
- [ ] Trang đăng ký/login xuất hiện: ✅

#### Phase 4: Tạo file .env (Ngày 2)
- [ ] Tạo file `Project/.env` (không commit lên Git):
  ```bash
  POSTGRES_USER=postgres
  POSTGRES_PASSWORD=123456
  POSTGRES_DB=fuel_prices
  KAFKA_BOOTSTRAP_SERVERS=kafka:29092
  ```

#### Phase 5: Viết README_DOCKER.md (Ngày 3)
- [ ] Tạo `Project/README_DOCKER.md`:
  ```markdown
  # Docker Setup Guide

  ## Khởi động
  cd Project/
  docker-compose up -d

  ## Dừng hệ thống
  docker-compose down

  ## Dừng và xóa data
  docker-compose down -v

  ## Xem log real-time
  docker-compose logs -f kafka
  docker-compose logs -f postgres-database

  ## Ports
  - Kafka: localhost:9092
  - PostgreSQL: localhost:5432 (user: postgres / pass: 123456 / db: fuel_prices)
  - Metabase: http://localhost:3000
  - Zookeeper: localhost:2181
  ```

### Sản phẩm bàn giao (Ngày 3)
- [ ] Screenshot: `docker-compose ps` (tất cả healthy)
- [ ] Screenshot: Metabase login page `http://localhost:3000`
- [ ] Screenshot: psql `\dt` output (3 tables)
- [ ] `README_DOCKER.md`
- [ ] Confirm với Lead: "Docker OK, mọi người có thể kết nối"

### Troubleshooting

| Lỗi | Fix |
|-----|-----|
| Kafka không healthy | `docker-compose logs kafka` → check Zookeeper connect |
| Port đã bị dùng | `netstat -ano \| findstr "9092"` → kill process |
| Postgres init script không chạy | Phải là lần đầu start; nếu volume cũ thì `docker-compose down -v` |
| Metabase lâu start (>3 phút) | Bình thường, chờ thêm |

---

---

# 👤 D. NGƯỜI 3 — Database Schema & SQL

## Mức độ: ⭐⭐ (Trung bình — schema đã tạo sẵn)

## Trạng thái: ✅ init_fuel_schema.sql đã tạo sẵn — cần verify + bổ sung views

### Schema đã tạo sẵn

```
Project/script/init_fuel_schema.sql gồm:

TABLES (3 bảng Flink ghi trực tiếp):
├── fuel_prices_raw          — mỗi record từ Kafka
├── fuel_price_window_agg    — tổng hợp 1-phút
└── fuel_price_alerts        — cảnh báo biến động >3%

VIEWS (6 views cho Metabase):
├── v_latest_prices          — giá 24h qua
├── v_daily_summary          — tóm tắt theo ngày (30 ngày)
├── v_price_trend            — biến động theo window (7 ngày)
├── v_location_comparison_today — so sánh địa điểm hôm nay
├── v_recent_alerts          — cảnh báo 24h qua
└── v_pipeline_health        — thống kê pipeline (7 ngày)

SEED DATA:
├── 540 records vào fuel_prices_raw (60 phút × 9 tổ hợp)
├── 72 records vào fuel_price_window_agg
└── 3 records vào fuel_price_alerts
```

### Checklist Người 3

#### Phase 1: Đọc và hiểu schema (Ngày 1)
- [ ] Mở file `Project/script/init_fuel_schema.sql`
- [ ] Đọc kỹ 3 CREATE TABLE → hiểu columns
- [ ] Đọc kỹ 6 CREATE VIEW → hiểu logic SQL từng view

#### Phase 2: Verify schema trên database (Ngày 2-3)
Sau khi Người 2 chạy Docker:
```bash
# Kết nối vào Postgres
docker exec -it postgres-database psql -U postgres -d fuel_prices
```

- [ ] Xem tất cả tables và views:
  ```sql
  \dt   -- tables
  \dv   -- views
  ```
- [ ] Kiểm tra dữ liệu seed:
  ```sql
  SELECT COUNT(*) FROM fuel_prices_raw;         -- Expect 540
  SELECT COUNT(*) FROM fuel_price_window_agg;   -- Expect 72
  SELECT COUNT(*) FROM fuel_price_alerts;       -- Expect 3
  ```
- [ ] Test từng view:
  ```sql
  SELECT * FROM v_latest_prices LIMIT 5;
  SELECT * FROM v_daily_summary LIMIT 5;
  SELECT * FROM v_price_trend LIMIT 5;
  SELECT * FROM v_location_comparison_today LIMIT 5;
  SELECT * FROM v_recent_alerts;
  SELECT * FROM v_pipeline_health;
  ```
- [ ] **Tất cả views trả về kết quả, không lỗi**

#### Phase 3: Tạo thêm views nếu Người 4 cần (Ngày 3-4)
Người 4 sẽ yêu cầu query cụ thể cho dashboard. Người 3 tạo thêm views:

```sql
-- Ví dụ: Người 4 muốn biến động giá theo từng ngày
CREATE OR REPLACE VIEW v_volatility_daily AS
SELECT
    DATE(event_timestamp) AS price_date,
    fuel_type,
    location,
    ROUND(STDDEV(price_per_liter)::numeric, 2) AS price_std_dev,
    ROUND((MAX(price_per_liter) - MIN(price_per_liter))::numeric, 2) AS price_range
FROM fuel_prices_raw
WHERE event_timestamp >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(event_timestamp), fuel_type, location
ORDER BY price_date DESC;
```

#### Phase 4: Tạo INDEX tổng hợp (nếu query chậm, Ngày 4)
```sql
-- Tăng tốc query Metabase filter theo nhiều cột
CREATE INDEX IF NOT EXISTS idx_raw_type_location_time
    ON fuel_prices_raw(fuel_type, location, event_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_agg_type_location_window
    ON fuel_price_window_agg(fuel_type, location, window_start DESC);
```

#### Phase 5: Viết DATABASE_SCHEMA.md (Ngày 4)
- [ ] Tạo `Project/DATABASE_SCHEMA.md`:
  ```markdown
  # Database Schema

  ## Tables
  ### fuel_prices_raw
  - id: BIGSERIAL (PK)
  - event_timestamp: TIMESTAMP (NOT NULL)
  - fuel_type: VARCHAR(50) — RON_92 | RON_95 | DIESEL | LPG
  - price_per_liter: DECIMAL(10,2)
  - location: VARCHAR(100) — Ho Chi Minh | Ha Noi | Da Nang | ...
  - region: VARCHAR(50) — North | Central | South
  - source: VARCHAR(100)
  - ingested_at: TIMESTAMP DEFAULT NOW()

  [mô tả tương tự cho 2 bảng còn lại...]

  ## Views
  [mô tả mục đích từng view...]
  ```

### Sản phẩm bàn giao (Ngày 4)
- [ ] Screenshot: `\dt` và `\dv` output
- [ ] Screenshot: kết quả query từng view (5 rows)
- [ ] `DATABASE_SCHEMA.md`
- [ ] Confirm với Người 4: "Views sẵn sàng, data có trong DB"

### Troubleshooting

| Lỗi | Fix |
|-----|-----|
| `ERROR: relation already exists` | Init script đã chạy; `docker-compose down -v` nếu muốn reset |
| View trả về 0 rows | Seed data chưa có; kiểm tra `SELECT COUNT(*) FROM fuel_prices_raw` |
| `function gen_random_uuid() does not exist` | Thêm `CREATE EXTENSION IF NOT EXISTS "pgcrypto";` vào đầu script |
| View chạy chậm | Thêm index theo gợi ý Phase 4 |

---

---

# 👤 E. NGƯỜI 4 — Metabase Dashboard & Báo cáo

## Mức độ: ⭐ (Dễ — step-by-step rõ ràng)

## Trạng thái: Bắt đầu ngày 3 (sau khi Người 2 setup Docker + Người 3 confirm views)

### Checklist Người 4

#### Phase 1: Setup Metabase (Ngày 2-3)
- [ ] Mở trình duyệt: `http://localhost:3000`
- [ ] Tạo tài khoản lần đầu:
  - Email: `admin@example.com`
  - Password: `Admin123456!`
- [ ] **Thêm database:**
  1. Click ⚙️ (góc trên phải) → **Admin settings**
  2. **Databases** → **Add a database**
  3. Chọn: **PostgreSQL**
  4. Điền:
     ```
     Display name: fuel_prices_db
     Host:         localhost  (hoặc postgres-database nếu cùng Docker network)
     Port:         5432
     Database:     fuel_prices
     Username:     postgres
     Password:     123456
     ```
  5. Click **Save** → chờ "Successfully connected"
- [ ] Verify: click **Browse data** → thấy tables và views

#### Phase 2: Tạo 6 Questions (Ngày 4-5)

> 💡 **Tip:** Dùng **views** (v_...) thay bảng trực tiếp để query nhanh hơn.

---

**Q1 — Giá mới nhất theo địa điểm**
1. **+ New** → **Question** → SQL (Custom query)
2. Paste:
   ```sql
   SELECT fuel_type, location, region, price_per_liter, event_timestamp
   FROM v_latest_prices
   ORDER BY event_timestamp DESC
   LIMIT 50
   ```
3. **Visualization:** Table
4. **Save:** `Q1 - Latest Prices`

---

**Q2 — Xu hướng giá theo ngày (line chart)**
1. **+ New** → **Question** → SQL
2. Paste:
   ```sql
   SELECT price_date, fuel_type, location, avg_price
   FROM v_daily_summary
   ORDER BY price_date DESC
   ```
3. **Visualization:** Line chart
   - X-axis: `price_date`
   - Y-axis: `avg_price`
   - Series (Group by): `fuel_type`
4. **Save:** `Q2 - Daily Price Trend`

---

**Q3 — So sánh giá theo địa điểm hôm nay (bar chart)**
1. **+ New** → **Question** → SQL
2. Paste:
   ```sql
   SELECT fuel_type, location, avg_price_today
   FROM v_location_comparison_today
   ORDER BY fuel_type, avg_price_today DESC
   ```
3. **Visualization:** Bar chart
   - X-axis: `location`
   - Y-axis: `avg_price_today`
   - Group by: `fuel_type`
4. **Save:** `Q3 - Location Comparison Today`

---

**Q4 — Cảnh báo giá gần đây**
1. **+ New** → **Question** → SQL
2. Paste:
   ```sql
   SELECT alert_timestamp, fuel_type, location,
          previous_price, current_price, change_percent, alert_type
   FROM v_recent_alerts
   ORDER BY alert_timestamp DESC
   ```
3. **Visualization:** Table
4. **Save:** `Q4 - Recent Alerts`

---

**Q5 — Tổng số records theo ngày (bar chart)**
1. **+ New** → **Question** → SQL
2. Paste:
   ```sql
   SELECT ingest_date, total_records, locations_active, fuel_types_active
   FROM v_pipeline_health
   ORDER BY ingest_date DESC
   ```
3. **Visualization:** Bar chart
4. **Save:** `Q5 - Pipeline Health`

---

**Q6 — KPI: Giá trung bình RON_95 hôm nay**
1. **+ New** → **Question** → SQL
2. Paste:
   ```sql
   SELECT ROUND(AVG(price_per_liter)::numeric, 0) AS avg_ron95_today
   FROM fuel_prices_raw
   WHERE fuel_type = 'RON_95'
     AND DATE(event_timestamp) = CURRENT_DATE
   ```
3. **Visualization:** Number (KPI card)
4. **Save:** `Q6 - RON95 Avg Today`

---

#### Phase 3: Tạo 3 Dashboards (Ngày 6-7)

**Dashboard 1: Tổng quan (Executive Summary)**
```
Layout:
┌────────────┬────────────┬────────────┐
│ Q6 KPI card│(thêm Q cho │(thêm Q cho │
│ RON95 avg  │ min price) │ alert count)│
└────────────┴────────────┴────────────┘
│         Q2 Line chart (full width)    │
│         Xu hướng 30 ngày             │
└───────────────────────────────────────┘
│         Q3 Bar chart (full width)     │
│         So sánh địa điểm hôm nay     │
└───────────────────────────────────────┘
```

1. **+ New** → **Dashboard** → Name: `Dashboard 1 - Tổng quan`
2. **Add a question** → Q6, Q2, Q3
3. Sắp xếp layout
4. **Save**
5. Bật **Auto-refresh:** click ↻ góc trên → chọn **Every 5 minutes**

---

**Dashboard 2: Chi tiết & Phân tích**
```
Layout:
┌─────────────────┬──────────────────┐
│ Q1 Latest Prices│ Q4 Alerts        │
│ (Table)         │ (Table)          │
└─────────────────┴──────────────────┘
│ Date filter  [Chọn ngày]            │
│ Fuel filter  [Chọn loại nhiên liệu] │
└─────────────────────────────────────┘
```

1. **+ New** → **Dashboard** → Name: `Dashboard 2 - Chi tiết`
2. Add Q1, Q4
3. **Add filter:** click **Filter** icon → **Time** → **Date range**
4. Click ✏️ trên filter → kết nối với Q1 (filter theo `event_timestamp`)
5. **Save**

---

**Dashboard 3: Giám sát Dữ liệu**
```
Layout:
┌──────────────────────────────────────┐
│ Q5 Pipeline Health (Bar chart)       │
│ Records theo ngày                    │
└──────────────────────────────────────┘
│ (Custom query: % alerts per day)     │
└──────────────────────────────────────┘
```

1. **+ New** → **Dashboard** → Name: `Dashboard 3 - Data Quality`
2. Add Q5
3. **Save**

---

#### Phase 4: Làm đẹp (Ngày 7-8)
- [ ] Mỗi chart: click **Edit visualization** → đặt title rõ ràng
- [ ] Line chart Q2: màu sắc khác nhau cho từng fuel type
  - RON_92: đỏ, RON_95: cam, DIESEL: xanh dương, LPG: xanh lá
- [ ] Số liệu price: format với dấu phẩy ngăn cách hàng nghìn (VND)
- [ ] Thêm text card mô tả mục đích dashboard (Markdown text)

#### Phase 5: Viết báo cáo + slide (Ngày 9)

**Slides (10 slides):**
1. Trang bìa: "Fuel Price Analytics System — IS402.P21"
2. Bài toán: Cần gì? Tại sao?
3. Kiến trúc hệ thống (sơ đồ)
4. Demo Dashboard 1 — screenshot
5. Demo Dashboard 2 — screenshot
6. Demo Dashboard 3 — screenshot
7. Kết quả: số record, latency, uptime
8. Phân công công việc (bảng 5 người)
9. Khó khăn & cách giải quyết
10. Kết luận & hướng phát triển

### Sản phẩm bàn giao (Ngày 9)
- [ ] 3 Dashboards hoạt động
- [ ] 6 Questions đã save
- [ ] Auto-refresh bật
- [ ] Slide báo cáo (PDF hoặc PPTX)
- [ ] Screenshot tất cả dashboards

### Troubleshooting

| Vấn đề | Fix |
|--------|-----|
| Không thấy tables khi Browse | Kết nối DB chưa đúng; vào Admin → Databases → Edit → Retest |
| Query lỗi `relation does not exist` | View chưa tạo; báo Người 3 |
| Dashboard load chậm | Thêm `LIMIT 1000` vào query; báo Người 3 thêm index |
| Số liệu không khớp DB | Clear cache: Admin → Troubleshooting → Clear cache |

---

---

## 🚨 ĐIỂM TÍCH HỢP QUAN TRỌNG

> Đây là những điểm các thành viên PHẢI phối hợp với nhau.

### 1. Kafka Topic Name
**Tất cả** phải dùng đúng: **`fuel-prices`**
- Producer: `application.properties` → `kafka.topic=fuel-prices`
- Flink: `application.properties` → `kafka.topic=fuel-prices`
- Kafka Docker: `KAFKA_AUTO_CREATE_TOPICS_ENABLE=true` → tự tạo

### 2. PostgreSQL Connection
| Khi chạy từ | Dùng host |
|------------|-----------|
| Máy local (IntelliJ) | `localhost:5432` |
| Trong Docker container | `postgres-database:5432` |
| Flink local (ngoài Docker) | `localhost:5432` |

### 3. Database Name
Tất cả dùng: **`fuel_prices`** (không phải `revenue` như cũ)

### 4. Thứ tự khởi động
```
1. docker-compose up -d     (Người 2 làm xong)
2. Chờ tất cả healthy
3. Chạy FuelPriceProducer   (Người 1)
4. Chạy Flink job           (Lead)
5. Mở Metabase              (Người 4)
```

---

## 📊 TIÊU CHÍ ĐÁNH GIÁ HOÀN THÀNH

| # | Tiêu chí | Kiểm tra như thế nào |
|---|---------|---------------------|
| 1 | Producer gửi data | `kafka-console-consumer` thấy JSON |
| 2 | Flink đọc từ Kafka | Log Flink in ra `[FuelPrice]` |
| 3 | Flink ghi vào Postgres | `SELECT COUNT(*) FROM fuel_prices_raw` > 0 |
| 4 | Window aggregation | `SELECT COUNT(*) FROM fuel_price_window_agg` > 0 |
| 5 | Alert detection | `SELECT COUNT(*) FROM fuel_price_alerts` > 0 (sau ~5-10 phút) |
| 6 | Metabase kết nối | `http://localhost:3000` load OK |
| 7 | Dashboard hiển thị | Ít nhất 1 chart có dữ liệu |
| 8 | End-to-end latency | Message → Postgres < 30 giây |

---

## 📌 GHI CHÚ QUAN TRỌNG

1. **Python Producer cũ đã thay thế bằng Java** — không dùng các file `ProducerBranch*.py` nữa
2. **Schema DB thay đổi hoàn toàn** — database mới là `fuel_prices`, không phải `revenue`
3. **Flink job đã refactor** — không còn xử lý invoice/doanh thu, giờ xử lý giá nhiên liệu
4. **Mock data sẵn** — Người 4 có thể test Metabase ngay với seed data, không cần chờ Producer
5. **Lead review tất cả code** trước khi merge vào main branch

---

*Cập nhật lần cuối: 12/04/2026 — Lead Senior Engineer*
