#!/usr/bin/env bash
# Apply the idempotent active-cart table bootstrap to a provisioned Astra keyspace.
set -euo pipefail

cd "$(dirname "$0")/.."

: "${CASSANDRA_PROVIDER:?set CASSANDRA_PROVIDER before applying the remote schema}"
: "${CASSANDRA_KEYSPACE:?set CASSANDRA_KEYSPACE before applying the remote schema}"
: "${ASTRA_DB_SECURE_BUNDLE_PATH:?set ASTRA_DB_SECURE_BUNDLE_PATH before applying the remote schema}"
: "${ASTRA_DB_APPLICATION_TOKEN:?set ASTRA_DB_APPLICATION_TOKEN before applying the remote schema}"

if [ "$CASSANDRA_PROVIDER" != "astra" ]; then
  echo "ERROR: CASSANDRA_PROVIDER must be astra" >&2
  exit 2
fi
if [ ! -f "$ASTRA_DB_SECURE_BUNDLE_PATH" ]; then
  echo "ERROR: ASTRA_DB_SECURE_BUNDLE_PATH must reference a readable file" >&2
  exit 2
fi

CQLSH_BIN="${CQLSH_BIN:-cqlsh}"
if ! command -v "$CQLSH_BIN" >/dev/null 2>&1; then
  echo "ERROR: cqlsh not found: ${CQLSH_BIN}"
  exit 1
fi

ARGS=(--cloudconf "$ASTRA_DB_SECURE_BUNDLE_PATH" -u token -p "$ASTRA_DB_APPLICATION_TOKEN" -k "$CASSANDRA_KEYSPACE")

"$CQLSH_BIN" "${ARGS[@]}" -f infra/cassandra/schema.cql
