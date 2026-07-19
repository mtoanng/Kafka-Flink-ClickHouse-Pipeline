#!/usr/bin/env bash
# Publish the deterministic Taobao fixture to Kafka with Avro.
# Usage: bash scripts/replay.sh
set -euo pipefail

cd "$(dirname "$0")/.."

echo "Starting Taobao Replay Engine..."
echo "  Kafka: ${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
echo "  Topic: ${KAFKA_TOPIC:-user-behavior-events}"
echo "  Speed: ${REPLAY_SPEED_FACTOR:-0}x"
echo ""

mkdir -p artifacts
PYTHONPATH=producer/src python -m taobao_replay publish \
  tests/fixtures/user_behavior_fixture.csv \
  --run-id "${REPLAY_RUN_ID:-fixture-run}" \
  --speed "${REPLAY_SPEED_FACTOR:-0}" \
  --invalid-output "${INVALID_OUTPUT:-artifacts/fixture-invalid.jsonl}" \
  --force
