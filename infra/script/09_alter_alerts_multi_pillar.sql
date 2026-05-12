-- =============================================================
--  09_alter_alerts_multi_pillar.sql  -- Phase 4
--  Mục tiêu: Cho phép bảng alerts lưu cảnh báo từ Pillar 3 (grid-load)
--  và Pillar 4 (emission) bên cạnh Pillar 2 (fuel-prices).
--
--  Tương thích ngược: rows cũ default metric_type = 'FUEL_PRICE'.
-- =============================================================

-- 1. Thêm cột metric_type (mặc định FUEL_PRICE để rows cũ vẫn hợp lệ)
ALTER TABLE alerts
    ADD COLUMN IF NOT EXISTS metric_type VARCHAR(50) NOT NULL DEFAULT 'FUEL_PRICE';

-- 2. Cho phép fuel_type / location NULL với non-FUEL_PRICE alerts
ALTER TABLE alerts ALTER COLUMN fuel_type DROP NOT NULL;
ALTER TABLE alerts ALTER COLUMN location  DROP NOT NULL;

-- 3. Constraint hợp lệ metric_type (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'chk_alerts_metric_type') THEN
        ALTER TABLE alerts ADD CONSTRAINT chk_alerts_metric_type
            CHECK (metric_type IN ('FUEL_PRICE','INVENTORY_DAYS','GRID_LOAD_PCT','RENEWABLE_PCT','EMISSION_INTENSITY'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_alerts_metric_type ON alerts(metric_type);

COMMENT ON COLUMN alerts.metric_type IS 'FUEL_PRICE | INVENTORY_DAYS | GRID_LOAD_PCT | RENEWABLE_PCT | EMISSION_INTENSITY';
COMMENT ON COLUMN alerts.fuel_type   IS 'NULL khi metric_type khác FUEL_PRICE (Pillar 3/4 không có fuel_type).';
COMMENT ON COLUMN alerts.location    IS 'NULL khi alert ở mức region (Pillar 3/4). Pillar 2 vẫn dùng tên sàn.';

-- 4. Re-create v_active_alerts kèm cột metric_type (giữ nguyên các cột cũ).
DROP VIEW IF EXISTS v_active_alerts;
CREATE VIEW v_active_alerts AS
SELECT
    a.id,
    a.rule_id,
    r.rule_name,
    a.metric_type,
    a.fuel_type,
    a.location,
    a.region,
    a.triggered_price,
    a.threshold,
    a.operator,
    a.severity,
    a.message,
    a.event_timestamp,
    a.alert_timestamp,
    EXTRACT(EPOCH FROM (NOW() - a.alert_timestamp))::INT AS age_seconds
FROM alerts a
LEFT JOIN alert_rules r ON r.id = a.rule_id
WHERE a.acknowledged = FALSE
ORDER BY a.alert_timestamp DESC;

COMMENT ON VIEW v_active_alerts IS 'Cảnh báo chưa acknowledge (mọi metric_type). Sort newest first.';
