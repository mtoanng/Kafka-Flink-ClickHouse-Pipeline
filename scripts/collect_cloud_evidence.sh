#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
RUN_ID="${REPLAY_RUN_ID:-cloud-$(date -u +%Y%m%dT%H%M%SZ)}"
OUT_DIR="${EVIDENCE_DIR:-docs/evidence/final-e2e/$RUN_ID}"
mkdir -p "$OUT_DIR"

{
  echo "run_id=$RUN_ID"
  echo "captured_at_utc=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  git rev-parse HEAD 2>/dev/null || true
  echo "credential_values=omitted"
} > "$OUT_DIR/run-metadata.txt"

if command -v docker >/dev/null 2>&1; then
  docker compose -f "${COMPOSE_FILE:-infra/docker-compose.yml}" ps > "$OUT_DIR/compose-ps.txt" 2>&1 || true
fi
if [ -d artifacts ]; then
  find artifacts -maxdepth 1 -type f -printf '%f\n' | sort > "$OUT_DIR/artifact-files.txt"
fi

echo "Evidence metadata written to $OUT_DIR; secrets were not collected."
