#!/usr/bin/env bash
# register_schemas.sh — Register Avro schemas to local Schema Registry
# Usage: bash scripts/register_schemas.sh
set -euo pipefail

SR_URL="${SCHEMA_REGISTRY_URL:-http://localhost:8081}"
SCHEMA_DIR="data-generators/f1-replay-engine/src/main/avro"

echo "Schema Registry: ${SR_URL}"

# Check Schema Registry is up
if ! curl -sf "${SR_URL}/subjects" > /dev/null 2>&1; then
  echo "ERROR: Schema Registry not reachable at ${SR_URL}"
  echo "Start it first: docker compose -f infra/docker-compose.yml up -d"
  exit 1
fi

# Register car-telemetry-event (subject: car-telemetry-events-value)
echo "Registering car-telemetry-events-value..."
SCHEMA=$(cat "${SCHEMA_DIR}/car-telemetry-event.avsc")
RESPONSE=$(curl -s -X POST "${SR_URL}/subjects/car-telemetry-events-value/versions" \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d "{\"schemaType\":\"AVRO\",\"schema\":$(echo "$SCHEMA" | python3 -c 'import sys,json; print(json.dumps(sys.stdin.read()))')}")
echo "  Response: ${RESPONSE}"

echo ""
echo "Registered subjects:"
curl -s "${SR_URL}/subjects" | python3 -m json.tool 2>/dev/null || curl -s "${SR_URL}/subjects"
