#!/usr/bin/env bash
# Stop the optional local CDC containers.
set -euo pipefail

cd "$(dirname "$0")/.."
if [ "${1:-}" = "--volumes" ]; then
  docker compose --profile cdc -f "${COMPOSE_FILE:-infra/docker-compose.yml}" down -v
else
  docker compose --profile cdc -f "${COMPOSE_FILE:-infra/docker-compose.yml}" down
fi
echo "CDC containers stopped."
