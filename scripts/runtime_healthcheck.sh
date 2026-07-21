#!/usr/bin/env bash
# Bounded local runtime health check; credentials are never printed.
set -euo pipefail

cd "$(dirname "$0")/.."
compose_file="${COMPOSE_FILE:-infra/docker-compose.yml}"
profiles=(--profile core)
[ "${CASSANDRA_ENABLED:-false}" = true ] && profiles+=(--profile serving)
[ "${CDC_ENABLED:-false}" = true ] && profiles+=(--profile cdc)
[ "${OBSERVABILITY_ENABLED:-false}" = true ] && profiles+=(--profile observability)
docker compose -f "$compose_file" "${profiles[@]}" ps --status running --services | sort

schema_registry_url="${SCHEMA_REGISTRY_URL:-http://localhost:8081/apis/ccompat/v7}"
curl --fail --silent --show-error --max-time "${HEALTHCHECK_TIMEOUT_SECONDS:-5}" \
  "${schema_registry_url%/}/subjects" >/dev/null

if [ -n "${CLICKHOUSE_ENDPOINT:-}" ]; then
  curl --fail --silent --show-error --max-time "${HEALTHCHECK_TIMEOUT_SECONDS:-5}" \
    "${CLICKHOUSE_ENDPOINT%/}/?query=SELECT%201" >/dev/null
fi

echo "Runtime health checks passed."
