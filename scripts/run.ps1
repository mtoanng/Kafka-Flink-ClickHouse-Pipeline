# =============================================================
#  VES Run Script (Windows PowerShell)
#
#  Usage:
#    .\scripts\run.ps1            # Lite profile
#    .\scripts\run.ps1 -Bi        # Lite + Metabase
#    .\scripts\run.ps1 -Wait      # Đợi tất cả service healthy
#    .\scripts\run.ps1 -Bi -Wait  # cả 2
#
#  Memory budget Lite:  ~2.7 GB total
#  Memory budget +BI:   ~3.5 GB total
# =============================================================
param(
    [switch]$Bi,
    [switch]$Wait
)
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

Write-Host "============================================="
Write-Host "  VES Fuel Price Monitor — start (Lite Profile)"
Write-Host "============================================="

# 1. Copy .env nếu chưa tồn tại
if (-not (Test-Path ".env")) {
    Write-Host "[INFO] .env chưa tồn tại, copy từ .env.example..."
    Copy-Item ".env.example" ".env"
}

# 2. Build compose command
$ComposeFiles = @("-f", "infra/docker-compose.yml")
if ($Bi) {
    $ComposeFiles += @("-f", "infra/docker-compose.bi.yml")
    Write-Host "[INFO] Bật BI overlay (Metabase)..."
}

# 3. Khởi động Docker stack
Write-Host "[INFO] Đang khởi động Docker Compose..."
& docker compose @ComposeFiles --env-file .env up -d
if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAIL] docker compose up gặp lỗi." -ForegroundColor Red
    exit $LASTEXITCODE
}

# 4. Đợi healthy nếu -Wait
if ($Wait) {
    Write-Host "[INFO] Đợi tất cả service healthy (tối đa 180s)..."
    $healthy = $false
    for ($i = 1; $i -le 36; $i++) {
        $running = & docker ps --filter "label=com.docker.compose.project=infra" --filter "health=healthy" --format "{{.Names}}" 2>$null
        $expected = if ($Bi) { 6 } else { 5 }
        if (($running | Measure-Object -Line).Lines -ge $expected) {
            Write-Host "[OK] Tất cả service healthy sau $($i * 5)s"
            $healthy = $true
            break
        }
        Start-Sleep -Seconds 5
    }
    if (-not $healthy) {
        Write-Host "[WARN] Timeout 180s. Một số service có thể chưa ready." -ForegroundColor Yellow
    }
}

# 5. Pre-create Kafka topics (idempotent)
Write-Host "[INFO] Đảm bảo Kafka topics cho 4 pillar..."
& powershell -ExecutionPolicy Bypass -File "scripts/create_kafka_topics.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "[WARN] Không tạo được topic tự động. Chạy 'scripts/create_kafka_topics.ps1' sau khi Kafka ready." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[OK] Stack đã khởi động."
Write-Host ""
Write-Host "URLs:"
Write-Host "  - Flink UI    : http://localhost:8081"
if ($Bi) { Write-Host "  - Metabase    : http://localhost:3000" }
Write-Host "  - PostgreSQL  : localhost:5432  (user=postgres, db=fuel_prices)"
Write-Host "  - Kafka       : localhost:9092"
Write-Host ""
Write-Host "Tiếp theo:"
Write-Host "  .\scripts\healthcheck.ps1    # verify stack health"
Write-Host "  .\scripts\stop.ps1           # dừng stack (giữ data)"
Write-Host "  .\scripts\stop.ps1 -Volumes  # dừng + xóa data"
