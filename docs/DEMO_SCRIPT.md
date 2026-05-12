# 🎬 Demo Script — VES-Monitor (18 min)

> **Mục đích**: copy-paste runbook cho buổi bảo vệ. Mọi lệnh đều quote-safe Windows PowerShell hoặc WSL bash. Có **Backup commands** ở cuối — chạy ngay khi có sự cố live.
>
> **Thời lượng**: 18 min · cấu trúc `T-0 → T+18`. Bám timer, đừng để slide đè lên live demo.

---

## ⏱️ T-2 → T+0 min — Pre-flight (trước khi mở slide)

**Mục đích**: xác nhận toàn bộ stack live + 4 cửa sổ đã mở sẵn (slide / IDE Postman / JavaFX / browser tabs Flink + Swagger).

### Pre-flight checklist

- [ ] **Docker stack 5 container UP**
  ```powershell
  wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && docker compose -f infra/docker-compose.yml ps'
  ```
  Mong đợi: `zookeeper / kafka / postgres-database / flink-jobmanager / flink-taskmanager` đều `Up`.

- [ ] **4 generator JVM đang chạy trên WSL**
  ```powershell
  wsl -d Ubuntu-22.04 bash -lc 'pgrep -af "(fuel-price-producer|grid-load-generator|renewable-generator)"'
  ```
  Mong đợi: 3 PID (`fuel-price-producer`, `grid-load-generator`, `renewable-generator` — note `renewable-generator` sinh cả 2 topic `renewable-output` và `emission` trong 1 JVM).

- [ ] **Backend REST API up trên port 8090**
  ```powershell
  curl.exe -s --noproxy "*" http://localhost:8090/api/health
  ```
  Mong đợi: `{"status":"UP","db":"UP","timestamp":"..."}`.

- [ ] **Flink JobManager up, job RUNNING**
  ```powershell
  curl.exe -s --noproxy "*" http://localhost:8081/jobs | Select-String -Pattern "RUNNING"
  ```
  Mong đợi: 1+ job với status `RUNNING`.

- [ ] **JavaFX desktop đã mở** (open trước slide!)
  ```powershell
  # Nếu chưa mở:
  cd C:\Users\GOT4HC\Real-time-processing-with-Kafka-Flink-Postgres
  mvn -pl desktop-admin javafx:run
  ```
  Login `admin / admin` ngay khi cửa sổ hiện, chờ dashboard load xong (sparklines bắt đầu vẽ).

- [ ] **Postman / Insomnia** đã load `docs/VES-Monitor.postman_collection.json`, request `POST /api/auth/login` đã pre-fill body `{"username":"admin","password":"admin"}`.

- [ ] **Browser** với 3 tab pre-open:
  - `http://localhost:8081` (Flink UI)
  - `http://localhost:8090/swagger-ui.html` (Swagger)
  - GitHub repo tab (back-up)

- [ ] **Tile internet** OK cho Leaflet world map (slide 13 demo) — nếu offline thì world map sẽ show overlay "Map tiles unavailable offline" — không crash, vẫn demo được.

> 🚨 **Nếu bất kỳ check nào fail** → xem `Backup commands` cuối file. **Không** start demo cho đến khi 6/6 check pass.

---

## 🟢 T+0 → T+8 min — Slide-driven intro (8 min)

Theo [`docs/SLIDES_OUTLINE.md`](./SLIDES_OUTLINE.md) slide 1 → 9. Chuyển từ "bối cảnh / vấn đề" → "kiến trúc / pillar framework / ESI formula".

| Slide | Nội dung | Thời lượng |
|---|---|---:|
| 1 | Title + tên SV / GV / Repo tag v1.0.0 | 20 s |
| 2 | Bối cảnh VN — oil import 40 % / 500 kV bottleneck / curtailment / Net-Zero | 1 min |
| 3 | Vấn đề — reporting thủ công, missing real-time | 50 s |
| 4 | Mục tiêu — distributed real-time platform IEA/APERC | 40 s |
| 5 | Kiến trúc tổng quan — chỉ vào diagram 5 tier | 1 min 30 s |
| 6 | Tech stack — table 8 row | 50 s |
| 7 | 4-pillar framework | 1 min |
| 8 | 16 sub-indicators | 50 s |
| 9 | ESI formula + status thresholds | 1 min |

**Tổng**: ~ 8 min. Chuyển sang live demo bằng câu: *"Dưới đây là pipeline thực sự đang chạy live trong khi tôi nói…"* → bật JavaFX window.

---

## 🎥 T+8 → T+14 min — LIVE demo (6 min, wow moment)

### Step 1 (45 s) · JavaFX dashboard — chứng minh "real-time"

- 🖱️ Switch sang JavaFX window (đã pre-login admin).
- 👆 **Chỉ vào top bar**: `⚡ X events/sec · Last tick HH:MM:SS` → "đây là metric thực từ Flink JobManager REST, EMA-smoothed 3 s".
- 👆 **Chỉ vào sparkline** trong mỗi tab pillar → "FIFO 60-sample buffer, 3 min trailing window, refresh mỗi 3 s".
- 👆 **Chỉ vào Security Score gauge** (top bar) → "live composite ESI từ `v_security_score` view, weight IEA 0.30/0.20/0.30/0.20".

### Step 2 (60 s) · 5 tab pillar — chứng minh "IEA/APERC compliance"

Click qua từng tab:
1. **Pillar 1 · Supply Security** — bảng IDR / SFRI / HHI / N-1 per region.
2. **Pillar 2 · Market Resilience** — bảng σ_30d / price_gap / β_crude / affordability.
3. **Pillar 3 · Grid Reliability** — bảng reserve_margin / peak_factor / shedding_prob / freq_stability.
4. **Pillar 4 · Energy Transition** — bảng renewable_pct / co2_intensity / curtailment / netzero_progress.

> Speaker line: *"4 pillar đều có 4 sub-indicator chuẩn IEA/APERC. Tổng 16 chỉ số".*

### Step 3 (60 s) · Maps · Bản đồ — chứng minh "geospatial visibility"

- 🖱️ Click tab **"Maps · Bản đồ"**.
- 👁️ Sub-tab **"Vietnam · 3 zones"**:
  - Hover qua 3 vùng → tooltip hiện score.
  - 👆 Click vùng (ví dụ `VN_NORTH`) → dashboard tự filter Pillar 1/3/4 theo region đó.
- 👁️ Sub-tab **"World · Fuel hubs"**:
  - Leaflet world map với 7 hub marker (Houston, NY, London, Dubai, Singapore, Hải Phòng, Vũng Tàu).
  - Marker tint theo Pillar 2 price delta (xanh = giảm, đỏ = tăng).
  - 👆 Click 1 marker → popup hiện `fuel_type / price / delta` real-time.

> Nếu offline: speaker line *"Map tiles offline — fallback overlay liệt kê text hubs, không crash app".*

### Step 4 (75 s) · Postman — chứng minh "REST API + JWT"

- 🖱️ Switch sang Postman, mở folder `VES-Monitor`.
- ▶️ Run request **`POST /api/auth/login`** với body `{"username":"admin","password":"admin"}` →
  - Status `200 OK`
  - Response: `{ "accessToken": "eyJhbGc...", "tokenType": "Bearer", "expiresIn": 28800, "user": { ... role: "ADMIN" ... } }`
  - 👁️ Chỉ ra token tự copy vào collection variable `{{token}}` (Postman pre-request script).
- ▶️ Run request **`GET /api/security/score`** (header `Authorization: Bearer {{token}}`) →
  - Status `200 OK`
  - Response: `{ "overallScore": 72.82, "pillar1Score": 64.83, ..., "status": "ELEVATED" }`
- 💡 Speaker line: *"JavaFX dùng JDBC trực tiếp, REST này cho mobile Android + external integration".*

### Step 5 (90 s) · Trigger CRITICAL alert LIVE — chứng minh "alert pipeline"

> ⚠️ Step quan trọng nhất — câu chuyện "real-time alert" cần demo.

```powershell
# Bước 1: hạ threshold rule FUEL_PRICE CRITICAL xuống mức chắc chắn vi phạm
wsl -d Ubuntu-22.04 bash -lc 'docker exec -i postgres-database psql -U postgres -d fuel_prices -c "UPDATE alert_rules SET threshold = 50 WHERE metric_type = ''FUEL_PRICE'' AND severity = ''CRITICAL'';"'

# Bước 2: restart Flink job để load rule mới (static rule design)
# Hoặc: nếu đã setup hot-reload trong Phase X, skip step này.
# Demo path: dùng phase4 stress script sẽ trigger trực tiếp price > rule cũ (90) → CRITICAL
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && bash scripts/_phase4_stress_alerts.sh'
```

- ⏳ Wait **5-10 s** → quay về JavaFX window.
- 🔔 **Toast slide-in từ top-right** — "CRITICAL: FUEL_PRICE 105.5 > 100 USD/barrel (WTI_CRUDE NY)".
- 🟥 **Recommendation sidebar** — row mới với pulse drop-shadow đỏ (`HEDGE_IMPORT` action).
- 👆 **Chỉ vào sparkline Pillar 2** — dot mới nằm dưới (score drop từ 92 → 32 do volatility detected).

> Speaker line: *"Đây là vòng đời end-to-end: generator → Kafka → Flink AlertDetectionFunction → MapState cooldown → sink alerts table → SQL view → JavaFX toast — tất cả trong 5-10 s".*

### Step 6 (45 s) · Flink UI — chứng minh "scale + observability"

- 🖱️ Switch sang browser tab Flink UI (`http://localhost:8081`).
- 👁️ Job overview → click job name → **18/18 vertices RUNNING** (xanh hết).
- 👁️ Subtask metrics → `numRecordsInPerSecond` non-zero.
- 💡 Speaker line: *"Flink job có thể scale parallelism mà không đổi code — chỉ tăng partitions Kafka và task-manager slot".*

### Step 7 (30 s) · Postgres trực tiếp — chứng minh "data plane is honest"

```powershell
wsl -d Ubuntu-22.04 bash -lc 'docker exec -i postgres-database psql -U postgres -d fuel_prices -c "SELECT pillar1_score, pillar2_score, pillar3_score, pillar4_score, overall_score, status FROM v_security_score;"'
```

Output đúng số JavaFX gauge đang hiển thị → speaker line *"View tính khi đọc, không cache — số JavaFX, Postman, psql đều consistent".*

---

## 🟦 T+14 → T+17 min — Wrap (3 min, slides 10-15)

| Slide | Nội dung | Thời lượng |
|---|---|---:|
| 10 | Streaming pipeline highlights (Flink 18 vertex + MapState + dedup) | 40 s |
| 11 | DB schema — 13 tables + 17 views | 30 s |
| 12 | Backend REST 13 endpoints + JWT + Swagger | 30 s |
| 13 | JavaFX 5 tab + design patterns recap | 40 s |
| 14 | 10 design patterns | 30 s |
| 15 | Testing & quality — 77/77 PASS | 30 s |

> Speaker line khép lại: *"77 desktop test green, lint clean, 18 Flink vertex RUNNING, 13 endpoint smoke OK — đây là một dự án production-quality, không chỉ demo-quality".*

---

## 🎤 T+17 → T+18 min — Q&A intro

- Slide 17 (cuối): "Q&A — mời thầy/cô đặt câu hỏi".
- Nói rõ: *"Source đã tag `v1.0.0`, có thể pull về review chi tiết, document đầy đủ trong `docs/`".*

---

## 🆘 Backup commands (nếu sự cố live)

### Nếu backend chết

```powershell
# Kill cũ
$pid = Get-Content C:\Users\GOT4HC\Real-time-processing-with-Kafka-Flink-Postgres\build-logs\backend-run.pid -ErrorAction SilentlyContinue
if ($pid) { Stop-Process -Id $pid -Force -ErrorAction SilentlyContinue }

# Start lại
cd C:\Users\GOT4HC\Real-time-processing-with-Kafka-Flink-Postgres
$proc = Start-Process java -ArgumentList '-jar','backend-api\target\ves-backend-api.jar' -PassThru -RedirectStandardOutput 'build-logs\backend-run.log' -RedirectStandardError 'build-logs\backend-run.err.log' -WindowStyle Hidden
$proc.Id | Out-File -Encoding ascii build-logs\backend-run.pid

# Wait 10s rồi smoke
Start-Sleep -Seconds 10
curl.exe -s --noproxy "*" http://localhost:8090/api/health
```

### Nếu Flink job vỡ

```powershell
# Cancel + re-submit
wsl -d Ubuntu-22.04 bash -lc 'JOB_ID=$(curl -s http://localhost:8081/jobs | jq -r ".jobs[] | select(.status==\"RUNNING\") | .id") && curl -X PATCH "http://localhost:8081/jobs/$JOB_ID?mode=cancel" && sleep 5'

# Submit lại từ JAR pre-built
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && JAR_ID=$(curl -s -F "jarfile=@flink-jobs/fuel-flink-job/target/fuel-flink-job-1.1.1.jar" http://localhost:8081/jars/upload | jq -r ".filename" | xargs basename) && curl -X POST "http://localhost:8081/jars/$JAR_ID/run?entry-class=org.cloud.KafkaConsumerApplication"'
```

### Nếu generator chết

```powershell
# Restart cả 3 generator
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && bash scripts/_phase4_start_generators.sh'
```

### Nếu JavaFX freeze

- Press `Ctrl+C` trong terminal đã chạy `mvn javafx:run` → re-run.
- Nếu Maven không response → kill javafx process: `Get-Process | Where-Object { $_.ProcessName -like 'java*' } | Stop-Process -Force`.
- Re-login admin / admin.

### Nếu DB demo bị "bẩn" sau stress test

```sql
-- Trong psql:
TRUNCATE alerts CASCADE;
TRUNCATE recommendations CASCADE;
UPDATE alert_rules SET threshold = 100 WHERE metric_type = 'FUEL_PRICE' AND severity = 'CRITICAL';
UPDATE alert_rules SET threshold =  90 WHERE metric_type = 'FUEL_PRICE' AND severity = 'WARNING';
```

```powershell
wsl -d Ubuntu-22.04 bash -lc 'docker exec -i postgres-database psql -U postgres -d fuel_prices <<EOF
TRUNCATE alerts CASCADE;
TRUNCATE recommendations CASCADE;
UPDATE alert_rules SET threshold=100 WHERE metric_type=''FUEL_PRICE'' AND severity=''CRITICAL'';
UPDATE alert_rules SET threshold= 90 WHERE metric_type=''FUEL_PRICE'' AND severity=''WARNING'';
EOF'
```

### Nếu Docker stack vỡ hoàn toàn

```powershell
# Nuclear option (~2 min restart, MẤT data sau lần này)
wsl -d Ubuntu-22.04 bash -lc 'cd /mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres && bash scripts/stop.sh && bash scripts/run.sh --wait'
```

> ⚠️ Sau khi `stop.sh && run.sh`, phải resubmit Flink job (xem "Nếu Flink job vỡ") và restart generator (xem "Nếu generator chết").

---

## 🎓 Q&A prep — anticipated questions

| Q | A (≤ 30 s) |
|---|---|
| **Tại sao JavaFX không qua REST?** | Direct JDBC giảm latency ~ 300 ms · JavaFX là internal admin tool · REST có sẵn cho mobile/Android (sibling repo `mtoanng/DataStream`) và external integration. |
| **Tại sao Flink chứ không Spark Streaming?** | True streaming (event-by-event) vs Spark micro-batch · latency 60 ms p50 vs Spark vài giây · MapState (keyed state) cho cooldown sạch hơn Spark mapWithState. |
| **Schema nào cho time-series?** | Phase 1 dùng Postgres + partial index trên `event_timestamp DESC` — đủ cho ~ 10M row · production sẽ migrate **TimescaleDB** (hypertable, tự partition) hoặc **InfluxDB** cho retention >1 năm. |
| **Scale 1 triệu events/sec?** | Kafka partitions từ 3 → 12 (`bin/kafka-topics --alter --partitions 12`) · Flink parallelism từ 4 → 16 (`-p 16`) · Postgres read replica (Patroni / pgpool-II) · Redis cache cho view ESI. Không đổi code. |
| **Backend Phase 7.6 đang fix gì?** | Phase 7.1 IEA/APERC redesign đổi tên view (`v_pillar1_supply_outlook` → `v_pillar1_supply_security`); 4 endpoint pillar + 1 cascade-risk return 500 do `PillarDao` / `SecurityDao` query view name cũ. Sibling worker migrate DAO + DTO theo schema mới — đã có PR chờ merge. |
| **Tại sao Spring Boot 2.7 chứ không 3.x?** | SB 3.x require Java 17 · 3 generator + Flink job pin Java 11 · bump Java sẽ phải retest toàn module. SB 2.7.18 LTS, security patch tới 2025-08, đủ cho đồ án. |
| **JavaFX có chạy được trên Mac không?** | Có. `mvn -pl desktop-admin javafx:run` cross-platform · javafx-maven-plugin auto-detect OS (mac-aarch64, mac-x64, linux, windows). |
| **Sao chọn JWT chứ không session cookie?** | Stateless (không cần Redis session store) · mobile-friendly (Bearer token trong header) · 8h expiry refresh manual. |
| **Test coverage thực sự bao nhiêu %?** | 77 test phủ 100 % DAO (4/4) + 100 % service (5/5) + 100 % util (3/3); nếu đo Jacoco line coverage ước tính ~ 85 %. Backend test coverage thấp hơn (~ 10 %) đang được Phase 7.6 bổ sung. |
| **Recommendation tự động sinh thế nào?** | Khi alert CRITICAL trigger, Flink AlertDetectionFunction emit event → SQL `INSERT INTO recommendations ... WHERE NOT EXISTS (... suggested_at > NOW() - INTERVAL '30 minutes')` dedup → admin acknowledge/dismiss qua REST. |
| **Sao là 4 pillar mà không 3 hoặc 5?** | Theo IEA Energy Security Indicators 2014 + APERC APEC 100 % framework — 4A: **A**vailability, **A**ffordability, **A**ccessibility, **A**cceptability. Đây là taxonomy quốc tế chuẩn, không tự bịa. |

---

## 📋 Final checklist (ngay trước khi vào phòng)

- [ ] 2 USB backup chứa toàn bộ repo + database dump
- [ ] Slide deck mở sẵn ở slide 1 · full screen mode
- [ ] JavaFX window mở sẵn ở slide bên cạnh (alt-tab nhanh)
- [ ] Postman / Insomnia mở sẵn
- [ ] Browser 3 tab pre-open (Flink UI / Swagger / GitHub)
- [ ] Terminal PowerShell mở sẵn trong workspace root
- [ ] Terminal WSL Ubuntu-22.04 mở sẵn ở `/mnt/c/Users/GOT4HC/Real-time-processing-with-Kafka-Flink-Postgres`
- [ ] Microphone test (nếu present remote)
- [ ] Internet ổn định (cho Leaflet world map tiles)
- [ ] **Tag `v1.0.0`** đã push lên GitHub (verify: `git tag -l`)
