#!/usr/bin/env bash
# =============================================================
#  VES Stop Script (Linux / macOS / WSL2)
#  Usage: bash scripts/stop.sh [--volumes]
#    --volumes : xóa cả volume (data sẽ mất)
# =============================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

if [ "${1:-}" = "--volumes" ]; then
    echo "[WARN] Đang dừng stack VÀ xóa volume (data sẽ mất)..."
    docker compose -f infra/docker-compose.yml down -v
else
    echo "[INFO] Đang dừng stack (data được giữ trong volume)..."
    docker compose -f infra/docker-compose.yml down
fi
echo "[OK] Stack đã dừng."
