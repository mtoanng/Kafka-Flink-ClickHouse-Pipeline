-- =============================================================
--  VES Schema: ENERGY SECURITY OUTPUT FEATURES  (Phase 2.6)
--  Chạy sau 06_seed_pillars.sql
--
--  Mục tiêu: TỪ "chỉ vẽ số liệu" → "actionable security platform".
--  Cung cấp 4 nhóm output:
--    1. DETECT  — alert_rules đa pillar (extend bảng có sẵn)
--    2. PREDICT — supply outlook, volatility signal
--    3. RECOMMEND — bảng recommendations + view ưu tiên hành động
--    4. SCORE   — Energy Security Score (light, không cần Flink phức tạp)
--
--  Backward-compat: tất cả thay đổi non-destructive (ADD COLUMN IF NOT
--  EXISTS, CREATE OR REPLACE VIEW). Rules cũ ở 04_seed_basic.sql tự
--  động lấy metric_type='FUEL_PRICE' (default).
-- =============================================================

-- =============================================================
-- 1. EXTEND alert_rules — multi-pillar metric_type
-- =============================================================
ALTER TABLE alert_rules
    ADD COLUMN IF NOT EXISTS metric_type VARCHAR(50) NOT NULL DEFAULT 'FUEL_PRICE',
    ADD COLUMN IF NOT EXISTS region_code VARCHAR(20) REFERENCES regions(code) ON DELETE SET NULL;

-- Quan trọng: fuel_type hiện đang NOT NULL — relax thành NULLABLE
-- để các rule Pillar 3/4 (GRID_LOAD_PCT, RENEWABLE_PCT, EMISSION_INTENSITY)
-- không cần điền fuel_type.
ALTER TABLE alert_rules
    ALTER COLUMN fuel_type DROP NOT NULL;

-- Constraint: metric_type phải hợp lệ
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'chk_alert_rules_metric_type' AND conrelid = 'alert_rules'::regclass
    ) THEN
        ALTER TABLE alert_rules ADD CONSTRAINT chk_alert_rules_metric_type
            CHECK (metric_type IN ('FUEL_PRICE','INVENTORY_DAYS','GRID_LOAD_PCT','RENEWABLE_PCT','EMISSION_INTENSITY'));
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_alert_rules_metric_type ON alert_rules(metric_type);
CREATE INDEX IF NOT EXISTS idx_alert_rules_region_code ON alert_rules(region_code);

COMMENT ON COLUMN alert_rules.metric_type IS 'FUEL_PRICE | INVENTORY_DAYS | GRID_LOAD_PCT | RENEWABLE_PCT | EMISSION_INTENSITY';
COMMENT ON COLUMN alert_rules.region_code IS 'NULL = mọi region. Dùng cho rule Pillar 1/3/4.';

-- =============================================================
-- 2. BẢNG recommendations — Action suggestions audit trail
-- =============================================================
-- Mỗi recommendation = 1 gợi ý hành động mà hệ thống (Flink/SQL job)
-- hoặc admin sinh ra. Admin có thể ACK hoặc DISMISS qua REST API.
CREATE TABLE IF NOT EXISTS recommendations (
    id              BIGSERIAL    PRIMARY KEY,
    pillar          SMALLINT     NOT NULL,                       -- 1-4, hoặc 0 cho cross-pillar
    action_type     VARCHAR(50)  NOT NULL,                       -- TRANSFER_STOCK | INCREASE_RESERVE | PEAK_SHAVING | ...
    severity        VARCHAR(20)  NOT NULL DEFAULT 'INFO',        -- INFO | WARNING | CRITICAL
    title           VARCHAR(200) NOT NULL,
    message         TEXT         NOT NULL,
    suggested_data  JSONB,                                       -- payload cấu trúc (region, mw, ngày, etc.)
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',     -- PENDING | ACKNOWLEDGED | DISMISSED | EXPIRED
    suggested_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    acknowledged_at TIMESTAMP,
    acknowledged_by BIGINT       REFERENCES users(id) ON DELETE SET NULL,
    note            TEXT,                                        -- ghi chú admin khi ACK/DISMISS
    expires_at      TIMESTAMP,                                   -- recommendation tự EXPIRED sau thời gian này
    CONSTRAINT chk_rec_pillar   CHECK (pillar BETWEEN 0 AND 4),
    CONSTRAINT chk_rec_severity CHECK (severity IN ('INFO','WARNING','CRITICAL')),
    CONSTRAINT chk_rec_status   CHECK (status IN ('PENDING','ACKNOWLEDGED','DISMISSED','EXPIRED'))
);

CREATE INDEX IF NOT EXISTS idx_rec_status        ON recommendations(status);
CREATE INDEX IF NOT EXISTS idx_rec_pillar        ON recommendations(pillar);
CREATE INDEX IF NOT EXISTS idx_rec_suggested_at  ON recommendations(suggested_at DESC);
CREATE INDEX IF NOT EXISTS idx_rec_severity      ON recommendations(severity);

COMMENT ON TABLE  recommendations IS 'Gợi ý hành động an ninh năng lượng. Admin acknowledge/dismiss qua REST API.';
COMMENT ON COLUMN recommendations.suggested_data IS 'JSONB payload có cấu trúc — UI render thành action button.';

-- =============================================================
-- 3. BẢNG daily_briefings — Bản tin tổng hợp daily
-- =============================================================
CREATE TABLE IF NOT EXISTS daily_briefings (
    id                  BIGSERIAL    PRIMARY KEY,
    briefing_date       DATE         NOT NULL UNIQUE,
    security_score      DECIMAL(5,2) NOT NULL,                   -- 0-100
    score_trend         VARCHAR(10)  NOT NULL DEFAULT 'STABLE',  -- UP | DOWN | STABLE
    headline            VARCHAR(300) NOT NULL,
    pillar1_summary     TEXT,
    pillar2_summary     TEXT,
    pillar3_summary     TEXT,
    pillar4_summary     TEXT,
    key_risks           JSONB,                                   -- list rủi ro hàng đầu
    recommendations_count INT       NOT NULL DEFAULT 0,
    active_alerts_count INT          NOT NULL DEFAULT 0,
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_briefing_score CHECK (security_score BETWEEN 0 AND 100),
    CONSTRAINT chk_briefing_trend CHECK (score_trend IN ('UP','DOWN','STABLE'))
);

CREATE INDEX IF NOT EXISTS idx_briefing_date ON daily_briefings(briefing_date DESC);

COMMENT ON TABLE daily_briefings IS 'Bản tin daily auto-generated bởi REST API scheduled job (00:05 mỗi ngày).';

-- =============================================================
-- 4. VIEW v_pillar1_supply_outlook — Forecast + Recommendation
-- =============================================================
-- Tính các chỉ số "tương lai" + sinh recommendation_text động.
-- Với mỗi (region, fuel): days_remaining = stock_days hiện tại.
-- Để forecast trong điều kiện không nhập gì thêm, cứ giả định consumption
-- giữ nguyên (simple linear extrapolation) → days_to_critical = stock_days - 30.
CREATE OR REPLACE VIEW v_pillar1_supply_outlook AS
WITH stock AS (
    SELECT
        i.region_code,
        r.name      AS region_name,
        r.vn_zone,
        i.fuel_type,
        i.stock_volume_kl,
        i.daily_consumption_kl,
        i.stock_days,
        i.target_days,
        i.reported_at
    FROM fuel_inventory_raw i
    JOIN regions r ON r.code = i.region_code
),
-- Tìm region nguồn (có stock_days cao nhất cùng fuel_type) để đề xuất điều chuyển
best_donor AS (
    SELECT
        fuel_type,
        region_code AS donor_region,
        stock_days  AS donor_stock_days
    FROM (
        SELECT
            fuel_type,
            region_code,
            stock_days,
            ROW_NUMBER() OVER (PARTITION BY fuel_type ORDER BY stock_days DESC) AS rn
        FROM stock
    ) ranked
    WHERE rn = 1
)
SELECT
    s.region_code,
    s.region_name,
    s.fuel_type,
    s.stock_volume_kl,
    s.daily_consumption_kl,
    s.stock_days,
    s.target_days,
    -- Forecast metrics
    GREATEST(s.stock_days - 30, 0)::DECIMAL(6,1)                    AS days_to_critical,
    GREATEST(s.stock_days - s.target_days, 0)::DECIMAL(6,1)         AS days_above_target,
    ROUND((s.stock_days / s.target_days) * 100, 1)                  AS target_achievement_pct,
    -- Status
    CASE
        WHEN s.stock_days < 30 THEN 'CRITICAL'
        WHEN s.stock_days < 60 THEN 'WARNING'
        WHEN s.stock_days < s.target_days THEN 'BELOW_TARGET'
        ELSE 'OK'
    END                                                              AS status,
    -- Recommendation
    CASE
        WHEN s.stock_days < 30 THEN
            'URGENT: Còn ' || s.stock_days || ' ngày. Đề xuất điều chuyển từ ' || COALESCE(d.donor_region, 'N/A') ||
            ' (đang có ' || COALESCE(d.donor_stock_days::TEXT, '0') || ' ngày) hoặc nhập khẩu khẩn cấp ' ||
            ROUND((30 - s.stock_days) * s.daily_consumption_kl)::TEXT || ' kl.'
        WHEN s.stock_days < 60 THEN
            'Cảnh báo: Tăng tồn kho thêm ' ||
            ROUND((60 - s.stock_days) * s.daily_consumption_kl)::TEXT || ' kl trong 14 ngày tới để đạt mức an toàn 60 ngày.'
        WHEN s.stock_days < s.target_days THEN
            'Tiến độ ' || ROUND((s.stock_days / s.target_days) * 100, 0)::TEXT ||
            '% mục tiêu chiến lược ' || s.target_days || ' ngày. Cần thêm ' ||
            ROUND((s.target_days - s.stock_days) * s.daily_consumption_kl)::TEXT || ' kl.'
        ELSE
            'Đạt mục tiêu (' || s.stock_days || '/' || s.target_days || ' ngày). Theo dõi định kỳ.'
    END                                                              AS recommendation_text,
    COALESCE(d.donor_region, NULL)                                   AS suggested_donor_region,
    s.reported_at
FROM stock s
LEFT JOIN best_donor d
    ON d.fuel_type = s.fuel_type AND d.donor_region <> s.region_code
ORDER BY s.stock_days ASC;

COMMENT ON VIEW v_pillar1_supply_outlook IS 'Pillar 1 actionable: forecast days_to_critical + recommendation điều chuyển/nhập khẩu.';

-- =============================================================
-- 5. VIEW v_pillar2_volatility_signal — Rolling σ + signal
-- =============================================================
-- Tính độ biến động 1h gần nhất cho mỗi (fuel_type, location).
-- relative_volatility = σ/avg * 100. Signal: STABLE/ELEVATED/VOLATILE.
CREATE OR REPLACE VIEW v_pillar2_volatility_signal AS
WITH recent AS (
    SELECT
        fuel_type,
        location,
        price,
        event_timestamp
    FROM fuel_prices_raw
    WHERE event_timestamp >= NOW() - INTERVAL '1 hour'
),
stats AS (
    SELECT
        fuel_type,
        location,
        COUNT(*)              AS sample_count,
        AVG(price)            AS avg_price,
        STDDEV_SAMP(price)    AS sigma,
        MIN(price)            AS min_price,
        MAX(price)            AS max_price,
        MAX(event_timestamp)  AS last_event
    FROM recent
    GROUP BY fuel_type, location
    HAVING COUNT(*) >= 3
)
SELECT
    fuel_type,
    location,
    sample_count,
    ROUND(avg_price, 4)                                             AS avg_price,
    ROUND(sigma,     4)                                             AS sigma,
    CASE WHEN avg_price > 0
        THEN ROUND((sigma / avg_price) * 100, 2)
        ELSE 0
    END                                                              AS relative_volatility_pct,
    ROUND(max_price - min_price, 4)                                 AS range_abs,
    CASE
        WHEN avg_price > 0 AND (sigma / avg_price) > 0.03 THEN 'VOLATILE'
        WHEN avg_price > 0 AND (sigma / avg_price) > 0.01 THEN 'ELEVATED'
        ELSE 'STABLE'
    END                                                              AS signal,
    last_event
FROM stats
ORDER BY relative_volatility_pct DESC;

COMMENT ON VIEW v_pillar2_volatility_signal IS 'Pillar 2 actionable: σ rolling 1h + signal STABLE/ELEVATED/VOLATILE.';

-- =============================================================
-- 6. VIEW v_pillar2_cross_exchange_divergence — Tín hiệu địa chính trị
-- =============================================================
-- Khi cùng 1 fuel_type giao dịch ở nhiều sàn → giá nên gần nhau.
-- Nếu divergence cao bất thường → tín hiệu disruption hoặc arbitrage.
CREATE OR REPLACE VIEW v_pillar2_cross_exchange_divergence AS
WITH latest AS (
    SELECT DISTINCT ON (fuel_type, location)
        fuel_type, location, price, event_timestamp
    FROM fuel_prices_raw
    WHERE event_timestamp >= NOW() - INTERVAL '15 minutes'
    ORDER BY fuel_type, location, event_timestamp DESC
)
SELECT
    fuel_type,
    COUNT(*)                                              AS exchanges_count,
    ROUND(MIN(price), 4)                                  AS min_price,
    ROUND(MAX(price), 4)                                  AS max_price,
    ROUND(AVG(price), 4)                                  AS avg_price,
    ROUND(MAX(price) - MIN(price), 4)                     AS divergence_abs,
    CASE WHEN AVG(price) > 0
        THEN ROUND(((MAX(price) - MIN(price)) / AVG(price)) * 100, 2)
        ELSE 0
    END                                                    AS divergence_pct,
    CASE
        WHEN AVG(price) > 0 AND ((MAX(price) - MIN(price)) / AVG(price)) > 0.05 THEN 'HIGH'
        WHEN AVG(price) > 0 AND ((MAX(price) - MIN(price)) / AVG(price)) > 0.02 THEN 'MODERATE'
        ELSE 'NORMAL'
    END                                                    AS divergence_signal
FROM latest
GROUP BY fuel_type
HAVING COUNT(*) >= 2
ORDER BY divergence_pct DESC;

COMMENT ON VIEW v_pillar2_cross_exchange_divergence IS 'Pillar 2 actionable: divergence giá cùng fuel giữa các sàn (>5%% = HIGH risk).';

-- =============================================================
-- 7. VIEW v_pillar3_load_shedding_plan — Khi load > 90%
-- =============================================================
-- Chỉ trả về row khi load_pct >= 85% (tiệm cận critical).
-- Đề xuất shed_mw = capacity * (load_pct - 80) / 100, ưu tiên region nóng nhất trước.
CREATE OR REPLACE VIEW v_pillar3_load_shedding_plan AS
WITH latest AS (
    SELECT DISTINCT ON (region_code)
        region_code, load_mw, capacity_mw, load_pct, is_peak_hour, event_time
    FROM grid_load_raw
    WHERE event_time >= NOW() - INTERVAL '15 minutes'
    ORDER BY region_code, event_time DESC
)
SELECT
    ROW_NUMBER() OVER (ORDER BY l.load_pct DESC)                     AS priority_level,
    l.region_code,
    r.name                                                            AS region_name,
    l.load_mw,
    l.capacity_mw,
    l.load_pct,
    l.is_peak_hour,
    -- Suggested action
    ROUND(l.capacity_mw * GREATEST(l.load_pct - 80, 0) / 100, 2)     AS suggested_shed_mw,
    CASE
        WHEN l.load_pct >= 95 THEN 'IMMEDIATE_LOAD_SHEDDING'
        WHEN l.load_pct >= 90 THEN 'PEAK_SHAVING'
        WHEN l.load_pct >= 85 THEN 'DEMAND_RESPONSE'
        ELSE 'MONITOR'
    END                                                                AS action_type,
    CASE
        WHEN l.load_pct >= 95 THEN 'CRITICAL: Cắt tải khẩn cấp ' ||
            ROUND(l.capacity_mw * (l.load_pct - 85) / 100)::TEXT || ' MW. Ưu tiên industrial loads phi thiết yếu.'
        WHEN l.load_pct >= 90 THEN 'WARNING: Kích hoạt peak shaving. Đề xuất giảm ' ||
            ROUND(l.capacity_mw * (l.load_pct - 80) / 100)::TEXT || ' MW qua DR aggregators.'
        WHEN l.load_pct >= 85 THEN 'WATCH: Phát thông báo tiết kiệm điện qua media kênh chính thức.'
        ELSE 'Theo dõi định kỳ.'
    END                                                                AS recommendation_text,
    l.event_time
FROM latest l
JOIN regions r ON r.code = l.region_code
WHERE l.load_pct >= 70
ORDER BY l.load_pct DESC;

COMMENT ON VIEW v_pillar3_load_shedding_plan IS 'Pillar 3 actionable: kế hoạch cắt tải khi load_pct ≥ 85%%, ordered by mức nóng.';

-- =============================================================
-- 8. VIEW v_pillar4_net_zero_progress — % renewable vs roadmap
-- =============================================================
-- Roadmap VN (tham khảo Quy hoạch điện 8):
--   - 2026 target ~25% renewable share
--   - 2030 target ~47%
--   - 2050 target ~70% (Net Zero)
-- Tính share từ data 1h gần nhất.
CREATE OR REPLACE VIEW v_pillar4_net_zero_progress AS
WITH renewable_1h AS (
    SELECT
        region_code,
        SUM(output_mw)                                AS renewable_mw
    FROM renewable_output_raw
    WHERE event_time >= NOW() - INTERVAL '1 hour'
    GROUP BY region_code
),
demand_1h AS (
    SELECT
        region_code,
        AVG(load_mw)                                  AS avg_load_mw
    FROM grid_load_raw
    WHERE event_time >= NOW() - INTERVAL '1 hour'
    GROUP BY region_code
)
SELECT
    COALESCE(rn.region_code, d.region_code)                          AS region_code,
    r.name                                                            AS region_name,
    COALESCE(rn.renewable_mw, 0)                                     AS renewable_mw,
    COALESCE(d.avg_load_mw, 0)                                       AS avg_load_mw,
    CASE WHEN COALESCE(d.avg_load_mw, 0) > 0
        THEN ROUND((COALESCE(rn.renewable_mw, 0) / d.avg_load_mw) * 100, 2)
        ELSE 0
    END                                                                AS current_renewable_share_pct,
    25.0                                                              AS target_2026_pct,
    47.0                                                              AS target_2030_pct,
    CASE
        WHEN COALESCE(d.avg_load_mw, 0) = 0 THEN 'NO_DATA'
        WHEN (COALESCE(rn.renewable_mw, 0) / d.avg_load_mw) * 100 >= 47.0 THEN 'AHEAD'
        WHEN (COALESCE(rn.renewable_mw, 0) / d.avg_load_mw) * 100 >= 25.0 THEN 'ON_TRACK'
        ELSE 'BEHIND'
    END                                                                AS status,
    CASE
        WHEN COALESCE(d.avg_load_mw, 0) = 0 THEN 'Chưa có data load (Pillar 3 generator chưa chạy).'
        WHEN (COALESCE(rn.renewable_mw, 0) / d.avg_load_mw) * 100 < 25.0 THEN
            'Đang BEHIND mục tiêu 2026 (25%). Đẩy nhanh nối lưới solar/wind.'
        WHEN (COALESCE(rn.renewable_mw, 0) / d.avg_load_mw) * 100 < 47.0 THEN
            'ON_TRACK mục tiêu 2026. Cần duy trì momentum cho mục tiêu 2030 (47%).'
        ELSE 'AHEAD mục tiêu — cân nhắc đẩy nhanh roadmap Net Zero 2050.'
    END                                                                AS recommendation_text
FROM renewable_1h rn
FULL OUTER JOIN demand_1h d ON rn.region_code = d.region_code
LEFT JOIN regions r ON r.code = COALESCE(rn.region_code, d.region_code)
ORDER BY current_renewable_share_pct DESC;

COMMENT ON VIEW v_pillar4_net_zero_progress IS 'Pillar 4 actionable: % renewable hiện tại vs roadmap VN 2026/2030/2050.';

-- =============================================================
-- 9. VIEW v_security_score — Energy Security Index (LIGHT version)
-- =============================================================
-- Weighted aggregation 4 pillars → 0-100. Mỗi pillar 25%.
-- Khi chưa có data Pillar 3/4 (generator chưa chạy), dùng baseline 70.
CREATE OR REPLACE VIEW v_security_score AS
WITH p1 AS (
    -- Pillar 1: avg stock_days vs target 90 (cap 100)
    SELECT
        LEAST(100, ROUND(AVG(stock_days) / 90.0 * 100, 2))           AS score,
        COUNT(*)                                                       AS samples
    FROM fuel_inventory_raw
),
p2 AS (
    -- Pillar 2: 100 - (avg relative_volatility * 10), clamp 0-100. Stable=high score.
    SELECT
        GREATEST(0, LEAST(100, ROUND(100 - COALESCE(AVG(relative_volatility_pct), 0) * 10, 2)))
                                                                       AS score,
        COUNT(*)                                                       AS samples
    FROM v_pillar2_volatility_signal
),
p3 AS (
    -- Pillar 3: 100 - max_load_pct * 1.1 (overload trừ điểm nặng). No data → 70.
    -- LƯU Ý: GREATEST(0, NULL) trả 0 (không NULL), nên dùng CASE để fallback đúng.
    SELECT
        CASE
            WHEN MAX(load_pct) IS NULL THEN 70::NUMERIC
            ELSE GREATEST(0, ROUND(100 - MAX(load_pct) * 1.1, 2))
        END                                                            AS score,
        COUNT(*)                                                       AS samples
    FROM grid_load_raw
    WHERE event_time >= NOW() - INTERVAL '15 minutes'
),
p4 AS (
    -- Pillar 4: % renewable share, baseline 70 nếu chưa có data
    SELECT
        CASE
            WHEN COUNT(*) = 0 THEN 70::NUMERIC
            ELSE LEAST(100, ROUND(AVG(current_renewable_share_pct) * 2, 2))
        END                                                            AS score,
        COUNT(*)                                                       AS samples
    FROM v_pillar4_net_zero_progress
    WHERE status <> 'NO_DATA'
)
SELECT
    p1.score                                                          AS pillar1_score,
    p2.score                                                          AS pillar2_score,
    p3.score                                                          AS pillar3_score,
    p4.score                                                          AS pillar4_score,
    ROUND((p1.score * 0.25 + p2.score * 0.25 + p3.score * 0.25 + p4.score * 0.25), 2)
                                                                       AS overall_score,
    CASE
        WHEN (p1.score * 0.25 + p2.score * 0.25 + p3.score * 0.25 + p4.score * 0.25) >= 80 THEN 'SECURE'
        WHEN (p1.score * 0.25 + p2.score * 0.25 + p3.score * 0.25 + p4.score * 0.25) >= 60 THEN 'STABLE'
        WHEN (p1.score * 0.25 + p2.score * 0.25 + p3.score * 0.25 + p4.score * 0.25) >= 40 THEN 'AT_RISK'
        ELSE 'CRITICAL'
    END                                                                AS status,
    NOW()                                                              AS computed_at
FROM p1 CROSS JOIN p2 CROSS JOIN p3 CROSS JOIN p4;

COMMENT ON VIEW v_security_score IS 'Energy Security Score (Light ESI): 0-100, weighted 25%% mỗi pillar. Phase 5 dùng làm gauge dashboard.';

-- =============================================================
-- 10. VIEW v_cascade_risks — Compound risk multi-pillar
-- =============================================================
-- Phát hiện rủi ro phức hợp khi nhiều pillar suy yếu đồng thời.
CREATE OR REPLACE VIEW v_cascade_risks AS
SELECT * FROM (
    -- Risk 1: Pillar 2 volatile + Pillar 1 inventory low
    SELECT
        'FUEL_SHORTAGE_RISK'                                          AS risk_type,
        'CRITICAL'                                                     AS severity,
        'Giá ' || vs.fuel_type || ' biến động cao (σ=' || vs.relative_volatility_pct ||
            '%) trong khi region ' || po.region_code || ' chỉ còn ' || po.stock_days || ' ngày dự trữ.'
                                                                       AS description,
        JSONB_BUILD_OBJECT(
            'fuel_type', vs.fuel_type,
            'volatility_pct', vs.relative_volatility_pct,
            'region', po.region_code,
            'stock_days', po.stock_days
        )                                                              AS details
    FROM v_pillar2_volatility_signal vs
    CROSS JOIN v_pillar1_supply_outlook po
    WHERE vs.signal = 'VOLATILE'
      AND po.status IN ('CRITICAL','WARNING')
      AND (
            (vs.fuel_type LIKE '%CRUDE%' AND po.fuel_type IN ('GASOLINE','DIESEL'))
            OR vs.fuel_type = po.fuel_type
          )

    UNION ALL
    -- Risk 2: Pillar 3 overload + Pillar 4 renewable low
    SELECT
        'GENERATION_DEFICIT_RISK',
        'CRITICAL',
        'Region ' || lsp.region_code || ' đang load ' || lsp.load_pct ||
            '% trong khi tỷ lệ renewable share chỉ ' || COALESCE(npg.current_renewable_share_pct, 0) || '%.',
        JSONB_BUILD_OBJECT(
            'region', lsp.region_code,
            'load_pct', lsp.load_pct,
            'renewable_share_pct', COALESCE(npg.current_renewable_share_pct, 0)
        )
    FROM v_pillar3_load_shedding_plan lsp
    LEFT JOIN v_pillar4_net_zero_progress npg ON npg.region_code = lsp.region_code
    WHERE lsp.load_pct >= 90
      AND COALESCE(npg.current_renewable_share_pct, 100) < 20

    UNION ALL
    -- Risk 3: high emission intensity + price surge
    SELECT
        'CARBON_COST_RISK',
        'WARNING',
        'Region ' || ei.region_code || ' intensity CO2 = ' || ei.avg_intensity_kg_per_mwh ||
            ' kg/MWh trong khi giá NL đang VOLATILE → chi phí xanh sẽ tăng mạnh.',
        JSONB_BUILD_OBJECT(
            'region', ei.region_code,
            'intensity_kg_per_mwh', ei.avg_intensity_kg_per_mwh
        )
    FROM v_pillar4_emission_intensity ei
    WHERE ei.avg_intensity_kg_per_mwh > 600
      AND EXISTS (SELECT 1 FROM v_pillar2_volatility_signal WHERE signal = 'VOLATILE')
) compound_risks
ORDER BY
    CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'WARNING' THEN 2 ELSE 3 END,
    risk_type;

COMMENT ON VIEW v_cascade_risks IS 'Compound risks phát hiện khi 2+ pillar suy yếu đồng thời (ví dụ: giá tăng + dự trữ thấp).';

-- =============================================================
-- 11. VIEW v_active_recommendations — Recommendations PENDING (cho dashboard)
-- =============================================================
CREATE OR REPLACE VIEW v_active_recommendations AS
SELECT
    r.id,
    r.pillar,
    r.action_type,
    r.severity,
    r.title,
    r.message,
    r.suggested_data,
    r.suggested_at,
    EXTRACT(EPOCH FROM (NOW() - r.suggested_at))::INT             AS age_seconds,
    r.expires_at,
    CASE WHEN r.expires_at IS NOT NULL AND r.expires_at < NOW()
        THEN TRUE ELSE FALSE END                                  AS is_expired
FROM recommendations r
WHERE r.status = 'PENDING'
ORDER BY
    CASE r.severity WHEN 'CRITICAL' THEN 1 WHEN 'WARNING' THEN 2 ELSE 3 END,
    r.suggested_at DESC;

COMMENT ON VIEW v_active_recommendations IS 'Recommendations PENDING — dashboard 4 pillars hiển thị badge "có hành động chờ duyệt".';
