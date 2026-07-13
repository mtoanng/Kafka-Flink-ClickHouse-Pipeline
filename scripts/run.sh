#!/usr/bin/env bash
# run.sh — Start local infrastructure (Kafka KRaft + Schema Registry)
# Usage: bash scripts/run.sh
set -euo pipefail

cd "$(dirname "$0")/.."

echo "Starting Kafka KRaft + Schema Registry..."
docker compose -f infra/docker-compose.yml up -d

echo ""
echo "Waiting for services to be healthy (max 60s)..."
for i in $(seq 1 12); do
  if docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >/dev/null 2>&1 \
     && curl -sf http://localhost:8081/subjects >/dev/null 2>&1; then
    echo "  Services ready after $((i * 5))s"
    break
  fi
  sleep 5
  if [ "$i" = "12" ]; then
    echo "  [WARN] Timeout — check: docker compose -f infra/docker-compose.yml ps"
  fi
done

echo ""
echo "Creating Kafka topic..."
bash scripts/create_topic.sh || echo "  [WARN] topic creation skipped"

echo ""
echo "Registering Avro schema..."
bash scripts/register_schemas.sh || echo "  [WARN] schema registration skipped"

echo ""
echo "=== Infrastructure ready ==="
echo "  Kafka:           localhost:9092"
echo "  Schema Registry: localhost:8081"
echo ""
echo "Next:"
echo "  bash scripts/fetch_f1_session.sh   # download data"
echo "  bash scripts/replay.sh             # publish to Kafka"
echo "  bash scripts/run_flink.sh          # start Flink job"
