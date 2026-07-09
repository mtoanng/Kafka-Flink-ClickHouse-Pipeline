# 🛣️ DE Upgrade Roadmap — VES-Monitor v2

> Lộ trình nâng cấp từ đồ án học phần → portfolio Data Engineering mức Middle.
> Toàn bộ CI/CD, security, planning chạy trên **GitLab Ultimate** (thay GitHub Actions/Student Pack).

## Kiến trúc mục tiêu

```
[Replay dữ liệu lịch sử thật] ──┐
[Debezium CDC: customer_master (Postgres)] ──┤
                                              ▼
                          Kafka (Confluent Cloud free tier)
                          + Schema Registry (Avro)
                                              │
                 ┌────────────────────────────┼────────────────────────────┐
                 ▼                                                         ▼
     Flink (Java, giữ nguyên)                          Spark Structured Streaming (Scala, mới)
     → alert detection, MapState cooldown               → windowed rollup/aggregate (5-min avg/region)
     chạy local/container nhẹ                            chạy trên Databricks Community Edition
                 │                                                         │
                 ▼                                                         ▼
        PostgreSQL (serving, OLTP)                          ClickHouse (OLAP real-time)
        + pgvector extension
                 │                                                         │
                 └──────────────┬──────────────────────────────────────────┘
                                ▼
                  Great Expectations (data quality trước khi ghi sink)
                                ▼
                  JavaFX desktop / REST API (giữ nguyên)
```

## Phases

### Phase 0 — Nền tảng & sửa nợ kỹ thuật ✅ (đang triển khai)
- [x] Migrate repo sang GitLab (`nguyendungmanhtoan-group/STREAMING`)
- [x] `.gitlab-ci.yml`: build + test (JUnit report trong MR) + **SAST + Secret Detection + Dependency Scanning** (Ultimate)
- [x] `Makefile` (`make up / demo / down`) + submit Flink job qua **REST API** thay upload tay qua UI
- [x] Tách README: học phần vs. DE roadmap
- [ ] Bật **Merge Request Approvals + Security Policies** (Ultimate) trên GitLab

### Phase 1 — Data source thật
- [ ] Module `data-generators/replay-engine`: đọc CSV lịch sử (giá xăng dầu / điện lưới VN), publish Kafka đúng thứ tự timestamp gốc, tốc độ scale được (`--speed 60x`)
- [ ] Module `anomaly-injector` tách biệt: chèn spike có kiểm soát để demo alert, topic/flag riêng — không trộn với dữ liệu thật

### Phase 2 — Schema Registry (Avro)
- [ ] Đăng ký Avro schema cho 4 topic, thay JSON tự do
- [ ] Test schema evolution (thêm field, backward compatibility) chạy trong CI

### Phase 3 — Debezium CDC
- [ ] Bảng `customer_master` / `region_config` bật CDC qua Debezium connector → topic riêng
- [ ] Flink job join enrich (cập nhật ngưỡng cảnh báo theo vùng không cần redeploy)

### Phase 4 — Spark Structured Streaming (Scala) + ClickHouse
- [ ] Spark job (Databricks Community Edition): windowed aggregation 5 phút theo vùng → ClickHouse
- [ ] Trade-off: Flink = low-latency stateful alert (CEP-style) · Spark = micro-batch rollup OLAP

### Phase 5 — Data quality + pgvector
- [ ] Great Expectations/Soda: check null/range trên output Flink/Spark trước khi ghi sink, log vào Grafana/Loki
- [ ] Bật `pgvector` extension trên Postgres hiện có

### Phase 6 — Cloud free tier + IaC
- [ ] Kafka → Confluent Cloud free tier (~$400 credit)
- [ ] Terraform dựng/`destroy` stack mỗi phiên demo (5–10 phút) — pipeline **IaC SAST** (Ultimate) scan Terraform
- [ ] **Container Scanning** (Ultimate) khi build image Flink/generators

## Tận dụng GitLab Ultimate

| Tính năng | Dùng cho |
|---|---|
| SAST / Secret Detection / Dependency Scanning | Mỗi push + MR widget, Security Dashboard |
| License Compliance | Kiểm soát license dependency Maven |
| IaC Scanning | Terraform (Phase 6) |
| Container Scanning | Docker images (Phase 6) |
| Merge Request Approvals + Scan Result Policies | Chặn merge khi có vulnerability Critical |
| Epics / Issues / Boards | Quản lý roadmap theo phase |
| Value Stream Analytics / DORA | Metrics cho portfolio |
| GitLab Duo | Code review, chat, root cause analysis pipeline |
