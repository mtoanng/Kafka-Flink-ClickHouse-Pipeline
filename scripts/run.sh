#!/usr/bin/env bash
# Start bounded Kafka and Schema Registry services on the integration host.
# Usage: bash scripts/run.sh
set -euo pipefail

cd "$(dirname "$0")/.."

echo "Starting bounded Kafka KRaft + Schema Registry services..."
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
echo "Next on this disposable integration host:"
echo "  pip install -e '.[kafka]'"
echo "  bash scripts/replay.sh"
echo "  bash scripts/run_flink.sh"
