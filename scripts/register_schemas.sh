#!/usr/bin/env bash
# register_schemas.sh — Register Avro schema to local Schema Registry
# Usage: bash scripts/register_schemas.sh
set -euo pipefail

SR_URL="${SCHEMA_REGISTRY_URL:-http://localhost:8081}"
SCHEMA_FILE="data-generators/f1-replay-engine/src/main/avro/car-telemetry-event.avsc"

echo "Schema Registry: ${SR_URL}"

if ! curl -sf "${SR_URL}/subjects" > /dev/null 2>&1; then
  echo "ERROR: Schema Registry not reachable at ${SR_URL}"
  echo "Start it: docker compose -f infra/docker-compose.yml up -d"
  exit 1
fi

echo "Registering car-telemetry-events-value..."
SCHEMA=$(cat "${SCHEMA_FILE}")
PAYLOAD=$(printf '{"schemaType":"AVRO","schema":%s}' \
  "$(echo "$SCHEMA" | python -c 'import sys,json;print(json.dumps(sys.stdin.read()))')")

RESPONSE=$(curl -s -X POST "${SR_URL}/subjects/car-telemetry-events-value/versions" \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d "$PAYLOAD")
echo "  Response: ${RESPONSE}"

echo ""
echo "Registered subjects:"
curl -s "${SR_URL}/subjects"
