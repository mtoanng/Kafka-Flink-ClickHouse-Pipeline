#!/usr/bin/env bash
set -euo pipefail

: "${KAFKA_BOOTSTRAP_SERVERS:?KAFKA_BOOTSTRAP_SERVERS is required}"
KAFKA_TOPICS_BIN="${KAFKA_TOPICS_BIN:-kafka-topics.sh}"
RULES_TOPIC="${RULES_KAFKA_TOPIC:-behavior-rules}"

"$KAFKA_TOPICS_BIN" \
  --bootstrap-server "$KAFKA_BOOTSTRAP_SERVERS" \
  --create --if-not-exists \
  --topic "$RULES_TOPIC" \
  --partitions "${RULES_TOPIC_PARTITIONS:-1}" \
  --replication-factor "${RULES_TOPIC_REPLICATION_FACTOR:-1}" \
  --config cleanup.policy=compact \
  --config min.compaction.lag.ms=0
