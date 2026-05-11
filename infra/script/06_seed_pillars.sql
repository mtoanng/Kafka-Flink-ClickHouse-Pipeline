-- =============================================================
--  VES Seed Data — 4 PILLARS (Phase 2.5)
--  Chạy sau 05_init_pillars.sql
--
--  Idempotent: WHERE NOT EXISTS — chạy nhiều lần không trùng.
--
--  Chỉ seed PILLAR 1 (fuel_inventory_raw): dự trữ chiến lược ít biến động,
--  hợp lý để dùng làm seed tĩnh. Mix các mức stock_days để demo cả 4 status:
--     - VN_SOUTH DIESEL    :  25 days  → CRITICAL    (< 30)
--     - VN_NORTH GASOLINE  :  30 days  → WARNING     (< 60)
--     - VN_NORTH DIESEL    :  55 days  → WARNING     (< 60)
--     - VN_CENTRAL GASOLINE:  75 days  → BELOW_TARGET (< 90)
--     - VN_CENTRAL DIESEL  :  90 days  → OK          (= target)
--     - VN_SOUTH GASOLINE  :  95 days  → OK          (> target)
--
--  Các bảng Pillar 3 (grid_load_raw) và Pillar 4 (renewable_output_raw,
--  emission_raw) sẽ được data live từ generator Phase 4 — KHÔNG seed.
-- =============================================================

INSERT INTO fuel_inventory_raw
    (region_code, fuel_type, stock_volume_kl, daily_consumption_kl, target_days)
SELECT * FROM (VALUES
    ('VN_NORTH',   'GASOLINE', 225000.00,  7500.00, 90),  -- 30 days  → WARNING
    ('VN_NORTH',   'DIESEL',   330000.00,  6000.00, 90),  -- 55 days  → WARNING
    ('VN_CENTRAL', 'GASOLINE', 225000.00,  3000.00, 90),  -- 75 days  → BELOW_TARGET
    ('VN_CENTRAL', 'DIESEL',   225000.00,  2500.00, 90),  -- 90 days  → OK (đúng target)
    ('VN_SOUTH',   'GASOLINE', 1140000.00, 12000.00, 90), -- 95 days  → OK (vượt target)
    ('VN_SOUTH',   'DIESEL',   250000.00,  10000.00, 90)  -- 25 days  → CRITICAL
) AS t(region_code, fuel_type, stock_volume_kl, daily_consumption_kl, target_days)
WHERE NOT EXISTS (
    SELECT 1 FROM fuel_inventory_raw fi
    WHERE fi.region_code = t.region_code
      AND fi.fuel_type   = t.fuel_type
);

-- =============================================================
-- Verification log
-- =============================================================
DO $$
DECLARE
    v_inv     INT;
    v_grid    INT;
    v_renew   INT;
    v_emit    INT;
BEGIN
    SELECT COUNT(*) INTO v_inv   FROM fuel_inventory_raw;
    SELECT COUNT(*) INTO v_grid  FROM grid_load_raw;
    SELECT COUNT(*) INTO v_renew FROM renewable_output_raw;
    SELECT COUNT(*) INTO v_emit  FROM emission_raw;
    RAISE NOTICE 'VES 4-pillar seed loaded: inventory=%, grid_load=%, renewable=%, emission=% (grid/renew/emit data from Phase 4 generators)',
        v_inv, v_grid, v_renew, v_emit;
END $$;
