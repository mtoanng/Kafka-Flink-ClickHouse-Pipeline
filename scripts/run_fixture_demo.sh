#!/usr/bin/env bash
# Run only on the disposable integration host with Kafka, Flink, and ClickHouse.
set -euo pipefail

cd "$(dirname "$0")/.."

: "${CLICKHOUSE_ENDPOINT:?set CLICKHOUSE_ENDPOINT before the remote smoke test}"
: "${CLICKHOUSE_PASSWORD:?set CLICKHOUSE_PASSWORD before the remote smoke test}"

RUN_ID="${REPLAY_RUN_ID:-fixture-$(date -u +%Y%m%dT%H%M%SZ)}"
export REPLAY_RUN_ID="$RUN_ID"
export KAFKA_CONSUMER_GROUP="${KAFKA_CONSUMER_GROUP:-taobao-fixture-${RUN_ID}}"

PYTHONPATH=producer/src python scripts/apply_clickhouse_schema.py
bash scripts/apply_cassandra_schema.sh
FLINK_DETACHED=true bash scripts/run_flink.sh
sleep "${FLINK_STARTUP_WAIT_SECONDS:-10}"
bash scripts/replay.sh
bash scripts/verify_bounded_pipeline.sh "$RUN_ID"

echo "Bounded core smoke completed for run ${RUN_ID}."
