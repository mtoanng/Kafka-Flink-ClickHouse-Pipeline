# ==============================================================================
# build-and-run.ps1
# Complete pipeline: clean, build, and run the Flink telemetry job
#
# Usage:
#   .\scripts\build-and-run.ps1
# ==============================================================================

$ErrorActionPreference = "Stop"

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "F1 Telemetry Pipeline - Build & Run" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

# ------------------------------------------------------------------------------
# Check if we are in the project root
# ------------------------------------------------------------------------------

if (-not (Test-Path "pom.xml")) {
    Write-Host "[ERROR] pom.xml not found." -ForegroundColor Red
    Write-Host "Please run this script from the project root directory." -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------------------------
# Check Maven
# ------------------------------------------------------------------------------

Write-Host "[INFO] Checking Maven installation..." -ForegroundColor Yellow

$mvnPath = Get-Command mvn -ErrorAction SilentlyContinue

if (-not $mvnPath) {
    Write-Host "[ERROR] Maven not found in PATH." -ForegroundColor Red
    Write-Host "Install Maven and add it to your PATH." -ForegroundColor Red
    Write-Host "https://maven.apache.org/download.cgi"
    exit 1
}

Write-Host "[OK] Maven found: $($mvnPath.Source)" -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------------------------
# Check Java
# ------------------------------------------------------------------------------

Write-Host "[INFO] Checking Java installation..." -ForegroundColor Yellow

$javaPath = Get-Command java -ErrorAction SilentlyContinue

if (-not $javaPath) {
    Write-Host "[ERROR] Java not found in PATH." -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Java found: $($javaPath.Source)" -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------------------------
# Load environment variables from .env
# ------------------------------------------------------------------------------

Write-Host "[INFO] Loading environment variables from .env..." -ForegroundColor Yellow

if (Test-Path ".env") {

    Get-Content ".env" | ForEach-Object {

        $line = $_.Trim()

        if (
            $line -ne "" -and
            -not $line.StartsWith("#") -and
            $line -match '^([^=]+)=(.*)$'
        ) {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()

            # Remove optional surrounding quotes
            $value = $value -replace '^["'']|["'']$', ''

            [Environment]::SetEnvironmentVariable(
                $key,
                $value,
                "Process"
            )

            Write-Host "  [OK] $key = $value" -ForegroundColor Green
        }
    }

}
else {
    Write-Host "[WARNING] .env file not found." -ForegroundColor Yellow
}

Write-Host ""

# ------------------------------------------------------------------------------
# Clean old builds
# ------------------------------------------------------------------------------

Write-Host "[INFO] Cleaning old builds..." -ForegroundColor Yellow

mvn clean

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Maven clean failed." -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Clean complete." -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------------------------
# Build all modules
# ------------------------------------------------------------------------------

Write-Host "[INFO] Building all modules..." -ForegroundColor Yellow
Write-Host "This may take a few minutes." -ForegroundColor Yellow

mvn package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Maven build failed." -ForegroundColor Red
    Write-Host "You can try: mvn package -X"
    exit 1
}

Write-Host "[OK] Build complete." -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------------------------
# Verify Flink job directory
# ------------------------------------------------------------------------------

$flinkJobDir = "flink-jobs\f1-telemetry-job"

if (-not (Test-Path $flinkJobDir)) {
    Write-Host "[ERROR] Flink job directory not found:" -ForegroundColor Red
    Write-Host "        $flinkJobDir" -ForegroundColor Red
    exit 1
}

# ------------------------------------------------------------------------------
# Display pipeline information
# ------------------------------------------------------------------------------

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "Starting Flink Telemetry Job" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Pipeline:" -ForegroundColor Yellow
Write-Host "  Kafka ($env:KAFKA_BOOTSTRAP_SERVERS)" -ForegroundColor Gray
Write-Host "      |" -ForegroundColor Gray
Write-Host "      v" -ForegroundColor Gray
Write-Host "  Flink DataStream (3-second watermark)" -ForegroundColor Gray
Write-Host "      |" -ForegroundColor Gray
Write-Host "      +--> raw_telemetry" -ForegroundColor Gray
Write-Host "      |" -ForegroundColor Gray
Write-Host "      +--> rollup_10s (10-second aggregate)" -ForegroundColor Gray
Write-Host ""
Write-Host "ClickHouse Host : $env:CLICKHOUSE_HOST" -ForegroundColor Gray
Write-Host "ClickHouse Port : $env:CLICKHOUSE_PORT" -ForegroundColor Gray
Write-Host ""
Write-Host "Press Ctrl + C to stop the job."
Write-Host ""

# ------------------------------------------------------------------------------
# Run Flink job
# ------------------------------------------------------------------------------

$jarPath = Join-Path $flinkJobDir "target\f1-telemetry-job-1.0.0-SNAPSHOT.jar"

if (-not (Test-Path $jarPath)) {
    Write-Host "[ERROR] Flink job jar not found:" -ForegroundColor Red
    Write-Host "        $jarPath" -ForegroundColor Red
    exit 1
}

java -jar $jarPath

$exitCode = $LASTEXITCODE

# ------------------------------------------------------------------------------
# Exit status
# ------------------------------------------------------------------------------

if ($exitCode -ne 0) {

    Write-Host ""
    Write-Host "[ERROR] Flink job exited with code: $exitCode" -ForegroundColor Red

    exit $exitCode
}

Write-Host ""
Write-Host "[OK] Flink job completed successfully." -ForegroundColor Green