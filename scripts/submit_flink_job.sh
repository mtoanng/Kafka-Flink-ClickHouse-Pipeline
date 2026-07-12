#!/usr/bin/env bash
# =============================================================
#  Submit Flink Job to Standalone Cluster
#  Prerequisites: Flink 1.18+ cluster running (bin/start-cluster.sh)
# =============================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
JAR_PATH="$PROJECT_ROOT/flink-jobs/f1-telemetry-job/target/f1-telemetry-job-1.0.0-SNAPSHOT.jar"

# Check if jar exists
if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: JAR not found. Build first: mvn clean package -DskipTests"
    exit 1
fi

# Find Flink installation (check common paths)
FLINK_HOME="${FLINK_HOME:-}"
if [ -z "$FLINK_HOME" ]; then
    if [ -d "$HOME/flink-1.18.0" ]; then
        FLINK_HOME="$HOME/flink-1.18.0"
    elif [ -d "/opt/flink" ]; then
        FLINK_HOME="/opt/flink"
    else
        echo "ERROR: FLINK_HOME not set. Download Flink 1.18.0 or set FLINK_HOME env var"
        echo "Download: https://archive.apache.org/dist/flink/flink-1.18.0/flink-1.18.0-bin-scala_2.12.tgz"
        exit 1
    fi
fi

FLINK_BIN="$FLINK_HOME/bin/flink"
if [ ! -x "$FLINK_BIN" ]; then
    echo "ERROR: Flink binary not found at $FLINK_BIN"
    exit 1
fi

echo "Submitting Flink job..."
echo "  JAR: $JAR_PATH"
echo "  Flink: $FLINK_HOME"

"$FLINK_BIN" run -c com.f1telemetry.F1TelemetryJob "$JAR_PATH"

echo "Job submitted successfully. Check Flink UI: http://localhost:8081"
