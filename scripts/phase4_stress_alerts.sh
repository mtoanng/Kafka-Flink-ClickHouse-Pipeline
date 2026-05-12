#!/usr/bin/env bash
# Stress-test alert detection by injecting events past thresholds:
#   GRID_LOAD_PCT > 90  → CRITICAL  (load_mw=11400 / capacity_mw=12000 → 95%)
#   GRID_LOAD_PCT > 80  → WARNING   (load_mw=10000 / 12000 → 83%)
#   EMISSION_INTENSITY > 600 → WARNING (co2=70000 / 100 mwh = 700 kg/MWh)
set -eu
NOW=$(date '+%Y-%m-%dT%H:%M:%S')

echo "[1] Sending CRITICAL grid-load event (VN_NORTH 95% load)..."
echo "VN_NORTH:{\"region_code\":\"VN_NORTH\",\"load_mw\":11400.0,\"capacity_mw\":12000.0,\"is_peak_hour\":true,\"event_time\":\"$NOW\"}" \
    | docker exec -i kafka kafka-console-producer \
        --bootstrap-server localhost:9092 \
        --topic grid-load \
        --property "parse.key=true" --property "key.separator=:" >/dev/null
echo "    ✓ sent"

echo "[2] Sending WARNING grid-load event (VN_CENTRAL 83% load)..."
echo "VN_CENTRAL:{\"region_code\":\"VN_CENTRAL\",\"load_mw\":5000.0,\"capacity_mw\":6000.0,\"is_peak_hour\":true,\"event_time\":\"$NOW\"}" \
    | docker exec -i kafka kafka-console-producer \
        --bootstrap-server localhost:9092 \
        --topic grid-load \
        --property "parse.key=true" --property "key.separator=:" >/dev/null
echo "    ✓ sent"

echo "[3] Sending high-intensity emission event (VN_SOUTH 700 kg/MWh)..."
echo "VN_SOUTH:{\"region_code\":\"VN_SOUTH\",\"co2_kg\":70000.0,\"energy_mwh\":100.0,\"event_time\":\"$NOW\"}" \
    | docker exec -i kafka kafka-console-producer \
        --bootstrap-server localhost:9092 \
        --topic emission \
        --property "parse.key=true" --property "key.separator=:" >/dev/null
echo "    ✓ sent"

echo ""
echo "[INFO] Sleeping 8s for Flink to process..."
sleep 8

docker exec -i postgres-database psql -U postgres -d fuel_prices <<'PSQL'
\echo '===== Alerts generated in last minute (by metric_type) ====='
SELECT id, metric_type, severity, region, ROUND(triggered_price::numeric,2) AS value,
       ROUND(threshold::numeric,0) AS threshold, alert_timestamp
FROM alerts
WHERE alert_timestamp > NOW() - INTERVAL '1 minute'
ORDER BY alert_timestamp DESC;

\echo ''
\echo '===== New recommendations in last minute ====='
SELECT id, pillar, action_type, severity, title, suggested_at
FROM recommendations
WHERE suggested_at > NOW() - INTERVAL '1 minute'
ORDER BY suggested_at DESC;
PSQL
