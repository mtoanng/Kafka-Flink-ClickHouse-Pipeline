# 🔧 Refactor Notes — TracingInsights Data Structure & Package Name

**Date**: 2026-01-12  
**Issue**: Code assumptions vs. actual TracingInsights data structure

---

## 🔍 Issue 1: Incorrect Package Name

### Problem
- Current: `vn.edu.f1` (Vietnam university legacy)
- Context: This is a **personal portfolio project**, not a university assignment
- Should be: `com.f1telemetry` (standard Java reverse domain convention)

### Affected Files
- `data-generators/f1-replay-engine/src/main/avro/car-telemetry-event-v1.avsc`
- `data-generators/f1-replay-engine/src/main/java/vn/edu/f1/F1ReplayEngine.java`
- `flink-jobs/f1-telemetry-job/src/main/avro/car-telemetry-event-v1.avsc`
- `flink-jobs/f1-telemetry-job/src/main/java/vn/edu/f1/*.java`

### Solution
Global replace: `vn.edu.f1` → `com.f1telemetry`

---

## 🔍 Issue 2: TracingInsights Data Structure (VERIFIED)

### Actual Structure (from GitHub)
```
Abu Dhabi Grand Prix/Race/
├── drivers.json              # Driver metadata
│   └── { "dn": "1", "driver": "VER", ... }
├── VER/                      # 3-letter abbreviation (NOT numeric!)
│   ├── laptimes.json         # Per-lap times
│   │   └── {"time": [...], "lap": [1,2,...], "compound": [...]}
│   └── 1_tel.json, 2_tel.json, ...  # Column-oriented telemetry
│       └── {"tel": {"time": [...], "speed": [...], "throttle": [...]}}
├── HAM/
├── LEC/
└── ...
```

### Key Findings

✅ **What EXISTS**:
- Driver folders use **3-letter abbreviations** (VER, HAM, LEC), NOT driver numbers
- `drivers.json` at race root → maps `driver` code to `dn` (driver number)
- `{DRIVER}/laptimes.json` → per-driver lap times (not session-wide)
- `{DRIVER}/{LAP}_tel.json` → column-oriented arrays per lap

❌ **What DOESN'T EXIST** (current code assumes):
- `session_laptimes.json` at race root → **DOES NOT EXIST**
- Driver folders named by number (e.g., `1/`, `44/`) → **WRONG**, they use abbreviations!
- Row-oriented JSON → **WRONG**, it's column-oriented arrays

### Data Format Examples

**drivers.json** (driver metadata):
```json
{
  "drivers": [
    {"driver": "VER", "dn": "1", "fn": "Max", "ln": "Verstappen"},
    {"driver": "HAM", "dn": "44", "fn": "Lewis", "ln": "Hamilton"}
  ]
}
```

**VER/laptimes.json** (lap times for Verstappen):
```json
{
  "time": [91.677, 89.117, 89.555, ...],
  "lap": [1, 2, 3, ...],
  "compound": ["MEDIUM", "MEDIUM", ...],
  "s1": [null, 18.085, 18.197, ...],
  "s2": [38.489, 38.383, ...]
}
```

**VER/1_tel.json** (telemetry for lap 1):
```json
{
  "tel": {
    "time": [0.0, 0.05, 0.39, ...],     // Lap-relative seconds
    "speed": [285.3, 286.1, ...],       // km/h
    "throttle": [100, 100, 98, ...],    // 0-100%
    "brake": [0, 0, 5, ...],
    "rpm": [11500, 11600, ...],
    "gear": [8, 8, 7, ...],
    "drs": [0, 0, 0, ...]
  }
}
```

---

## 📝 Required Changes to F1ReplayEngine.java

### Current Logic (WRONG)
1. Load `session_laptimes.json` → get lap start times ❌
2. Iterate `{DRIVER_NUMBER}/` folders ❌
3. Parse `{LAP}_tel.json` ✅ (this part OK)
4. Reconstruct event_time ✅ (concept OK, implementation needs fix)

### Correct Logic
1. **Load `drivers.json`** → build map: `{abbreviation → driver_number}`
   - e.g., `{"VER": 1, "HAM": 44, "LEC": 16}`
2. **Iterate driver folders** by **abbreviation** (VER/, HAM/, etc.)
3. **Load `{DRIVER}/laptimes.json`** → get lap start times
   - Challenge: Lap times are **durations**, not absolute timestamps!
   - Need to compute: `lap_start[i] = race_start + sum(laptimes[0:i])`
4. **Parse `{DRIVER}/{LAP}_tel.json`** → transpose column arrays
5. **Reconstruct event_time**: `race_start + cumulative_laptime + tel["time"][i]`

### Missing Information
- **Race start timestamp** → NOT in TracingInsights data!
- **Options**:
  - A) Use arbitrary timestamp (e.g., `2025-12-08T14:00:00Z`)
  - B) Use current time when replay starts
  - C) Fetch from external source (Ergast API, F1 calendar)

**Recommendation**: Option A (arbitrary fixed timestamp) for reproducibility.

---

## 📝 Implementation TODO

### Phase 6A: Package Refactor
- [ ] Update Avro schema namespace: `vn.edu.f1.avro` → `com.f1telemetry.avro`
- [ ] Move Java packages: `vn/edu/f1/` → `com/f1telemetry/`
- [ ] Update all imports in Java files
- [ ] Update pom.xml Avro plugin output directory (if hardcoded)
- [ ] Rebuild & verify Avro codegen works

### Phase 6B: F1ReplayEngine Rewrite
- [ ] Add `DriverMetadata` class (parse `drivers.json`)
- [ ] Change from `extractDriverNumber(dirname)` → `lookupDriverNumber(abbreviation)`
- [ ] Rewrite `loadLapStartTimes()`:
  - Parse `{DRIVER}/laptimes.json` per driver
  - Compute cumulative lap start times from durations
  - Use fixed race start timestamp
- [ ] Update `loadAllTelemetry()`:
  - Iterate driver folders by abbreviation (not number)
  - Load per-driver laptimes
  - Parse telemetry files as before
- [ ] Test with small sample (1-2 drivers, 3-5 laps)

### Phase 6C: Fetch Script Implementation
- [ ] Implement `scripts/fetch_f1_session.sh`:
  - Clone/download from `https://github.com/TracingInsights-Archive/2025`
  - Extract `Abu Dhabi Grand Prix/Race/` → `datasets/`
  - Size: ~150MB (already in .gitignore)
- [ ] Add download progress indicator
- [ ] Verify dataset integrity (check key files exist)

### Phase 6D: Schema Registry Script
- [ ] Implement `scripts/register_schemas.sh`:
  - curl POST to `http://localhost:8081/subjects/car-telemetry-events-value/versions`
  - Register `car-telemetry-event-v1.avsc`
  - Handle errors (registry not available, schema conflict)

---

## 🎯 Acceptance Criteria

### Package Refactor
- [ ] `mvn clean package -DskipTests` succeeds
- [ ] Avro classes generated in `com.f1telemetry.avro` package
- [ ] No references to `vn.edu` in codebase

### Replay Engine
- [ ] Loads TracingInsights data successfully
- [ ] Events sorted by absolute event_time
- [ ] Published to Kafka with correct Avro serialization
- [ ] Watermark-compatible timestamps (monotonic per driver)

### End-to-End
- [ ] Fetch script downloads dataset
- [ ] Schema registration succeeds
- [ ] Replay engine publishes events
- [ ] Flink job consumes & processes
- [ ] Data visible in ClickHouse
- [ ] Grafana dashboard shows live data

---

## 📚 References

- **TracingInsights GitHub**: https://github.com/TracingInsights-Archive/2025
- **Abu Dhabi GP Data**: https://github.com/TracingInsights-Archive/2025/tree/main/Abu%20Dhabi%20Grand%20Prix/Race
- **FastF1 Library**: https://github.com/theOehrly/Fast-F1 (used by TracingInsights)
- **Data format documentation**: Inferred from raw JSON inspection (no official schema doc found)

---

## 🚨 Critical Path

1. **Package refactor** (30 min) → enables build
2. **Fetch script** (15 min) → gets real data
3. **Engine rewrite** (2-3 hours) → core logic
4. **Integration test** (30 min) → verify end-to-end

**Total estimated**: ~4 hours for complete working pipeline with real data.
