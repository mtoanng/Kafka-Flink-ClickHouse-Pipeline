#!/usr/bin/env bash
# Verify one bounded run across ClickHouse and Cassandra. Run only with live services.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ "$#" -ne 1 ] || [ -z "$1" ]; then
  echo "usage: bash scripts/verify_bounded_pipeline.sh <replay-run-id>" >&2
  exit 2
fi

run_id="$1"
export REPLAY_RUN_ID="$run_id"
export EVIDENCE_DIR="${EVIDENCE_DIR:-artifacts/reconciliation/$run_id}"

echo "Computing independent bounded expectations"
PYTHONPATH=producer/src python scripts/reconcile_final_e2e.py

echo "Verifying deduplicated ClickHouse results"
PYTHONPATH=producer/src python scripts/verify_clickhouse.py --run-id "$run_id"

echo "Verifying Cassandra active cart for user 100"
lookup_output="$(bash scripts/lookup_active_cart.sh 100)"
echo "$lookup_output"
if ! grep -q 'user_id=100 item_id=501 ' <<<"$lookup_output"; then
  echo "ERROR: expected active item 501 for user 100" >&2
  exit 1
fi
if grep -q 'user_id=100 item_id=500 ' <<<"$lookup_output"; then
  echo "ERROR: purchased item 500 is still active for user 100" >&2
  exit 1
fi

echo "Bounded pipeline reconciliation passed for run $run_id"
