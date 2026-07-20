#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
bash scripts/cloud_preflight.sh
PYTHONPATH=producer/src python scripts/generate_fixture.py --force
bash scripts/run_fixture_demo.sh
bash scripts/collect_cloud_evidence.sh
