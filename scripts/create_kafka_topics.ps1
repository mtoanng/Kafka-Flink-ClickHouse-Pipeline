# =============================================================
#  Pre-create Kafka topics dùng cho VES-Monitor (Windows PowerShell)
# =============================================================
$ErrorActionPreference = "Stop"

$KafkaContainer = if ($env:KAFKA_CONTAINER) { $env:KAFKA_CONTAINER } else { "kafka" }
$Partitions     = if ($env:KAFKA_TOPIC_PARTITIONS) { $env:KAFKA_TOPIC_PARTITIONS } else { 3 }
$Replication    = if ($env:KAFKA_TOPIC_REPLICATION) { $env:KAFKA_TOPIC_REPLICATION } else { 1 }

$Topics = @(
    "fuel-prices",        # Pillar 2
    "grid-load",          # Pillar 3
    "renewable-output",   # Pillar 4
    "emission"            # Pillar 4
)

Write-Host "[INFO] Pre-create topics on container=$KafkaContainer (partitions=$Partitions, rf=$Replication)"

# Đợi Kafka healthy
$ready = $false
for ($i = 1; $i -le 12; $i++) {
    & docker exec $KafkaContainer kafka-topics --bootstrap-server localhost:9092 --list *> $null
    if ($LASTEXITCODE -eq 0) { $ready = $true; break }
    Start-Sleep -Seconds 5
}
if (-not $ready) {
    Write-Host "[FAIL] Kafka container chưa sẵn sàng sau 60s." -ForegroundColor Red
    exit 1
}

foreach ($t in $Topics) {
    & docker exec $KafkaContainer kafka-topics `
        --bootstrap-server localhost:9092 `
        --create --if-not-exists `
        --topic $t `
        --partitions $Partitions `
        --replication-factor $Replication *> $null
    Write-Host "  ✓ topic $t"
}

Write-Host "[OK] Tất cả $($Topics.Length) topic sẵn sàng."
