# =============================================================
#  VES Healthcheck (Windows PowerShell)
#  Usage: .\scripts\healthcheck.ps1
#  Exit 0 nếu tất cả healthy, 1 nếu có service fail.
# =============================================================
$ErrorActionPreference = "Continue"

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

# Load .env if exists
$envFile = ".env"
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        if ($_ -match '^\s*([^#=]+)\s*=\s*(.*?)\s*$') {
            Set-Item -Path "env:$($matches[1])" -Value $matches[2] -ErrorAction SilentlyContinue
        }
    }
}

$PostgresPort = if ($env:POSTGRES_PORT) { $env:POSTGRES_PORT } else { "5432" }
$KafkaPort    = if ($env:KAFKA_HOST_PORT) { $env:KAFKA_HOST_PORT } else { "9092" }
$FlinkPort    = if ($env:FLINK_UI_PORT) { $env:FLINK_UI_PORT } else { "8081" }
$MetabasePort = if ($env:METABASE_PORT) { $env:METABASE_PORT } else { "3000" }

$Pass = 0
$Fail = 0

function Check-Service {
    param([string]$Name, [scriptblock]$Cmd)
    Write-Host -NoNewline ("  {0,-25} ... " -f $Name)
    try {
        $null = & $Cmd 2>$null
        if ($LASTEXITCODE -eq 0) {
            Write-Host "OK" -ForegroundColor Green
            $script:Pass++
        } else {
            Write-Host "FAIL" -ForegroundColor Red
            $script:Fail++
        }
    } catch {
        Write-Host "FAIL" -ForegroundColor Red
        $script:Fail++
    }
}

Write-Host "============================================="
Write-Host "  VES Healthcheck"
Write-Host "============================================="

Check-Service "PostgreSQL" { docker exec postgres-database pg_isready -U postgres -d fuel_prices }
Check-Service "Kafka"      { docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 }
Check-Service "Zookeeper"  { docker exec zookeeper zookeeper-shell localhost:2181 ls / }
Check-Service "Flink JobManager" {
    $r = Invoke-WebRequest -Uri "http://localhost:$FlinkPort/overview" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
    if ($r.StatusCode -ne 200) { $global:LASTEXITCODE = 1 } else { $global:LASTEXITCODE = 0 }
}

# Metabase optional (chỉ check nếu container chạy)
$metabaseRunning = docker ps --filter "name=metabase" --format "{{.Names}}" 2>$null
if ($metabaseRunning -match "metabase") {
    Check-Service "Metabase" {
        $r = Invoke-WebRequest -Uri "http://localhost:$MetabasePort/api/health" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($r.StatusCode -ne 200) { $global:LASTEXITCODE = 1 } else { $global:LASTEXITCODE = 0 }
    }
}

Write-Host ""
Write-Host "Tổng: $Pass OK, $Fail FAIL"
exit $Fail
