#!/usr/bin/env bash
# stop.sh — Stop local infrastructure
# Usage:
#   bash scripts/stop.sh             # stop, keep volumes
#   bash scripts/stop.sh --volumes   # stop + wipe volumes
set -euo pipefail

cd "$(dirname "$0")/.."

if [ "${1:-}" = "--volumes" ]; then
  echo "Stopping stack AND wiping volumes (data lost)..."
  docker compose -f infra/docker-compose.yml down -v
else
  echo "Stopping stack (volumes preserved)..."
  docker compose -f infra/docker-compose.yml down
fi
echo "Stopped."
