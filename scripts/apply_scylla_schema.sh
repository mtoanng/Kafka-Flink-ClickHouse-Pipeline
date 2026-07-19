#!/usr/bin/env bash
# Apply the Phase 4 CQL schema on a remote ScyllaDB-compatible endpoint.
set -euo pipefail

cd "$(dirname "$0")/.."

: "${SCYLLA_HOST:?set SCYLLA_HOST before applying the remote schema}"

CQLSH_BIN="${CQLSH_BIN:-cqlsh}"
if ! command -v "$CQLSH_BIN" >/dev/null 2>&1; then
  echo "ERROR: cqlsh not found: ${CQLSH_BIN}"
  exit 1
fi

ARGS=("$SCYLLA_HOST" "${SCYLLA_PORT:-9042}")
if [ -n "${SCYLLA_USER:-}" ]; then
  : "${SCYLLA_PASSWORD:?set SCYLLA_PASSWORD when SCYLLA_USER is set}"
  ARGS+=("-u" "$SCYLLA_USER" "-p" "$SCYLLA_PASSWORD")
fi
if [ -n "${SCYLLA_CQLSH_EXTRA_ARGS:-}" ]; then
  read -r -a EXTRA_ARGS <<< "$SCYLLA_CQLSH_EXTRA_ARGS"
  ARGS+=("${EXTRA_ARGS[@]}")
fi

"$CQLSH_BIN" "${ARGS[@]}" -f infra/scylladb/schema.cql
