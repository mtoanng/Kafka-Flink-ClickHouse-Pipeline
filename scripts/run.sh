#!/usr/bin/env bash
# Start the selected local runtime profiles without printing credentials.
set -euo pipefail

cd "$(dirname "$0")/.."
compose_file="${COMPOSE_FILE:-infra/docker-compose.yml}"
profiles=(--profile core)
profile_names=(core)

if [ "${SERVING_ENABLED:-false}" = "true" ]; then
  : "${SCYLLA_HOST:?SCYLLA_HOST is required when SERVING_ENABLED=true}"
  : "${SCYLLA_LOCAL_DATACENTER:?SCYLLA_LOCAL_DATACENTER is required when SERVING_ENABLED=true}"
  profiles+=(--profile serving)
  profile_names+=(serving)
fi
if [ "${CDC_ENABLED:-false}" = "true" ]; then
  : "${POSTGRES_DB:?POSTGRES_DB is required when CDC_ENABLED=true}"
  : "${POSTGRES_USER:?POSTGRES_USER is required when CDC_ENABLED=true}"
  : "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required when CDC_ENABLED=true}"
  : "${RULES_KAFKA_TOPIC:?RULES_KAFKA_TOPIC is required when CDC_ENABLED=true}"
  profiles+=(--profile cdc)
  profile_names+=(cdc)
fi
if [ "${OBSERVABILITY_ENABLED:-false}" = "true" ]; then
  : "${CLICKHOUSE_GRAFANA_URL:?CLICKHOUSE_GRAFANA_URL is required when OBSERVABILITY_ENABLED=true}"
  : "${CLICKHOUSE_PASSWORD:?CLICKHOUSE_PASSWORD is required when OBSERVABILITY_ENABLED=true}"
  profiles+=(--profile observability)
  profile_names+=(observability)
fi

docker compose -f "$compose_file" "${profiles[@]}" up -d
echo "Runtime profiles started: ${profile_names[*]}"
