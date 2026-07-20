#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
if [ "${1:-}" = "--volumes" ]; then
  docker compose -f "${COMPOSE_FILE:-infra/docker-compose.yml}" down -v
else
  docker compose -f "${COMPOSE_FILE:-infra/docker-compose.yml}" down
fi
echo "Runtime containers stopped."
