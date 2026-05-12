#!/usr/bin/env bash
# =============================================================
# Phase 4.5 smoke test — kiểm tra 14 endpoint REST của backend-api.
#
# Yêu cầu chạy ĐÃ:
#   1. bash scripts/run.sh                 (Docker stack UP, Postgres healthy)
#   2. mvn -pl backend-api -am clean package -DskipTests   (đã build JAR)
#   3. java -jar backend-api/target/ves-backend-api.jar &  (đang chạy port 8090)
#
# Cách dùng:
#   bash scripts/phase45_smoke_api.sh                  # mặc định BASE=http://localhost:8090
#   BASE=http://192.168.1.50:8090 bash scripts/phase45_smoke_api.sh
#   USER=manager PASS=manager bash scripts/phase45_smoke_api.sh
# =============================================================
set -u

BASE="${BASE:-http://localhost:8090}"
USER="${USER:-admin}"
PASS="${PASS:-admin}"

# Cấu hình proxy: chắc chắn bypass corporate proxy với localhost.
export NO_PROXY="localhost,127.0.0.1,::1"
export no_proxy="$NO_PROXY"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PASS_COUNT=0
FAIL_COUNT=0

run_curl() {
    # $1 = label
    # $2 = method
    # $3 = path
    # $4 = optional body (or - for none)
    # $5 = needs-auth (yes/no)
    local label="$1" method="$2" path="$3" body="$4" auth="$5"
    local code resp tmp

    tmp=$(mktemp)
    local hdrs=(-H "Accept: application/json")
    if [ "$auth" = "yes" ]; then
        hdrs+=(-H "Authorization: Bearer ${TOKEN}")
    fi
    if [ "$body" != "-" ] && [ -n "$body" ]; then
        hdrs+=(-H "Content-Type: application/json")
        code=$(curl -sS -o "$tmp" -w '%{http_code}' --noproxy '*' \
                    -X "$method" "${hdrs[@]}" -d "$body" "${BASE}${path}" || echo 000)
    else
        code=$(curl -sS -o "$tmp" -w '%{http_code}' --noproxy '*' \
                    -X "$method" "${hdrs[@]}" "${BASE}${path}" || echo 000)
    fi
    resp=$(cat "$tmp")
    rm -f "$tmp"

    if [[ "$code" =~ ^2 ]]; then
        echo -e "${GREEN}✓${NC} ${label} [${code}]  ${CYAN}${method} ${path}${NC}"
        # In gọn 200 ký tự đầu của response để mắt thường thấy là JSON đúng
        echo "    ${resp:0:200}"
        PASS_COUNT=$((PASS_COUNT + 1))
    else
        echo -e "${RED}✗${NC} ${label} [${code}]  ${CYAN}${method} ${path}${NC}"
        echo "    ${resp:0:300}"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi
}

echo -e "${YELLOW}=== Phase 4.5 API Smoke Test ===${NC}"
echo "Base URL: ${BASE}"
echo "User:     ${USER}"
echo ""

# ---------- 1. PUBLIC endpoints (no token) ----------
echo -e "${YELLOW}--- Public ---${NC}"
TOKEN="dummy"
run_curl "1.  Health (DB UP?)"          GET  "/api/health"        -    no

# ---------- 2. Login → JWT ----------
echo ""
echo -e "${YELLOW}--- Auth ---${NC}"
LOGIN_BODY="{\"username\":\"${USER}\",\"password\":\"${PASS}\"}"
LOGIN_RESP=$(curl -sS --noproxy '*' -H "Content-Type: application/json" \
                  -d "$LOGIN_BODY" "${BASE}/api/auth/login" 2>/dev/null || echo "")
TOKEN=$(echo "$LOGIN_RESP" | grep -oP '"accessToken"\s*:\s*"\K[^"]+' | head -1)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Login failed.${NC} Response:"
    echo "$LOGIN_RESP" | head -c 400
    echo ""
    echo "Hint:  thử USER=admin PASS=admin (seed mặc định) hoặc kiểm tra app log."
    exit 1
fi
echo -e "${GREEN}✓${NC} 2.  Login OK — token ${TOKEN:0:32}..."
PASS_COUNT=$((PASS_COUNT + 1))

run_curl "3.  Auth Me"                   GET  "/api/auth/me"                              -    yes

# ---------- 3. Pillar dashboards ----------
echo ""
echo -e "${YELLOW}--- Pillar dashboards ---${NC}"
run_curl "4.  Pillar 1 outlook"          GET  "/api/pillars/1/outlook"                    -    yes
run_curl "5.  Pillar 2 volatility"       GET  "/api/pillars/2/volatility"                 -    yes
run_curl "6.  Pillar 3 shedding-plan"    GET  "/api/pillars/3/shedding-plan"              -    yes
run_curl "7.  Pillar 4 net-zero"         GET  "/api/pillars/4/net-zero"                   -    yes

# ---------- 4. Security cross-pillar ----------
echo ""
echo -e "${YELLOW}--- Security (cross-pillar) ---${NC}"
run_curl "8.  Security score (ESI)"      GET  "/api/security/score"                       -    yes
run_curl "9.  Cascade risks"             GET  "/api/security/cascade-risks"               -    yes

# ---------- 5. Recommendations ----------
echo ""
echo -e "${YELLOW}--- Recommendations ---${NC}"
run_curl "10. Recommendations list"      GET  "/api/recommendations?limit=20"             -    yes

# Acknowledge recommendation id=1 nếu nó tồn tại & PENDING (sample seed)
ACK_BODY='{"status":"ACKNOWLEDGED","note":"smoke-test from phase45_smoke_api.sh"}'
run_curl "11. ACK recommendation id=1"   POST "/api/recommendations/1/acknowledge"        "$ACK_BODY"  yes

# ---------- 6. Raw / alerts ----------
echo ""
echo -e "${YELLOW}--- Raw data + alerts ---${NC}"
run_curl "12. Active alerts"             GET  "/api/alerts/active?limit=10"               -    yes
run_curl "13. Fuel prices latest"        GET  "/api/fuel-prices/latest?limit=5"           -    yes
run_curl "14. Grid load latest"          GET  "/api/grid-load/latest"                     -    yes

# ---------- Summary ----------
echo ""
echo -e "${YELLOW}=== Summary ===${NC}"
TOTAL=$((PASS_COUNT + FAIL_COUNT))
if [ "$FAIL_COUNT" -eq 0 ]; then
    echo -e "${GREEN}✓ ALL ${PASS_COUNT}/${TOTAL} endpoints passed.${NC}"
    exit 0
else
    echo -e "${RED}✗ ${FAIL_COUNT}/${TOTAL} endpoints FAILED.${NC}  Passed: ${PASS_COUNT}"
    exit 1
fi
