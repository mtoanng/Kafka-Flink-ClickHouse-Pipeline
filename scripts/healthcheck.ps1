# healthcheck.ps1 — Verify Kafka + Schema Registry (PowerShell)
$ErrorActionPreference = "Continue"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$Pass = 0; $Fail = 0

function Check($name, $cmd) {
    Write-Host -NoNewline ("  {0,-25} ... " -f $name)
    try {
        & $cmd | Out-Null 2>&1
        if ($LASTEXITCODE -eq 0 -or $?) { Write-Host "OK" -ForegroundColor Green; $Pass++ }
        else { Write-Host "FAIL" -ForegroundColor Red; $Fail++ }
    } catch { Write-Host "FAIL" -ForegroundColor Red; $Fail++ }
}

Write-Host "=== F1 Telemetry Healthcheck ===" -ForegroundColor Cyan
Check "Kafka" { docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 }
Check "Schema Registry" {
    $r = Invoke-WebRequest -Uri "http://localhost:8081/subjects" -UseBasicParsing -TimeoutSec 3
    if ($r.StatusCode -ne 200) { throw "non-200" }
}
Write-Host ""
Write-Host "Total: $Pass OK, $Fail FAIL"
exit $Fail
