#!/usr/bin/env bash
# Create the Phase 3 Kafka topic inside the integration broker container.
# Usage: bash scripts/create_topic.sh
set -euo pipefail

cd "$(dirname "$0")/.."

docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic user-behavior-events \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

echo "Topic created. Verify:"
docker exec kafka kafka-topics --describe --bootstrap-server localhost:9092 --topic user-behavior-events
