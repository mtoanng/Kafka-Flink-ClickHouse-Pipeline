#!/usr/bin/env bash
# =============================================================
#  VES Stop Script (Linux / macOS / WSL2)
#
#  Usage:
#    bash scripts/stop.sh             # Dừng stack, giữ volume
#    bash scripts/stop.sh --volumes   # Dừng + xóa volume (data sẽ mất)
# =============================================================
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# Auto-detect: nếu Metabase đang chạy → cũng dừng BI overlay
COMPOSE_FILES="-f infra/docker-compose.yml"
if docker ps --filter "name=metabase" --format "{{.Names}}" 2>/dev/null | grep -q metabase; then
    COMPOSE_FILES="$COMPOSE_FILES -f infra/docker-compose.bi.yml"
fi

if [ "${1:-}" = "--volumes" ]; then
    echo "[WARN] Đang dừng stack VÀ xóa volume (data sẽ mất)..."
    # shellcheck disable=SC2086
    docker compose $COMPOSE_FILES down -v
else
    echo "[INFO] Đang dừng stack (data được giữ trong volume)..."
    # shellcheck disable=SC2086
    docker compose $COMPOSE_FILES down
fi
echo "[OK] Stack đã dừng."
