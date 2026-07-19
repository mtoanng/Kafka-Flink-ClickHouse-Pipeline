# Quick Start - F1 Telemetry Pipeline (Official Connector)

## TL;DR - Run This Now

```powershell
# 1. Create ClickHouse tables (in ClickHouse Cloud console)
# Run: infra/clickhouse/schema.sql

# 2. Start Docker infrastructure
docker-compose -f infra/docker-compose.yml up -d

# 3. Build & run Flink job (from project root)
.\scripts\build-and-run.ps1

# 4. In another terminal: Start F1 replay engine
cd data-generators\f1-replay-engine
mvn exec:java
```

Then check ClickHouse:
```sql
SELECT count(*) FROM f1_telemetry.raw_telemetry;
```

---

## What Changed - Official Connector Implementation

### ✅ Now Using (Correct Approach)
- **Official `flink-connector-clickhouse-1.17:0.9.5`** from ClickHouse team
- **RowBinaryWithDefaults format** (binary, typed, fast)
- **POJOConvertor** for automatic field mapping
- **Built-in metrics, retry, compression**
- **Flink 1.20.2** (upgraded from 1.18.0)

### ❌ Removed (Custom Implementation)
- Custom sink with manual HTTP calls
- TabSeparated string formatting
- Manual batching/flushing logic
- clickhouse-client dependency

---

## Key Files Changed

1. **`pom.xml`** - New dependencies
   - `com.clickhouse.flink:flink-connector-clickhouse-1.17:0.9.5`
   - Flink 1.20.2
   
2. **`ClickHouseSinkFactory.java`** - Thin wrapper (150 lines)
   - Replaces 300+ lines custom sink
   
3. **`F1TelemetryPipeline.java`** - Clean pipeline
   - Proper window aggregation
   - Environment variable config

4. **`infra/clickhouse/schema.sql`** - Production schema
   - Optimized ORDER BY
   - No Nullable columns
   - Proper numeric types

---

## Prerequisites Check

### 1. ClickHouse Tables Created?
```sql
-- Run in ClickHouse Cloud console:
-- infra/clickhouse/schema.sql

SHOW TABLES FROM f1_telemetry;
-- Should show: raw_telemetry, rollup_10s
```

### 2. Docker Running?
```powershell
docker ps | findstr kafka
```
Should show: `kafka`, `schema-registry`

### 3. Maven Installed?
```powershell
mvn -version
```
Should show Maven 3.x

### 4. Environment Variables Set?
Check `.env` file has:
```bash
CLICKHOUSE_HOST=your-host.clickhouse.cloud
CLICKHOUSE_PORT=8443
CLICKHOUSE_USER=default
CLICKHOUSE_PASSWORD=<your-password>
CLICKHOUSE_DATABASE=f1_telemetry
```

---

## Expected Output

### Flink Job Logs
```
INFO  F1TelemetryPipeline - === F1 Telemetry Pipeline ===
INFO  F1TelemetryPipeline - Kafka: localhost:9092
INFO  F1TelemetryPipeline - ClickHouse: your-host:8443/f1_telemetry
INFO  ClickHouseAsyncSink - Writing batch of 20000 records
INFO  ClickHouseAsyncSink - Batch write completed in 245ms
```

### ClickHouse Query Results
```sql
-- After 10-20 seconds, should see data
SELECT count(*) FROM f1_telemetry.raw_telemetry;
-- Result: 20000+ rows

SELECT driver_number, count(*) as cnt
FROM f1_telemetry.raw_telemetry
GROUP BY driver_number
ORDER BY driver_number;
-- Result: Multiple drivers with data

-- Check rollup aggregates
SELECT * FROM f1_telemetry.rollup_10s 
ORDER BY window_start DESC 
LIMIT 10;
```

---

## Troubleshooting

### No data in ClickHouse after 2 minutes?

**Check 1:** Is Flink job running without errors?
```powershell
# Look for exceptions in console output
# Should see "Batch write completed" messages
```

**Check 2:** Is Kafka receiving data?
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic car-telemetry-events \
  --from-beginning \
  --max-messages 5
```

**Check 3:** Is replay engine running?
```powershell
# Should see: "Publishing telemetry for driver X..."
```

**Check 4:** ClickHouse table structure correct?
```sql
DESCRIBE TABLE f1_telemetry.raw_telemetry;
-- Field names must match Java POJO fields (case-sensitive)
```

**Check 5:** ClickHouse logs
```sql
SELECT * FROM system.query_log 
WHERE database = 'f1_telemetry' 
ORDER BY event_time DESC 
LIMIT 10;
```

---

## Pipeline Architecture

```
┌─────────────────┐
│ F1 Replay       │  (Java, reads JSON datasets)
│ Engine          │
└────────┬────────┘
         │ Avro events
         ↓
┌─────────────────┐
│ Kafka KRaft     │  (topic: car-telemetry-events)
│ + Schema Reg    │
└────────┬────────┘
         │ KafkaSource
         ↓
┌─────────────────┐
│ Flink 1.20.2    │  (watermark 5s, window 10s)
│ DataStream      │
└────┬────────┬───┘
     │        │
     │        └──────────────────┐
     ↓                           ↓
┌────────────────┐    ┌──────────────────┐
│ Official       │    │ Official         │
│ ClickHouse     │    │ ClickHouse       │
│ AsyncSink      │    │ AsyncSink        │
│                │    │                  │
│ raw_telemetry  │    │ rollup_10s       │
│ (batch 20K)    │    │ (batch 1K)       │
└────────────────┘    └──────────────────┘
     ClickHouse Cloud
     (RowBinaryWithDefaults format)
```

---

## Configuration Highlights

### Raw Telemetry Sink
- **Batch size:** 20,000 rows (ClickHouse best practice: 10K-100K)
- **Max in-flight:** 3 concurrent INSERTs (limits parts/sec)
- **Flush interval:** 10 seconds max
- **Format:** RowBinaryWithDefaults (binary, typed, fast)
- **Deduplication:** `insert_deduplicate=1` (safe retries)

### Rollup Sink  
- **Batch size:** 1,000 rows (lower volume, pre-aggregated)
- **Max in-flight:** 2 concurrent INSERTs
- **Flush interval:** 10 seconds max

---

## Next Steps After Success

1. ✅ **Verify Grafana dashboard works**
   - Open: http://localhost:3000
   - Dashboard: "F1 Live Telemetry Cloud"
   - Should show real-time driver data

2. ✅ **Check Flink metrics**
   - Open: http://localhost:8081
   - Job → Metrics
   - Should see: numRecordsSend, writeLatency, etc.

3. ✅ **Monitor pipeline health**
   ```sql
   -- Data freshness
   SELECT max(event_time) as latest_event
   FROM f1_telemetry.raw_telemetry;
   
   -- Records per minute
   SELECT 
       toStartOfMinute(event_time) as minute,
       count(*) as records
   FROM f1_telemetry.raw_telemetry
   WHERE event_time >= now() - INTERVAL 10 MINUTE
   GROUP BY minute
   ORDER BY minute DESC;
   ```

---

## Performance Tuning (Optional)

### Increase Parallelism
Edit `F1TelemetryPipeline.java`:
```java
// From: env.setParallelism(1);
// To:   env.setParallelism(4);

// Also adjust:
RAW_MAX_BUFFERED = 400_000  // = parallelism * 100_000
```

### Enable Compression (after verifying it works)
Edit `ClickHouseSinkFactory.java`:
```java
Map<String, String> clientOptions = Map.of(
    "compress", "1"  // LZ4 compression
);
```

### Larger Batches (for very high volume)
```java
RAW_MAX_BATCH_SIZE = 50_000  // from 20_000
```

---

## Documentation

- **Complete technical docs:** `OFFICIAL_CONNECTOR_IMPLEMENTATION.md`
- **Infrastructure setup:** `INFRASTRUCTURE_SETUP_GUIDE.md`
- **ClickHouse schema:** `infra/clickhouse/schema.sql`
- **Setup checklist:** `SETUP_CHECKLIST.md`

---

**Last Updated:** 2026-07-18  
**Status:** Production-ready with official connector  
**Estimated time to first data:** 10-20 seconds after starting all components
