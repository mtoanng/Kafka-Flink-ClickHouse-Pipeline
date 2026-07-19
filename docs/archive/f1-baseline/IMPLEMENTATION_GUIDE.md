# 🛠️ Implementation Guide — F1 Replay Engine Rewrite

**Target Audience**: You (manual implementation & learning)  
**Goal**: Understand TracingInsights data structure and implement correct parsing logic

---

## 📚 Phase 7A: Fix Fetch Script

### Current Issue
Script tries to download `session_laptimes.json` which **DOESN'T EXIST** in TracingInsights repo.

### What To Do

**File**: `scripts/fetch_f1_session.sh`

**Line 17** - Remove this from the file list:
```bash
# WRONG (file doesn't exist):
for FILE in drivers.json rcm.json corners.json session_laptimes.json weather.json; do

# CORRECT (remove session_laptimes.json):
for FILE in drivers.json rcm.json corners.json weather.json; do
```

**Why**: TracingInsights stores lap times **per-driver** in `{DRIVER}/laptimes.json`, NOT in a session-wide file.

### Test
```bash
bash scripts/fetch_f1_session.sh
# Should download without errors
# Check: ls data-generators/f1-replay-engine/src/main/resources/datasets/
```

---

## 📚 Phase 7B: Understand Data Structure

### Task 1: Explore Downloaded Data

After running fetch script, inspect the files manually:

```bash
cd data-generators/f1-relay-engine/src/main/resources/datasets

# 1. Check root files
ls -lh
# You should see: drivers.json, rcm.json, corners.json, weather.json, VER/, HAM/, LEC/, ...

# 2. Inspect drivers.json (driver metadata)
cat drivers.json | python3 -m json.tool | head -30
# Look for: "driver", "dn" (driver number), "fn", "ln"

# 3. Check a driver folder structure
ls -lh VER/
# You should see: laptimes.json, 1_tel.json, 2_tel.json, ..., 58_tel.json

# 4. Inspect VER/laptimes.json
cat VER/laptimes.json | python3 -m json.tool | head -20
# Look for: "time" (array), "lap" (array), "compound" (array)

# 5. Inspect VER/1_tel.json (first lap telemetry)
cat VER/1_tel.json | python3 -m json.tool | head -30
# Look for: "tel": {"time": [...], "speed": [...], "throttle": [...]}
```

### Task 2: Map the Data Flow

Draw this diagram on paper/whiteboard:

```
drivers.json  →  Map: abbreviation → driver_number
    ↓               (e.g., "VER" → 1, "HAM" → 44)
    
VER/              ←  Iterate driver folders by abbreviation
├── laptimes.json →  Get lap durations (time array)
├── 1_tel.json    →  Get lap 1 telemetry (column arrays)
├── 2_tel.json    →  Get lap 2 telemetry
└── ...

Compute:
  lap_start[1] = race_start_time
  lap_start[2] = race_start_time + laptimes[0]
  lap_start[3] = race_start_time + laptimes[0] + laptimes[1]
  ...
  
For each telemetry point in lap N:
  event_time = lap_start[N] + tel["time"][i]
```

### Key Insight
- **laptimes.json has lap DURATIONS** (in seconds)
- Need to convert to **absolute timestamps** by cumulative sum
- Need arbitrary **race_start_time** (e.g., `2025-12-08T14:00:00Z`)

---

## 📚 Phase 7C: Rewrite F1ReplayEngine (Step-by-Step)

### Current Code Structure (BROKEN)
```java
// In F1ReplayEngine.java
private Map<Integer, Map<Integer, Long>> loadLapStartTimes() {
    // Tries to load session_laptimes.json ❌
    // Returns map: {driver_number: {lap_number: lap_start_timestamp}}
}

private int extractDriverNumber(String dirname) {
    // Tries to parse driver number from folder name ❌
    // But folders are named VER, HAM, LEC (not 1, 44, 16)
}
```

### Step 1: Add DriverMetadata Class

**Location**: `data-generators/f1-replay-engine/src/main/java/com/f1telemetry/F1ReplayEngine.java`

**Add this inner class** (around line 30):
```java
private static class DriverMetadata {
    String abbreviation;  // e.g., "VER"
    int driverNumber;     // e.g., 1
    String firstName;
    String lastName;
    
    DriverMetadata(String abbr, int number, String fn, String ln) {
        this.abbreviation = abbr;
        this.driverNumber = number;
        this.firstName = fn;
        this.lastName = ln;
    }
}
```

**Why**: Need to map driver abbreviations (VER) to driver numbers (1).

### Step 2: Load drivers.json

**Replace** the `loadLapStartTimes()` method with:

```java
private Map<String, DriverMetadata> loadDriverMetadata() throws IOException {
    Path driversPath = Paths.get(DATASET_PATH, "..", "drivers.json");
    // Note: drivers.json is at RACE ROOT, not inside driver folders
    
    if (!Files.exists(driversPath)) {
        throw new IOException("drivers.json not found at: " + driversPath);
    }
    
    JsonNode root = mapper.readTree(driversPath.toFile());
    Map<String, DriverMetadata> drivers = new HashMap<>();
    
    // Parse JSON structure: {"drivers": [{...}, {...}]}
    JsonNode driversArray = root.get("drivers");
    if (driversArray == null || !driversArray.isArray()) {
        throw new IOException("Invalid drivers.json format");
    }
    
    for (JsonNode driverNode : driversArray) {
        String abbr = driverNode.get("driver").asText();      // e.g., "VER"
        int number = Integer.parseInt(driverNode.get("dn").asText()); // e.g., "1"
        String fn = driverNode.get("fn").asText();
        String ln = driverNode.get("ln").asText();
        
        drivers.put(abbr, new DriverMetadata(abbr, number, fn, ln));
    }
    
    LOG.info("Loaded {} drivers", drivers.size());
    return drivers;
}
```

**Exercise for you**:
1. Check JSON field names match (use `cat drivers.json | python3 -m json.tool`)
2. Add error handling (what if "dn" is missing?)
3. Add logging for each driver loaded

### Step 3: Compute Lap Start Times Per Driver

**Add this method**:
```java
private Map<Integer, Long> computeLapStartTimes(
        Path driverFolder, 
        long raceStartTime) throws IOException {
    
    // Load {DRIVER}/laptimes.json
    Path laptimesPath = driverFolder.resolve("laptimes.json");
    if (!Files.exists(laptimesPath)) {
        LOG.warn("No laptimes.json for {}", driverFolder.getFileName());
        return Collections.emptyMap();
    }
    
    JsonNode root = mapper.readTree(laptimesPath.toFile());
    
    // Parse "time" array (lap durations in seconds)
    List<Double> lapDurations = jsonArrayToDoubleList(root.get("time"));
    // Parse "lap" array (lap numbers: [1, 2, 3, ...])
    List<Integer> lapNumbers = jsonArrayToIntList(root.get("lap"));
    
    if (lapDurations.isEmpty()) {
        return Collections.emptyMap();
    }
    
    // Compute cumulative lap start times
    Map<Integer, Long> lapStartTimes = new HashMap<>();
    long cumulativeTime = 0; // Milliseconds from race start
    
    for (int i = 0; i < lapNumbers.size(); i++) {
        int lapNumber = lapNumbers.get(i);
        lapStartTimes.put(lapNumber, raceStartTime + cumulativeTime);
        
        // Add this lap's duration to cumulative time
        if (i < lapDurations.size()) {
            cumulativeTime += (long) (lapDurations.get(i) * 1000); // sec → ms
        }
    }
    
    return lapStartTimes;
}
```

**Exercise for you**:
1. What happens if `lapDurations.size() != lapNumbers.size()`?
2. Add validation: check first lap number is 1
3. Log the computed lap start times for debugging

### Step 4: Update Main Run Logic

**In `run()` method**, replace the logic:

```java
public void run() throws IOException, InterruptedException {
    // NEW: Fixed race start time (arbitrary but reproducible)
    long raceStartTime = parseIsoTimestamp("2025-12-08T14:00:00Z");
    LOG.info("Using race start time: {}", 
        java.time.Instant.ofEpochMilli(raceStartTime));
    
    // Step 1: Load driver metadata
    Map<String, DriverMetadata> drivers = loadDriverMetadata();
    
    // Step 2: Load all telemetry with computed lap times
    List<TelemetryEvent> events = new ArrayList<>();
    
    Path racePath = Paths.get(DATASET_PATH);
    if (!Files.exists(racePath)) {
        throw new IOException("Race path not found: " + racePath);
    }
    
    // Iterate driver folders (by abbreviation, not number!)
    try (var driverDirs = Files.list(racePath)) {
        driverDirs.filter(Files::isDirectory).forEach(driverFolder -> {
            try {
                String abbr = driverFolder.getFileName().toString();
                
                // Skip if not a driver folder (3-letter uppercase code)
                if (abbr.length() != 3 || !abbr.matches("[A-Z]{3}")) {
                    return;
                }
                
                // Lookup driver number
                DriverMetadata driver = drivers.get(abbr);
                if (driver == null) {
                    LOG.warn("Unknown driver: {}", abbr);
                    return;
                }
                
                LOG.info("Processing driver: {} ({})", abbr, driver.driverNumber);
                
                // Compute lap start times for this driver
                Map<Integer, Long> lapStartTimes = computeLapStartTimes(
                    driverFolder, raceStartTime);
                
                // Load telemetry files
                try (var telFiles = Files.list(driverFolder)) {
                    telFiles.filter(p -> p.getFileName().toString().endsWith("_tel.json"))
                            .forEach(telFile -> {
                                try {
                                    int lapNumber = extractLapNumber(
                                        telFile.getFileName().toString());
                                    
                                    Long lapStartTime = lapStartTimes.get(lapNumber);
                                    if (lapStartTime == null) {
                                        LOG.warn("No lap start time for {} lap {}", 
                                            abbr, lapNumber);
                                        return;
                                    }
                                    
                                    List<TelemetryEvent> lapEvents = parseTelemetryFile(
                                        telFile, driver.driverNumber, lapStartTime);
                                    
                                    synchronized (events) {
                                        events.addAll(lapEvents);
                                    }
                                    
                                } catch (IOException e) {
                                    LOG.error("Failed to parse {}: {}", telFile, e.getMessage());
                                }
                            });
                }
                
            } catch (IOException e) {
                LOG.error("Failed to process driver {}: {}", 
                    driverFolder.getFileName(), e.getMessage());
            }
        });
    }
    
    LOG.info("Loaded {} telemetry events", events.size());
    
    // Step 3: Global sort by event_time
    events.sort(Comparator.comparingLong(e -> e.eventTime));
    LOG.info("Events sorted by event_time");
    
    // Step 4: Replay events
    replayEvents(events);
    
    producer.flush();
    producer.close();
    LOG.info("Replay completed");
}
```

**Exercise for you**:
1. Add progress logging: "Processed lap X/Y for driver Z"
2. Add statistics: count events per driver
3. Handle edge case: what if a driver has missing laps?

### Step 5: Remove Broken Methods

**Delete these methods** (no longer needed):
- `extractDriverNumber(String dirname)` → replaced by driver metadata lookup
- Old `loadLapStartTimes()` → replaced by per-driver computation

### Step 6: Test with Sample Data

Before running full dataset:

```java
// In main(), add debug flag:
private static final boolean DEBUG_MODE = true;
private static final int DEBUG_MAX_DRIVERS = 2;
private static final int DEBUG_MAX_LAPS = 3;

// In run(), add early exits:
if (DEBUG_MODE) {
    if (driverCount >= DEBUG_MAX_DRIVERS) return;
    if (lapNumber > DEBUG_MAX_LAPS) return;
}
```

**Exercise**: 
1. Run with 2 drivers, 3 laps each
2. Check output: sorted by event_time?
3. Verify driver numbers are correct (not all 0 or -1)

---

## 📚 Phase 7D: Schema Registry Script

### Task: Implement register_schemas.sh

**File**: `scripts/register_schemas.sh`

```bash
#!/usr/bin/env bash
set -e

SCHEMA_REGISTRY_URL="${SCHEMA_REGISTRY_URL:-http://localhost:8081}"
SCHEMA_FILE="data-generators/f1-replay-engine/src/main/avro/car-telemetry-event-v1.avsc"
SUBJECT="car-telemetry-events-value"

echo "Registering Avro schema to Schema Registry..."
echo "  Registry: $SCHEMA_REGISTRY_URL"
echo "  Subject: $SUBJECT"
echo "  Schema: $SCHEMA_FILE"

# Check if schema file exists
if [ ! -f "$SCHEMA_FILE" ]; then
    echo "ERROR: Schema file not found: $SCHEMA_FILE"
    exit 1
fi

# Check if Schema Registry is available
if ! curl -sf "$SCHEMA_REGISTRY_URL" > /dev/null; then
    echo "ERROR: Schema Registry not available at $SCHEMA_REGISTRY_URL"
    echo "Start it with: docker compose -f infra/docker-compose.yml up -d"
    exit 1
fi

# Register schema
SCHEMA_JSON=$(cat "$SCHEMA_FILE" | jq -c '.')
PAYLOAD=$(jq -n --arg schema "$SCHEMA_JSON" '{"schema": $schema}')

RESPONSE=$(curl -s -X POST \
    -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data "$PAYLOAD" \
    "$SCHEMA_REGISTRY_URL/subjects/$SUBJECT/versions")

echo "Response: $RESPONSE"

# Check if successful (response should contain "id")
if echo "$RESPONSE" | grep -q '"id"'; then
    SCHEMA_ID=$(echo "$RESPONSE" | jq -r '.id')
    echo "✅ Schema registered successfully! ID: $SCHEMA_ID"
else
    echo "❌ Failed to register schema"
    exit 1
fi
```

**Exercise for you**:
1. Test with Schema Registry down (what error message?)
2. Register same schema twice (what happens?)
3. Verify: `curl http://localhost:8081/subjects/car-telemetry-events-value/versions`

---

## 📚 Phase 7E: End-to-End Testing Guide

### Test Checklist

**1. Infrastructure**
```bash
# Start services
docker compose -f infra/docker-compose.yml up -d

# Wait for health
docker compose -f infra/docker-compose.yml ps
# All should show "healthy"
```

**2. Build**
```bash
mvn clean package -DskipTests
# Should complete without errors
# Check JARs exist in target/ directories
```

**3. Fetch Data**
```bash
bash scripts/fetch_f1_session.sh
# Should download ~150MB
# Verify: ls data-generators/f1-replay-engine/src/main/resources/datasets/
```

**4. Register Schema**
```bash
bash scripts/register_schemas.sh
# Should print: "Schema registered successfully! ID: 1"
```

**5. Run Replay Engine (Debug Mode)**
```bash
# Edit F1ReplayEngine.java: set DEBUG_MODE = true
mvn -pl data-generators/f1-replay-engine exec:java
# Should publish ~500 events (2 drivers × 3 laps × ~80 points/lap)
```

**6. Verify Kafka**
```bash
# Check topic exists
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
# Should show: car-telemetry-events

# Consume messages (should see Avro binary data)
docker exec kafka kafka-console-consumer \
    --bootstrap-server localhost:9092 \
    --topic car-telemetry-events \
    --from-beginning \
    --max-messages 5
```

**7. Run Flink Job**
```bash
# Option A: IDE
# Run F1TelemetryJob main() in IntelliJ

# Option B: Command line
mvn -pl flink-jobs/f1-telemetry-job exec:java
```

**8. Verify ClickHouse**
```bash
docker exec -it clickhouse clickhouse-client

# Check raw telemetry
SELECT count() FROM f1_telemetry.raw_telemetry;
-- Should show > 0

# Check rollup
SELECT count() FROM f1_telemetry.rollup_10s;
-- Should show > 0

# Top speeds
SELECT driver_number, max(speed) as top_speed 
FROM raw_telemetry 
GROUP BY driver_number 
ORDER BY top_speed DESC;
```

---

## 🎯 Learning Outcomes

After completing this guide manually, you should understand:

1. ✅ **Data Structures**: Column-oriented JSON, why it matters
2. ✅ **Time Reconstruction**: Converting durations → absolute timestamps
3. ✅ **Java Streams**: File I/O, JSON parsing, error handling
4. ✅ **Event-Time Processing**: Why global sort matters for watermarks
5. ✅ **Schema Registry**: Why Avro needs centralized schema management
6. ✅ **Debugging**: How to trace data flow from JSON → Kafka → Flink → ClickHouse

---

## 📝 Common Issues & Solutions

### Issue 1: "No such file: session_laptimes.json"
**Solution**: Remove it from fetch script (doesn't exist in TracingInsights)

### Issue 2: "Driver number is -1"
**Solution**: Check driver metadata lookup (abbr → number mapping)

### Issue 3: "Events out of order"
**Solution**: Check lap start time computation (cumulative sum)

### Issue 4: "Flink late events"
**Solution**: Check watermark strategy (3s out-of-order should be enough)

### Issue 5: "ClickHouse empty tables"
**Solution**: Check Flink logs for exceptions in sink

---

## 🚀 Next Steps After Manual Implementation

1. **Optimize**: Add parallel processing (ForkJoinPool for driver folders)
2. **Monitor**: Add metrics (events/sec, lag, errors)
3. **Scale**: Test with full dataset (20 drivers × 58 laps = ~92k events)
4. **Grafana**: Configure dashboard queries
5. **Document**: Write README for your learned insights

**You got this! 💪**
