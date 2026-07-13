# run.ps1 — Start local infrastructure (PowerShell)
# Usage: powershell -File scripts\run.ps1
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

Write-Host "=== F1 Telemetry: Starting Kafka KRaft + Schema Registry ===" -ForegroundColor Cyan

if (-not (Test-Path ".env")) {
    Write-Host "Copying .env.example -> .env" -ForegroundColor Yellow
    Copy-Item ".env.example" ".env"
}

& docker compose -f infra/docker-compose.yml --env-file .env up -d
if ($LASTEXITCODE -ne 0) { Write-Host "docker compose up failed" -ForegroundColor Red; exit 1 }

Write-Host ""
Write-Host "Waiting for services (max 60s)..." -ForegroundColor Cyan
$ready = $false
for ($i = 1; $i -le 12; $i++) {
    $kafkaOk = $true; $srOk = $true
    try { docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 2>$null | Out-Null } catch { $kafkaOk = $false }
    try { $r = Invoke-WebRequest -Uri "http://localhost:8081/subjects" -UseBasicParsing -TimeoutSec 3; if ($r.StatusCode -ne 200) { $srOk = $false } } catch { $srOk = $false }
    if ($kafkaOk -and $srOk) { Write-Host "  Ready after $($i * 5)s" -ForegroundColor Green; $ready = $true; break }
    Start-Sleep -Seconds 5
}
if (-not $ready) { Write-Host "  [WARN] Timeout — check: docker compose -f infra/docker-compose.yml ps" -ForegroundColor Yellow }

Write-Host ""
Write-Host "Creating topic + registering schema..." -ForegroundColor Cyan
& bash scripts/create_topic.sh
& bash scripts/register_schemas.sh

Write-Host ""
Write-Host "=== Ready ===" -ForegroundColor Green
Write-Host "  Kafka:           localhost:9092"
Write-Host "  Schema Registry: localhost:8081"
Write-Host ""
Write-Host "Next: bash scripts/fetch_f1_session.sh  |  bash scripts/replay.sh  |  bash scripts/run_flink.sh"
