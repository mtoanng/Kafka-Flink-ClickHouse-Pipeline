# 📋 PHÂN CÔNG CÔNG VIỆC — Real-time Global Oil Price Monitor
> Môn: IS402.P21 | Nhóm: __ | Ngày: ___/___/2025

---

## 🏗️ Kiến trúc hệ thống

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                        LUỒNG DỮ LIỆU CHÍNH                                     │
│                                                                                  │
│  [Java Producer]  ──► [Kafka Topic]  ──► [Apache Flink]  ──► [PostgreSQL]       │
│  MockDataGenerator     "fuel-prices"     3 stream jobs       3 tables           │
│  (hoặc Alpha Vantage)  1 partitions      window agg          + 4 views          │
│                                          alert detect                            │
│                                                 │                                │
│                                                 ▼                                │
│                                          [Metabase]                              │
│                                          Dashboard                               │
│                                          (port 3000)                             │
└────────────────────────────────────────────────────────────────────────────────┘

Docker services:
  zookeeper   :2181   kafka       :9092,29092
  flink-jm    :8081   flink-tm    (internal)
  postgres    :5432   metabase    :3000
```

---

## 📦 Stack công nghệ

| Layer | Technology | Version |
|---|---|---|
| Message Broker | Apache Kafka | 7.6.0 (Confluent) |
| Stream Processing | Apache Flink | 1.17.0 |
| Database | PostgreSQL | 15 |
| BI Dashboard | Metabase | latest |
| Language (Consumer) | Java | 11 |
| Language (Producer) | Java | 11 |
| Build tool | Maven | 3.x + Shade Plugin |
| Containerization | Docker Compose | v3.8 |

---

## 📊 Dữ liệu: Giá dầu thế giới (mock)

| Loại dầu | Đơn vị | Giá cơ sở | Sàn |
|---|---|---|---|
| WTI_CRUDE | USD/barrel | ~75.80 | NYMEX, New York |
| BRENT_CRUDE | USD/barrel | ~79.50 | ICE, London |
| GASOLINE_FUTURES | USD/gallon | ~2.34 | NYMEX |
| DIESEL | USD/gallon | ~2.68 | US market |
| NATURAL_GAS | USD/MMBtu | ~2.10 | Henry Hub, Houston |

Sàn giao dịch mô phỏng: New York (NYMEX), London (ICE), Singapore (SGX), Houston, Rotterdam, Tokyo

---

## 👥 PHÂN CÔNG CHI TIẾT

---

### 🧑‍💻 NGƯỜI 1 — Java Producer (Kafka Producer)

**Phạm vi:** Toàn bộ thư mục `Project/KafkaProducer/FuelPriceProducer/`

**Mục tiêu:** Đảm bảo producer chạy được, gửi dữ liệu vào Kafka topic `fuel-prices` thành công.

#### Files chịu trách nhiệm:
```
Project/KafkaProducer/FuelPriceProducer/
├── pom.xml                              ← đọc hiểu để build
├── src/main/resources/
│   └── application.properties           ← cấu hình kết nối Kafka
└── src/main/java/org/fuel/
    ├── FuelPriceProducer.java            ← main class, đọc hiểu
    ├── MockDataGenerator.java            ✏️ CÓ THỂ SỬA thêm noise/pattern
    ├── HttpApiSource.java                ✏️ SỬA API_KEY nếu dùng API thực
    └── model/
        └── FuelPrice.java               ← đọc hiểu (không cần sửa)
```

#### Checklist công việc:

**Bước 1: Hiểu kiến trúc (30 phút)**
- [ ] Đọc `FuelPriceProducer.java` → hiểu vòng lặp while(true) + Kafka send
- [ ] Đọc `MockDataGenerator.java` → hiểu random walk simulation
- [ ] Đọc `model/FuelPrice.java` → biết các fields: `fuelType`, `price`, `priceUnit`, `location`, `region`

**Bước 2: Build và test local (45 phút)**
- [ ] Cài Maven nếu chưa có: `mvn --version`
- [ ] Build uber-JAR:
  ```bash
  cd Project/KafkaProducer/FuelPriceProducer
  mvn clean package -DskipTests
  ```
  → File xuất ra: `target/FuelPriceProducer-1.0-SNAPSHOT.jar`
- [ ] Verify build thành công: không có BUILD FAILURE

**Bước 3: Chạy thử mock producer (sau khi Người 2 đã start Kafka)**
- [ ] Đảm bảo Kafka đang chạy: `docker ps | grep kafka`
- [ ] Chạy producer (từ terminal):
  ```bash
  java -jar target/FuelPriceProducer-1.0-SNAPSHOT.jar
  ```
- [ ] Quan sát log: mỗi 10 giây thấy "Batch #1: sent 30 records"
- [ ] Kiểm tra Kafka nhận được message:
  ```bash
  docker exec -it kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic fuel-prices --from-beginning --max-messages 5
  ```
- [ ] Xác nhận JSON trông như:
  ```json
  {"timestamp":"2025-04-12T10:30:00","fuel_type":"WTI_CRUDE","price":75.23,"price_unit":"USD/barrel","location":"New York (NYMEX)","region":"North America","source":"mock-generator"}
  ```

**Bước 4 (Optional/Nâng cao): Tích hợp Alpha Vantage API thực**
- [ ] Đăng ký API key miễn phí tại: https://www.alphavantage.co/support/#api-key
- [ ] Mở `HttpApiSource.java` → sửa dòng `API_KEY = "demo"` thành key của bạn
- [ ] Test standalone: `java HttpApiSource` (xem có lấy được WTI/Brent không)
- [ ] Nếu API hoạt động → sửa `FuelPriceProducer.java` dòng 64:
  ```java
  // Đổi từ:  MockDataGenerator generator = new MockDataGenerator();
  // Thành:   HttpApiSource generator = new HttpApiSource();
  ```
- [ ] Lưu ý: Free tier Alpha Vantage chỉ có 25 req/ngày → dùng mock cho demo

**Bước 5: Tạo slide báo cáo phần Producer**
- [ ] Slide 1: Kiến trúc Producer (sơ đồ luồng dữ liệu)
- [ ] Slide 2: Demo chạy producer + screenshot log
- [ ] Slide 3: Giải thích MockDataGenerator (random walk, 5 loại dầu × 6 sàn = 30 records/batch)
- [ ] Slide 4 (optional): Alpha Vantage API demo

#### Deliverables:
- ✅ `target/FuelPriceProducer-1.0-SNAPSHOT.jar` build thành công
- ✅ Log chạy producer hiển thị "sent 30 records" mỗi 10 giây
- ✅ Kafka consumer CLI xác nhận nhận được JSON đúng format
- ✅ 4 slides báo cáo

---

### 🐳 NGƯỜI 2 — Docker / Infrastructure

**Phạm vi:** `Project/docker-compose.yml` + toàn bộ việc deploy

**Mục tiêu:** Đảm bảo toàn bộ stack (Kafka, Flink, Postgres, Metabase) chạy ổn định bằng 1 lệnh `docker-compose up`.

#### Files chịu trách nhiệm:
```
Project/
├── docker-compose.yml                   ✏️ FILE CHÍNH
├── script/
│   └── init_fuel_schema.sql             ← đọc hiểu (Người 3 sẽ sửa nếu cần)
└── KafkaConsumer/
    └── target/
        └── KafkaConsumer-1.0.jar        ← build bởi bạn, submit lên Flink
```

#### Checklist công việc:

**Bước 1: Hiểu docker-compose.yml (30 phút)**
- [ ] Đọc toàn bộ `docker-compose.yml` (6 services: zookeeper, kafka, flink-jm, flink-tm, postgres, metabase)
- [ ] Xác nhận port mapping không bị conflict với máy local:
  - 2181 (zookeeper), 9092 (kafka), 8081 (flink), 5432 (postgres), 3000 (metabase)
- [ ] Hiểu volume mount: `./script:/docker-entrypoint-initdb.d` → auto run SQL khi Postgres start

**Bước 2: Start toàn bộ stack**
- [ ] Chạy từ thư mục `Project/`:
  ```bash
  docker-compose up -d
  ```
- [ ] Chờ 60-90 giây để Kafka, Flink, Postgres khởi động hoàn toàn
- [ ] Kiểm tra tất cả container running:
  ```bash
  docker-compose ps
  ```
  Kết quả mong đợi: 6 services, tất cả "Up"

**Bước 3: Verify từng service**
- [ ] **Kafka:** `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092` (nếu trống là OK, topic tạo auto)
- [ ] **Flink UI:** Mở trình duyệt → http://localhost:8081 → thấy Flink Dashboard
- [ ] **PostgreSQL:** `docker exec -it postgres psql -U postgres -d fuel_prices -c "\dt"` → thấy 3 tables
- [ ] **Metabase:** Mở http://localhost:3000 → thấy trang setup/login

**Bước 4: Tạo Kafka topic (nếu chưa tự tạo)**
- [ ] Topic sẽ tự tạo khi producer gửi message đầu tiên
- [ ] Hoặc tạo thủ công:
  ```bash
  docker exec kafka kafka-topics --create \
    --bootstrap-server localhost:9092 \
    --topic fuel-prices \
    --partitions 3 \
    --replication-factor 1
  ```

**Bước 5: Build và deploy Flink job**
- [ ] Build KafkaConsumer JAR:
  ```bash
  cd Project/KafkaConsumer
  mvn clean package -DskipTests
  ```
  → `target/KafkaConsumer-1.0-SNAPSHOT-jar-with-dependencies.jar`
- [ ] Submit lên Flink qua UI: http://localhost:8081 → Submit New Job → Upload JAR
- [ ] Hoặc submit qua CLI:
  ```bash
  docker exec flink-jobmanager flink run \
    -c org.cloud.KafkaConsumerApplication \
    /path/to/KafkaConsumer-1.0-SNAPSHOT-jar-with-dependencies.jar
  ```
- [ ] Verify job đang chạy: Flink UI → Jobs → Running Jobs → thấy "Fuel Price Stream Processor"

**Bước 6: End-to-end test**
- [ ] Start producer (Người 1)
- [ ] Sau 30 giây, kiểm tra PostgreSQL có data:
  ```sql
  SELECT COUNT(*) FROM fuel_prices_raw;           -- phải > 0
  SELECT COUNT(*) FROM fuel_price_window_agg;     -- phải > 0 sau 1 phút
  SELECT COUNT(*) FROM fuel_price_alerts;         -- có thể 0 nếu chưa đủ biến động
  ```
- [ ] Troubleshoot nếu cần: `docker logs flink-taskmanager` hoặc xem Flink UI → Task Manager → Logs

**Bước 7: Tạo slide báo cáo phần Infrastructure**
- [ ] Slide 1: Docker Compose architecture diagram
- [ ] Slide 2: `docker-compose ps` screenshot (6 services running)
- [ ] Slide 3: Flink UI screenshot (job running)
- [ ] Slide 4: Kafka topic và consumer group screenshot

#### Deliverables:
- ✅ `docker-compose up -d` → 6 containers chạy ổn định
- ✅ Kafka topic `fuel-prices` tồn tại
- ✅ Flink job "Fuel Price Stream Processor" đang chạy (Running Jobs)
- ✅ Postgres có 3 tables với data sau khi producer chạy 5 phút
- ✅ 4 slides báo cáo

---

### 🗄️ NGƯỜI 3 — PostgreSQL / Database

**Phạm vi:** `Project/script/init_fuel_schema.sql` + queries, views, tối ưu DB

**Mục tiêu:** Schema hoạt động đúng, dữ liệu được insert chính xác, có views phục vụ Metabase.

#### Files chịu trách nhiệm:
```
Project/script/
└── init_fuel_schema.sql                 ✏️ FILE CHÍNH
```

Đọc thêm để hiểu Flink insert gì:
```
Project/KafkaConsumer/src/main/java/org/cloud/
├── KafkaConsumerApplication.java        ← xem INSERT statements (dòng ~90-140)
├── model/
│   ├── FuelPrice.java                   ← raw record model
│   ├── WindowedFuelPrice.java           ← window aggregation model
│   └── PriceAlert.java                  ← alert model
```

#### Checklist công việc:

**Bước 1: Đọc và hiểu schema (45 phút)**
- [ ] Đọc `init_fuel_schema.sql` toàn bộ
- [ ] Hiểu 3 bảng chính:
  ```sql
  fuel_prices_raw          ← Flink INSERT mỗi raw record (30 records/10 giây)
  fuel_price_window_agg    ← Flink INSERT mỗi 1 phút / (fuelType, region)
  fuel_price_alerts        ← Flink INSERT khi giá biến động > 5%
  ```
- [ ] Hiểu 4 views: v_latest_prices, v_hourly_avg, v_price_volatility, v_regional_comparison
- [ ] Đọc `KafkaConsumerApplication.java` dòng ~90-140 để xác nhận tên cột khớp

**Bước 2: Verify schema sau khi Postgres start**
- [ ] Kết nối Postgres:
  ```bash
  docker exec -it postgres psql -U postgres -d fuel_prices
  ```
- [ ] Kiểm tra tables tồn tại: `\dt`
- [ ] Kiểm tra columns: `\d fuel_prices_raw`
- [ ] Chạy thử query:
  ```sql
  SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
  ```

**Bước 3: Test INSERT thủ công (không cần Flink)**
- [ ] Insert 1 record test vào `fuel_prices_raw`:
  ```sql
  INSERT INTO fuel_prices_raw (event_timestamp, fuel_type, price, price_unit, location, region, source)
  VALUES ('2025-04-12 10:00:00', 'WTI_CRUDE', 75.23, 'USD/barrel', 'New York (NYMEX)', 'North America', 'manual-test');
  ```
- [ ] Verify: `SELECT * FROM fuel_prices_raw;`
- [ ] Test view: `SELECT * FROM v_latest_prices;`

**Bước 4: Verify data từ Flink (sau khi end-to-end test)**
- [ ] Chạy các query kiểm tra data quality:
  ```sql
  -- Xem 10 records gần nhất
  SELECT * FROM fuel_prices_raw ORDER BY event_timestamp DESC LIMIT 10;

  -- Số records theo loại dầu
  SELECT fuel_type, COUNT(*) FROM fuel_prices_raw GROUP BY fuel_type;

  -- Xem aggregation theo 1 phút
  SELECT * FROM fuel_price_window_agg ORDER BY window_start DESC LIMIT 10;

  -- Xem alerts
  SELECT * FROM fuel_price_alerts ORDER BY alert_timestamp DESC;
  ```
- [ ] Xác nhận không có NULL trong các cột bắt buộc
- [ ] Xác nhận giá trong range hợp lý: WTI > 50 và < 100

**Bước 5: Thêm index để tối ưu (nếu có thời gian)**
- [ ] Thêm vào `init_fuel_schema.sql` (hoặc chạy trực tiếp):
  ```sql
  CREATE INDEX IF NOT EXISTS idx_raw_event_time  ON fuel_prices_raw(event_timestamp DESC);
  CREATE INDEX IF NOT EXISTS idx_raw_fuel_region ON fuel_prices_raw(fuel_type, region);
  CREATE INDEX IF NOT EXISTS idx_agg_window_fuel ON fuel_price_window_agg(window_start DESC, fuel_type);
  ```

**Bước 6: Chuẩn bị queries cho Metabase (Người 4)**
- [ ] Viết file `Project/script/metabase_queries.sql` với các query sẵn:

  **Query 1: Giá mới nhất mỗi loại dầu**
  ```sql
  SELECT fuel_type, price, price_unit, location, event_timestamp
  FROM fuel_prices_raw r1
  WHERE event_timestamp = (
      SELECT MAX(event_timestamp) FROM fuel_prices_raw r2
      WHERE r2.fuel_type = r1.fuel_type
  )
  ORDER BY fuel_type;
  ```

  **Query 2: Biến động giá 1 giờ qua**
  ```sql
  SELECT fuel_type, region,
         AVG(avg_price) AS mean_price,
         MAX(max_price) AS peak_price,
         MIN(min_price) AS trough_price,
         MAX(max_price) - MIN(min_price) AS price_range
  FROM fuel_price_window_agg
  WHERE window_start >= NOW() - INTERVAL '1 hour'
  GROUP BY fuel_type, region
  ORDER BY price_range DESC;
  ```

  **Query 3: Lịch sử giá WTI theo thời gian (cho line chart)**
  ```sql
  SELECT window_start AS "Time", avg_price AS "Price (USD/barrel)"
  FROM fuel_price_window_agg
  WHERE fuel_type = 'WTI_CRUDE' AND region = 'North America'
  ORDER BY window_start;
  ```

  **Query 4: So sánh WTI vs Brent**
  ```sql
  SELECT w.window_start AS "Time",
         MAX(CASE WHEN w.fuel_type = 'WTI_CRUDE'   THEN w.avg_price END) AS "WTI (USD/barrel)",
         MAX(CASE WHEN w.fuel_type = 'BRENT_CRUDE' THEN w.avg_price END) AS "Brent (USD/barrel)"
  FROM fuel_price_window_agg w
  WHERE w.fuel_type IN ('WTI_CRUDE','BRENT_CRUDE')
  GROUP BY w.window_start
  ORDER BY w.window_start;
  ```

  **Query 5: Alerts gần đây**
  ```sql
  SELECT alert_timestamp, fuel_type, location,
         previous_price, current_price,
         ROUND(change_percent::numeric, 2) AS "Change %",
         direction
  FROM fuel_price_alerts
  ORDER BY alert_timestamp DESC
  LIMIT 20;
  ```

**Bước 7: Tạo slide báo cáo phần Database**
- [ ] Slide 1: ERD (3 tables + relationships)
- [ ] Slide 2: Schema design decisions (why 3 tables)
- [ ] Slide 3: Query results screenshots (data đang insert)
- [ ] Slide 4: Views explanation

#### Deliverables:
- ✅ 3 tables tồn tại và nhận data từ Flink
- ✅ 4 views hoạt động và trả về kết quả đúng
- ✅ File `Project/script/metabase_queries.sql` với 5 queries cho Người 4
- ✅ 4 slides báo cáo

---

### 📊 NGƯỜI 4 — Metabase Dashboard

**Phạm vi:** Metabase UI tại http://localhost:3000

**Mục tiêu:** Dashboard trực quan, realtime, hiển thị giá dầu thế giới với charts đẹp.

#### Không cần sửa code, chỉ cần:
- Docker đang chạy (Người 2)
- PostgreSQL có data (Người 3)
- Các query SQL từ Người 3

#### Checklist công việc:

**Bước 1: Setup Metabase lần đầu (15 phút)**
- [ ] Mở http://localhost:3000
- [ ] Chọn ngôn ngữ → nhập thông tin tài khoản admin
- [ ] Kết nối database:
  - Database type: **PostgreSQL**
  - Host: **postgres** (tên service trong Docker, không phải localhost!)
  - Port: **5432**
  - Database name: **fuel_prices**
  - Username: **postgres**
  - Password: **123456**
- [ ] Click "Test Connection" → xác nhận kết nối thành công
- [ ] Click "Next" → hoàn thành setup

**Bước 2: Tạo Dashboard "Global Oil Price Monitor"**
- [ ] Vào Dashboards → New Dashboard
- [ ] Đặt tên: **"🌍 Global Oil Price Monitor — Real-time"**

**Bước 3: Tạo các Cards/Charts**

> Dùng SQL queries từ file `Project/script/metabase_queries.sql` do Người 3 cung cấp.

**Card 1: Bảng giá mới nhất (Table)**
- [ ] New Question → SQL Editor → dán Query 1 (giá mới nhất mỗi loại dầu)
- [ ] Visualization: **Table**
- [ ] Title: "📌 Current Oil Prices"
- [ ] Save → Add to Dashboard

**Card 2: WTI Crude — line chart (Line Chart)**
- [ ] New Question → SQL Editor → dán Query 3 (lịch sử WTI)
- [ ] Visualization: **Line**
- [ ] X-axis: Time | Y-axis: Price (USD/barrel)
- [ ] Title: "📈 WTI Crude Oil (USD/barrel)"
- [ ] Save → Add to Dashboard

**Card 3: WTI vs Brent so sánh (Line Chart)**
- [ ] New Question → SQL Editor → dán Query 4 (WTI vs Brent)
- [ ] Visualization: **Line** (2 series)
- [ ] Title: "📊 WTI vs Brent Spread"
- [ ] Save → Add to Dashboard

**Card 4: Price Volatility (Bar Chart)**
- [ ] New Question → SQL Editor → dán Query 2 (volatility)
- [ ] Visualization: **Bar** → group by fuel_type
- [ ] Title: "⚡ Price Volatility (1h Range)"
- [ ] Save → Add to Dashboard

**Card 5: Price Alerts (Table)**
- [ ] New Question → SQL Editor → dán Query 5 (alerts)
- [ ] Visualization: **Table** (highlight row: direction = UP = màu đỏ, DOWN = xanh)
- [ ] Title: "🚨 Price Alerts"
- [ ] Save → Add to Dashboard

**Bước 4: Cấu hình Auto-refresh**
- [ ] Mở Dashboard → click "..." → Auto-refresh
- [ ] Chọn: **1 minute** (refresh mỗi 1 phút)
- [ ] Dashboard sẽ tự update khi có data mới từ Flink

**Bước 5: Điều chỉnh layout**
- [ ] Sắp xếp các cards: Row 1 (Current Prices), Row 2 (WTI Line + Brent Line), Row 3 (Volatility + Alerts)
- [ ] Resize charts cho đẹp
- [ ] Thêm Text card làm tiêu đề section

**Bước 6: Tạo slide báo cáo phần Metabase**
- [ ] Slide 1: Screenshot Dashboard tổng quan
- [ ] Slide 2: Giải thích từng chart
- [ ] Slide 3: Demo realtime (producer đang chạy + dashboard tự update)
- [ ] Slide 4: Giải thích Auto-refresh và kết nối PostgreSQL

#### Deliverables:
- ✅ Dashboard với 5+ charts đang hiển thị data thực
- ✅ Auto-refresh 1 phút hoạt động
- ✅ Ít nhất 1 line chart giá theo thời gian
- ✅ Bảng alerts hiển thị khi có biến động giá
- ✅ 4 slides báo cáo

---

### 🎯 TRƯỞNG NHÓM / NGƯỜI LEAD — Tích hợp & Báo cáo

**Phạm vi:** Phối hợp 4 người, tổng hợp báo cáo, demo end-to-end

**Checklist:**
- [ ] **Ngày 1:** Fork repo, clone về máy tất cả thành viên, giải thích kiến trúc
- [ ] **Ngày 2-3:** Người 1 build producer, Người 2 start Docker stack
- [ ] **Ngày 4-5:** End-to-end test (producer → Kafka → Flink → Postgres)
- [ ] **Ngày 6-7:** Người 3 verify DB, Người 4 setup Metabase
- [ ] **Ngày 8:** Integration test toàn hệ thống cùng lúc
- [ ] **Ngày 9:** Fix bugs, optimize
- [ ] **Ngày 10:** Chuẩn bị demo, merge slides

**Lệnh khởi động toàn hệ thống (thứ tự đúng):**
```bash
# 1. Start infrastructure
cd Project
docker-compose up -d

# 2. Chờ 90 giây, verify
docker-compose ps

# 3. Submit Flink job (sau khi build)
# Vào http://localhost:8081 → Submit Job → upload JAR

# 4. Start producer
cd KafkaProducer/FuelPriceProducer
java -jar target/FuelPriceProducer-1.0-SNAPSHOT.jar

# 5. Mở Metabase
# http://localhost:3000
```

---

## 🗓️ Timeline (10 ngày)

| Ngày | Người 1 | Người 2 | Người 3 | Người 4 |
|---|---|---|---|---|
| 1 | Đọc code, hiểu Producer | Đọc docker-compose | Đọc SQL schema | Xem hướng dẫn Metabase |
| 2 | Build producer JAR | `docker-compose up` | Connect DB, verify tables | Setup Metabase UI |
| 3 | Test gửi Kafka | Verify 6 containers | Test INSERT thủ công | Kết nối Postgres |
| 4 | Producer gửi data ổn định | Submit Flink job | Viết metabase_queries.sql | Tạo Card 1+2 |
| 5 | (optional) Alpha Vantage | Monitor Flink logs | Verify Flink insert data | Tạo Card 3+4+5 |
| 6 | Slide 1-4 | Slide 1-4 | Slide 1-4 | Slide 1-4 |
| 7 | Integration test | Fix network issues | Fix schema nếu cần | Dashboard layout |
| 8 | End-to-end demo | End-to-end demo | End-to-end demo | Auto-refresh test |
| 9 | Review slide | Review slide | Review slide | Review slide |
| 10 | **DEMO + BÁO CÁO** | | | |

---

## ⚠️ Troubleshooting thường gặp

| Vấn đề | Nguyên nhân | Giải pháp |
|---|---|---|
| Kafka connection refused | Container chưa ready | Chờ 30s thêm, `docker logs kafka` |
| Flink job failed | JAR classpath sai | Check `pom.xml` shade plugin config |
| Postgres table not found | SQL init chưa chạy | `docker exec postgres psql -U postgres -d fuel_prices -f /docker-entrypoint-initdb.d/init_fuel_schema.sql` |
| Metabase can't connect DB | Dùng `localhost` thay vì `postgres` | Host trong Metabase phải là `postgres` (tên service Docker) |
| No data in Flink | Producer chưa chạy hoặc topic sai | Verify topic name = `fuel-prices` ở cả producer lẫn consumer |
| Build failure: pricePerLiter | Field cũ còn sót | Tìm kiếm `pricePerLiter` trong toàn project, đổi thành `price` |

---

## 📁 Cấu trúc file quan trọng

```
Project/
├── docker-compose.yml               ← NGƯỜI 2
├── PHAN_CONG_CONG_VIEC.md          ← file này
├── script/
│   ├── init_fuel_schema.sql         ← NGƯỜI 3 (auto-load khi Postgres start)
│   └── metabase_queries.sql         ← NGƯỜI 3 tạo cho NGƯỜI 4
├── KafkaProducer/
│   └── FuelPriceProducer/           ← NGƯỜI 1
│       ├── pom.xml
│       └── src/main/java/org/fuel/
│           ├── FuelPriceProducer.java
│           ├── MockDataGenerator.java
│           ├── HttpApiSource.java   ← optional: Alpha Vantage API
│           └── model/FuelPrice.java
└── KafkaConsumer/                   ← NGƯỜI 2 build + deploy
    ├── pom.xml
    └── src/main/java/org/cloud/
        ├── KafkaConsumerApplication.java
        ├── Constant.java
        ├── source/KafkaInvoiceSource.java
        ├── process/
        │   ├── FuelPriceAggregator.java
        │   └── PriceChangeDetector.java
        └── model/
            ├── FuelPrice.java
            ├── WindowedFuelPrice.java
            └── PriceAlert.java
```

---

*File này được tạo bởi Lead Engineer. Cập nhật lần cuối: tháng 4/2025.*
