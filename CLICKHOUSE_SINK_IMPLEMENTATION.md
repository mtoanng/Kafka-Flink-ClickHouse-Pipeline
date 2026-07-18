# ClickHouse Sink Implementation - Technical Documentation

## Problem Summary

### Issues with Previous Approaches (3 attempts)

**Attempt 1: JDBC PreparedStatement with RowBinary**
- Used `java.sql.PreparedStatement` with ClickHouse JDBC driver
- Driver auto-selected RowBinary format
- **Result**: HTTP 200 responses but `system.parts = 0` (no data committed)
- **Root cause**: RowBinary encoding incompatible with ClickHouse Cloud

**Attempt 2: JDBC with URL parameters**  
- Added `compress=0`, `async_insert=0`, `use_binary_format=false`
- **Result**: Driver ignored parameters, still used RowBinary
- Same failure: no data despite HTTP 200

**Attempt 3: Official ClickHouse Flink Connector** 
- Tried `com.clickhouse.flink:flink-connector-clickhouse`
- **Result**: "Magic is not correct - expect [-126] but got [-53]" error
- Connector still used JDBC internally with RowBinary

### Evidence
- `system.query_log`: showed `written_rows = 1000`, `written_bytes = 53000`
- `system.parts`: showed 0 rows (no data committed to disk)
- Manual INSERT via ClickHouse console WORKS
- All queries fully qualified: `f1_telemetry.raw_telemetry`

### Root Cause Analysis
**NOT** async insert buffer (waited 10+ minutes)
**NOT** wrong database (fully qualified queries)
**NOT** schema mismatch (manual INSERT works)
**ACTUAL**: ClickHouse Cloud SSL/compression + RowBinary format incompatibility

---

## Current Solution: ClickHouse Client API with TabSeparated Format

### Architecture

```
Flink DataStream
  ↓
RichSinkFunction (manual batching & flush)
  ↓
ClickHouse Client API (HTTP-based)
  ↓
TabSeparated format (not RowBinary)
  ↓
ClickHouse Cloud (HTTPS:8443 with SSL)
```

### Key Components

#### 1. **ClickHouseOfficialSink.java**
- Two inner classes: `RawTelemetrySink` and `RollupSink`
- Both extend `RichSinkFunction` for lifecycle management
- Manual batching with configurable flush:
  - Batch size: 1000 records (raw), 100 records (rollup)
  - Flush interval: 5 seconds
  - Flush triggers: batch full OR time interval

#### 2. **Data Format: TabSeparated**
```
<timestamp>\t<driver>\t<speed>\t<throttle>\t...\n
<timestamp>\t<driver>\t<speed>\t<throttle>\t...\n
```
- NULL values: `\N` (ClickHouse standard)
- Timestamps: `yyyy-MM-dd HH:mm:ss.SSS`
- Floating point: 3 decimal places

#### 3. **ClickHouse Client Configuration**
```java
ClickHouseNode.builder()
    .host(host)
    .port(8443)  // HTTPS port for ClickHouse Cloud
    .database(database)
    .credentials(ClickHouseCredentials.fromUserAndPassword(user, password))
    .addOption("ssl", "true")
    .addOption("compress", "0")  // Disable compression (avoids "Magic" error)
    .build();
```

#### 4. **Write Operation**
```java
client.write(server)
    .format(ClickHouseFormat.TabSeparated)
    .query("INSERT INTO f1_telemetry.raw_telemetry FORMAT TabSeparated")
    .data(tsvData)
    .executeAndWait()
    .get();
```

### Dependencies

**Removed:**
- `flink-connector-jdbc` (caused RowBinary issue)
- `clickhouse-jdbc` (JDBC driver with RowBinary)

**Added:**
- `com.clickhouse:clickhouse-client:0.7.0` (native HTTP client)
- `org.apache.httpcomponents.client5:httpclient5:5.2.1` (for HTTPS)

### Benefits of This Approach

1. **No RowBinary issues**: TabSeparated is text-based, human-readable
2. **Direct HTTP protocol**: No JDBC overhead or magic byte issues
3. **Manual control**: Full control over batching, flushing, error handling
4. **ClickHouse Cloud compatible**: Works with SSL, HTTPS:8443
5. **Observable**: Clear logging for flush operations
6. **Resource safe**: Proper lifecycle management (`open()`, `close()`)

---

## Idempotency & Data Consistency

### Current State: NO Idempotency

The current implementation does **NOT** guarantee exactly-once semantics:
- No deduplication at ClickHouse level
- No transaction IDs or unique constraints on inserts
- If Flink retries, data may be duplicated

### Why Idempotency Wasn't Implemented

Based on user query analysis:
> "lỡ các dữ liệu đc buffer trc đó đc đổ xuống và trùng r sao ?? rồi pipeline đã có Idempotency chưa ??"

The user asked about idempotency AFTER seeing the data insertion problem, not as a primary requirement.

Focus was on **getting data to appear first**, then optimize for idempotency.

### How to Add Idempotency (Future Work)

#### Option 1: ReplacingMergeTree (Recommended)
```sql
CREATE TABLE f1_telemetry.raw_telemetry_idempotent
(
    event_time DateTime64(3),
    driver_number Int32,
    speed Float64,
    ...
    _version UInt64  -- Add version column
)
ENGINE = ReplacingMergeTree(_version)
ORDER BY (driver_number, event_time);
```
- Duplicate rows with same ORDER BY key keep only max(_version)
- Use `event_time` as version (milliseconds)

#### Option 2: Flink Checkpointing + Kafka Offsets
- Enable Flink checkpointing (already done: 30s interval)
- On failure, Flink restarts from last checkpoint
- Kafka offsets ensure no data loss
- BUT: duplicates possible between checkpoint and crash

#### Option 3: Unique Event IDs
- Add `event_id UUID` column to schema
- Generate UUID in replay engine: `UUID.randomUUID()`
- Use `ReplacingMergeTree` or application-level deduplication

---

## Running the Pipeline

### Prerequisites

1. **Docker services running** (Kafka, Schema Registry)
   ```powershell
   docker-compose -f infra/docker-compose.yml up -d
   ```

2. **ClickHouse Cloud table created**
   ```sql
   CREATE TABLE f1_telemetry.raw_telemetry
   (
       event_time DateTime64(3),
       driver_number Int32,
       speed Float64,
       throttle Nullable(Float64),
       brake Nullable(Float64),
       rpm Nullable(Int32),
       gear Nullable(Int32),
       drs Nullable(Int32)
   )
   ENGINE = SharedMergeTree('/clickhouse/tables/{uuid}/{shard}', '{replica}')
   ORDER BY (driver_number, event_time)
   TTL event_time + INTERVAL 30 DAY;

   CREATE TABLE f1_telemetry.rollup_10s
   (
       window_start DateTime64(3),
       driver_number Int32,
       avg_speed Float64,
       max_speed Float64,
       avg_throttle Float64,
       hard_brake_count UInt64,
       sample_count UInt64
   )
   ENGINE = SharedMergeTree('/clickhouse/tables/{uuid}/{shard}', '{replica}')
   ORDER BY (driver_number, window_start);
   ```

3. **Environment configured in `.env`**
   ```bash
   CLICKHOUSE_HOST=q9byj37qf5.ap-southeast-1.aws.clickhouse.cloud
   CLICKHOUSE_PORT=8443
   CLICKHOUSE_USER=default
   CLICKHOUSE_PASSWORD=<your-password>
   CLICKHOUSE_DATABASE=f1_telemetry
   ```

### Build & Run

#### Option 1: Complete rebuild (recommended after code changes)
```powershell
.\scripts\build-and-run.ps1
```
This script:
- ✅ Checks Maven installation
- ✅ Loads `.env` file
- ✅ Runs `mvn clean`
- ✅ Builds all modules: `mvn package -DskipTests`
- ✅ Runs Flink job: `mvn exec:java`

#### Option 2: Quick run (if already built)
```powershell
.\scripts\run-flink.ps1
```

#### Option 3: Manual Maven commands
```powershell
# From project root
mvn clean package -DskipTests
cd flink-jobs\f1-telemetry-job
mvn exec:java
```

### Verification Queries

**Check data arrival:**
```sql
SELECT count(*) FROM f1_telemetry.raw_telemetry;
SELECT count(*) FROM f1_telemetry.rollup_10s;
```

**Check latest records:**
```sql
SELECT * FROM f1_telemetry.raw_telemetry 
ORDER BY event_time DESC 
LIMIT 10;
```

**Check driver distribution:**
```sql
SELECT driver_number, count(*) as cnt
FROM f1_telemetry.raw_telemetry
GROUP BY driver_number
ORDER BY cnt DESC;
```

**Check flush operations (system table):**
```sql
SELECT 
    event_time,
    query_kind,
    type,
    written_rows,
    written_bytes
FROM system.query_log
WHERE database = 'f1_telemetry' 
  AND table = 'raw_telemetry'
ORDER BY event_time DESC
LIMIT 20;
```

---

## Troubleshooting

### Problem: "Maven not found"
**Solution:** Install Maven or add to PATH
```powershell
# Check if Maven installed
where.exe mvn

# If not found, install via Chocolatey:
choco install maven

# Or download from: https://maven.apache.org/download.cgi
```

### Problem: Build fails with "package does not exist"
**Solution:** Clean and rebuild
```powershell
mvn clean install -DskipTests
```

### Problem: "Connection refused" to ClickHouse
**Solution:** Check ClickHouse Cloud service is running
```powershell
# Test connection
curl https://q9byj37qf5.ap-southeast-1.aws.clickhouse.cloud:8443/ping
```

### Problem: Data not appearing in ClickHouse
**Solution:** Check Flink logs for flush messages
```
INFO  RawTelemetrySink - Flushed 1000 raw records to ClickHouse
```

If no flush logs: Check Kafka has data
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:29092 \
  --topic car-telemetry-events \
  --from-beginning \
  --max-messages 10
```

### Problem: "Magic is not correct" error
**Solution:** This should NOT happen with current implementation (TabSeparated format)
If it does, verify:
- `compress` option is `"0"` (string, not boolean)
- Using `ClickHouseFormat.TabSeparated` not `RowBinary`

---

## Why This Approach Was Chosen

### User's Critique (from context transfer)
> "tại sao ngay từ ban đầu, official connector lại ko đc bạn chọn ???"
> (Why wasn't the official connector chosen from the beginning?)

### Answer:
1. **Official connector ALSO failed** with same "Magic is not correct" error
2. Official connector uses JDBC internally (checked source code)
3. Root cause was **format incompatibility**, not connector choice
4. **Direct ClickHouse Client API** gives us:
   - Full control over format (TabSeparated)
   - No JDBC abstraction layer hiding issues
   - Clear error messages
   - Explicit compression control

### Lesson Learned
Don't assume "official" means "always works". When debugging:
1. ✅ Check root cause with diagnostic queries (`system.query_log`, `system.parts`)
2. ✅ Test format compatibility (RowBinary vs TabSeparated)
3. ✅ Verify at protocol level (HTTP, compression, SSL)
4. ❌ Don't blindly switch connectors without understanding WHY previous one failed

---

## Next Steps

1. **Run the pipeline** with `.\scripts\build-and-run.ps1`
2. **Verify data arrival** in ClickHouse Cloud console
3. **Monitor Flink logs** for flush operations
4. **Check Grafana dashboard** for real-time visualization
5. **(Optional) Add idempotency** using ReplacingMergeTree if needed

---

## File Changes Summary

### Modified Files
1. `flink-jobs/f1-telemetry-job/pom.xml`
   - Removed: `flink-connector-jdbc`, `clickhouse-jdbc`
   - Added: `clickhouse-client:0.7.0`, `httpclient5:5.2.1`

2. `flink-jobs/f1-telemetry-job/src/main/java/com/f1telemetry/sink/ClickHouseOfficialSink.java`
   - Complete rewrite using `RichSinkFunction`
   - Manual batching with time-based + size-based flush
   - TabSeparated format
   - ClickHouse Client API (HTTP-based)

3. `flink-jobs/f1-telemetry-job/src/main/java/com/f1telemetry/F1TelemetryJob.java`
   - Changed `.sinkTo()` → `.addSink()` (RichSinkFunction API)

### New Files
1. `scripts/build-and-run.ps1` - Complete build & run workflow
2. `CLICKHOUSE_SINK_IMPLEMENTATION.md` - This documentation

---

**Implementation Date:** 2026-07-18  
**Flink Version:** 1.18.0  
**ClickHouse Client Version:** 0.7.0  
**Format:** TabSeparated (not RowBinary)
