#!/usr/bin/env bash
# Verify final cloud endpoints without printing credentials.
set -euo pipefail

cd "$(dirname "$0")/.."
bash scripts/cloud_preflight.sh
