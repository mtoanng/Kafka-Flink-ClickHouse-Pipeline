# 🏗️ F1 Live Telemetry - Complete Infrastructure Setup Guide (A to Z)

**Mục đích**: Hướng dẫn setup toàn bộ infrastructure từ con số 0, bao gồm cloud và local  
**Thời gian ước tính**: 3-4 giờ (bao gồm waiting time cho provisioning)  
**Yêu cầu**: Windows 10/11 với quyền admin

---

## 📋 TABLE OF CONTENTS

1. [Prerequisites Installation](#phase-1-prerequisites-installation)
2. [Local Development Tools](#phase-2-local-development-tools)
3. [ClickHouse Cloud Setup](#phase-3-clickhouse-cloud-setup)
4. [Grafana Cloud Setup](#phase-4-grafana-cloud-setup)
5. [Local Infrastructure (Docker)](#phase-5-local-infrastructure-docker)
6. [Project Build & Configuration](#phase-6-project-build--configuration)
7. [Data Preparation](#phase-7-data-preparation)
8. [Pipeline Deployment](#phase-8-pipeline-deployment)
9. [Validation & Testing](#phase-9-validation--testing)
10. [Troubleshooting](#phase-10-troubleshooting)

---

## 🎯 OVERVIEW

### Architecture Diagram
```
┌────────────────────────────────────────────────────────────────┐
│                        YOUR WINDOWS PC                         │
│                                                                │
│  ┌───────────────┐    ┌─────────────┐    ┌─────────────────┐   │
│  │   Dataset     │ →  │   Replay    │ →  │  Kafka KRaft    │   │
│  │TracingInsights│    │   Engine    │    │ + Schema Reg    │   │
│  │   (150MB)     │    │   (Java)    │    │   (Docker)      │   │
│  └───────────────┘    └─────────────┘    └────────┬────────┘   │
│                                                   │            │
│                                          ┌────────▼─────────┐  │
│                                          │   Flink Job      │  │
│                                          │  (DataStream)    │  │
│                                          └────────┬─────────┘  │
└───────────────────────────────────────────────────┼────────────┘
                                                    │
                    ┌───────────────────────────────┼─────────────┐
                    │                               │             │
                    │            INTERNET           │             │
                    │                               │             │
         ┌──────────▼──────────┐       ┌────────────▼─────────┐   │
         │  ClickHouse Cloud   │       │   Grafana Cloud      │   │
         │  (OLAP Database)    │ ◄─────┤  (Visualization)     │   │
         │  - raw_telemetry    │       │  - Live Dashboard    │   │
         │  - rollup_10s       │       │  - Real-time charts  │   │
         └─────────────────────┘       └──────────────────────┘   │
                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### What We'll Setup
- ✅ **Local**: Docker (Kafka + Schema Registry), Java, Maven, Git Bash
- ☁️ **Cloud**: ClickHouse Cloud, Grafana Cloud
- 📦 **Data**: TracingInsights F1 dataset (Abu Dhabi GP 2025)
- 🔧 **Code**: Build Maven projects, configure environment

---

## 🚀 PHASE 1: PREREQUISITES INSTALLATION

### Step 1.1: Install Chocolatey (Package Manager) ⏱️ 5 mins

**Tại sao cần**: Chocolatey giúp cài đặt tools nhanh chóng qua command line

**Cách cài**:
1. Mở **PowerShell** as Administrator (Right-click → Run as Administrator)
2. Check execution policy:
```powershell
Get-ExecutionPolicy
# If it returns "Restricted", run:
Set-ExecutionPolicy Bypass -Scope Process -Force
```

3. Install Chocolatey:
```powershell
[System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```

4. Verify installation:
```powershell
choco --version
# Expected output: 2.x.x
```

**Troubleshooting**:
- Nếu lỗi "execution policy": Chạy PowerShell as Admin
- Nếu lỗi network: Check firewall/antivirus

---

### Step 1.2: Install Docker Desktop ⏱️ 10 mins

**Status**: ✅ Already installed (Docker version 29.6.1)

**Verify**:
```powershell
docker --version
docker compose version
```

**If not installed**, download from: https://www.docker.com/products/docker-desktop/

**Configure Docker Desktop**:
1. Open Docker Desktop
2. Settings → Resources → Memory: **Set to 6GB** (important!)
3. Settings → Resources → CPU: 4 cores (recommended)
4. Apply & Restart

---

### Step 1.3: Install Java Development Kit (JDK) ⏱️ 5 mins

**Status**: ✅ Already installed (Java 20.0.2)

**Verify**:
```powershell
java -version
# Expected: java version "20.0.2" or higher
```

**If not installed**:
```powershell
choco install openjdk17 -y
# OR download manually from: https://jdk.java.net/
```

**Set JAVA_HOME** (if not set):
```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-20", "Machine")
```

---

### Step 1.4: Install Apache Maven ⏱️ 5 mins

**Status**: ⚠️ NOT INSTALLED (Critical requirement!)

**Install via Chocolatey**:
```powershell
# Open PowerShell as Administrator
choco install maven -y
```

**Verify installation**:
```powershell
# Close and reopen PowerShell (to reload PATH)
mvn -version

# Expected output:
# Apache Maven 3.9.x
# Maven home: C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.x
# Java version: 20.0.2
```

**Manual Installation** (alternative):
1. Download: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Maven`
3. Add to PATH:
```powershell
$env:Path += ";C:\Program Files\Maven\bin"
[Environment]::SetEnvironmentVariable("Path", $env:Path, "Machine")
```

---

### Step 1.5: Install Git + Git Bash ⏱️ 5 mins

**Tại sao cần**: Scripts sử dụng bash syntax

**Check if installed**:
```powershell
git --version
```

**If not installed**:
```powershell
choco install git -y
```

**Verify Git Bash**:
1. Right-click in any folder → "Git Bash Here" should appear
2. Or open Start Menu → search "Git Bash"

---

### Step 1.6: Install Python 3.x ⏱️ 5 mins

**Status**: ✅ Có vẻ đã cài (dựa vào hardcoded path trong script)

**Verify**:
```powershell
python --version
# OR
python3 --version

# Expected: Python 3.10+ 
```

**If not installed**:
```powershell
choco install python -y
```

**Verify pip**:
```powershell
pip --version
```

---

### Step 1.7: Install cURL ⏱️ 2 mins

**Check if installed**:
```powershell
curl --version
```

**If not installed**:
```powershell
choco install curl -y
```

---

## 💻 PHASE 2: LOCAL DEVELOPMENT TOOLS

### Step 2.1: Install IDE (Optional but Recommended) ⏱️ 10 mins

**Options**:

**Option A: IntelliJ IDEA Community** (Recommended for Java)
```powershell
choco install intellijidea-community -y
```

**Option B: VS Code** (Lightweight)
```powershell
choco install vscode -y

# Install Java extensions:
# - Extension Pack for Java (Microsoft)
# - Maven for Java
```

---

### Step 2.2: Clone Project Repository ⏱️ 2 mins

**Navigate to project**:
```powershell
cd c:\Users\ADMIN\STREAMING\Kafka-Flink-ClickHouse-Pipeline
```

**Check Git status**:
```powershell
git status
git log --oneline -5
```

---

## ☁️ PHASE 3: CLICKHOUSE CLOUD SETUP

### Step 3.1: Create ClickHouse Cloud Account ⏱️ 5 mins

1. **Go to**: https://clickhouse.cloud
2. Click **"Start free trial"** hoặc **"Sign up"**
3. Chọn phương thức đăng ký:
   - Google account
   - GitHub account
   - Email + password
4. Xác nhận email (check inbox)
5. Login vào ClickHouse Cloud console

**Free Trial Info**:
- ✅ $300 credits (enough for development)
- ✅ No credit card required initially
- ✅ 30 days trial period

---

### Step 3.2: Create ClickHouse Service ⏱️ 5 mins (+ 2-3 mins provisioning)

1. **In ClickHouse Cloud Dashboard**, click **"Create service"**

2. **Configure Service**:
   ```
   Service name: f1-telementry-warehouse
   Cloud Provider: AWS (hoặc GCP/Azure)
   Region: us-east-1 (hoặc gần bạn nhất)
   Tier: Development (free tier)
   ```

3. Click **"Create service"**

4. **Wait for provisioning**: Status sẽ chuyển từ "Provisioning" → "Running" (2-3 mins)

5. **Important**: Service sẽ tự động sleep sau 30 phút idle (free tier) - wake up bằng cách click vào service

---

### Step 3.3: Get Connection Details ⏱️ 2 mins

1. Click vào service **"f1-telementry-warehouse"**
2. Click tab **"Connect"**
3. **Copy these values** (save to notepad):

```
Host: xxxxxxxx.us-east-1.aws.clickhouse.cloud
Port: 8443
User: default
Password: **************** (click "Show" to reveal)
Database: default
```

**Example connection string**:
```
jdbc:ch://abc123def.us-east-1.aws.clickhouse.cloud:8443/f1_telemetry?ssl=true
```

---

### Step 3.4: Create Database & Tables ⏱️ 3 mins

1. **Open SQL Console**:
   - In service page, click **"Open SQL console"** button
   - Or click **"Query"** tab

2. **Create database**:
```sql
CREATE DATABASE IF NOT EXISTS f1_telemetry;
```

3. **Switch to database**:
```sql
USE f1_telemetry;
```

4. **Copy DDL script from local file**:
   - Open file: `c:\Users\ADMIN\STREAMING\Kafka-Flink-ClickHouse-Pipeline\infra\clickhouse\01_tables.sql`
   - Copy toàn bộ nội dung (từ CREATE DATABASE... đến cuối)

5. **Paste vào SQL console** và click **"Run"**

6. **Verify tables created**:
```sql
SHOW TABLES FROM f1_telemetry;

-- Expected output:
-- raw_telemetry
-- rollup_10s
```

7. **Check table structure**:
```sql
DESCRIBE f1_telemetry.raw_telemetry;
DESCRIBE f1_telemetry.rollup_10s;
```

---

### Step 3.5: Test Connection from Local Machine ⏱️ 2 mins

**Using cURL**:
```powershell
# Replace with your actual credentials
$CLICKHOUSE_HOST = "your-host.aws.clickhouse.cloud"
$CLICKHOUSE_USER = "default"
$CLICKHOUSE_PASSWORD = "your-password"

# Test query
curl "https://${CLICKHOUSE_HOST}:8443/?query=SELECT+1" `
  -u "${CLICKHOUSE_USER}:${CLICKHOUSE_PASSWORD}"

# Expected output: 1
```

**If successful**: ✅ ClickHouse Cloud is ready!

---

## 📊 PHASE 4: GRAFANA CLOUD SETUP

### Step 4.1: Create Grafana Cloud Account ⏱️ 5 mins

1. **Go to**: https://grafana.com/auth/sign-up/create-user
2. **Sign up** with:
   - Email + password
   - Or Google/GitHub account
3. **Complete profile**:
   ```
   Organization name: F1-Telemetry (or your choice)
   Role: Developer/Engineer
   ```
4. Click **"Finish setup"**

**Free Tier Includes**:
- ✅ 10,000 series for Prometheus metrics
- ✅ 50 GB logs
- ✅ 50 GB traces
- ✅ Unlimited dashboards
- ✅ 14-day retention

---

### Step 4.2: Create Grafana Stack ⏱️ 2 mins

1. After signup, Grafana tự động tạo một stack cho bạn
2. **Stack name**: Mặc định là `<your-org>-<random>`
3. **Region**: Chọn gần bạn nhất (US, EU, AP)
4. Click **"Launch"** (nếu chưa launch)

**Access your Grafana**:
- URL: `https://<your-org>.grafana.net`
- Username: (your email)
- Password: (set during signup)

---

### Step 4.3: Install ClickHouse Data Source Plugin ⏱️ 3 mins

1. **Login to your Grafana instance**: https://your-org.grafana.net

2. **Navigate to**:
   - Left sidebar → ⚙️ **Configuration** (hoặc **Connections**)
   - Click **"Data sources"**

3. **Add data source**:
   - Click **"Add data source"** button
   - Search: **"ClickHouse"**
   - Select **"ClickHouse"** by Grafana Labs

4. **If not installed yet**, click **"Install"** first

---

### Step 4.4: Configure ClickHouse Data Source ⏱️ 5 mins

**Fill in connection details**:

```yaml
Name: clickhouse-f1

# Server Settings
Server address: your-host.aws.clickhouse.cloud
Server port: 8443
Protocol: HTTPS

# Database
Default database: f1_telemetry

# Auth
Username: default
Password: **************** (your ClickHouse password)

# Additional Settings
Secure connection: ✅ Enabled
Skip TLS verification: ❌ Disabled (keep secure)
Forward OAuth identity: ❌ Disabled
```

**Advanced Settings** (expand):
```yaml
Timeout: 30
Max open connections: 10
Max idle connections: 2
Connection max lifetime: 3600
```

**Click "Save & test"**

**Expected result**: ✅ Green checkmark "Data source is working"

**If error occurs**:
- ❌ "Connection refused" → Check ClickHouse service is running (not sleeping)
- ❌ "Bad credentials" → Verify username/password from ClickHouse Connect tab
- ❌ "SSL error" → Make sure Protocol is HTTPS and port is 8443

---

### Step 4.5: Test Query ⏱️ 2 mins

1. Click **"Explore"** (or go to Explore page)
2. Select datasource: **clickhouse-f1**
3. In query editor, paste:
```sql
SELECT 1 as test
```
4. Click **"Run query"**
5. Should see result: `test: 1`

---

### Step 4.6: Import F1 Dashboard ⏱️ 5 mins

**Later step - Do this AFTER data is flowing**

We'll come back to this in Phase 9 after we have data in ClickHouse.

**Bookmark for later**:
- Dashboard JSON location: `infra/grafana/dashboards/f1-live-telemetry.json`

---

## 🐳 PHASE 5: LOCAL INFRASTRUCTURE (DOCKER)

### Step 5.1: Start Docker Desktop ⏱️ 2 mins

1. **Open Docker Desktop** application
2. **Wait for Docker Engine** to start (icon turns green)
3. **Verify**:
```powershell
docker ps
# Should show empty list (no error)
```

---

### Step 5.2: Navigate to Project Directory ⏱️ 1 min

```bash
cd /c/Users/ADMIN/STREAMING/Kafka-Flink-ClickHouse-Pipeline
```

---

### Step 5.3: Start Kafka + Schema Registry ⏱️ 5 mins

**Start services from Git Bash**:
```bash
bash scripts/run.sh
```

**Expected output**:
```
Starting Kafka KRaft + Schema Registry...
   Services ready after 15s
   Creating Kafka topic...
   Topic created. Verify:
   Registering Avro schema...
   Registered subjects:
   === Infrastructure ready ===
```

**Verify in another terminal**:
```bash
docker compose -f infra/docker-compose.yml ps
```

**Note**: `bash scripts/run.sh` already starts Kafka, waits for health, creates the topic, and registers the Avro schema. Do not run `create_topic.sh` or `register_schemas.sh` separately unless you need to repair one of those steps.

---

### Step 5.4: Verify Kafka ⏱️ 2 mins

**Check Kafka broker**:
```powershell
docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

**Expected output**: Long list of API versions (means Kafka is ready)

**Check logs**:
```powershell
docker logs kafka --tail 50
```

Look for: `[KafkaServer id=1] started` (SUCCESS)

---

### Step 5.5: Verify Schema Registry ⏱️ 2 mins

**Test HTTP endpoint**:
```bash
curl http://localhost:8081/subjects
```

**Expected output**: A non-empty list that includes `car-telemetry-events-value` after `bash scripts/run.sh` has completed.

**Note**: If you are checking Schema Registry before running `bash scripts/run.sh`, then `[]` is expected. After `run.sh`, the Avro schema should already be registered.

**Check logs**:
```bash
docker logs schema-registry --tail 50
```

Look for: `Server started, listening for requests` (SUCCESS)

---

### Step 5.6: Create Kafka Topic ⏱️ 2 mins

**Only if you need to recreate the topic manually**:
```bash
bash scripts/create_topic.sh
```

If you already ran `bash scripts/run.sh`, this step is optional because the topic has already been created.

**Verify topic created**:
```bash
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
```

**Expected output**: `car-telemetry-events`

**Describe topic**:
```bash
docker exec kafka kafka-topics --describe \
   --bootstrap-server localhost:9092 \
   --topic car-telemetry-events
```

Should show 3 partitions, replication factor 1.

---

### Step 5.7: Stop Local Infrastructure ⏱️ 1 min

**Stop containers but keep data volumes**:
```bash
bash scripts/stop.sh
```

**Stop containers and wipe volumes**:
```bash
bash scripts/stop.sh --volumes
```

Use `--volumes` only when you want a full reset of Kafka and Schema Registry data.

---

## 🔧 PHASE 6: PROJECT BUILD & CONFIGURATION

### Step 6.1: Build Maven Project ⏱️ 10 mins

**Clean build**:
```bash
mvn clean package -DskipTests
```

**Expected output**:
```
[INFO] Reactor Summary:
[INFO]
[INFO] F1 Live Telemetry Streaming Platform (Parent) . SUCCESS [  0.xxx s]
[INFO] F1 Telemetry Replay Engine ..................... SUCCESS [ xx.xxx s]
[INFO] F1 Telemetry Flink Stream Processing Job ....... SUCCESS [ xx.xxx s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  xx.xxx s
```

**Generated JARs location**:
```
data-generators/f1-replay-engine/target/f1-replay-engine-1.0.0-SNAPSHOT.jar
flink-jobs/f1-telemetry-job/target/f1-telemetry-job-1.0.0-SNAPSHOT.jar
```

**If build fails**:
- ❌ Maven not found → Reopen PowerShell to reload PATH
- ❌ Java version mismatch → Check `mvn -version` shows Java 11+
- ❌ Dependency download error → Check internet connection, retry with `-U` flag

---

### Step 6.2: Register Avro Schema ⏱️ 2 mins

**Using Git Bash**:
```bash
bash scripts/register_schemas.sh
```

**Expected output**:
```
Schema Registry: http://localhost:8081
Registering car-telemetry-events-value...
  Response: {"id":1}

Registered subjects:
["car-telemetry-events-value"]
```

**Verify**:
```bash
curl http://localhost:8081/subjects
# Output: ["car-telemetry-events-value"]

curl http://localhost:8081/subjects/car-telemetry-events-value/versions
# Output: [1]

curl http://localhost:8081/subjects/car-telemetry-events-value/versions/1
# Output: Full schema JSON
```

---

### Step 6.3: Create .env File ⏱️ 3 mins

**Copy template**:
```bash
cp .env.example .env
```

**Edit .env file**:
```powershell
notepad .env
```

**Update with your ClickHouse credentials**:
```bash
# ── Kafka (local KRaft) ─────────────────────────────────────
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC=car-telemetry-events
SCHEMA_REGISTRY_URL=http://localhost:8081

# ── ClickHouse Cloud ────────────────────────────────────────
# UPDATE THESE with your actual credentials from Phase 3
CLICKHOUSE_HOST=abc123def.us-east-1.aws.clickhouse.cloud
CLICKHOUSE_PORT=8443
CLICKHOUSE_USER=default
CLICKHOUSE_PASSWORD=your-actual-password-here
CLICKHOUSE_DATABASE=f1_telemetry

# ── Replay Engine ───────────────────────────────────────────
REPLAY_SPEED_FACTOR=5
F1_DATASET_PATH=data-generators/f1-replay-engine/src/main/resources/datasets
```

**Save and close**

**⚠️ Important**: Never commit `.env` file to Git (already in `.gitignore`)

---

## 📥 PHASE 7: DATA PREPARATION

### Step 7.1: Download F1 Dataset ⏱️ 15-20 mins (depending on internet speed)

**Dataset info**:
- Source: TracingInsights/2025 GitHub repository
- Race: Abu Dhabi Grand Prix 2025
- Size: ~150MB
- Files: 400+ JSON files (drivers, telemetry laps, race control)

**Download using Git Bash**:
```bash
bash scripts/fetch_f1_session.sh
```

**Expected output**:
```
Downloading Abu Dhabi Grand Prix/Race from TracingInsights/2025...
Destination: data-generators/f1-replay-engine/src/main/resources/datasets
  - drivers.json
  - rcm.json
  - corners.json
  - weather.json
  - VER/ (telemetry laps)
  - ALB/ (telemetry laps)
  - HAM/ (telemetry laps)
  ...
  - LEC/ (telemetry laps)

Done. 423 files downloaded.
Total size: 147M
```

---

### Step 7.2: Verify Dataset ⏱️ 2 mins

**Check directory exists**:
```powershell
cd data-generators\f1-replay-engine\src\main\resources\datasets
dir
```

**Expected structure**:
```
drivers.json        (driver metadata)
rcm.json            (race control messages)
corners.json        (track corners)
weather.json        (weather data)
session_laptimes.json  (lap start times)
VER/                (Verstappen telemetry)
  ├── laptimes.json
  ├── 1_tel.json
  ├── 2_tel.json
  └── ... (58 laps)
ALB/                (Albon telemetry)
HAM/                (Hamilton telemetry)
LEC/                (Leclerc telemetry)
... (20 drivers total)
```

**Check file count**:
```powershell
(Get-ChildItem -Recurse -File).Count
# Should be ~400-450 files
```

**Check drivers.json**:
```powershell
cat drivers.json | python -m json.tool | head -20
```

Should see driver list with codes (VER, HAM, LEC, etc.)

---

### Step 7.3: Validate Dataset Schema ⏱️ 2 mins

**Quick validation**:
```bash
# Check if session_laptimes.json exists
test -f session_laptimes.json && echo "✅ session_laptimes.json found" || echo "❌ Missing"

# Check if driver folders exist
test -d VER && echo "✅ VER folder found" || echo "❌ Missing"

# Check telemetry files
ls VER/*_tel.json | wc -l
# Should show ~58 (number of laps)
```

**If any files missing**: Re-run `bash scripts/fetch_f1_session.sh`

---

## 🚀 PHASE 8: PIPELINE DEPLOYMENT

### Step 8.1: Load Environment Variables ⏱️ 1 min

**For Git Bash** (when running scripts):
```bash
# Git Bash automatically loads .env if you use 'source'
source .env
echo $CLICKHOUSE_HOST  # Should print your host
```

**Tip**: Keep all run commands in Git Bash so the same environment is used for replay, schema registration, and Flink.

---

### Step 8.2: Wake Up ClickHouse Service ⏱️ 1 min

**Important**: ClickHouse Cloud free tier auto-sleeps after 30 mins idle

1. Go to https://clickhouse.cloud
2. Click on your service **"f1-telementry-warehouse"**
3. If status shows **"Idle"**, click **"Resume"**
4. Wait ~30 seconds for status → **"Running"**

---

### Step 8.3: Start Replay Engine ⏱️ 2 mins (Terminal 1)

**Open Git Bash Terminal 1**:
```bash
cd /c/Users/ADMIN/STREAMING/Kafka-Flink-ClickHouse-Pipeline
bash scripts/replay.sh
```

**Expected output** (first 30 seconds):
```
INFO  Replay Engine init | Kafka=localhost:9092 SR=http://localhost:8081 topic=car-telemetry-events speed=5x
INFO  Drivers: {VER=1, ALB=23, HAM=44, LEC=16, SAI=55, ...}
INFO  Lap start times for 20 drivers
INFO  Total events loaded: 92345
INFO  Events sorted by event_time
INFO  Published 1000 events | driver=1 speed=298km/h
INFO  Published 2000 events | driver=44 speed=312km/h
...
```

**Leave this running** - It will publish events for ~40 minutes (at 5x speed)

---

### Step 8.4: Start Flink Job ⏱️ 2 mins (Terminal 2)

**Wait 30 seconds** after Replay Engine starts producing messages

**Open New Git Bash Terminal 2**:
```bash
cd /c/Users/ADMIN/STREAMING/Kafka-Flink-ClickHouse-Pipeline
bash scripts/run_flink.sh
```

**Expected output**:
```
INFO  === F1 Telemetry Flink Job ===
INFO  Kafka=localhost:9092 | SR=http://localhost:8081 | topic=car-telemetry-events
INFO  ClickHouse: abc123def.us-east-1.aws.clickhouse.cloud:8443 (user=default)
INFO  ClickHouse raw sink ready (batch=1000, url=jdbc:ch://...)
INFO  ClickHouse rollup sink ready (batch=100)
INFO  Job started
```

**After ~10 seconds**, you should see:
```
DEBUG Flushed 1000 raw records
DEBUG Flushed 100 rollup records
```

**Leave this running**

---

## ✅ PHASE 9: VALIDATION & TESTING

### Step 9.1: Verify Kafka Messages ⏱️ 2 mins

**Check messages are flowing**:
```powershell
docker exec kafka kafka-console-consumer `
  --bootstrap-server localhost:9092 `
  --topic car-telemetry-events `
  --max-messages 3 `
  --from-beginning
```

**Expected**: Binary Avro data (garbled text with some readable fields)

**Check consumer lag**:
```powershell
docker exec kafka kafka-consumer-groups `
  --bootstrap-server localhost:9092 `
  --describe `
  --group f1-telemetry-job
```

Should show lag decreasing (Flink consuming messages)

---

### Step 9.2: Query ClickHouse Data ⏱️ 5 mins

**Open ClickHouse Cloud SQL Console**:
1. Go to https://clickhouse.cloud
2. Click your service "f1-telementry-warehouse"
3. Click "Open SQL console"

**Query 1: Check raw ingestion**:
```sql
SELECT 
    count() as total_events,
    min(event_time) as first_event,
    max(event_time) as last_event,
    count(DISTINCT driver_number) as drivers
FROM f1_telemetry.raw_telemetry;
```

**Expected** (after 2-3 minutes):
```
total_events: 5000+
first_event:  2025-12-07 13:03:27
last_event:   2025-12-07 13:15:42
drivers:      20
```

**Query 2: Top speeds per driver**:
```sql
SELECT 
    driver_number,
    max(speed) as top_speed,
    count() as samples
FROM f1_telemetry.raw_telemetry
GROUP BY driver_number
ORDER BY top_speed DESC
LIMIT 10;
```

**Expected output**:
```
driver_number | top_speed | samples
1             | 351.2     | 3456
44            | 349.8     | 3421
16            | 348.5     | 3398
...
```

**Query 3: Check rollup aggregates**:
```sql
SELECT 
    window_start,
    driver_number,
    avg_speed,
    max_speed,
    hard_brake_count,
    sample_count
FROM f1_telemetry.rollup_10s
ORDER BY window_start DESC, driver_number
LIMIT 20;
```

**Expected**: 10-second windowed aggregates per driver

---

### Step 9.3: Import Grafana Dashboard ⏱️ 5 mins

**Now we have data, let's visualize it!**

1. **Login to Grafana Cloud**: https://your-org.grafana.net

2. **Navigate to Dashboards**:
   - Click **"+"** icon or **"Dashboards"**
   - Click **"Import"**

3. **Upload dashboard JSON**:
   - Click **"Upload JSON file"**
   - Select: `c:\Users\ADMIN\STREAMING\Kafka-Flink-ClickHouse-Pipeline\infra\grafana\dashboards\f1-live-telemetry.json`

4. **Configure import**:
   ```
   Name: F1 Live Telemetry (keep default)
   Folder: General (or create "F1 Telemetry")
   UID: f1-live-telemetry (keep default)
   ```

5. **Select datasource**:
   - In dropdown, select: **clickhouse-f1** (created in Phase 4)

6. Click **"Import"**

---

### Step 9.4: Explore Dashboard ⏱️ 5 mins

**Dashboard should show**:

**Panel 1: Live Speed Gauge**
- Shows current speed for drivers 1, 11, 44
- Updates every 5 seconds
- Range: 0-350 km/h

**Panel 2: 10s Rollup Time Series**
- Line chart showing avg_speed and max_speed
- Per driver (color-coded)
- Time range: Last 15 minutes (refresh 5s)

**Panel 3: Top Speed Ticker** (if implemented)
- Single stat showing global max speed
- Updates in real-time as new data arrives

**Panel 4: Hard Brake Events** (if implemented)
- Count of hard braking per driver
- Bar chart or stat panel

**Troubleshooting dashboard**:
- ❌ "No data" → Check time range (set to "Last 15 minutes")
- ❌ "Query error" → Check datasource connection (Step 4.4)
- ❌ "Permission denied" → Check ClickHouse credentials in datasource

---

### Step 9.5: Real-time Validation ⏱️ 3 mins

**Watch data flow in real-time**:

**Terminal 3** (PowerShell):
```powershell
# Query ClickHouse every 5 seconds
while ($true) {
    $count = (curl -u "default:YOUR_PASSWORD" `
        "https://YOUR_HOST.aws.clickhouse.cloud:8443/?query=SELECT+count()+FROM+f1_telemetry.raw_telemetry" `
        -s)
    Write-Host "Total events: $count"
    Start-Sleep -Seconds 5
}
```

You should see count increasing steadily (~60-70 events/sec at 5x speed)

---

## 🐛 PHASE 10: TROUBLESHOOTING

### Issue 1: Maven not found after installation

**Symptom**: `mvn: command not found` or `mvn not recognized`

**Solution**:
```powershell
# 1. Close ALL PowerShell windows
# 2. Reopen PowerShell (to reload PATH)
# 3. Verify
mvn -version

# If still not working, manually check PATH
$env:Path -split ';' | Select-String -Pattern "maven"

# Should show: C:\ProgramData\chocolatey\lib\maven\...
```

---

### Issue 2: Docker containers fail to start

**Symptom**: `ERROR: ... port 9092 already allocated`

**Solution**:
```powershell
# Check what's using port 9092
netstat -ano | findstr :9092

# Kill process (replace PID with actual number)
taskkill /PID <PID> /F

# Restart Docker
docker compose -f infra/docker-compose.yml down
docker compose -f infra/docker-compose.yml up -d
```

---

### Issue 3: Kafka not healthy after 2 minutes

**Symptom**: `docker compose ps` shows "unhealthy" or "starting"

**Solution**:
```powershell
# Check Kafka logs
docker logs kafka --tail 100

# Look for errors like:
# - "out of memory" → Increase Docker memory to 6GB
# - "disk full" → Free up disk space
# - "port conflict" → Check port 9092 not in use

# If error is unclear, restart with clean state
docker compose -f infra/docker-compose.yml down -v
docker compose -f infra/docker-compose.yml up -d
```

---

### Issue 4: ClickHouse connection timeout

**Symptom**: Flink job logs show `Connection timeout` or `Connection refused`

**Solution**:
```powershell
# 1. Check ClickHouse service is running (not sleeping)
# Login to https://clickhouse.cloud → Resume service if idle

# 2. Test connection from local machine
curl "https://YOUR_HOST.aws.clickhouse.cloud:8443/?query=SELECT+1" `
  -u "default:YOUR_PASSWORD"

# Expected: 1

# 3. Check firewall/antivirus not blocking HTTPS
# Try from different network (mobile hotspot) to isolate issue
```

---

### Issue 5: Avro deserialization error

**Symptom**: `Unknown magic byte` or `Could not find schema`

**Solution**:
```bash
# 1. Verify Schema Registry is accessible
curl http://localhost:8081/subjects

# 2. Check schema is registered
curl http://localhost:8081/subjects/car-telemetry-events-value/versions

# 3. Re-register schema
bash scripts/register_schemas.sh

# 4. Restart Flink job
# Ctrl+C in Terminal 2, then restart with Git Bash
cd /c/Users/ADMIN/STREAMING/Kafka-Flink-ClickHouse-Pipeline
bash scripts/run_flink.sh
```

---

### Issue 6: Dataset download fails

**Symptom**: `fetch_f1_session.sh` errors or incomplete download

**Solution**:
```bash
# 1. Check internet connection
curl -I https://api.github.com

# 2. Check rate limit
curl https://api.github.com/rate_limit

# If rate limited (60 requests/hour):
# - Wait 1 hour
# - OR authenticate with GitHub token:
export GITHUB_TOKEN=ghp_your_token_here
bash scripts/fetch_f1_session.sh

# 3. If partial download, delete and retry
rm -rf data-generators/f1-replay-engine/src/main/resources/datasets
bash scripts/fetch_f1_session.sh
```

---

### Issue 7: Grafana shows "No data"

**Symptom**: Dashboard panels show "No data" despite ClickHouse having data

**Solution**:
```sql
-- 1. Check time range in ClickHouse
SELECT min(event_time), max(event_time) 
FROM f1_telemetry.raw_telemetry;

-- 2. In Grafana dashboard:
-- - Click time picker (top right)
-- - Set to "Last 24 hours" or custom range covering your data
-- - Click "Refresh dashboard"

-- 3. Check query in panel edit mode
-- - Click panel title → Edit
-- - Check SQL query
-- - Click "Run query" button
-- - Should see data in table preview
```

---

### Issue 8: Flink job stops with OutOfMemoryError

**Symptom**: Flink job crashes with `java.lang.OutOfMemoryError`

**Solution**:
```bash
# Increase JVM heap when running Flink
export MAVEN_OPTS="-Xms1G -Xmx2G"
cd /c/Users/ADMIN/STREAMING/Kafka-Flink-ClickHouse-Pipeline
bash scripts/run_flink.sh
```

---

### Issue 9: Replay Engine runs too fast/slow

**Symptom**: Data ingestion rate not matching expected throughput

**Solution**:
```bash
# Adjust replay speed in .env
code .env || notepad .env

# Change this line:
REPLAY_SPEED_FACTOR=5   # Try 1 (realtime), 10, or 20

# Restart Replay Engine
# Ctrl+C in Terminal 1, then:
bash scripts/replay.sh
```

---

### Issue 10: Python script errors in register_schemas.sh

**Symptom**: `python3: command not found` or encoding errors

**Solution**:
```bash
# Try different Python commands
which python
which python3
which py

# Update script to use available command
# Edit scripts/register_schemas.sh
# Replace 'python3' with 'python' or 'py'
```

---

## 📞 GET HELP

### Community Resources
- **ClickHouse**: https://clickhouse.com/docs
- **Grafana**: https://grafana.com/docs
- **Apache Flink**: https://flink.apache.org/docs
- **Kafka**: https://kafka.apache.org/documentation

### Project Issues
- Check existing issues in repo: `docs/IMPLEMENTATION_GUIDE.md`
- Review troubleshooting: `README.md`

---

## 🎓 COMPLETION CHECKLIST

After completing all phases, you should have:

### ✅ Infrastructure Running
- [ ] Docker containers healthy (Kafka + Schema Registry)
- [ ] ClickHouse Cloud service running
- [ ] Grafana Cloud dashboard accessible

### ✅ Data Pipeline
- [ ] Dataset downloaded (~150MB, 400+ files)
- [ ] Replay Engine publishing messages
- [ ] Flink job consuming and processing
- [ ] ClickHouse tables receiving data

### ✅ Monitoring
- [ ] Grafana dashboard showing live data
- [ ] Queries returning results
- [ ] No errors in Flink/Replay logs

### 🎉 SUCCESS CRITERIA
**When you see**:
1. Replay Engine: `Published 10000 events | driver=1 speed=312km/h`
2. Flink Job: `Flushed 1000 raw records`
3. ClickHouse: `SELECT count() > 5000 FROM raw_telemetry`
4. Grafana: Live speed gauge updating every 5 seconds

**YOU'RE DONE! 🚀**

---

## ⏱️ TIME SUMMARY

| Phase | Task | Time |
|-------|------|------|
| 1 | Prerequisites Installation | 30 mins |
| 2 | Local Dev Tools | 15 mins |
| 3 | ClickHouse Cloud Setup | 15 mins |
| 4 | Grafana Cloud Setup | 15 mins |
| 5 | Local Infrastructure (Docker) | 15 mins |
| 6 | Project Build & Config | 20 mins |
| 7 | Data Preparation | 25 mins |
| 8 | Pipeline Deployment | 10 mins |
| 9 | Validation & Testing | 20 mins |
| **Total** | | **2.5-3 hours** |

---

**Guide Created**: 2026-07-17  
**Version**: 1.0  
**Author**: Kiro AI Assistant  
**Status**: ✅ Ready for Production Use
