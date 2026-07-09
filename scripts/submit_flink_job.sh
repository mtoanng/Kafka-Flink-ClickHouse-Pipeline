#!/usr/bin/env bash
# =============================================================================
# Submit Flink job qua REST API — thay thế bước upload JAR thủ công qua Flink UI.
#
# Usage:
#   bash scripts/submit_flink_job.sh [path/to/job.jar]
#
# Env:
#   FLINK_URL    (default: http://localhost:8081)
#   ENTRY_CLASS  (default: org.cloud.KafkaConsumerApplication)
# =============================================================================
set -euo pipefail

FLINK_URL="${FLINK_URL:-http://localhost:8081}"
ENTRY_CLASS="${ENTRY_CLASS:-org.cloud.KafkaConsumerApplication}"

# Tìm JAR: tham số 1, hoặc glob mặc định
JAR_PATH="${1:-}"
if [[ -z "$JAR_PATH" ]]; then
  JAR_PATH=$(ls flink-jobs/fuel-flink-job/target/fuel-flink-job-*.jar 2>/dev/null | head -n1 || true)
fi
if [[ -z "$JAR_PATH" || ! -f "$JAR_PATH" ]]; then
  echo "❌ Không tìm thấy JAR. Hãy chạy 'make build' trước." >&2
  exit 1
fi

echo "⏳ Chờ Flink JobManager tại $FLINK_URL ..."
for i in $(seq 1 30); do
  if curl -sf "$FLINK_URL/overview" >/dev/null 2>&1; then break; fi
  [[ $i -eq 30 ]] && { echo "❌ Flink không phản hồi sau 60s" >&2; exit 1; }
  sleep 2
done

# Nếu job cùng tên đang RUNNING thì bỏ qua (idempotent)
RUNNING=$(curl -sf "$FLINK_URL/jobs/overview" | grep -o '"state":"RUNNING"' | wc -l || echo 0)
if [[ "$RUNNING" -gt 0 ]]; then
  echo "ℹ️  Đã có $RUNNING job RUNNING — bỏ qua submit (dùng 'curl -X PATCH $FLINK_URL/jobs/<id>' để cancel nếu muốn re-submit)."
  exit 0
fi

echo "📤 Upload $JAR_PATH ..."
UPLOAD_RESP=$(curl -sf -X POST -H "Expect:" -F "jarfile=@${JAR_PATH}" "$FLINK_URL/jars/upload")
JAR_ID=$(echo "$UPLOAD_RESP" | sed -n 's#.*"filename":"[^"]*/\([^"]*\)".*#\1#p')
if [[ -z "$JAR_ID" ]]; then
  echo "❌ Upload thất bại: $UPLOAD_RESP" >&2
  exit 1
fi

echo "🚀 Run entry class $ENTRY_CLASS ..."
RUN_RESP=$(curl -sf -X POST "$FLINK_URL/jars/${JAR_ID}/run" \
  -H 'Content-Type: application/json' \
  -d "{\"entryClass\":\"${ENTRY_CLASS}\"}")
JOB_ID=$(echo "$RUN_RESP" | sed -n 's#.*"jobid":"\([^"]*\)".*#\1#p')

echo "✅ Job submitted — jobId=${JOB_ID:-unknown} — theo dõi tại $FLINK_URL/#/job/${JOB_ID}/overview"
