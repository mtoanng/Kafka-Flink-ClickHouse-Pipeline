#!/usr/bin/env bash
# Register and verify the Taobao event Avro value schema.
# Usage: bash scripts/register_schemas.sh
set -euo pipefail

cd "$(dirname "$0")/.."

SR_URL="${SCHEMA_REGISTRY_URL:-http://localhost:8081/apis/ccompat/v7}"
SCHEMA_FILE="schemas/user-behavior-event.avsc"
SUBJECT="user-behavior-events-value"
CURL_AUTH=()
if [ -n "${SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO:-}" ]; then
  CURL_AUTH=(-u "$SCHEMA_REGISTRY_BASIC_AUTH_USER_INFO")
elif [ -n "${SCHEMA_REGISTRY_API_KEY:-}" ] || [ -n "${SCHEMA_REGISTRY_API_SECRET:-}" ]; then
  : "${SCHEMA_REGISTRY_API_KEY:?SCHEMA_REGISTRY_API_KEY is required}"
  : "${SCHEMA_REGISTRY_API_SECRET:?SCHEMA_REGISTRY_API_SECRET is required}"
  CURL_AUTH=(-u "$SCHEMA_REGISTRY_API_KEY:$SCHEMA_REGISTRY_API_SECRET")
fi

echo "Schema Registry: ${SR_URL}"

if ! curl -sf "${CURL_AUTH[@]}" "${SR_URL}/subjects" > /dev/null 2>&1; then
  echo "ERROR: Schema Registry not reachable at ${SR_URL}"
  echo "Start it: docker compose -f infra/docker-compose.yml up -d"
  exit 1
fi

echo "Setting ${SUBJECT} compatibility to BACKWARD..."
curl -sf "${CURL_AUTH[@]}" -X PUT "${SR_URL}/config/${SUBJECT}" \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d '{"compatibility":"BACKWARD"}' > /dev/null

echo "Checking compatibility with the latest registered version..."
SCHEMA=$(cat "${SCHEMA_FILE}")
PAYLOAD=$(printf '{"schemaType":"AVRO","schema":%s}' \
  "$(echo "$SCHEMA" | python -c 'import sys,json;print(json.dumps(sys.stdin.read()))')")

if curl -sf "${CURL_AUTH[@]}" "${SR_URL}/subjects/${SUBJECT}/versions/latest" > /dev/null 2>&1; then
  COMPATIBLE=$(curl -sf "${CURL_AUTH[@]}" -X POST \
    "${SR_URL}/compatibility/subjects/${SUBJECT}/versions/latest" \
    -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    -d "$PAYLOAD")
  echo "  Compatibility: ${COMPATIBLE}"
  echo "$COMPATIBLE" | python -c 'import json,sys; assert json.load(sys.stdin)["is_compatible"] is True'
fi

echo "Registering ${SUBJECT}..."
RESPONSE=$(curl -sf "${CURL_AUTH[@]}" -X POST "${SR_URL}/subjects/${SUBJECT}/versions" \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  -d "$PAYLOAD")
echo "  Response: ${RESPONSE}"
echo "$RESPONSE" | python -c 'import json,sys; assert isinstance(json.load(sys.stdin)["id"], int)'

echo ""
echo "Registered subjects:"
curl -s "${CURL_AUTH[@]}" "${SR_URL}/subjects"
