#!/usr/bin/env bash
# Apply the idempotent active-cart schema to local Cassandra or Astra.
set -euo pipefail

cd "$(dirname "$0")/.."

: "${CASSANDRA_MODE:?set CASSANDRA_MODE=local or astra}"
: "${CASSANDRA_KEYSPACE:?set CASSANDRA_KEYSPACE before applying the schema}"
: "${CASSANDRA_TABLE:?set CASSANDRA_TABLE before applying the schema}"

if [ "$CASSANDRA_TABLE" != "user_active_cart" ]; then
  echo "ERROR: CASSANDRA_TABLE must be user_active_cart" >&2
  exit 2
fi

CQLSH_BIN="${CQLSH_BIN:-cqlsh}"

case "$CASSANDRA_MODE" in
  local)
    : "${CASSANDRA_HOSTS:?CASSANDRA_HOSTS is required for local mode}"
    : "${CASSANDRA_DATACENTER:?CASSANDRA_DATACENTER is required for local mode}"
    host="${CASSANDRA_HOSTS%%,*}"
    port="${CASSANDRA_PORT:-9042}"
    auth=()
    if [ -n "${CASSANDRA_USERNAME:-}" ] || [ -n "${CASSANDRA_PASSWORD:-}" ]; then
      : "${CASSANDRA_USERNAME:?CASSANDRA_USERNAME and CASSANDRA_PASSWORD must be provided together}"
      : "${CASSANDRA_PASSWORD:?CASSANDRA_USERNAME and CASSANDRA_PASSWORD must be provided together}"
      auth=(-u "$CASSANDRA_USERNAME" -p "$CASSANDRA_PASSWORD")
    fi
    if [ "$CASSANDRA_KEYSPACE" != "taobao_streaming" ]; then
      echo "ERROR: local bootstrap supports CASSANDRA_KEYSPACE=taobao_streaming" >&2
      exit 2
    fi
    if command -v "$CQLSH_BIN" >/dev/null 2>&1; then
      "$CQLSH_BIN" "$host" "$port" "${auth[@]}" -f infra/cassandra/local-keyspace.cql
      "$CQLSH_BIN" "$host" "$port" "${auth[@]}" -k "$CASSANDRA_KEYSPACE" \
        -f infra/cassandra/schema.cql
    elif command -v docker >/dev/null 2>&1; then
      docker compose -f "${COMPOSE_FILE:-infra/docker-compose.yml}" exec -T cassandra \
        cqlsh 127.0.0.1 9042 "${auth[@]}" -f /opt/taobao/cassandra/local-keyspace.cql
      docker compose -f "${COMPOSE_FILE:-infra/docker-compose.yml}" exec -T cassandra \
        cqlsh 127.0.0.1 9042 "${auth[@]}" -k "$CASSANDRA_KEYSPACE" \
        -f /opt/taobao/cassandra/schema.cql
    else
      echo "ERROR: local schema bootstrap requires cqlsh or Docker Compose" >&2
      exit 1
    fi
    ;;
  astra)
    : "${ASTRA_DB_SECURE_BUNDLE_PATH:?ASTRA_DB_SECURE_BUNDLE_PATH is required for astra mode}"
    : "${ASTRA_DB_APPLICATION_TOKEN:?ASTRA_DB_APPLICATION_TOKEN is required for astra mode}"
    if [ ! -f "$ASTRA_DB_SECURE_BUNDLE_PATH" ]; then
      echo "ERROR: ASTRA_DB_SECURE_BUNDLE_PATH must reference a readable file" >&2
      exit 2
    fi
    if ! command -v "$CQLSH_BIN" >/dev/null 2>&1; then
      echo "ERROR: Astra schema bootstrap requires cqlsh: ${CQLSH_BIN}" >&2
      exit 1
    fi
    "$CQLSH_BIN" --cloudconf "$ASTRA_DB_SECURE_BUNDLE_PATH" \
      -u token -p "$ASTRA_DB_APPLICATION_TOKEN" -k "$CASSANDRA_KEYSPACE" \
      -f infra/cassandra/schema.cql
    ;;
  *)
    echo "ERROR: CASSANDRA_MODE must be local or astra" >&2
    exit 2
    ;;
esac
