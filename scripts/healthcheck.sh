#!/usr/bin/env bash
# =============================================================
#  VES Healthcheck — Verify tất cả service đang chạy đúng
#  Usage: bash scripts/healthcheck.sh
#  Exit 0 nếu tất cả healthy, 1 nếu có service fail.
# =============================================================
set -uo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

# shellcheck disable=SC1091
[ -f .env ] && source .env

POSTGRES_PORT="${POSTGRES_PORT:-5432}"
KAFKA_HOST_PORT="${KAFKA_HOST_PORT:-9092}"
FLINK_UI_PORT="${FLINK_UI_PORT:-8081}"
METABASE_PORT="${METABASE_PORT:-3000}"

PASS=0
FAIL=0

check() {
    local name="$1"
    local cmd="$2"
    printf "  %-25s ... " "$name"
    if eval "$cmd" > /dev/null 2>&1; then
        echo "OK"
        PASS=$((PASS + 1))
    else
        echo "FAIL"
        FAIL=$((FAIL + 1))
    fi
}

echo "============================================="
echo "  VES Healthcheck"
echo "============================================="

check "PostgreSQL"       "docker exec postgres-database pg_isready -U postgres -d fuel_prices"
check "Kafka"            "docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092"
check "Zookeeper"        "docker exec zookeeper zookeeper-shell localhost:2181 ls /"
check "Flink JobManager" "curl -sf http://localhost:${FLINK_UI_PORT}/overview"
check "Metabase"         "curl -sf http://localhost:${METABASE_PORT}/api/health"

echo ""
echo "Tổng: ${PASS} OK, ${FAIL} FAIL"
exit $FAIL
