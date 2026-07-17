# =============================================================
#  run-replay.ps1 - Start F1 Replay Engine with env vars loaded
#  Usage: .\scripts\run-replay.ps1
# =============================================================

Write-Host "🏎️  Starting F1 Replay Engine..." -ForegroundColor Cyan

# Load .env file
if (Test-Path ".env") {
    Write-Host "📥 Loading .env file..." -ForegroundColor Yellow
    
    Get-Content .env | ForEach-Object {
        $line = $_.Trim()
        if ($line -ne "" -and -not $line.StartsWith("#") -and $line -match '^([^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim() -replace '^["'']|["'']$', ''
            [Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "  ✅ $key" -ForegroundColor Green
        }
    }
} else {
    Write-Host "⚠️  Warning: .env file not found, using defaults" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🚀 Starting Replay Engine..." -ForegroundColor Cyan
Write-Host "   (Press Ctrl+C to stop)" -ForegroundColor Gray
Write-Host ""

# Run Maven in replay engine directory
Set-Location data-generators\f1-replay-engine
mvn exec:java

# Return to original directory
Set-Location ..\..
