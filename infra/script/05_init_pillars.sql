-- =============================================================
--  VES Schema: 4-PILLAR RAW TABLES  (Phase 2.5)
--  Chạy sau 04_seed_basic.sql
--
--  Mục tiêu: Light coverage cho 3 pillars còn lại của An ninh năng lượng.
--    Pillar 2 (Biến động Kinh tế) đã có sẵn ở 01_init_fuel_schema.sql
--    qua bảng fuel_prices_raw + fuel_price_window_agg + fuel_price_alerts.
--
--  3 pillars bổ sung:
--    Pillar 1 — Nguồn cung & Hạ tầng        : fuel_inventory_raw
--    Pillar 3 — Phụ tải & Tiêu thụ          : grid_load_raw
--    Pillar 4 — Chuyển đổi & Môi trường     : renewable_output_raw + emission_raw
--
--  Generator real-time cho 3 bảng grid_load_raw / renewable_output_raw /
--  emission_raw sẽ được build ở Phase 4. Bảng fuel_inventory_raw được seed
--  tĩnh ở 06_seed_pillars.sql (vì dự trữ chiến lược ít biến động).
-- =============================================================

-- =============================================================
-- PILLAR 1 — Nguồn cung & Hạ tầng
-- BẢNG fuel_inventory_raw — Dự trữ chiến lược xăng dầu theo vùng
-- =============================================================
-- VN đang hướng tới mục tiêu dự trữ tương đương 90 ngày nhập ròng.
-- Đơn vị: stock_days = số ngày tiêu thụ nội địa mà tồn kho hiện tại
--                     có thể cover. Dưới 30 ngày => CẢNH BÁO nghiêm trọng.
CREATE TABLE IF NOT EXISTS fuel_inventory_raw (
    id                  BIGSERIAL    PRIMARY KEY,
    region_code         VARCHAR(20)  NOT NULL REFERENCES regions(code) ON DELETE CASCADE,
    fuel_type           VARCHAR(50)  NOT NULL,                    -- GASOLINE | DIESEL | LNG | CRUDE_OIL
    stock_volume_kl     DECIMAL(14,2) NOT NULL,                   -- nghìn lít (kilolitre)
    daily_consumption_kl DECIMAL(14,2) NOT NULL,                  -- tiêu thụ TB/ngày của region
    stock_days          DECIMAL(6,1)  GENERATED ALWAYS AS (
                            CASE WHEN daily_consumption_kl > 0
                                THEN stock_volume_kl / daily_consumption_kl
                                ELSE 0
                            END
                        ) STORED,
    target_days         INT          NOT NULL DEFAULT 90,         -- mục tiêu chiến lược
    reported_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventory_volume_positive CHECK (stock_volume_kl >= 0),
    CONSTRAINT chk_inventory_daily_positive  CHECK (daily_consumption_kl >= 0)
);

CREATE INDEX IF NOT EXISTS idx_inventory_region      ON fuel_inventory_raw(region_code);
CREATE INDEX IF NOT EXISTS idx_inventory_fuel_type   ON fuel_inventory_raw(fuel_type);
CREATE INDEX IF NOT EXISTS idx_inventory_reported_at ON fuel_inventory_raw(reported_at DESC);

COMMENT ON TABLE  fuel_inventory_raw  IS 'PILLAR 1: Dự trữ chiến lược xăng dầu theo vùng (VN target: 90 ngày).';
COMMENT ON COLUMN fuel_inventory_raw.stock_days IS 'Tự tính từ stock_volume_kl / daily_consumption_kl. <30 = CRITICAL.';

-- =============================================================
-- PILLAR 3 — Phụ tải & Tiêu thụ
-- BẢNG grid_load_raw — Biểu đồ phụ tải điện theo region
-- =============================================================
-- Generator (Phase 4) sẽ sinh mỗi 5 giây cho 3 region VN_NORTH/CENTRAL/SOUTH.
-- load_pct > 90 = quá tải (CẢNH BÁO).
CREATE TABLE IF NOT EXISTS grid_load_raw (
    id              BIGSERIAL    PRIMARY KEY,
    region_code     VARCHAR(20)  NOT NULL REFERENCES regions(code) ON DELETE CASCADE,
    load_mw         DECIMAL(12,2) NOT NULL,                       -- công suất tải hiện tại (MW)
    capacity_mw     DECIMAL(12,2) NOT NULL,                       -- công suất tối đa lưới (MW)
    load_pct        DECIMAL(5,2)  GENERATED ALWAYS AS (
                        CASE WHEN capacity_mw > 0
                            THEN ROUND((load_mw / capacity_mw) * 100, 2)
                            ELSE 0
                        END
                    ) STORED,
    is_peak_hour    BOOLEAN      NOT NULL DEFAULT FALSE,          -- 18h-22h thường là cao điểm
    event_time      TIMESTAMP    NOT NULL,
    ingestion_time  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_grid_load_positive     CHECK (load_mw >= 0),
    CONSTRAINT chk_grid_capacity_positive CHECK (capacity_mw > 0)
);

CREATE INDEX IF NOT EXISTS idx_grid_load_region     ON grid_load_raw(region_code);
CREATE INDEX IF NOT EXISTS idx_grid_load_event_time ON grid_load_raw(event_time DESC);
CREATE INDEX IF NOT EXISTS idx_grid_load_pct        ON grid_load_raw(load_pct);

COMMENT ON TABLE  grid_load_raw       IS 'PILLAR 3: Phụ tải điện real-time. >90%% = quá tải.';
COMMENT ON COLUMN grid_load_raw.is_peak_hour IS 'TRUE nếu rơi vào 18-22h (giờ cao điểm sinh hoạt VN).';

-- =============================================================
-- PILLAR 4 — Chuyển đổi & Môi trường
-- BẢNG renewable_output_raw — Sản lượng năng lượng tái tạo
-- =============================================================
-- Generator (Phase 4) mô phỏng solar (ngày) + wind (cả ngày), phụ thuộc giờ.
CREATE TABLE IF NOT EXISTS renewable_output_raw (
    id              BIGSERIAL    PRIMARY KEY,
    region_code     VARCHAR(20)  NOT NULL REFERENCES regions(code) ON DELETE CASCADE,
    source_type     VARCHAR(20)  NOT NULL,                         -- SOLAR | WIND | HYDRO
    output_mw       DECIMAL(12,2) NOT NULL,
    capacity_mw     DECIMAL(12,2) NOT NULL,
    utilization_pct DECIMAL(5,2)  GENERATED ALWAYS AS (
                        CASE WHEN capacity_mw > 0
                            THEN ROUND((output_mw / capacity_mw) * 100, 2)
                            ELSE 0
                        END
                    ) STORED,
    event_time      TIMESTAMP    NOT NULL,
    ingestion_time  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_renewable_source CHECK (source_type IN ('SOLAR', 'WIND', 'HYDRO')),
    CONSTRAINT chk_renewable_output CHECK (output_mw >= 0)
);

CREATE INDEX IF NOT EXISTS idx_renewable_region     ON renewable_output_raw(region_code);
CREATE INDEX IF NOT EXISTS idx_renewable_source     ON renewable_output_raw(source_type);
CREATE INDEX IF NOT EXISTS idx_renewable_event_time ON renewable_output_raw(event_time DESC);

COMMENT ON TABLE renewable_output_raw IS 'PILLAR 4: Sản lượng năng lượng sạch (solar/wind/hydro) real-time.';

-- =============================================================
-- PILLAR 4 — BẢNG emission_raw — Phát thải CO2 từ phát điện
-- =============================================================
-- Tách bảng riêng để dễ truy vấn theo Net Zero KPI.
CREATE TABLE IF NOT EXISTS emission_raw (
    id              BIGSERIAL    PRIMARY KEY,
    region_code     VARCHAR(20)  NOT NULL REFERENCES regions(code) ON DELETE CASCADE,
    co2_kg          DECIMAL(14,2) NOT NULL,                       -- tổng CO2 phát ra (kg) trong window
    energy_mwh      DECIMAL(12,2) NOT NULL,                       -- năng lượng sinh ra (MWh) trong window
    intensity_kg_per_mwh DECIMAL(10,2) GENERATED ALWAYS AS (
                        CASE WHEN energy_mwh > 0
                            THEN ROUND(co2_kg / energy_mwh, 2)
                            ELSE 0
                        END
                    ) STORED,
    event_time      TIMESTAMP    NOT NULL,
    ingestion_time  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_emission_co2_positive    CHECK (co2_kg >= 0),
    CONSTRAINT chk_emission_energy_positive CHECK (energy_mwh >= 0)
);

CREATE INDEX IF NOT EXISTS idx_emission_region     ON emission_raw(region_code);
CREATE INDEX IF NOT EXISTS idx_emission_event_time ON emission_raw(event_time DESC);

COMMENT ON TABLE emission_raw IS 'PILLAR 4: Phát thải CO2 (kg/MWh). Hỗ trợ KPI Net Zero.';

-- =============================================================
-- VIEW v_pillar1_inventory_status — Bảng điều khiển Pillar 1
-- =============================================================
CREATE OR REPLACE VIEW v_pillar1_inventory_status AS
SELECT
    i.id,
    i.region_code,
    r.name        AS region_name,
    r.vn_zone,
    i.fuel_type,
    i.stock_volume_kl,
    i.daily_consumption_kl,
    i.stock_days,
    i.target_days,
    CASE
        WHEN i.stock_days < 30  THEN 'CRITICAL'
        WHEN i.stock_days < 60  THEN 'WARNING'
        WHEN i.stock_days < i.target_days THEN 'BELOW_TARGET'
        ELSE 'OK'
    END           AS status,
    i.reported_at
FROM fuel_inventory_raw i
JOIN regions r ON r.code = i.region_code
ORDER BY i.reported_at DESC, r.code;

COMMENT ON VIEW v_pillar1_inventory_status IS 'Pillar 1 dashboard: trạng thái dự trữ theo region (CRITICAL/WARNING/BELOW_TARGET/OK).';

-- =============================================================
-- VIEW v_pillar3_grid_load_latest — Phụ tải gần nhất mỗi region
-- =============================================================
CREATE OR REPLACE VIEW v_pillar3_grid_load_latest AS
SELECT DISTINCT ON (g.region_code)
    g.region_code,
    r.name        AS region_name,
    r.vn_zone,
    g.load_mw,
    g.capacity_mw,
    g.load_pct,
    g.is_peak_hour,
    CASE
        WHEN g.load_pct >= 95  THEN 'CRITICAL'
        WHEN g.load_pct >= 85  THEN 'WARNING'
        WHEN g.load_pct >= 70  THEN 'HIGH'
        ELSE 'NORMAL'
    END           AS status,
    g.event_time
FROM grid_load_raw g
JOIN regions r ON r.code = g.region_code
ORDER BY g.region_code, g.event_time DESC;

COMMENT ON VIEW v_pillar3_grid_load_latest IS 'Pillar 3 dashboard: phụ tải lưới điện gần nhất mỗi region.';

-- =============================================================
-- VIEW v_pillar4_renewable_share — Tỷ lệ năng lượng sạch theo giờ
-- =============================================================
CREATE OR REPLACE VIEW v_pillar4_renewable_share AS
SELECT
    DATE_TRUNC('hour', re.event_time)            AS hour_bucket,
    re.region_code,
    r.name                                       AS region_name,
    SUM(re.output_mw) FILTER (WHERE re.source_type = 'SOLAR')  AS solar_mw,
    SUM(re.output_mw) FILTER (WHERE re.source_type = 'WIND')   AS wind_mw,
    SUM(re.output_mw) FILTER (WHERE re.source_type = 'HYDRO')  AS hydro_mw,
    SUM(re.output_mw)                            AS total_renewable_mw
FROM renewable_output_raw re
JOIN regions r ON r.code = re.region_code
GROUP BY 1, 2, 3
ORDER BY 1 DESC, 2;

COMMENT ON VIEW v_pillar4_renewable_share IS 'Pillar 4 dashboard: tỷ trọng solar/wind/hydro mỗi giờ theo region.';

-- =============================================================
-- VIEW v_pillar4_emission_intensity — Intensity CO2 theo region (24h gần nhất)
-- =============================================================
CREATE OR REPLACE VIEW v_pillar4_emission_intensity AS
SELECT
    e.region_code,
    r.name        AS region_name,
    SUM(e.co2_kg)        AS co2_kg_24h,
    SUM(e.energy_mwh)    AS energy_mwh_24h,
    CASE WHEN SUM(e.energy_mwh) > 0
        THEN ROUND(SUM(e.co2_kg) / SUM(e.energy_mwh), 2)
        ELSE 0
    END           AS avg_intensity_kg_per_mwh,
    COUNT(*)      AS samples_24h
FROM emission_raw e
JOIN regions r ON r.code = e.region_code
WHERE e.event_time >= NOW() - INTERVAL '24 hours'
GROUP BY e.region_code, r.name
ORDER BY avg_intensity_kg_per_mwh DESC;

COMMENT ON VIEW v_pillar4_emission_intensity IS 'Pillar 4 dashboard: cường độ phát thải 24h gần nhất theo region.';
