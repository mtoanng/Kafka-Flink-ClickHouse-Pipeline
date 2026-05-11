-- =============================================================
--  VES Seed Data — SECURITY OUTPUT FEATURES  (Phase 2.6)
--  Chạy sau 07_init_security_features.sql
--
--  Idempotent: WHERE NOT EXISTS — chạy nhiều lần không trùng.
--
--  Nội dung:
--    A. 5 alert_rules multi-pillar (Pillar 1/3/4) — Flink Phase 3 sẽ đọc
--    B. 3 sample recommendations PENDING — để JavaFX UI có data demo
-- =============================================================

-- =============================================================
-- A. ALERT RULES MULTI-PILLAR (5 rule mới, vẫn idempotent theo rule_name)
-- =============================================================
INSERT INTO alert_rules
    (rule_name, metric_type, fuel_type, region_code, operator, threshold, severity, enabled, created_by)
SELECT * FROM (VALUES
    -- Pillar 1: Inventory rules
    ('Pillar1 - Dự trữ dưới 30 ngày (CRITICAL)',
        'INVENTORY_DAYS', NULL::VARCHAR, NULL::VARCHAR, '<', 30.0,
        'CRITICAL', TRUE, (SELECT id FROM users WHERE username='admin')),
    ('Pillar1 - Dự trữ dưới 60 ngày (WARNING)',
        'INVENTORY_DAYS', NULL::VARCHAR, NULL::VARCHAR, '<', 60.0,
        'WARNING',  TRUE, (SELECT id FROM users WHERE username='admin')),

    -- Pillar 3: Grid load rules
    ('Pillar3 - Phụ tải lưới điện vượt 90% (CRITICAL)',
        'GRID_LOAD_PCT', NULL::VARCHAR, NULL::VARCHAR, '>', 90.0,
        'CRITICAL', TRUE, (SELECT id FROM users WHERE username='admin')),
    ('Pillar3 - Phụ tải lưới điện vượt 80% (WARNING)',
        'GRID_LOAD_PCT', NULL::VARCHAR, NULL::VARCHAR, '>', 80.0,
        'WARNING',  TRUE, (SELECT id FROM users WHERE username='admin')),

    -- Pillar 4: Emission intensity rule
    ('Pillar4 - Intensity CO2 vượt 600 kg/MWh (WARNING)',
        'EMISSION_INTENSITY', NULL::VARCHAR, NULL::VARCHAR, '>', 600.0,
        'WARNING',  TRUE, (SELECT id FROM users WHERE username='admin'))
) AS t(rule_name, metric_type, fuel_type, region_code, operator, threshold, severity, enabled, created_by)
WHERE NOT EXISTS (
    SELECT 1 FROM alert_rules ar WHERE ar.rule_name = t.rule_name
);

-- =============================================================
-- B. SAMPLE RECOMMENDATIONS (3 row PENDING — để UI demo có data ngay)
-- =============================================================
-- Idempotent qua title — chạy lại không duplicate.
INSERT INTO recommendations
    (pillar, action_type, severity, title, message, suggested_data, status, expires_at)
SELECT * FROM (VALUES
    -- Pillar 1: stock transfer
    (1::SMALLINT,
     'TRANSFER_STOCK',
     'CRITICAL',
     'Đề xuất điều chuyển DIESEL từ VN_NORTH sang VN_SOUTH'::VARCHAR(200),
     'VN_SOUTH chỉ còn 25 ngày dự trữ DIESEL. Đề xuất điều chuyển 100,000 kl từ VN_NORTH (đang có 55 ngày) trong vòng 7 ngày tới để đạt mức an toàn 30 ngày.',
     '{"from_region":"VN_NORTH","to_region":"VN_SOUTH","fuel_type":"DIESEL","volume_kl":100000,"deadline_days":7}'::JSONB,
     'PENDING'::VARCHAR(20),
     (NOW() + INTERVAL '7 days')::TIMESTAMP),

    -- Pillar 2: reserve buildup
    (2::SMALLINT,
     'INCREASE_RESERVE',
     'WARNING',
     'WTI biến động cao — cân nhắc đẩy mạnh nhập khẩu trước biến động giá',
     'Giá WTI tăng 5% trong 24h qua. Đề xuất cân nhắc tăng nhập khẩu thêm 50,000 kl GASOLINE trong tuần tới để hedge biến động.',
     '{"trigger":"WTI_VOLATILITY","fuel_type":"GASOLINE","additional_volume_kl":50000}'::JSONB,
     'PENDING'::VARCHAR(20),
     (NOW() + INTERVAL '5 days')::TIMESTAMP),

    -- Pillar 3: peak shaving preparation
    (3::SMALLINT,
     'PEAK_SHAVING_PREP',
     'INFO',
     'Chuẩn bị peak shaving khung giờ 18-22h hôm nay',
     'Dự báo phụ tải miền Nam có thể đạt 85-92% công suất trong khung 18-22h. Đề xuất chuẩn bị: (1) thông báo tiết kiệm điện qua truyền thông, (2) sẵn sàng kích hoạt DR aggregator giảm 200-300 MW.',
     '{"region":"VN_SOUTH","time_window":"18:00-22:00","expected_load_pct":[85,92],"prepared_reduction_mw":300}'::JSONB,
     'PENDING'::VARCHAR(20),
     (NOW() + INTERVAL '1 day')::TIMESTAMP)
) AS t(pillar, action_type, severity, title, message, suggested_data, status, expires_at)
WHERE NOT EXISTS (
    SELECT 1 FROM recommendations r WHERE r.title = t.title
);

-- =============================================================
-- Verification log
-- =============================================================
DO $$
DECLARE
    v_rules_total INT;
    v_rules_p1    INT;
    v_rules_p3    INT;
    v_rules_p4    INT;
    v_recs        INT;
    v_score       DECIMAL(5,2);
BEGIN
    SELECT COUNT(*) INTO v_rules_total FROM alert_rules;
    SELECT COUNT(*) INTO v_rules_p1    FROM alert_rules WHERE metric_type='INVENTORY_DAYS';
    SELECT COUNT(*) INTO v_rules_p3    FROM alert_rules WHERE metric_type='GRID_LOAD_PCT';
    SELECT COUNT(*) INTO v_rules_p4    FROM alert_rules WHERE metric_type='EMISSION_INTENSITY';
    SELECT COUNT(*) INTO v_recs        FROM recommendations;
    SELECT overall_score INTO v_score  FROM v_security_score LIMIT 1;
    RAISE NOTICE 'VES security features seeded: rules=% (P1=%, P3=%, P4=%, rest FUEL_PRICE), recommendations=%, current_security_score=%',
        v_rules_total, v_rules_p1, v_rules_p3, v_rules_p4, v_recs, v_score;
END $$;
