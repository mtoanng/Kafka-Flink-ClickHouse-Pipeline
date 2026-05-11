# 📢 SCRIPT THUYẾT TRÌNH BÁO CÁO TIẾN ĐỘ ĐỒ ÁN

## 🎯 Mục tiêu
- Giới thiệu kiến trúc tổng quan của hệ thống.
- Trình bày tiến độ hiện tại của từng thành viên.
- Giải thích cách các thành phần kết nối với nhau.
- Đề xuất kế hoạch hoàn thành các phần còn lại.

---

## 🗂️ Nội dung trình bày

### 1. **Mở đầu (Lead trình bày)**
- Chào thầy/cô và các bạn.
- Giới thiệu đề tài: **"Hệ thống giám sát giá dầu thế giới thời gian thực"**.
- Mục tiêu: Xây dựng pipeline xử lý dữ liệu từ Kafka → Flink → PostgreSQL → Metabase.
- Công nghệ sử dụng: Kafka, Flink, PostgreSQL, Metabase, Docker.
- Tổng quan kiến trúc:

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
```

- Phân công công việc:
  - Người 1: Java Producer (Kafka).
  - Người 2: Docker Compose, deploy stack.
  - Người 3: PostgreSQL schema, queries.
  - Người 4: Metabase dashboard.
  - Lead: Tích hợp, báo cáo.

---

### 2. **Phần của từng thành viên**

#### 🧑‍💻 **Người 1: Java Producer (Kafka)**
- **Mục tiêu:** Gửi dữ liệu giá dầu vào Kafka topic `fuel-prices`.
- **Tiến độ:**
  - Đã tạo module Producer (Java 11, Maven).
  - Đã viết `MockDataGenerator` sinh dữ liệu giả lập (5 loại dầu × 6 sàn = 30 records/batch).
  - Đã cấu hình Kafka Producer (`application.properties`).
  - Đã test gửi dữ liệu vào Kafka thành công.
- **Demo:**
  - Chạy Producer → log "Batch #1: sent 30 records".
  - Kafka consumer CLI → thấy JSON đúng format.
- **Kế hoạch:**
  - (Optional) Tích hợp Alpha Vantage API để lấy giá thực.

#### 🐳 **Người 2: Docker / Infrastructure**
- **Mục tiêu:** Deploy toàn bộ stack (Kafka, Flink, Postgres, Metabase).
- **Tiến độ:**
  - Đã viết `docker-compose.yml` (6 services: zookeeper, kafka, flink-jm, flink-tm, postgres, metabase).
  - Đã start stack thành công, tất cả container "Up".
  - Đã verify từng service:
    - Kafka: topic `fuel-prices` tự tạo.
    - Flink UI: http://localhost:8081 → thấy dashboard.
    - PostgreSQL: 3 tables được tạo tự động.
    - Metabase: http://localhost:3000 → setup xong.
- **Demo:**
  - `docker-compose ps` → 6 containers running.
  - Flink UI → thấy job "Fuel Price Stream Processor".
- **Kế hoạch:**
  - Monitor logs, fix lỗi nếu có.

#### 🗄️ **Người 3: PostgreSQL / Database**
- **Mục tiêu:** Thiết kế schema, đảm bảo data được insert đúng.
- **Tiến độ:**
  - Đã viết `init_fuel_schema.sql` (3 tables: raw, window agg, alerts).
  - Đã tạo 4 views phục vụ Metabase.
  - Đã test INSERT thủ công → data đúng format.
  - Đã verify Flink insert data → 3 tables có data.
- **Demo:**
  - Query `fuel_prices_raw` → thấy 10 records gần nhất.
  - Query `v_latest_prices` → thấy giá mới nhất mỗi loại dầu.
- **Kế hoạch:**
  - Tối ưu index, viết thêm queries cho Metabase.

#### 📊 **Người 4: Metabase Dashboard**
- **Mục tiêu:** Tạo dashboard trực quan, realtime.
- **Tiến độ:**
  - Đã setup Metabase, kết nối PostgreSQL.
  - Đã tạo dashboard "Global Oil Price Monitor".
  - Đã thêm 5 charts:
    - Bảng giá mới nhất.
    - Line chart giá WTI theo thời gian.
    - Line chart so sánh WTI vs Brent.
    - Bar chart volatility.
    - Table alerts.
  - Đã cấu hình auto-refresh mỗi 1 phút.
- **Demo:**
  - Dashboard tự update khi producer gửi data mới.
- **Kế hoạch:**
  - Điều chỉnh layout, thêm text cards.

---

### 3. **Kết luận (Lead trình bày)**
- Tổng kết tiến độ:
  - Producer → Kafka → Flink → PostgreSQL → Metabase đã hoạt động end-to-end.
  - Tất cả thành viên đã hoàn thành phần việc được giao.
- Kế hoạch hoàn thành:
  - Tích hợp Alpha Vantage API (nếu cần).
  - Tối ưu schema, thêm index.
  - Hoàn thiện dashboard (layout, text cards).
- Cảm ơn thầy/cô và các bạn đã lắng nghe.

---

## 🕒 Thời lượng trình bày
- **Mở đầu:** 3 phút.
- **Người 1:** 4 phút.
- **Người 2:** 4 phút.
- **Người 3:** 4 phút.
- **Người 4:** 4 phút.
- **Kết luận:** 2 phút.
- **Tổng cộng:** 21 phút.

---

*Script này được tạo bởi Lead Engineer. Cập nhật lần cuối: tháng 4/2026.*