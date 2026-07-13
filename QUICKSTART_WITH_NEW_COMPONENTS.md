# Quick Start Guide - Enhanced F1 Telemetry Platform

This guide walks you through starting the enhanced platform with all 4 new components.

---

## 🚀 Prerequisites

- Docker Desktop running
- JDK 11+
- Maven 3.8+
- 8GB RAM minimum (recommended: 16GB)

---

## 📋 Step 1: Start Infrastructure (Docker)

```bash
cd c:\Users\ADMIN\STREAMING\Real-time-processing-with-Kafka-Flink-Postgres

# Start all Docker services
docker compose -f infra\docker-compose.yml up -d

# Wait ~30 seconds for Kafka KRaft controller election

# Verify all services are healthy
docker compose -f infra\docker-compose.yml ps
```

**Expected output:**
```
NAME               STATUS
kafka              healthy
schema-registry    healthy
kafka-ui           running
schema-registry-ui running
redis              healthy
```

---

## 📋 Step 2: Access Web UIs (Before Starting Pipeline)

Open in browser to verify connectivity:

1. **Kafka UI**: http://localhost:8080
   - Should show empty topics list (normal before first run)

2. **Schema Registry UI**: http://localhost:8000
   - Should show "No schemas" (normal before schema registration)

3. **Redis**: 
   ```bash
   redis-cli -p 6379 PING
   # Should return: PONG
   ```

---

## 📋 Step 3: Download F1 Dataset

```bash
# Run fetch script (downloads ~150MB from TracingInsights)
bash scripts/fetch_f1_session.sh

# Verify download
dir data-generators\f1-replay-engine\src\main\resources\datasets
# Should show: drivers.json, session_laptimes.json, VER/, HAM/, etc.
```

---

## 📋 Step 4: Create Kafka Topic + Register Schema

```bash
# Create topic
bash scripts/create_topic.sh

# Register Avro schema
bash scripts/register_schemas.sh
```

**Now refresh web UIs:**
- Kafka UI should show: `car-telemetry-events` topic
- Schema Registry UI should show: `car-telemetry-events-value` schema

---

## 📋 Step 5: Configure ClickHouse Cloud

**Manual steps (one-time setup):**

1. Sign up: https://clickhouse.cloud (free trial, $300 credits)
2. Create a service (any region)
3. Open SQL console → run `infra/clickhouse/01_tables.sql`
4. Go to **Connect** → copy credentials:
   - Host (e.g., `xxx.aws.clickhouse.cloud`)
   - Port: `8443`
   - User: `default`
   - Password: `your-password`

5. Create `.env` from template:
   ```bash
   copy .env.example .env
   # Edit .env with your ClickHouse credentials
   ```

---

## 📋 Step 6: Build Java Projects

```bash
# Build both modules (Replay Engine + Flink Job)
mvn clean package -DskipTests

# Verify JARs exist
dir data-generators\f1-replay-engine\target\*.jar
dir flink-jobs\f1-telemetry-job\target\*.jar
```

---

## 📋 Step 7: Start Replay Engine (Terminal 1)

```bash
# Start publishing events to Kafka
bash scripts/replay.sh

# Expected output:
# Replay Engine init | Kafka=localhost:9092 SR=http://localhost:8081
# Drivers: {VER=1, HAM=44, LEC=16, ...}
# Lap start times for 20 drivers
# Total events loaded: ~92000
# Events sorted by event_time
# Publishing... (this will take ~15 minutes at 5x speed)
```

**Monitor in Kafka UI:**
- Go to http://localhost:8080
- Topics → `car-telemetry-events` → Messages
- Should see Avro-decoded messages streaming in

---

## 📋 Step 8: Start Flink Job (Terminal 2)

```bash
# Wait ~30 seconds after replay starts, then:
mvn -pl flink-jobs/f1-telemetry-job exec:java

# Expected output:
# === F1 Telemetry Flink Job ===
# Kafka=localhost:9092 | SR=http://localhost:8081 | topic=car-telemetry-events
# ClickHouse: xxx.aws.clickhouse.cloud:8443 (user=default)
# Redis: localhost:6379
# Flink Web UI: http://localhost:8082
# Job submitted...
```

**Access Flink Web UI:**
- Go to http://localhost:8082
- Should see job graph with 3 sink operators:
  1. `clickhouse-raw-sink`
  2. `clickhouse-rollup-sink`
  3. `redis-leaderboard-sink`

---

## 📋 Step 9: Monitor Real-Time Data

### **A. Kafka UI (http://localhost:8080)**
```
Topics → car-telemetry-events
- Partitions: 3
- Messages: increasing rapidly
- Click "Messages" tab → see decoded Avro records
```

### **B. Flink Web UI (http://localhost:8082)**
```
Overview → Running Jobs → F1 Live Telemetry Pipeline
- Click job → see live metrics:
  - Records Sent: increasing
  - Checkpoints: every 30s
  - Watermarks: progressing
```

### **C. Redis Leaderboard**
```bash
redis-cli -p 6379

# Query top 10 fastest drivers
127.0.0.1:6379> ZREVRANGE f1:leaderboard:speed 0 9 WITHSCORES

# Example output:
1) "1"       # Driver 1 (Verstappen)
2) "351.45"  # 351.45 km/h
3) "44"      # Driver 44 (Hamilton)
4) "349.82"
5) "16"      # Driver 16 (Leclerc)
6) "348.91"
...

# Get specific driver's max speed
127.0.0.1:6379> ZSCORE f1:leaderboard:speed 1
"351.45"

# Get driver's rank (0 = 1st place)
127.0.0.1:6379> ZREVRANK f1:leaderboard:speed 1
(integer) 0
```

### **D. ClickHouse (SQL Console)**
```sql
-- Verify raw telemetry ingestion
SELECT count(*) as total_events,
       min(event_time) as first_event,
       max(event_time) as last_event,
       count(DISTINCT driver_number) as num_drivers
FROM f1_telemetry.raw_telemetry;

-- Top speeds per driver
SELECT driver_number, 
       max(speed) as max_speed_kmh,
       avg(speed) as avg_speed_kmh
FROM f1_telemetry.raw_telemetry
GROUP BY driver_number
ORDER BY max_speed_kmh DESC
LIMIT 10;

-- 10-second rollup stats
SELECT window_start, 
       driver_number,
       avg_speed,
       max_speed,
       hard_brake_count
FROM f1_telemetry.rollup_10s
ORDER BY window_start DESC
LIMIT 20;
```

---

## 📋 Step 10: Setup Grafana Cloud (Optional)

1. Sign up: https://grafana.com/cloud (free tier)
2. Install ClickHouse datasource plugin
3. Add datasource pointing to your ClickHouse Cloud
4. Import dashboard from `infra/grafana/dashboards/f1-live-telemetry.json`

---

## 🎯 Success Criteria

After 5 minutes of running, you should see:

✅ **Kafka UI**: ~15k+ messages in `car-telemetry-events` topic
✅ **Flink Web UI**: Job running, 3 operators processing, checkpoints succeeding
✅ **Redis**: 20 drivers in leaderboard with realistic speeds (300-350 km/h)
✅ **ClickHouse raw_telemetry**: ~15k+ rows
✅ **ClickHouse rollup_10s**: ~300+ rollup windows
✅ **Flink logs**: No errors, "Flushed N records" messages

---

## 🛑 Stopping

```bash
# Stop Flink job: Ctrl+C in terminal 2
# Stop Replay Engine: Ctrl+C in terminal 1

# Stop Docker services
docker compose -f infra\docker-compose.yml down

# Keep volumes (data persists)
# Or remove everything:
docker compose -f infra\docker-compose.yml down -v
```

---

## 🔧 Troubleshooting

### Kafka UI shows "Connection refused"
```bash
docker logs kafka
# Wait for "Kafka Server started" message
```

### Flink job: ClassNotFoundException: Redis
```bash
# Rebuild with dependencies
mvn clean package -DskipTests
```

### Redis leaderboard empty after 1 minute
```bash
# Check Flink logs for Redis errors
docker logs redis

# Test Redis connection
redis-cli -p 6379 PING
```

### ClickHouse connection failed
```bash
# Verify credentials in .env
# Test connection:
clickhouse-client --host=YOUR_HOST --port=9440 --secure \
  --user=default --password=YOUR_PASSWORD --query="SELECT 1"
```

---

## 📊 Ports Summary

| Service | Port | URL |
|---------|------|-----|
| Kafka | 9092 | - |
| Schema Registry | 8081 | http://localhost:8081 |
| **Kafka UI** | **8080** | **http://localhost:8080** |
| **Schema Registry UI** | **8000** | **http://localhost:8000** |
| **Flink Web UI** | **8082** | **http://localhost:8082** |
| **Redis** | **6379** | `redis-cli -p 6379` |
| ClickHouse Cloud | 8443 | (external) |

---

## 🎓 Learning Resources

- **Kafka UI docs**: https://docs.kafka-ui.provectus.io
- **Flink Web UI**: https://nightlies.apache.org/flink/flink-docs-release-1.18/docs/ops/monitoring/
- **Redis commands**: https://redis.io/commands/?group=sorted-set

---

**Next Steps**: See `docs/COMPONENTS_ADDED.md` for architecture details and testing queries.
