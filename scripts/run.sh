#!/usr/bin/env bash
# Prepare the disposable EC2 host. Kafka and Schema Registry are Confluent Cloud.
set -euo pipefail

cd "$(dirname "$0")/.."
: "${KAFKA_BOOTSTRAP_SERVERS:?KAFKA_BOOTSTRAP_SERVERS is required}"
: "${SCHEMA_REGISTRY_URL:?SCHEMA_REGISTRY_URL is required}"

if [ "${CDC_ENABLED:-false}" = "true" ]; then
  : "${POSTGRES_DB:?POSTGRES_DB is required for CDC}"
  : "${POSTGRES_USER:?POSTGRES_USER is required for CDC}"
  : "${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required for CDC}"
  : "${KAFKA_SASL_JAAS_CONFIG:?KAFKA_SASL_JAAS_CONFIG is required for CDC}"
  docker compose --profile cdc -f "${COMPOSE_FILE:-infra/docker-compose.yml}" up -d
else
  echo "Core profile selected: using Confluent Cloud; no local broker is started."
fi

echo "Confluent Kafka: ${KAFKA_BOOTSTRAP_SERVERS}"
echo "Schema Registry: ${SCHEMA_REGISTRY_URL}"
echo "Next: bash scripts/cloud_preflight.sh"
