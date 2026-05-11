# 🛢️ VES — Real-time Fuel Price Monitoring System

> Hệ thống giám sát giá nhiên liệu thời gian thực qua **Kafka + Flink + PostgreSQL**, kèm **JavaFX Admin Desktop** (môn Java) và **Android App** (môn Mobile). Tận dụng 90% code stream-processing có sẵn, thêm UI Java + Spring Boot REST API + Android client.

[![Status](https://img.shields.io/badge/status-WIP%20Phase%200-yellow)]() [![Java](https://img.shields.io/badge/Java-11%2B-orange)]() [![License](https://img.shields.io/badge/license-Educational-blue)]()

---

## 📦 Stack tổng quan

| Tầng | Công nghệ | Vai trò |
|------|-----------|---------|
| **Data Source** | Java Producer (Mock + Alpha Vantage API) | Sinh dữ liệu giá 5 loại nhiên liệu × 5 địa điểm |
| **Message Broker** | Apache Kafka 7.5.0 | Topic `fuel-prices` |
| **Stream Processing** | Apache Flink 1.17.0 | 3 luồng song song: raw / window 1-phút / alert |
| **Storage** | PostgreSQL 15 | 3 bảng + 6 view + (Phase 2 thêm: users/regions/alerts/rules) |
| **BI** | Metabase 0.47 | Dashboard tự thiết lập |
| **REST API** | Spring Boot 3 (Phase 4) | JWT + 8 endpoint cho Desktop + Mobile |
| **Desktop UI** | JavaFX 21 (Phase 5) | 3-layer, JDBC trực tiếp, 5 màn, JUnit |
| **Mobile UI** | Android Studio (Phase 6) | Retrofit + MPAndroidChart, 4 màn |
| **Orchestration** | Docker Compose | One-click stack 6 service |

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

## 🛣️ Lộ trình build (Minimalist Plan — §24 UPGRADE_PLAN.md)

| Phase | Mục tiêu | Status |
|-------|----------|--------|
| **0** | Foundation — restructure repo + verify code có sẵn | 🟡 In progress |
| **1** | Docker Lite — tối ưu RAM ~2.7GB | ⬜ |
| **2** | Schema mở rộng — thêm 4 bảng (`users`, `regions`, `alerts`, `alert_rules`) | ⬜ |
| **3** | Flink Alert PF — thêm 1 process function vào Flink job | ⬜ |
| **4** | Spring Boot REST API — 1 module, 8 endpoint, JWT, SSE | ⬜ |
| **5** | JavaFX Admin Desktop ⭐ — 5 màn, 3-layer, JDBC, 10+ JUnit | ⬜ |
| **6** | Android App — 4 activity, Retrofit, MPAndroidChart | ⬜ |
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
