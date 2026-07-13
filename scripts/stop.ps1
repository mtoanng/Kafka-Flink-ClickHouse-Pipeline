# stop.ps1 — Stop local infrastructure (PowerShell)
# Usage: powershell -File scripts\stop.ps1 [-Volumes]
param([switch]$Volumes)
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

if ($Volumes) {
    Write-Host "Stopping + wiping volumes..." -ForegroundColor Yellow
    & docker compose -f infra/docker-compose.yml down -v
} else {
    Write-Host "Stopping (volumes preserved)..." -ForegroundColor Cyan
    & docker compose -f infra/docker-compose.yml down
}
Write-Host "Stopped." -ForegroundColor Green
