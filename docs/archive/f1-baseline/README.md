# F1 Live Telemetry Streaming Platform

Real-time streaming platform replaying authentic F1 telemetry data from the 2025 Abu Dhabi Grand Prix.

**Pipeline**: TracingInsights JSON → Replay Engine (Java) → Kafka KRaft (Avro) → Flink (DataStream API) → ClickHouse Cloud → Grafana Cloud

---

## Architecture

```
TracingInsights JSON (local, ~150MB)
    → Replay Engine (Java, Maven)
    → Kafka KRaft local (Avro + Schema Registry)
    → Flink in-process (1 job, window aggregate 10s)
    → ClickHouse Cloud (raw_telemetry + rollup_10s)
    → Grafana Cloud (4-panel dashboard)
```

**Local only** (RAM ~6.5GB): Kafka KRaft (768MB), Schema Registry (512MB), Flink JVM (~900MB).
**Cloud**: ClickHouse Cloud, Grafana Cloud.

---

## Setup Checklist

Prerequisites: Docker Desktop + JDK 11+ + Maven 3.8+ + Python 3.13

### 1. Start local infrastructure

```bash
docker compose -f infra/docker-compose.yml up -d
# Wait ~30s for KRaft controller election
```

Services: Kafka `localhost:9092`, Schema Registry `localhost:8081`.

### 2. Create Kafka topic + register Avro schema

```bash
bash scripts/create_topic.sh
bash scripts/register_schemas.sh
```

### 3. Download F1 dataset

```bash
bash scripts/fetch_f1_session.sh
# ~150MB, placed in data-generators/f1-replay-engine/src/main/resources/datasets/
```

### 4. ClickHouse Cloud — YOU NEED TO DO THIS

1. Sign up at https://clickhouse.cloud (free trial, $300 credits)
2. Create a service (any region)
3. Open SQL console → run `infra/clickhouse/01_tables.sql` (creates `f1_telemetry` database + tables)
4. Go to **Connect** → copy: host, port (8443), user, password, database name

### 5. Grafana Cloud — YOU NEED TO DO THIS

1. Sign up at https://grafana.com/cloud (free tier)
2. Install **ClickHouse** data source plugin
3. Add datasource pointing to your ClickHouse Cloud endpoint

### 6. Configure credentials

```bash
cp .env.example .env
# Edit .env with your ClickHouse Cloud credentials from step 4
```

### 7. Build + Run

```bash
# Build
mvn clean package -DskipTests

# Start replay engine (terminal 1)
bash scripts/replay.sh

# Start Flink job (terminal 2, after replay starts producing)
bash scripts/run_flink.sh
```

---

## Project Structure

```
data-generators/f1-replay-engine/     # Replay engine
    src/main/avro/car-telemetry-event.avsc
    src/main/java/com/f1telemetry/F1ReplayEngine.java

flink-jobs/f1-telemetry-job/         # Flink DataStream job
    src/main/avro/car-telemetry-event.avsc
    src/main/java/com/f1telemetry/F1TelemetryJob.java
    src/main/java/com/f1telemetry/SpeedRollupAggregator.java
    src/main/java/com/f1telemetry/model/TelemetryRollup.java
    src/main/java/com/f1telemetry/sink/ClickHouseSink.java

infra/docker-compose.yml             # Kafka KRaft + Schema Registry
infra/clickhouse/01_tables.sql       # ClickHouse Cloud DDL (run manually)

scripts/                             # Helper scripts
docs/PROJECT1_BLUEPRINT.md           # Architecture blueprint
docs/legacy/                         # Historical docs from v1.0.0
```

---

## Data Transformations

Beyond simple aggregation, the pipeline includes:

1. **Event-time reconstruction** (Replay Engine): Merge `session_laptimes.json` lap start timestamps (ISO) + per-lap telemetry time arrays (lap-relative seconds) → absolute epoch millis per record.

2. **Column-to-row transpose** (Replay Engine): TracingInsights stores telemetry as column-oriented arrays (`{"tel":{"time":[...],"speed":[...],...}}`) → transposed to per-record Avro.

3. **Conditional counting** (Flink `SpeedRollupAggregator`): `hardBrakeCount` = count of samples where `brake > 0` within 10s window. This is a stateful conditional aggregation, not just avg/max.

4. **Cross-driver global sort** (Replay Engine): All events sorted by `event_time` before Kafka publish, ensuring Flink watermark correctness (bounded out-of-order = 3s).

---

## ClickHouse Cloud Queries

```sql
-- Verify ingestion
SELECT count(), min(event_time), max(event_time), count(DISTINCT driver_number)
FROM f1_telemetry.raw_telemetry;

-- Top speed per driver
SELECT driver_number, max(speed) as max_speed
FROM f1_telemetry.raw_telemetry
GROUP BY driver_number ORDER BY max_speed DESC;

-- 10s rollup with braking stats
SELECT * FROM f1_telemetry.rollup_10s
ORDER BY window_start DESC LIMIT 50;
```

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| Kafka connection refused | Wait 30s: `docker logs kafka` |
| Schema Registry 404 | Kafka must be healthy first |
| Replay: "dataset not found" | Run `bash scripts/fetch_f1_session.sh` |
| Flink: ClickHouse connection failed | Verify `.env` credentials + ClickHouse Cloud is running |
| Docker OOM | Increase Docker Desktop memory to 6GB |

---

## Tech Stack

| Layer | Technology | Local/Cloud |
|---|---|---|
| Data Source | TracingInsights F1 dataset 2025 | Local (JSON files) |
| Replay Engine | Java 11 + Kafka Avro producer | Local (Maven) |
| Message Broker | Kafka KRaft 7.6.0 + Schema Registry | Local (Docker) |
| Stream Processor | Apache Flink 1.18.0 (DataStream API) | Local (in-process JVM) |
| OLAP Serving | ClickHouse Cloud | **Cloud** |
| Visualization | Grafana Cloud + ClickHouse datasource | **Cloud** |
