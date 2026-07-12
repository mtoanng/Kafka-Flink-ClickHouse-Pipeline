#!/usr/bin/env bash
# =============================================================
#  Run F1 Replay Engine
#  Reads TracingInsights dataset and publishes to Kafka
# =============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR_PATH="$PROJECT_ROOT/data-generators/f1-replay-engine/target/f1-replay-engine-1.0.0-SNAPSHOT.jar"

# Check if jar exists
if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: JAR not found. Build first: mvn clean package -DskipTests"
    exit 1
fi

# Check if dataset exists
DATASET_PATH="$PROJECT_ROOT/data-generators/f1-replay-engine/src/main/resources/datasets"
if [ ! -d "$DATASET_PATH/Abu Dhabi Grand Prix" ]; then
    echo "ERROR: Dataset not found. Run scripts/fetch_f1_session.sh first"
    exit 1
fi

echo "Starting F1 Replay Engine..."
echo "  JAR: $JAR_PATH"
echo "  Dataset: $DATASET_PATH"
echo "  Kafka: ${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
echo "  Speed Factor: ${REPLAY_SPEED_FACTOR:-5}x"
echo ""

java -jar "$JAR_PATH"
