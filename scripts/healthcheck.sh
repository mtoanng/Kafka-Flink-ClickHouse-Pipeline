#!/usr/bin/env bash
# healthcheck.sh — Verify Kafka + Schema Registry are healthy
# Usage: bash scripts/healthcheck.sh
set -uo pipefail

export NO_PROXY="localhost,127.0.0.1,::1"
export no_proxy="localhost,127.0.0.1,::1"

PASS=0
FAIL=0

check() {
  printf "  %-25s ... " "$1"
  if eval "$2" >/dev/null 2>&1; then
    echo "OK"
    PASS=$((PASS + 1))
  else
    echo "FAIL"
    FAIL=$((FAIL + 1))
  fi
}

echo "=== Taobao Kafka and Schema Registry healthcheck ==="
check "Kafka"           "docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092"
check "Schema Registry" "curl --noproxy '*' -sf http://localhost:8081/subjects"
echo ""
echo "Total: ${PASS} OK, ${FAIL} FAIL"
exit $FAIL
