#!/usr/bin/env bash
set -euo pipefail

if [ "${RELEASE_E2E_CONFIRM:-}" != "YES" ]; then
  echo "Refusing release E2E. Set RELEASE_E2E_CONFIRM=YES on the disposable host." >&2
  exit 2
fi

cd "$(dirname "$0")/.."
bash scripts/cloud_preflight.sh
PYTHONPATH=producer/src python -m unittest discover -s producer/tests -v
mvn -B -pl flink-jobs/taobao-stream-job -am test
PYTHONPATH=producer/src python scripts/reconcile_final_e2e.py
bash scripts/run_fixture_demo.sh
bash scripts/collect_cloud_evidence.sh
