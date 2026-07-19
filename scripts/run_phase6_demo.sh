#!/usr/bin/env bash
set -euo pipefail

: "${POSTGRES_HOST:?POSTGRES_HOST is required}"
: "${POSTGRES_DB:?POSTGRES_DB is required}"
: "${POSTGRES_USER:?POSTGRES_USER is required}"
: "${KAFKA_BOOTSTRAP_SERVERS:?KAFKA_BOOTSTRAP_SERVERS is required}"
: "${CONNECT_URL:?CONNECT_URL is required}"
: "${DEBEZIUM_CONFIG_FILE:?DEBEZIUM_CONFIG_FILE must point to a private connector config}"
: "${PGPASSWORD:?PGPASSWORD is required}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CONNECTOR_NAME="taobao-postgres-behavior-rules"

bash "$SCRIPT_DIR/apply_behavior_rules_schema.sh"
bash "$SCRIPT_DIR/create_behavior_rules_topic.sh"

curl --fail-with-body --silent --show-error \
  -X PUT "$CONNECT_URL/connectors/$CONNECTOR_NAME/config" \
  -H 'Content-Type: application/json' \
  --data-binary "@$DEBEZIUM_CONFIG_FILE"

KAFKA_BOOTSTRAP_SERVERS="$KAFKA_BOOTSTRAP_SERVERS" \
SCHEMA_REGISTRY_URL="${SCHEMA_REGISTRY_URL:-http://localhost:8081}" \
RULES_KAFKA_TOPIC="${RULES_KAFKA_TOPIC:-behavior-rules}" \
FLINK_DETACHED=true \
bash "$SCRIPT_DIR/run_flink.sh"

psql \
  --host "$POSTGRES_HOST" \
  --port "${POSTGRES_PORT:-5432}" \
  --username "$POSTGRES_USER" \
  --dbname "$POSTGRES_DB" \
  --command "INSERT INTO behavior_rules(rule_id, rule_type, threshold_seconds, enabled, version) VALUES ('cart_abandonment', 'cart_abandonment', 60, true, 1) ON CONFLICT (rule_id) DO UPDATE SET threshold_seconds = EXCLUDED.threshold_seconds, enabled = EXCLUDED.enabled, version = EXCLUDED.version, updated_at = now();"

echo "Rule update submitted; verify a cart alert before and after a versioned UPDATE in the remote Flink logs."
