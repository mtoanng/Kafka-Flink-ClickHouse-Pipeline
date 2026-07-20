#!/usr/bin/env bash
set -euo pipefail

if [ "${RECOVERY_TEST_CONFIRM:-}" != "YES" ]; then
  echo "Refusing recovery test. Set RECOVERY_TEST_CONFIRM=YES on the disposable host." >&2
  exit 2
fi
: "${FLINK_JOB_ID:?FLINK_JOB_ID is required}"
: "${FLINK_TASKMANAGER_RESTART_COMMAND:?FLINK_TASKMANAGER_RESTART_COMMAND is required}"
: "${FLINK_CHECKPOINT_DIR:?FLINK_CHECKPOINT_DIR is required}"
FLINK_CLI="${FLINK_CLI:-flink}"
EVIDENCE_DIR="${EVIDENCE_DIR:-docs/evidence/final-e2e}"
mkdir -p "$EVIDENCE_DIR"
"$FLINK_CLI" checkpoint "$FLINK_JOB_ID" "$FLINK_CHECKPOINT_DIR" | tee "$EVIDENCE_DIR/recovery-before.log"
bash -lc "$FLINK_TASKMANAGER_RESTART_COMMAND"
"$FLINK_CLI" list -r | tee "$EVIDENCE_DIR/recovery-after.log"
echo "TaskManager recovery command completed; inspect checkpoint and restart logs in $EVIDENCE_DIR."
