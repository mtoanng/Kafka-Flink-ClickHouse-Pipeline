#!/usr/bin/env bash
# create_topic.sh — Create Kafka topic (run inside kafka container)
# Usage: bash scripts/create_topic.sh
set -euo pipefail

docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic car-telemetry-events \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

echo "Topic created. Verify:"
docker exec kafka kafka-topics --describe --bootstrap-server localhost:9092 --topic car-telemetry-events
