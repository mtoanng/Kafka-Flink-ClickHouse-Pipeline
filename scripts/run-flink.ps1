# =============================================================
#  run-flink.ps1 - Start Flink Job with env vars loaded
#  Usage: .\scripts\run-flink.ps1
# =============================================================

Write-Host "⚡ Starting Flink Telemetry Job..." -ForegroundColor Cyan

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
Write-Host "🚀 Starting Flink Job..." -ForegroundColor Cyan
Write-Host "   (Press Ctrl+C to stop)" -ForegroundColor Gray
Write-Host ""

# Run Maven in flink job directory
Set-Location flink-jobs\f1-telemetry-job
mvn exec:java

# Return to original directory
Set-Location ..\..
