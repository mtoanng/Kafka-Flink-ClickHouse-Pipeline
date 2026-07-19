#!/usr/bin/env bash
# Prepare a remote checkpoint/restart experiment without triggering a failure.
set -euo pipefail

cd "$(dirname "$0")/.."

: "${FLINK_CHECKPOINT_DIR:?set FLINK_CHECKPOINT_DIR to durable remote storage}"
: "${FLINK_BIN:=flink}"

export FLINK_CHECKPOINTING_ENABLED=true
export FLINK_CHECKPOINT_INTERVAL_MS="${FLINK_CHECKPOINT_INTERVAL_MS:-60000}"

echo "Checkpoint policy prepared:"
echo "  storage: ${FLINK_CHECKPOINT_DIR}"
echo "  interval_ms: ${FLINK_CHECKPOINT_INTERVAL_MS}"
echo ""
echo "Next, on the disposable Flink host:"
echo "  1. FLINK_DETACHED=true bash scripts/run_flink.sh"
echo "  2. Record the job ID and wait for at least one completed checkpoint."
echo "  3. Perform one controlled TaskManager/job failure."
echo "  4. Confirm the same job resumes from the checkpoint and replay counts remain stable."
echo "  5. Capture the Flink UI or CLI log and destroy the demo resources."
