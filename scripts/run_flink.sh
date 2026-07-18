#!/usr/bin/env bash
# run_flink.sh — Run the Flink job in-process (MiniCluster)
# Usage: bash scripts/run_flink.sh
# Prerequisites: Kafka running locally, ClickHouse Cloud credentials in .env
set -euo pipefail

if [ ! -f .env ]; then
  echo "WARNING: .env not found. Copy .env.example and fill in ClickHouse Cloud credentials."
  echo "         cp .env.example .env"
  exit 1
fi

echo "Loading .env..."
set -a
source .env
set +a

echo "Starting Flink job (in-process)..."
echo "  Kafka: ${KAFKA_BOOTSTRAP_SERVERS}"
echo "  ClickHouse: ${CLICKHOUSE_HOST}:${CLICKHOUSE_PORT}"
echo ""

JAR_PATH="flink-jobs/f1-telemetry-job/target/f1-telemetry-job-1.0.0-SNAPSHOT.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "ERROR: JAR not found at $JAR_PATH"
  echo "       Run: mvn clean package -DskipTests -pl flink-jobs/f1-telemetry-job"
  exit 1
fi

java -jar "$JAR_PATH"
