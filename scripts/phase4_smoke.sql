-- =============================================================
--  Phase 4 — Smoke test queries
-- =============================================================

\echo '===== RAW TABLE COUNTS ====='
SELECT 'grid_load_raw'        AS tbl, COUNT(*) AS rows FROM grid_load_raw
UNION ALL SELECT 'renewable_output_raw', COUNT(*) FROM renewable_output_raw
UNION ALL SELECT 'emission_raw',         COUNT(*) FROM emission_raw
UNION ALL SELECT 'fuel_prices_raw',      COUNT(*) FROM fuel_prices_raw
ORDER BY tbl;

\echo ''
\echo '===== Pillar 3 — Latest grid load per region ====='
SELECT region_code,
       ROUND(load_mw::numeric,1)     AS load_mw,
       ROUND(capacity_mw::numeric,0) AS capacity,
       ROUND(load_pct::numeric,1)    AS load_pct,
       is_peak_hour,
       event_time
FROM (
    SELECT *, ROW_NUMBER() OVER (PARTITION BY region_code ORDER BY event_time DESC) AS rn
    FROM grid_load_raw
) t WHERE rn = 1
ORDER BY region_code;

\echo ''
\echo '===== Pillar 4 — Latest renewable output (3 region x 3 source) ====='
SELECT region_code, source_type,
       ROUND(output_mw::numeric,1)     AS output_mw,
       ROUND(utilization_pct::numeric,1) AS util_pct,
       event_time
FROM (
    SELECT *, ROW_NUMBER() OVER (PARTITION BY region_code, source_type ORDER BY event_time DESC) AS rn
    FROM renewable_output_raw
) t WHERE rn = 1
ORDER BY region_code, source_type;

\echo ''
\echo '===== Pillar 4 — Latest emission per region ====='
SELECT region_code,
       ROUND(co2_kg::numeric,0)              AS co2_kg,
       ROUND(energy_mwh::numeric,1)          AS energy_mwh,
       ROUND(intensity_kg_per_mwh::numeric,1) AS intensity_kg_per_mwh,
       event_time
FROM (
    SELECT *, ROW_NUMBER() OVER (PARTITION BY region_code ORDER BY event_time DESC) AS rn
    FROM emission_raw
) t WHERE rn = 1
ORDER BY region_code;

\echo ''
\echo '===== Active alerts by metric_type ====='
SELECT metric_type, severity, COUNT(*) AS alerts, MAX(alert_timestamp) AS latest
FROM alerts
WHERE alert_timestamp > NOW() - INTERVAL '5 minutes'
GROUP BY metric_type, severity
ORDER BY metric_type, severity;

\echo ''
\echo '===== Recommendations (active pillar+action) ====='
SELECT pillar, action_type, severity, status, COUNT(*) AS rec_count, MAX(suggested_at) AS latest
FROM recommendations
WHERE status = 'PENDING'
GROUP BY pillar, action_type, severity, status
ORDER BY pillar, severity DESC;

\echo ''
\echo '===== Security Score (4-pillar weighted) ====='
SELECT pillar1_score, pillar2_score, pillar3_score, pillar4_score, overall_score, status
FROM v_security_score;

\echo ''
\echo '===== Cascade Risks ====='
SELECT risk_type, severity, LEFT(description, 100) AS description
FROM v_cascade_risks
LIMIT 10;
