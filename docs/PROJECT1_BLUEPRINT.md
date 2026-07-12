# Blueprint — VES-Monitor → "F1 Live Telemetry Platform" (Modern Streaming Platform)

Repo: `Real-time-processing-with-Kafka-Flink-Postgres`

> **Phiên bản này đã gọt phần domain complexity** (anomaly detection rolling-average, Canvas track map, ground-truth overlay) để tập trung effort vào **engineering value**: managed Kafka, Avro/Schema Registry, stateful stream processing, CDC, ClickHouse serving, Kafka Connect S3, Terraform, GitLab CI.

## 0. Ground rules — bắt buộc đọc trước khi code

- **Không generate/fake dữ liệu.** Chỉ dùng dữ liệu thật từ OpenF1 API, replay theo đúng timestamp gốc của session.
- **Một Flink job duy nhất**, viết bằng DataStream API — không tách Job 2, không Table/SQL API riêng, không Spark/Scala. Windowed aggregate dùng `.window(TumblingEventTimeWindows...)` ngay trong cùng job.
- **Postgres không phải serving layer song song với ClickHouse** — Postgres chỉ tồn tại vì (1) Debezium CDC cần 1 relational source (bảng `driver_alert_config`), và (2) bảng `users` cho JWT auth. Toàn bộ analytical serving đi qua ClickHouse.
- **MVP-first, cắt ceremony**: mỗi phase chỉ cần đạt đúng Acceptance Criteria tối thiểu đã ghi — không cần test toàn diện edge-case, không cần viết docs dài dòng ngoài phạm vi yêu cầu, không cần cron/service hoá những việc chạy 1 lần là đủ chứng minh khái niệm.
- **Test cũ**: giữ **phần test nào còn hợp lý** pass (JavaFX admin logic, backend-api auth). Test nào gắn với dataset/schema cũ → xoá cùng lúc với code nó test, thay bằng test tối thiểu cho logic mới. Không ép buộc "88 test pass xuyên suốt" khi nature of project đã đổi.
- CI/CD: GitLab CI, không dùng GitHub Actions.
- Thiếu credential cloud → hỏi người dùng, không tự bịa.
- Commit theo từng phase.

## 1. Theme & vì sao đổi

Theme cũ ("Vietnam Energy Security") và các bản dataset trung gian (UCI Electricity/Household Power) không đạt tiêu chí "ai nhìn Grafana cũng hiểu ngay, logic xử lý đơn giản". Theme cuối cùng:

**"F1 Live Telemetry Platform" — replay dữ liệu telemetry thật của 1 chặng đua F1 (qua OpenF1 API), xử lý real-time bằng Flink, hiển thị live trên Grafana.**

Lý do chọn, đã thống nhất:
- Dữ liệu JSON sạch từ API, không cần parse CSV châu Âu phức tạp như trước.
- `keyBy(driver_number)` tự nhiên — đúng grain của bài toán streaming, không cần tự bịa mapping zone/vùng.
- `driver_number` là key phân vùng hoàn hảo cho việc demo watermark, keyed state, windowing.
- Ai xem dashboard cũng hiểu ngay, không cần kiến thức domain.

## 2. Nguồn dữ liệu — OpenF1 API

**API**: `https://api.openf1.org/v1/` — public, không cần API key.

| Endpoint | Trường quan trọng | Vai trò |
|---|---|---|
| `GET /car_data?session_key={key}` | `date, driver_number, speed, throttle, brake, rpm, n_gear, drs` | Nguồn chính cho stream (~3-4 mẫu/giây/xe) |
| `GET /location?session_key={key}` | `date, driver_number, x, y, z` | Toạ độ track (dự phòng, không bắt buộc cho MVP) |
| `GET /race_control?session_key={key}` | `date, category, flag, message` | Bảng phụ sự kiện hiển thị trên Grafana |
| `GET /laps?session_key={key}` | `driver_number, lap_number, lap_duration` | Bảng phụ, fastest lap ticker |
| `GET /sessions?year=2025` | `session_key, session_name, circuit_short_name, date_start` | Dùng để chọn 1 session cụ thể trước khi tải data |

**Chọn 1 session cụ thể** (ví dụ 1 chặng đua gần đây, race session — không lấy cả practice/qualifying/race để giữ dữ liệu gọn) — không tải toàn bộ lịch sử nhiều mùa giải.

## 3. Kiến trúc cuối cùng

```
OpenF1 API (car_data + race_control, 1 session cụ thể)
                    │
        Fetch script tải về JSON, lưu local
        làm nguồn cho Replay Engine (không gọi API live khi replay)
                    │
              Replay Engine (Java)
              giữ đúng timestamp gốc (field `date` trong response),
              speed factor cấu hình (1x/5x/20x)
                    │
        Confluent Cloud Kafka + Schema Registry (Avro)
        topics: car-telemetry-events, race-control-events
                    │
              Flink (Java) — MỘT job duy nhất
    ┌───────────────────────────────────────────┐
    │  1. Parse + watermark (event time = `date`) │
    │  2. keyBy(driver_number)                    │
    │  3. Tumbling window 10-giây rollup            │
    │     (avg speed, max speed, avg throttle)     │
    │  4. Broadcast state nhận config runtime        │
    │     (CDC từ driver_alert_config)              │
    │     → ví dụ: route/flag thay đổi apply ngay   │
    │  Checkpoint 30s, Savepoint khi deploy          │
    └───────────────────────────────────────────┘
                    │
        ┌───────────┴────────────┐
        ▼                        ▼
   ClickHouse                S3 Archive
   (serving chính)      (Kafka Connect S3 Sink)
   bảng: raw_telemetry,
   rollup_10s, race_control_events
        │
        ▼
   Grafana ("F1 Live Telemetry" dashboard)
        ▲
        │ (đọc/ghi config qua Postgres)
   Postgres (chỉ 2 bảng: driver_alert_config, users)
        │
        ▼
   Debezium CDC → Kafka topic riêng → Flink broadcast state
        ▲
        │
   JavaFX desktop admin (sửa config qua Postgres,
   KHÔNG dùng để visualize — Grafana đảm nhận việc đó)
```

**Pattern kỹ thuật chính** (đây là phần ăn tiền khi phỏng vấn, không phải nghiệp vụ):
- **Managed Kafka + Schema Registry** (Confluent Cloud) — không tự operate cluster.
- **Avro + schema evolution** — contract-first messaging.
- **Stateful stream processing** — watermark + keyed window + checkpoint.
- **Broadcast state pattern** — CDC cập nhật config runtime vào Flink không cần redeploy (pattern chính thức trong Flink docs, lý do kiến trúc rõ ràng).
- **CDC via Debezium** — change data capture thực thụ, không phải polling.
- **Kafka Connect S3 Sink** — tận dụng Connect cluster đã chạy Debezium, không thêm hạ tầng thừa.
- **OLAP swap** — Postgres → ClickHouse cho serving analytical (swap, không song song).

## 4. Definition of Done (MVP)

1. Dữ liệu 100% từ OpenF1 API (1 session thật), replay đúng event-time.
2. 1 Flink job duy nhất: watermark, keyed window aggregate, broadcast state (CDC), checkpoint, savepoint (verify 1 lần) — đủ để chứng minh khái niệm, không cần test exhaustive.
3. Kafka + Schema Registry trên Confluent Cloud, verify schema evolution 1 lần (thêm 1 field, xác nhận không lỗi).
4. Postgres chỉ 2 bảng, CDC qua Debezium hoạt động, cập nhật config runtime không cần redeploy Flink.
5. ClickHouse là serving layer analytical duy nhất.
6. S3 nhận archive qua Kafka Connect S3 Sink.
7. Grafana dashboard "F1 Live Telemetry" với các panel ở Phase 6.
8. JavaFX giữ vai trò admin tool.
9. Terraform cho S3 (bắt buộc); Terraform cho Confluent Cloud là optional — làm tay qua UI cũng chấp nhận được.
10. `.gitlab-ci.yml` chạy build + test khi push.
11. README phản ánh đúng kiến trúc + theme F1.

---

## PHASE 1 — Fetch data + Replay Engine

### Việc cần làm

1. Script fetch dữ liệu 1 lần (không gọi API lúc replay):
   ```bash
   # scripts/fetch_f1_session.sh
   #!/usr/bin/env bash
   set -euo pipefail
   SESSION_KEY="${1:?Cần truyền session_key, tra cứu qua /sessions endpoint}"
   DEST="data-generators/historical-replay/src/main/resources/datasets"
   mkdir -p "$DEST"
   curl -s "https://api.openf1.org/v1/car_data?session_key=${SESSION_KEY}" -o "$DEST/car_data.json"
   curl -s "https://api.openf1.org/v1/race_control?session_key=${SESSION_KEY}" -o "$DEST/race_control.json"
   ```
   Trước khi chạy, dùng `GET /sessions?year=2025&session_name=Race` để chọn 1 `session_key` cụ thể — ghi rõ vào `docs/DATASET_NOTES.md` đã chọn chặng đua nào, vì sao.
   Thêm `datasets/` vào `.gitignore`.

2. Module Maven mới `data-generators/historical-replay/` (thêm vào `<modules>` parent pom).

3. `ReplayEngine.java`:
   - Parse JSON (dùng Jackson) từ `car_data.json`, sort theo field `date`.
   - Mỗi record → 1 event Avro/JSON gồm `event_time`, `driver_number`, `speed`, `throttle`, `brake`, `rpm`, `n_gear`, `drs`.
   - Publish tuần tự vào topic `car-telemetry-events`, key = `driver_number`.
   - Song song, đọc `race_control.json`, publish vào topic riêng `race-control-events` theo đúng timestamp.
   - Tốc độ replay qua `REPLAY_SPEED_FACTOR` (1/5/20).

4. Giữ 3 generator random cũ trong `data-generators/legacy-random-generators/`, ghi rõ lý do giữ (lịch sử phát triển), không dùng mặc định.

### Acceptance criteria
- `mvn -q clean package -DskipTests` build thành công.
- Chạy thử, so vài dòng đầu `car_data.json` khớp đúng event xuất hiện trên Kafka.

---

## PHASE 2 — Confluent Cloud + Schema Registry

### Việc cần làm

1. Hỏi người dùng: `CONFLUENT_BOOTSTRAP_SERVERS`, `CONFLUENT_API_KEY/SECRET`, `SCHEMA_REGISTRY_URL` + credentials.

2. Xoá service `kafka`/`zookeeper` khỏi `docker-compose.yml`.

3. `schemas/car-telemetry-event-v1.avsc`:
   ```json
   {
     "type": "record",
     "name": "CarTelemetryEvent",
     "fields": [
       {"name": "event_time", "type": "long", "logicalType": "timestamp-millis"},
       {"name": "driver_number", "type": "int"},
       {"name": "speed", "type": "double"},
       {"name": "throttle", "type": ["null", "double"], "default": null},
       {"name": "brake", "type": ["null", "double"], "default": null},
       {"name": "rpm", "type": ["null", "int"], "default": null},
       {"name": "n_gear", "type": ["null", "int"], "default": null},
       {"name": "drs", "type": ["null", "int"], "default": null}
     ]
   }
   ```
   Đăng ký qua `scripts/register_schemas.sh`.

4. Sửa `ReplayEngine.java` dùng `KafkaAvroSerializer`.

5. Verify schema evolution **1 lần**: thêm field `tyre_compound` (optional, default null) vào `.avsc` v2, đăng ký, xác nhận Schema Registry chấp nhận (backward compatible) — ghi 1 dòng kết quả vào README, không cần file docs riêng.

### Acceptance criteria
- Message hiển thị đúng schema trên Confluent Cloud UI.
- v2 schema đăng ký thành công.

---

## PHASE 3 — Flink job + ClickHouse DDL (MỘT job duy nhất, DataStream API)

> DDL ClickHouse được đưa lên đầu phase này (không nằm ở Phase 5) để job chạy end-to-end ngay khi phase kết thúc.

### Việc cần làm

1. Đổi tên module `flink-jobs/fuel-flink-job` → `flink-jobs/f1-telemetry-job`.

2. **ClickHouse DDL** (thêm service `clickhouse` vào `docker-compose.yml`, tạo `infra/script/clickhouse/01_init_tables.sql`):
   ```sql
   CREATE TABLE IF NOT EXISTS raw_telemetry (
     event_time DateTime64(3), driver_number Int32, speed Float64,
     throttle Nullable(Float64), brake Nullable(Float64),
     rpm Nullable(Int32), n_gear Nullable(Int32), drs Nullable(Int32)
   ) ENGINE = MergeTree() ORDER BY (driver_number, event_time);

   CREATE TABLE IF NOT EXISTS rollup_10s (
     window_start DateTime64(3), driver_number Int32,
     avg_speed Float64, max_speed Float64, avg_throttle Float64
   ) ENGINE = MergeTree() ORDER BY (driver_number, window_start);

   CREATE TABLE IF NOT EXISTS race_control_events (
     event_time DateTime64(3), category String, flag String, message String
   ) ENGINE = MergeTree() ORDER BY event_time;
   ```

3. `F1TelemetryJob.java` (main class), các bước xử lý nối tiếp trong cùng 1 `StreamExecutionEnvironment`:

   a. **Nguồn + watermark**:
      ```java
      env.fromSource(kafkaSource, WatermarkStrategy
          .<CarTelemetryEvent>forBoundedOutOfOrderness(Duration.ofSeconds(3))
          .withTimestampAssigner((event, ts) -> event.getEventTime()),
          "kafka-source");
      ```

   b. **Windowed aggregate** (Tumbling 10 giây, DataStream API `.window()`, KHÔNG dùng Table/SQL API riêng):
      ```java
      telemetryStream
        .keyBy(e -> e.getDriverNumber())
        .window(TumblingEventTimeWindows.of(Time.seconds(10)))
        .aggregate(new SpeedRollupAggregator());
      ```

   c. **Broadcast state CDC**: đọc topic CDC (`f1-cdc.public.driver_alert_config`), cập nhật config runtime theo `driver_number` — connect vào stream chính qua `connect(broadcastStream)`. Pattern: `BroadcastProcessFunction` apply config (ví dụ flag enable/disable aggregate per-driver, hoặc sampling rate) ngay lập tức, không cần redeploy.

   d. **Checkpoint + Savepoint**: `env.enableCheckpointing(30000)`, `EXACTLY_ONCE`. Viết `scripts/flink_savepoint.sh` để trigger savepoint — verify hoạt động **1 lần** (kill job, restart từ checkpoint, xác nhận state không mất), không cần test lặp lại nhiều kịch bản.

4. Sink: 2 bảng ClickHouse (`raw_telemetry`, `rollup_10s`) qua Flink JDBC sink connector. Topic `race-control-events` ghi thẳng vào bảng ClickHouse `race_control_events` (pass-through, không qua Flink xử lý thêm).

### Acceptance criteria
- Job chạy end-to-end, 1 `main()` duy nhất.
- `SELECT count() FROM raw_telemetry` > 0 và `SELECT count() FROM rollup_10s` > 0 sau vài phút chạy.
- Checkpoint/savepoint verify 1 lần thành công.

---

## PHASE 4 — Postgres tối giản + Debezium CDC

### Việc cần làm

1. `infra/script/01_init_minimal.sql` (thay toàn bộ SQL cũ):
   ```sql
   CREATE TABLE IF NOT EXISTS driver_alert_config (
     driver_number INT PRIMARY KEY,
     is_active BOOLEAN DEFAULT TRUE,
     updated_at TIMESTAMP DEFAULT now()
   );
   -- Seed vài driver_number thật có trong session đã chọn ở Phase 1

   CREATE TABLE IF NOT EXISTS users (
     id SERIAL PRIMARY KEY,
     username VARCHAR(50) UNIQUE NOT NULL,
     password_hash VARCHAR(255) NOT NULL,
     role VARCHAR(20) DEFAULT 'admin'
   );
   ```
   Xoá SQL cũ (13 bảng, 17 view) — giữ 1 bản backup trong `docs/legacy/`.

2. Bật `wal_level=logical` trong `docker-compose.yml` service `postgres`.

3. Service `debezium-connect` + `infra/debezium/driver-alert-connector.json` trỏ bảng `driver_alert_config`.

4. `scripts/register_debezium_connector.sh`.

5. **Verify kết nối Connect→Confluent Cloud sớm** (chỉ cần bootstrap + SASL auth) — không để tới cuối phase mới phát hiện lỗi auth/network.

6. Sửa `backend-api`: giữ endpoint auth (bảng `users`), xoá endpoint đọc 13 bảng cũ, thêm endpoint đọc ClickHouse cho phần analytical.

### Acceptance criteria
- Update `is_active` qua `psql`, xác nhận CDC event xuất hiện trên Kafka trong vài giây.
- Flink job áp dụng config mới ngay, không cần restart.

---

## PHASE 5 — Kafka Connect S3 Sink + serving consolidation

> Đã gọt: Great Expectations (chỉ chạy 1 lần thủ công → giá trị chứng minh thấp, bỏ; ClickHouse type constraints là đủ). DDL ClickHouse đã dời lên Phase 3.

### Việc cần làm

1. Kafka Connect S3 Sink connector (`infra/connect/s3-sink-connector.json`) archive topic `car-telemetry-events` ra S3 (parquet/json).

2. Sửa `backend-api` dùng ClickHouse JDBC driver cho endpoint analytical (đọc `raw_telemetry`, `rollup_10s`).

### Acceptance criteria
- File xuất hiện trên S3 sau vài phút chạy (verify qua AWS CLI / S3 console).
- REST API trả dữ liệu đúng từ ClickHouse.

---

## PHASE 6 — Grafana: "F1 Live Telemetry" (engineering-first)

> Đã gọt: Canvas Track Map, Speed Anomaly Timeline overlay Safety Car (domain-heavy, không còn anomaly detection). Giữ lại các panel chứng minh pipeline streaming + engineering.

### Việc cần làm

1. Cài plugin `grafana-clickhouse-datasource`, trỏ vào ClickHouse.

2. Panel:

   - **Panel "Live speed gauge"**: gauge cho 2-3 xe được chọn.
   - **Panel "Rollup 10s — avg/max speed" (Time series)**: đọc bảng `rollup_10s`, theo trục thời gian.
   - **Panel "Top speed ticker" (Stat panel)**: `MAX(speed)` toàn session tính đến thời điểm hiện tại — tăng dần khi replay chạy, tạo hiệu ứng "kỷ lục mới" khi demo.
   - **Panel "Throttle/Brake/RPM" (Time series nhỏ)**: theo dõi 1 xe cụ thể.
   - **Panel "Race Control Events" (Table)**: đọc bảng `race_control_events`, sort theo thời gian — bảng sự kiện phụ.
   - **Panel "Active driver config (CDC)" (Table)**: đọc bảng `driver_alert_config` qua Postgres datasource — trực quan hoá engineering: thay đổi 1 dòng → thấy cập nhật qua CDC pipeline.

3. Import dashboard JSON vào `infra/grafana/dashboards/f1-live-telemetry.json`, provision qua `infra/grafana/provisioning/dashboards/dashboard.yml`.

### Acceptance criteria
- Dashboard load được, time series tăng dần khi replay chạy.
- Top speed ticker tăng dần theo thời gian.
- Thay đổi `is_active` trong Postgres → thấy cập nhật qua CDC (panel cuối) trong vài giây.

---

## PHASE 7 — Terraform + GitLab CI + README

### Việc cần làm

1. `terraform/s3.tf` provision bucket archive (bắt buộc).
2. Terraform cho Confluent Cloud: optional, chỉ làm nếu setup nhanh — nếu phức tạp, tạo tay qua UI cũng được, không block tiến độ.
3. `.gitlab-ci.yml`:
   ```yaml
   stages: [build, test]
   build:
     stage: build
     image: maven:3.9-eclipse-temurin-17
     script: [mvn -q clean package -DskipTests]
   test:
     stage: test
     image: maven:3.9-eclipse-temurin-17
     script:
       - mvn -pl desktop-admin test
       - mvn -pl backend-api test
   ```
4. README: đổi theme thành "F1 Live Telemetry Platform", diagram Mermaid mới, mô tả rõ vai trò JavaFX (admin) vs Grafana (visualization chính), ghi rõ session F1 đã chọn (circuit, năm) và lý do.

### Acceptance criteria
- Pipeline GitLab CI chạy xanh.
- README mô tả đúng 100% kiến trúc + theme mới.

---

## Phụ lục — Diff so với bản blueprint trước

Phần này ghi rõ đã gọt gì, để dễ truy nguyên và trả lời phỏng vấn "vì sao không có X".

| Hạng mục | Trước | Sau | Lý do |
|---|---|---|---|
| Anomaly detection (MapState rolling avg) | Có | Bỏ | Domain-heavy, side output không có mục đích engineering rõ nếu không validate ground-truth |
| Side output `speed_anomalies` | Có | Bỏ | Theo anomaly detection |
| Ground-truth validation (overlay Safety Car) | Có | Bỏ | Nghiệp vụ validate, không phải kỹ năng DE chuẩn cần chứng minh |
| Bảng `speed_alerts` (ClickHouse) | Có | Bỏ | Không còn anomaly |
| Canvas Track Map (Grafana) | Có | Bỏ | Effort cao, dễ block Phase 6, domain visualization |
| Speed Anomaly Timeline (Grafana) | Có | Bỏ | Theo anomaly detection |
| Threshold tuning per-driver | Có | Đổi | CDC broadcast state giữ, nhưng đổi mục đích sang config-driven routing (engineering pattern) |
| Great Expectations (1 lần thủ công) | Có | Bỏ | Giá trị chứng minh thấp, ClickHouse type constraints là đủ |
| Window aggregate 10s | Có | Giữ | Engineering: keyed stateful windowing |
| Broadcast state CDC | Có | Giữ | Engineering: pattern chính thức Flink |
| ClickHouse serving | Có | Giữ | Engineering: OLAP swap |
| Kafka Connect S3 Sink | Có | Giữ | Engineering: tận dụng Connect cluster |
| Checkpoint/Savepoint | Có | Giữ | Engineering: stateful processing |
| Schema Registry Avro + evolution | Có | Giữ | Engineering: contract messaging |
