#!/usr/bin/env bash
# =============================================================
#  Pre-create Kafka topics dùng cho VES-Monitor.
#
#  Mặc dù auto.create.topics=true, Flink consumer khởi động trước
#  Producer sẽ crash nếu topic chưa được tạo. Script này đảm bảo cả
#  4 pillar topic tồn tại ngay sau khi stack lên.
#
#  Idempotent: dùng `--if-not-exists`.
# =============================================================
set -euo pipefail

KAFKA_CONTAINER="${KAFKA_CONTAINER:-kafka}"
PARTITIONS="${KAFKA_TOPIC_PARTITIONS:-3}"
REPLICATION="${KAFKA_TOPIC_REPLICATION:-1}"

TOPICS=(
    "fuel-prices"        # Pillar 2 (đã có ở Phase 1)
    "grid-load"          # Pillar 3
    "renewable-output"   # Pillar 4
    "emission"           # Pillar 4
)

echo "[INFO] Pre-create topics on container=${KAFKA_CONTAINER} (partitions=${PARTITIONS}, rf=${REPLICATION})"

# Đợi Kafka healthy (tối đa 60s)
for i in $(seq 1 12); do
    if docker exec "${KAFKA_CONTAINER}" kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; then
        break
    fi
    sleep 5
    if [ "$i" = "12" ]; then
        echo "[FAIL] Kafka container chưa sẵn sàng sau 60s." >&2
        exit 1
    fi
done

for t in "${TOPICS[@]}"; do
    docker exec "${KAFKA_CONTAINER}" kafka-topics \
        --bootstrap-server localhost:9092 \
        --create --if-not-exists \
        --topic "$t" \
        --partitions "$PARTITIONS" \
        --replication-factor "$REPLICATION" >/dev/null
    echo "  ✓ topic ${t}"
done

echo "[OK] Tất cả ${#TOPICS[@]} topic sẵn sàng."
