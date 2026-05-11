# =============================================================
#  VES Run Script (Windows PowerShell)
#  Usage: .\scripts\run.ps1
#  Phase 0 skeleton — sẽ mở rộng ở Phase 1
# =============================================================
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

Write-Host "============================================="
Write-Host "  VES Fuel Price Monitor — start"
Write-Host "============================================="

# 1. Copy .env nếu chưa tồn tại
if (-not (Test-Path ".env")) {
    Write-Host "[INFO] .env chưa tồn tại, copy từ .env.example..."
    Copy-Item ".env.example" ".env"
}

# 2. Khởi động Docker stack
Write-Host "[INFO] Đang khởi động Docker Compose..."
docker compose -f infra/docker-compose.yml --env-file .env up -d

Write-Host ""
Write-Host "[OK] Stack đang khởi động. Đợi ~60s để các service healthy..."
Write-Host "     Kiểm tra trạng thái: docker compose -f infra/docker-compose.yml ps"
Write-Host ""
Write-Host "URLs sau khi start xong:"
Write-Host "  - Flink UI    : http://localhost:8081"
Write-Host "  - Metabase    : http://localhost:3000"
Write-Host "  - PostgreSQL  : localhost:5432  (user=postgres, db=fuel_prices)"
Write-Host "  - Kafka       : localhost:9092"
