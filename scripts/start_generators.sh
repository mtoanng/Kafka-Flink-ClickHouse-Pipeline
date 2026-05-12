#!/usr/bin/env bash
# =============================================================
#  VES-Monitor — Start 3 data generators in background (Phase 4)
#
#  Khởi động song song 3 producer Java cho 4 pillar:
#    - fuel-price-producer  (Pillar 2)
#    - grid-load-generator  (Pillar 3)
#    - renewable-generator  (Pillar 4 — 2 topic)
#
#  Idempotent: tự kill instance cũ trước khi start.
#  Logs ghi vào /tmp/ves-logs/*.log
#
#  Yêu cầu: đã chạy `mvn -q clean package -DskipTests` trước đó
#  để có JAR trong data-generators/*/target/.
# =============================================================
set -eu
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

pkill -f 'grid-load-generator-1.0.0.jar' 2>/dev/null || true
pkill -f 'renewable-generator-1.0.0.jar' 2>/dev/null || true
pkill -f 'fuel-price-producer-1.0.0.jar' 2>/dev/null || true
sleep 1

mkdir -p /tmp/ves-logs

echo "[INFO] Starting Pillar 2 (fuel-price-producer)..."
nohup java -jar data-generators/fuel-price-producer/target/fuel-price-producer-1.0.0.jar \
    > /tmp/ves-logs/fuel-price.log 2>&1 &
FUEL_PID=$!
echo "  fuel-price PID=$FUEL_PID"

echo "[INFO] Starting Pillar 3 (grid-load-generator)..."
nohup java -jar data-generators/grid-load-generator/target/grid-load-generator-1.0.0.jar \
    > /tmp/ves-logs/grid-load.log 2>&1 &
GRID_PID=$!
echo "  grid-load PID=$GRID_PID"

echo "[INFO] Starting Pillar 4 (renewable-generator)..."
nohup java -jar data-generators/renewable-generator/target/renewable-generator-1.0.0.jar \
    > /tmp/ves-logs/renewable.log 2>&1 &
REN_PID=$!
echo "  renewable PID=$REN_PID"

sleep 3
echo "[OK] All 3 generators launched. Tail logs in /tmp/ves-logs/"
ps -ef | grep -E 'grid-load|renewable|fuel-price-producer' | grep -v grep || true
