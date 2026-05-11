-- =============================================================
--  VES Seed Data  (Phase 2)
--  Chạy sau 03_init_alerts.sql
--
--  Idempotent: ON CONFLICT DO NOTHING — chạy nhiều lần không lỗi.
--  Mật khẩu BCrypt được sinh bằng Python bcrypt (rounds=10), tương
--  thích với Spring Security BCryptPasswordEncoder.
-- =============================================================

-- =============================================================
-- SEED users
--   admin   / admin    (ADMIN)
--   manager / manager  (MANAGER)
--   user    / user     (VIEWER)
-- =============================================================
INSERT INTO users (username, password_hash, full_name, email, role) VALUES
    ('admin',   '$2b$10$Oja1u17iYTrz6O/NqzeOtOZrBpXqxDCVgJ4Agcu/Tt.a4Exo2d2PS', 'System Administrator', 'admin@ves.local',   'ADMIN'),
    ('manager', '$2b$10$oriHZsP0ve0aapnST8Dwguid5abaGsz523HSdEePGvDj/CXSQd4iK', 'Energy Manager',       'manager@ves.local', 'MANAGER'),
    ('user',    '$2b$10$oIRgkU0riBFTJSeX85LRPe0VsV5PaTbpzgHyiU28zztOcbjNQ6bSa', 'Read-only Viewer',     'user@ves.local',    'VIEWER')
ON CONFLICT (username) DO NOTHING;

-- =============================================================
-- SEED regions — 3 miền VN + 3 nhóm quốc tế
-- =============================================================
INSERT INTO regions (code, name, vn_zone, country_code, description) VALUES
    ('VN_NORTH',   'Miền Bắc Việt Nam',     'BAC',   'VN', 'Hà Nội, Hải Phòng, Quảng Ninh, Bắc Ninh'),
    ('VN_CENTRAL', 'Miền Trung Việt Nam',   'TRUNG', 'VN', 'Đà Nẵng, Khánh Hòa, Quảng Nam'),
    ('VN_SOUTH',   'Miền Nam Việt Nam',     'NAM',   'VN', 'TP.HCM, Vũng Tàu, Đồng Nai, Bình Dương'),
    ('INTL_NA',    'North America',         NULL,    'US', 'NYMEX (WTI), New York, Houston'),
    ('INTL_EU',    'Europe',                NULL,    'GB', 'ICE (Brent), London'),
    ('INTL_ASIA',  'Asia Pacific',          NULL,    'SG', 'Singapore, Dubai (Brent), Tokyo')
ON CONFLICT (code) DO NOTHING;

-- =============================================================
-- SEED alert_rules — 5 quy tắc mẫu (Phase 3 sẽ load)
--   Lưu ý: created_by = SELECT id user admin (subquery) để tránh hard-code id
-- =============================================================
INSERT INTO alert_rules (rule_name, fuel_type, location, operator, threshold, severity, enabled, created_by)
SELECT * FROM (VALUES
    ('WTI vượt 90 USD/barrel',          'WTI_CRUDE',    NULL,           '>',  90.0000,   'WARNING',  TRUE,  (SELECT id FROM users WHERE username = 'admin')),
    ('WTI vượt 100 USD/barrel',         'WTI_CRUDE',    NULL,           '>',  100.0000,  'CRITICAL', TRUE,  (SELECT id FROM users WHERE username = 'admin')),
    ('Brent vượt 95 USD/barrel',        'BRENT_CRUDE',  NULL,           '>',  95.0000,   'WARNING',  TRUE,  (SELECT id FROM users WHERE username = 'admin')),
    ('Gasoline NY vượt 3 USD/gallon',   'GASOLINE',     'New York',     '>',  3.0000,    'WARNING',  TRUE,  (SELECT id FROM users WHERE username = 'admin')),
    ('Diesel London dưới 2 USD/gallon', 'DIESEL',       'London',       '<',  2.0000,    'INFO',     TRUE,  (SELECT id FROM users WHERE username = 'admin'))
) AS t(rule_name, fuel_type, location, operator, threshold, severity, enabled, created_by)
WHERE NOT EXISTS (
    SELECT 1 FROM alert_rules ar WHERE ar.rule_name = t.rule_name
);

-- =============================================================
-- Verification queries (in stdout khi container init lần đầu)
-- =============================================================
DO $$
DECLARE
    v_users  INT;
    v_regs   INT;
    v_rules  INT;
BEGIN
    SELECT COUNT(*) INTO v_users FROM users;
    SELECT COUNT(*) INTO v_regs  FROM regions;
    SELECT COUNT(*) INTO v_rules FROM alert_rules;
    RAISE NOTICE 'VES seed loaded: % users, % regions, % alert_rules', v_users, v_regs, v_rules;
END $$;
