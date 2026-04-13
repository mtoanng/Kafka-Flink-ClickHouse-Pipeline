## IS402.P21 — Real-time Global Oil Price Monitor

# 🎯 Bài toán
Xây dựng hệ thống **giám sát giá dầu thế giới thời gian thực** từ Kafka → Flink → PostgreSQL → Metabase.

# 🎯 Mục tiêu
- Thiết kế và triển khai hệ thống xử lý dữ liệu thời gian thực.
- Thu thập, tổng hợp, phân tích giá dầu từ nhiều sàn giao dịch.
- Phát hiện biến động giá (alerts) tự động.
- Hiển thị dashboard trực quan, realtime.
- Hệ thống có khả năng mở rộng, tin cậy cao, hiệu suất tốt.

# 🏗️ Kiến trúc hệ thống

```
┌────────────────────────────────────────────────────────────────────────────┐
│                           REALTIME DATA PIPELINE                          │
│                                                                             │
│ [Java Producer]  ──► [Kafka]  ──► [Flink]  ──► [PostgreSQL]  ──► [Metabase]│
│ MockDataGenerator     Topic        3 jobs       3 tables       Dashboard   │
│ (realtime)        fuel-prices    window agg     + 4 views      (port 3000) │
│                                   alert detect                             │
└────────────────────────────────────────────────────────────────────────────┘
```

# 📦 Stack công nghệ
- **Message Broker:** Apache Kafka 7.6.0
- **Stream Processing:** Apache Flink 1.17.0 (Java 11)
- **Database:** PostgreSQL 15
- **BI Dashboard:** Metabase (latest)
- **Containerization:** Docker Compose v3.8
- **Build Tool:** Maven 3.x

# 📊 Dữ liệu: Giá dầu thế giới
- **WTI Crude** (USD/barrel) — NYMEX, New York
- **Brent Crude** (USD/barrel) — ICE, London
- **Gasoline Futures** (USD/gallon) — NYMEX
- **Diesel** (USD/gallon) — US market
- **Natural Gas** (USD/MMBtu) — Henry Hub, Houston

6 sàn giao dịch mock: New York, London, Singapore, Houston, Rotterdam, Tokyo

# 👥 Phân công công việc

| Thành viên | Phạm vi | Deliverables |
|---|---|---|
| **Người 1** | Java Producer + MockDataGenerator | Uber-JAR, log, Kafka messages |
| **Người 2** | Docker Compose, deploy stack | 6 containers running, Flink job |
| **Người 3** | PostgreSQL schema, queries | 3 tables, 4 views, metabase_queries.sql |
| **Người 4** | Metabase dashboard | 5+ charts, auto-refresh |
| **Lead** | Tích hợp, demo, báo cáo | End-to-end test, presentation |

**Chi tiết xem:** [Project/PHAN_CONG_CONG_VIEC.md](Project/PHAN_CONG_CONG_VIEC.md)

# 🚀 Quick Start

**Bước 1: Start infrastructure**
```bash
cd Project
docker-compose up -d
```

**Bước 2: Build & submit Flink job**
```bash
cd KafkaConsumer
mvn clean package -DskipTests
# → Submit JAR tại http://localhost:8081
```

**Bước 3: Start producer**
```bash
cd KafkaProducer/FuelPriceProducer
mvn clean package -DskipTests
java -jar target/FuelPriceProducer-1.0-SNAPSHOT.jar
```

**Bước 4: Dashboard**
- Metabase: http://localhost:3000
- Flink UI: http://localhost:8081

# 📚 Tài liệu quan trọng

| File | Mô tả |
|---|---|
| [Project/PHAN_CONG_CONG_VIEC.md](Project/PHAN_CONG_CONG_VIEC.md) | Phân công chi tiết + checklist cho 4 người |
| [Project/script/presentation_script.md](Project/script/presentation_script.md) | Script thuyết trình báo cáo tiến độ |
| [Project/docker-compose.yml](Project/docker-compose.yml) | Cấu hình Docker stack |
| [Project/script/init_fuel_schema.sql](Project/script/init_fuel_schema.sql) | Schema PostgreSQL |
| [Project/script/metabase_queries.sql](Project/script/metabase_queries.sql) | SQL queries cho Metabase |

# 📁 Cấu trúc project

```
Project/
├── docker-compose.yml               ← cấu hình stack
├── PHAN_CONG_CONG_VIEC.md          ← phân công chi tiết
├── script/
│   ├── init_fuel_schema.sql         ← schema PostgreSQL
│   ├── metabase_queries.sql         ← queries cho Metabase
│   └── presentation_script.md       ← script thuyết trình
├── KafkaProducer/
│   └── FuelPriceProducer/           ← Java producer module
│       ├── pom.xml
│       └── src/main/java/org/fuel/
│           ├── FuelPriceProducer.java
│           ├── MockDataGenerator.java
│           ├── HttpApiSource.java   ← optional: Alpha Vantage API
│           └── model/FuelPrice.java
└── KafkaConsumer/                   ← Flink consumer module
    ├── pom.xml
    └── src/main/java/org/cloud/
        ├── KafkaConsumerApplication.java
        ├── Constant.java
        ├── process/ (FuelPriceAggregator, PriceChangeDetector)
        └── model/ (FuelPrice, WindowedFuelPrice, PriceAlert)
```

# ⚙️ Cấu hình mặc định

| Thành phần | Host | Port | Cấu hình |
|---|---|---|---|
| Zookeeper | localhost | 2181 | - |
| Kafka | localhost | 9092 | broker.id=1 |
| Flink JobManager | localhost | 8081 | parallelism=2 |
| PostgreSQL | localhost | 5432 | user=postgres, pass=123456, db=fuel_prices |
| Metabase | localhost | 3000 | - |

# 🐛 Troubleshooting

| Vấn đề | Giải pháp |
|---|---|
| Kafka connection refused | `docker logs kafka` → chờ 30s thêm |
| Flink job failed | Check `pom.xml` shade plugin, verify JAR classpath |
| Postgres table not found | Verify SQL init script chạy: `docker logs postgres` |
| Metabase can't connect DB | Host phải là `postgres` (tên service Docker), không phải localhost |
| No data in dashboard | Verify producer đang chạy, topic name = `fuel-prices` |

---

**Môn học:** IS402.P21  
**Cập nhật lần cuối:** Tháng 4/2026
