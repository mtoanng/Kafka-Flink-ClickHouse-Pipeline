#!/usr/bin/env bash
# Stop the selected runtime profile and optionally remove local volumes.
set -euo pipefail

cd "$(dirname "$0")/.."
bash scripts/stop.sh
