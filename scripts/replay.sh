#!/usr/bin/env bash
# replay.sh — Run the F1 replay engine (publishes telemetry to Kafka)
# Usage: bash scripts/replay.sh
set -euo pipefail

cd "$(dirname "$0")/.."

DATASET="data-generators/f1-replay-engine/src/main/resources/datasets"
if [ ! -d "$DATASET" ]; then
  echo "ERROR: Dataset not found. Run: bash scripts/fetch_f1_session.sh"
  exit 1
fi

echo "Starting F1 Replay Engine..."
echo "  Kafka: ${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
echo "  Speed: ${REPLAY_SPEED_FACTOR:-5}x"
echo ""

mvn -q -pl data-generators/f1-replay-engine exec:java \
  -Dexec.mainClass=com.f1telemetry.F1ReplayEngine
