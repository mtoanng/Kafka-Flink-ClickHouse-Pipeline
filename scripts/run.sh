#!/usr/bin/env bash
# =============================================================
#  VES Run Script (Linux / macOS / WSL2)
#  Usage: bash scripts/run.sh
#  Phase 0 skeleton — sẽ mở rộng ở Phase 1
# =============================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

echo "============================================="
echo "  VES Fuel Price Monitor — start"
echo "============================================="

# 1. Copy .env nếu chưa tồn tại
if [ ! -f ".env" ]; then
    echo "[INFO] .env chưa tồn tại, copy từ .env.example..."
    cp .env.example .env
fi

# 2. Khởi động Docker stack
echo "[INFO] Đang khởi động Docker Compose..."
docker compose -f infra/docker-compose.yml --env-file .env up -d

echo ""
echo "[OK] Stack đang khởi động. Đợi ~60s để các service healthy..."
echo "     Kiểm tra trạng thái: docker compose -f infra/docker-compose.yml ps"
echo ""
echo "URLs sau khi start xong:"
echo "  - Flink UI    : http://localhost:8081"
echo "  - Metabase    : http://localhost:3000"
echo "  - PostgreSQL  : localhost:5432  (user=postgres, db=fuel_prices)"
echo "  - Kafka       : localhost:9092"
