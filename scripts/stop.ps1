# =============================================================
#  VES Stop Script (Windows PowerShell)
#  Usage: .\scripts\stop.ps1 [-Volumes]
#    -Volumes : xóa cả volume (data sẽ mất)
# =============================================================
param([switch]$Volumes)
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

if ($Volumes) {
    Write-Host "[WARN] Đang dừng stack VÀ xóa volume (data sẽ mất)..."
    docker compose -f infra/docker-compose.yml down -v
} else {
    Write-Host "[INFO] Đang dừng stack (data được giữ trong volume)..."
    docker compose -f infra/docker-compose.yml down
}
Write-Host "[OK] Stack đã dừng."
