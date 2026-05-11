-- =============================================================
--  VES Schema: ALERT_RULES + ALERTS  (Phase 2)
--  Chạy sau 02_init_users_regions.sql
--
--  Note về bảng alerts:
--    Bảng fuel_price_alerts (có sẵn ở 01_init_fuel_schema.sql) được Flink
--    ghi tự động khi phát hiện biến động > 3%. Đó là alerts "auto" (legacy).
--    Bảng alerts mới này dùng cho Phase 3 (Flink AlertDetectionFunction)
--    với rule-based detection dựa trên alert_rules đã định nghĩa.
-- =============================================================

-- =============================================================
-- BẢNG alert_rules — Quy tắc cảnh báo do người dùng cấu hình
-- =============================================================
CREATE TABLE IF NOT EXISTS alert_rules (
    id              BIGSERIAL    PRIMARY KEY,
    rule_name       VARCHAR(150) NOT NULL,
    fuel_type       VARCHAR(50)  NOT NULL,                    -- WTI_CRUDE | BRENT_CRUDE | GASOLINE | DIESEL | NATURAL_GAS | * (wildcard)
    location        VARCHAR(100),                              -- NULL = mọi địa điểm
    operator        VARCHAR(5)   NOT NULL,                    -- '>', '<', '>=', '<=', '='
    threshold       DECIMAL(12,4) NOT NULL,
    severity        VARCHAR(20)  NOT NULL DEFAULT 'WARNING',  -- INFO | WARNING | CRITICAL
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by      BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_alert_rules_operator CHECK (operator IN ('>', '<', '>=', '<=', '=')),
    CONSTRAINT chk_alert_rules_severity CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL'))
);

CREATE INDEX IF NOT EXISTS idx_alert_rules_fuel_type ON alert_rules(fuel_type);
CREATE INDEX IF NOT EXISTS idx_alert_rules_enabled   ON alert_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_alert_rules_severity  ON alert_rules(severity);

COMMENT ON TABLE alert_rules IS 'Quy tắc cảnh báo do người dùng cấu hình (Phase 3 sẽ load vào Flink).';
COMMENT ON COLUMN alert_rules.fuel_type IS 'Loại nhiên liệu cần monitor. ''*'' = tất cả.';

-- =============================================================
-- BẢNG alerts — Cảnh báo được Flink tạo theo rule trong alert_rules
-- =============================================================
CREATE TABLE IF NOT EXISTS alerts (
    id              BIGSERIAL    PRIMARY KEY,
    rule_id         BIGINT       NOT NULL REFERENCES alert_rules(id) ON DELETE CASCADE,
    fuel_type       VARCHAR(50)  NOT NULL,
    location        VARCHAR(100) NOT NULL,
    region          VARCHAR(50),
    triggered_price DECIMAL(12,4) NOT NULL,
    threshold       DECIMAL(12,4) NOT NULL,                   -- snapshot threshold tại thời điểm trigger
    operator        VARCHAR(5)   NOT NULL,                    -- snapshot operator
    severity        VARCHAR(20)  NOT NULL,
    message         TEXT,
    event_timestamp TIMESTAMP    NOT NULL,                    -- thời điểm event Kafka gốc
    alert_timestamp TIMESTAMP    NOT NULL DEFAULT NOW(),      -- thời điểm Flink tạo alert
    acknowledged    BOOLEAN      NOT NULL DEFAULT FALSE,
    acknowledged_at TIMESTAMP,
    acknowledged_by BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_alerts_severity CHECK (severity IN ('INFO', 'WARNING', 'CRITICAL'))
);

CREATE INDEX IF NOT EXISTS idx_alerts_alert_ts       ON alerts(alert_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_severity       ON alerts(severity);
CREATE INDEX IF NOT EXISTS idx_alerts_fuel_type      ON alerts(fuel_type);
CREATE INDEX IF NOT EXISTS idx_alerts_acknowledged   ON alerts(acknowledged);
CREATE INDEX IF NOT EXISTS idx_alerts_rule_id        ON alerts(rule_id);

COMMENT ON TABLE alerts IS 'Cảnh báo rule-based (Phase 3 Flink + Phase 4 REST API quản lý acknowledge).';

-- =============================================================
-- VIEW v_active_alerts — Cảnh báo chưa acknowledge, dùng cho dashboard
-- =============================================================
CREATE OR REPLACE VIEW v_active_alerts AS
SELECT
    a.id,
    a.rule_id,
    r.rule_name,
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

COMMENT ON VIEW v_active_alerts IS 'Cảnh báo chưa được acknowledge, mới nhất trước.';
