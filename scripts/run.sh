#!/usr/bin/env bash
# Start the selected local runtime profile without printing credentials.
set -euo pipefail

cd "$(dirname "$0")/.."
compose_file="${COMPOSE_FILE:-infra/docker-compose.yml}"
runtime_profile="${RUNTIME_PROFILE:-core}"
runtime_dependencies="${RUNTIME_DEPENDENCIES:-local}"

case "$runtime_profile" in
  core|full) ;;
  checks)
    echo "RUNTIME_PROFILE=checks starts no services; run make checks" >&2
    exit 2
    ;;
  *)
    echo "ERROR: RUNTIME_PROFILE must be checks, core, or full" >&2
    exit 2
    ;;
esac

: "${CASSANDRA_MODE:?CASSANDRA_MODE=local or astra is required}"
: "${CASSANDRA_KEYSPACE:?CASSANDRA_KEYSPACE is required}"
: "${CASSANDRA_TABLE:?CASSANDRA_TABLE is required}"
case "$CASSANDRA_MODE" in
  local)
    : "${CASSANDRA_HOSTS:?CASSANDRA_HOSTS is required for local mode}"
    : "${CASSANDRA_DATACENTER:?CASSANDRA_DATACENTER is required for local mode}"
    ;;
  astra)
    : "${ASTRA_DB_SECURE_BUNDLE_PATH:?ASTRA_DB_SECURE_BUNDLE_PATH is required for astra mode}"
    : "${ASTRA_DB_APPLICATION_TOKEN:?ASTRA_DB_APPLICATION_TOKEN is required for astra mode}"
    ;;
  *)
    echo "ERROR: CASSANDRA_MODE must be local or astra" >&2
    exit 2
    ;;
esac

if [ "$runtime_profile" = "full" ]; then
  : "${POSTGRES_DB:?POSTGRES_DB is required for full}"
  : "${POSTGRES_USER:?POSTGRES_USER is required for full}"
  : "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required for full}"
  : "${RULES_KAFKA_TOPIC:?RULES_KAFKA_TOPIC is required for full}"
fi

case "$runtime_dependencies" in
  local)
    services=(kafka schema-registry clickhouse cassandra)
    if [ "$runtime_profile" = "full" ]; then
      services+=(postgres debezium-connect grafana)
    fi
    docker compose -f "$compose_file" --profile "$runtime_profile" up -d "${services[@]}"
    echo "Local runtime dependencies started: $runtime_profile"
    ;;
  managed)
    echo "Managed runtime selected; no local containers were started."
    ;;
  *)
    echo "ERROR: RUNTIME_DEPENDENCIES must be local or managed" >&2
    exit 2
    ;;
esac
