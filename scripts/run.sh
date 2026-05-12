#!/usr/bin/env bash
# =============================================================
#  VES Run Script (Linux / macOS / WSL2)
#
#  Usage:
#    bash scripts/run.sh           # Lite profile (Kafka+Flink+Postgres)
#    bash scripts/run.sh --bi      # Lite + Metabase BI dashboard
#    bash scripts/run.sh --wait    # Đợi tất cả service healthy
#
#  Memory budget Lite:  ~2.7 GB total
#  Memory budget +BI:   ~3.5 GB total
# =============================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

WITH_BI=false
WAIT_HEALTHY=false
for arg in "$@"; do
    case "$arg" in
        --bi)   WITH_BI=true ;;
        --wait) WAIT_HEALTHY=true ;;
        -h|--help)
            grep '^#' "$0" | head -n 14
            exit 0
            ;;
    esac
done

echo "============================================="
echo "  VES Fuel Price Monitor — start (Lite Profile)"
echo "============================================="

# 1. Copy .env nếu chưa tồn tại
if [ ! -f ".env" ]; then
    echo "[INFO] .env chưa tồn tại, copy từ .env.example..."
    cp .env.example .env
fi

# 2. Build compose command
COMPOSE_FILES="-f infra/docker-compose.yml"
if [ "$WITH_BI" = "true" ]; then
    COMPOSE_FILES="$COMPOSE_FILES -f infra/docker-compose.bi.yml"
    echo "[INFO] Bật BI overlay (Metabase)..."
fi

# 3. Khởi động Docker stack
echo "[INFO] Đang khởi động Docker Compose..."
# shellcheck disable=SC2086
docker compose $COMPOSE_FILES --env-file .env up -d

# 4. Đợi healthy nếu --wait
if [ "$WAIT_HEALTHY" = "true" ]; then
    echo "[INFO] Đợi tất cả service healthy (tối đa 180s)..."
    for i in $(seq 1 36); do
        if bash scripts/healthcheck.sh > /dev/null 2>&1; then
            echo "[OK] Tất cả service healthy sau $((i * 5))s"
            break
        fi
        sleep 5
        if [ "$i" = "36" ]; then
            echo "[WARN] Timeout 180s. Một số service có thể chưa ready. Chạy healthcheck thủ công."
        fi
    done
fi

# 5. Pre-create Kafka topics (idempotent) — tránh Flink crash khi consumer lên trước producer.
echo "[INFO] Đảm bảo Kafka topics cho 4 pillar..."
if ! bash scripts/create_kafka_topics.sh; then
    echo "[WARN] Không tạo được topic tự động. Hãy chạy 'bash scripts/create_kafka_topics.sh' sau khi Kafka ready."
fi

echo ""
echo "[OK] Stack đã khởi động."
echo ""
echo "URLs:"
echo "  - Flink UI    : http://localhost:8081"
[ "$WITH_BI" = "true" ] && echo "  - Metabase    : http://localhost:3000"
echo "  - PostgreSQL  : localhost:5432  (user=postgres, db=fuel_prices)"
echo "  - Kafka       : localhost:9092"
echo ""
echo "Tiếp theo:"
echo "  bash scripts/healthcheck.sh    # verify stack health"
echo "  bash scripts/stop.sh           # dừng stack (giữ data)"
echo "  bash scripts/stop.sh --volumes # dừng + xóa data"
