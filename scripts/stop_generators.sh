#!/usr/bin/env bash
# =============================================================
#  VES-Monitor — Stop 3 data generators (Phase 4)
#  Idempotent: không error nếu chưa chạy.
# =============================================================
set -eu

for jar in fuel-price-producer grid-load-generator renewable-generator; do
    if pkill -f "${jar}-1.0.0.jar" 2>/dev/null; then
        echo "  ✓ stopped ${jar}"
    else
        echo "  - ${jar} not running"
    fi
done

echo "[OK] Generators stopped (Docker stack vẫn chạy)."
