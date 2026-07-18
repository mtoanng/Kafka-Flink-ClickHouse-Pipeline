#!/usr/bin/env bash
# fetch_f1_session.sh — Download Abu Dhabi GP 2021 Race data from TracingInsights
# Usage: bash scripts/fetch_f1_session.sh [DEST_DIR]
# Default DEST_DIR: data-generators/f1-replay-engine/src/main/resources/datasets
set -euo pipefail

REPO="TracingInsights/2021"
BRANCH="main"
SESSION_PATH="Abu Dhabi Grand Prix/Race"
DEST="${1:-data-generators/f1-replay-engine/src/main/resources/datasets}"

mkdir -p "$DEST"

echo "Downloading ${SESSION_PATH} from ${REPO}..."
echo "Destination: ${DEST}"

# Download race session files (top-level JSON: drivers, rcm, corners, session_laptimes, weather)
BASE_URL="https://raw.githubusercontent.com/${REPO}/${BRANCH}/${SESSION_PATH}"

for FILE in drivers.json rcm.json corners.json weather.json; do
  echo "  - ${FILE}"
  curl -sL "${BASE_URL}/${FILE}" -o "${DEST}/${FILE}"
done

# Download per-driver telemetry (skip location data to save ~50% space)
DRIVERS_URL="https://api.github.com/repos/${REPO}/contents/${SESSION_PATH}"
DRIVER_DIRS=$(curl -sL "${DRIVERS_URL}" | python3 -c "
import sys, json
for item in json.load(sys.stdin):
    if item['type'] == 'dir' and len(item['name']) == 3 and item['name'].isupper():
        print(item['name'])
" 2>/dev/null || echo "")

for DRIVER in ${DRIVER_DIRS}; do
  echo "  - ${DRIVER}/ (telemetry laps)"
  mkdir -p "${DEST}/${DRIVER}"

  # List lap files for this driver
  LAP_URLS=$(curl -sL "https://api.github.com/repos/${REPO}/contents/${SESSION_PATH}/${DRIVER}" | python3 -c "
import sys, json
for item in json.load(sys.stdin):
    if item['type'] == 'file' and item['name'].endswith('_tel.json'):
        print(item['download_url'])
" 2>/dev/null)

  for URL in ${LAP_URLS}; do
    FILENAME=$(basename "${URL}")
    curl -sL "${URL}" -o "${DEST}/${DRIVER}/${FILENAME}"
  done

  # Also grab laptimes.json for this driver
  curl -sL "https://raw.githubusercontent.com/${REPO}/${BRANCH}/${SESSION_PATH}/${DRIVER}/laptimes.json" \
    -o "${DEST}/${DRIVER}/laptimes.json" 2>/dev/null || true
done

echo ""
echo "Done. $(find "${DEST}" -type f | wc -l) files downloaded."
echo "Total size: $(du -sh "${DEST}" | cut -f1)"
