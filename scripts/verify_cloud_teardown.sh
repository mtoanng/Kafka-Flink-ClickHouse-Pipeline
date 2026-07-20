#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
COMPOSE_FILE="${COMPOSE_FILE:-infra/docker-compose.yml}"

if command -v docker >/dev/null 2>&1; then
  remaining="$(docker compose -f "$COMPOSE_FILE" ps -q 2>/dev/null || true)"
  if [ -n "$remaining" ]; then
    echo "ERROR: Compose containers still exist; stop them before declaring teardown complete." >&2
    exit 1
  fi
fi

if [ -d infra/terraform/.terraform ]; then
  state="$(terraform -chdir=infra/terraform state list 2>/dev/null || true)"
  if [ -n "$state" ]; then
    echo "ERROR: Terraform state still lists managed resources." >&2
    exit 1
  fi
fi

echo "Local teardown checks passed. Provider-side deletion remains operator-verified."
