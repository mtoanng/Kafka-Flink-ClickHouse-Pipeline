# =============================================================
#  load-env.ps1 - Load .env file into PowerShell session
#  Usage: . .\scripts\load-env.ps1
#  (Note the dot and space before the path - this "sources" the script)
# =============================================================

param(
    [string]$EnvFile = ".env"
)

if (-Not (Test-Path $EnvFile)) {
    Write-Host "❌ Error: $EnvFile not found!" -ForegroundColor Red
    Write-Host "Copy .env.example to .env first:" -ForegroundColor Yellow
    Write-Host "  cp .env.example .env" -ForegroundColor Yellow
    exit 1
}

Write-Host "📥 Loading environment variables from $EnvFile..." -ForegroundColor Cyan

Get-Content $EnvFile | ForEach-Object {
    $line = $_.Trim()
    
    # Skip empty lines and comments
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    
    # Parse KEY=VALUE
    if ($line -match '^([^=]+)=(.*)$') {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        
        # Remove quotes if present
        $value = $value -replace '^["'']|["'']$', ''
        
        # Set environment variable for current process
        [Environment]::SetEnvironmentVariable($key, $value, "Process")
        
        Write-Host "  ✅ $key = $value" -ForegroundColor Green
    }
}

Write-Host ""
Write-Host "✅ Environment variables loaded successfully!" -ForegroundColor Green
Write-Host "🚀 Now you can run:" -ForegroundColor Cyan
Write-Host "   mvn exec:java" -ForegroundColor White
