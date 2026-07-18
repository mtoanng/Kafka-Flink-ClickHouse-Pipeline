# =============================================================
#  build-and-run.ps1 - Complete pipeline: clean, build, run
#  Usage: .\scripts\build-and-run.ps1
# =============================================================

$ErrorActionPreference = "Stop"

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "  F1 Telemetry Pipeline - Build & Run" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""

# Check Maven
Write-Host "🔍 Checking Maven installation..." -ForegroundColor Yellow
$mvnPath = Get-Command mvn -ErrorAction SilentlyContinue

if (-not $mvnPath) {
    Write-Host "❌ ERROR: Maven not found in PATH!" -ForegroundColor Red
    Write-Host "   Please install Maven: https://maven.apache.org/download.cgi" -ForegroundColor Red
    Write-Host "   Or add Maven to PATH" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Maven found: $($mvnPath.Source)" -ForegroundColor Green
Write-Host ""

# Load .env file
Write-Host "📥 Loading environment variables from .env..." -ForegroundColor Yellow
if (Test-Path ".env") {
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -ne "" -and -not $line.StartsWith("#") -and $line -match '^([^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim() -replace '^["'']|["'']$', ''
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "  ✅ $key = $value" -ForegroundColor Green
        }
    }
} else {
    Write-Host "⚠️  Warning: .env file not found" -ForegroundColor Yellow
}
Write-Host ""

# Clean old builds
Write-Host "🧹 Cleaning old builds..." -ForegroundColor Yellow
mvn clean -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven clean failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Clean complete" -ForegroundColor Green
Write-Host ""

# Build all modules
Write-Host "🔨 Building all modules - this may take a few minutes..." -ForegroundColor Yellow
mvn package -DskipTests -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Maven build failed!" -ForegroundColor Red
    Write-Host "   Try running: mvn package -X" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Build complete" -ForegroundColor Green
Write-Host ""

# Run Flink job
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host "🚀 Starting Flink Telemetry Job..." -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Pipeline:" -ForegroundColor Yellow
Write-Host "   Kafka ($env:KAFKA_BOOTSTRAP_SERVERS)" -ForegroundColor Gray
Write-Host "     ↓" -ForegroundColor Gray
Write-Host "   Flink DataStream (watermark 3s)" -ForegroundColor Gray
Write-Host "     ├→ raw_telemetry (ClickHouse $env:CLICKHOUSE_HOST)" -ForegroundColor Gray
Write-Host "     └→ rollup_10s (window 10s aggregate)" -ForegroundColor Gray
Write-Host ""
Write-Host "Press Ctrl+C to stop..." -ForegroundColor Yellow
Write-Host ""

# Run Flink job
Set-Location flink-jobs\f1-telemetry-job
mvn exec:java
$exitCode = $LASTEXITCODE
Set-Location ..\..

if ($exitCode -ne 0) {
    Write-Host ""
    Write-Host "❌ Flink job exited with error code: $exitCode" -ForegroundColor Red
    exit $exitCode
}

Write-Host ""
Write-Host "✅ Flink job completed successfully" -ForegroundColor Green
