\echo '===== Inserts in the last 60 seconds per raw table ====='
SELECT 'grid_load_raw'        AS tbl, COUNT(*) AS recent_60s FROM grid_load_raw        WHERE ingestion_time > NOW() - INTERVAL '60 seconds'
UNION ALL SELECT 'renewable_output_raw', COUNT(*) FROM renewable_output_raw WHERE ingestion_time > NOW() - INTERVAL '60 seconds'
UNION ALL SELECT 'emission_raw',         COUNT(*) FROM emission_raw         WHERE ingestion_time > NOW() - INTERVAL '60 seconds'
UNION ALL SELECT 'fuel_prices_raw',      COUNT(*) FROM fuel_prices_raw      WHERE ingestion_time > NOW() - INTERVAL '60 seconds'
ORDER BY tbl;

\echo ''
\echo '===== TOTAL row counts ====='
SELECT 'grid_load_raw' AS tbl, COUNT(*) AS total_rows FROM grid_load_raw
UNION ALL SELECT 'renewable_output_raw', COUNT(*) FROM renewable_output_raw
UNION ALL SELECT 'emission_raw',         COUNT(*) FROM emission_raw
UNION ALL SELECT 'fuel_prices_raw',      COUNT(*) FROM fuel_prices_raw
UNION ALL SELECT 'alerts (all metric_types)', COUNT(*) FROM alerts
UNION ALL SELECT 'recommendations (PENDING)', COUNT(*) FROM recommendations WHERE status='PENDING'
ORDER BY tbl;

\echo ''
\echo '===== Alerts grouped by metric_type (all-time) ====='
SELECT metric_type, severity, COUNT(*) AS alerts FROM alerts GROUP BY metric_type, severity ORDER BY metric_type, severity;

\echo ''
\echo '===== Recent recommendations by pillar (last 1 hour) ====='
SELECT pillar, action_type, severity, COUNT(*) AS cnt, MAX(suggested_at) AS latest
FROM recommendations
WHERE suggested_at > NOW() - INTERVAL '1 hour'
GROUP BY pillar, action_type, severity ORDER BY pillar, severity DESC;
