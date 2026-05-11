# =============================================================
#  VES Stop Script (Windows PowerShell)
#
#  Usage:
#    .\scripts\stop.ps1             # Dừng stack, giữ volume
#    .\scripts\stop.ps1 -Volumes    # Dừng + xóa volume (data sẽ mất)
# =============================================================
param([switch]$Volumes)
$ErrorActionPreference = "Stop"

$RepoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $RepoRoot

# Auto-detect: nếu Metabase đang chạy → cũng dừng BI overlay
$ComposeFiles = @("-f", "infra/docker-compose.yml")
$metabaseRunning = & docker ps --filter "name=metabase" --format "{{.Names}}" 2>$null
if ($metabaseRunning -match "metabase") {
    $ComposeFiles += @("-f", "infra/docker-compose.bi.yml")
}

if ($Volumes) {
    Write-Host "[WARN] Đang dừng stack VÀ xóa volume (data sẽ mất)..."
    & docker compose @ComposeFiles down -v
} else {
    Write-Host "[INFO] Đang dừng stack (data được giữ trong volume)..."
    & docker compose @ComposeFiles down
}
Write-Host "[OK] Stack đã dừng."
