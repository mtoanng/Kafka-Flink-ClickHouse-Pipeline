-- =============================================================
--  FUEL PRICE ANALYTICS — DATABASE INITIALIZATION
--  Database: fuel_prices
--  Chạy tự động khi PostgreSQL container start lần đầu
-- =============================================================

-- Extension để dùng gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================
-- LAYER 1: RAW DATA
-- Flink ghi mỗi record trực tiếp từ Kafka vào đây.
-- Không có FK, không validate — lưu nguyên bản.
-- =============================================================
CREATE TABLE IF NOT EXISTS fuel_prices_raw (
    id              BIGSERIAL    PRIMARY KEY,
    event_timestamp TIMESTAMP    NOT NULL,
    fuel_type       VARCHAR(50)  NOT NULL,   -- WTI_CRUDE | BRENT_CRUDE | GASOLINE | DIESEL | NATURAL_GAS
    price           DECIMAL(12,4) NOT NULL,  -- Giá theo đơn vị tự nhiên (barrel / gallon / MMBtu)
    price_unit      VARCHAR(20)  NOT NULL,   -- USD/barrel | USD/gallon | USD/MMBtu
    location        VARCHAR(100) NOT NULL,   -- New York | London | Dubai | Singapore | Houston
    region          VARCHAR(50),             -- North America | Europe | Middle East | Asia Pacific
    source          VARCHAR(100),            -- mock-generator | alpha-vantage | eia-api
    ingested_at     TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_raw_timestamp  ON fuel_prices_raw(event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_raw_fuel_type  ON fuel_prices_raw(fuel_type);
CREATE INDEX IF NOT EXISTS idx_raw_location   ON fuel_prices_raw(location);

-- =============================================================
-- LAYER 2: WINDOW AGGREGATIONS
-- Flink ghi kết quả tổng hợp mỗi 1 phút vào đây.
-- avg / min / max / count theo (fuel_type, location).
-- =============================================================
CREATE TABLE IF NOT EXISTS fuel_price_window_agg (
    id           BIGSERIAL    PRIMARY KEY,
    window_start TIMESTAMP    NOT NULL,
    window_end   TIMESTAMP    NOT NULL,
    fuel_type    VARCHAR(50)  NOT NULL,
    location     VARCHAR(100) NOT NULL,
    region       VARCHAR(50),
    avg_price    DECIMAL(12,4),
    min_price    DECIMAL(12,4),
    max_price    DECIMAL(12,4),
    record_count INTEGER,
    computed_at  TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_agg_window_start ON fuel_price_window_agg(window_start DESC);
CREATE INDEX IF NOT EXISTS idx_agg_fuel_type    ON fuel_price_window_agg(fuel_type);
CREATE INDEX IF NOT EXISTS idx_agg_location     ON fuel_price_window_agg(location);

-- =============================================================
-- LAYER 3: PRICE CHANGE ALERTS
-- Flink ghi cảnh báo khi giá thay đổi > 3%.
-- =============================================================
CREATE TABLE IF NOT EXISTS fuel_price_alerts (
    id             BIGSERIAL    PRIMARY KEY,
    alert_timestamp TIMESTAMP   NOT NULL,
    fuel_type      VARCHAR(50)  NOT NULL,
    location       VARCHAR(100) NOT NULL,
    region         VARCHAR(50),
    previous_price DECIMAL(12,4),
    current_price  DECIMAL(12,4),
    change_percent DECIMAL(6,2),
    alert_type     VARCHAR(20),             -- PRICE_SPIKE | PRICE_DROP
    created_at     TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_alerts_timestamp ON fuel_price_alerts(alert_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_type      ON fuel_price_alerts(alert_type);
CREATE INDEX IF NOT EXISTS idx_alerts_location  ON fuel_price_alerts(location);

-- =============================================================
-- VIEWS CHO METABASE
-- Người 4 tạo dashboard trực tiếp từ các view này.
-- Không cần biết cấu trúc bảng phức tạp bên dưới.
-- =============================================================

-- View 1: Giá mới nhất (24 giờ qua) — dùng cho KPI cards
CREATE OR REPLACE VIEW v_latest_prices AS
SELECT
    event_timestamp,
    fuel_type,
    price_unit,
    location,
    region,
    price
FROM fuel_prices_raw
WHERE event_timestamp >= NOW() - INTERVAL '24 hours'
ORDER BY event_timestamp DESC;

-- View 2: Tóm tắt theo ngày — dùng cho trend line chart
CREATE OR REPLACE VIEW v_daily_summary AS
SELECT
    DATE(event_timestamp)               AS price_date,
    fuel_type,
    price_unit,
    location,
    region,
    ROUND(AVG(price)::numeric, 4)       AS avg_price,
    MIN(price)                          AS min_price,
    MAX(price)                          AS max_price,
    COUNT(*)                            AS record_count
FROM fuel_prices_raw
WHERE event_timestamp >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(event_timestamp), fuel_type, price_unit, location, region
ORDER BY price_date DESC, fuel_type, location;

-- View 3: Biến động giá theo window — dùng cho area chart
CREATE OR REPLACE VIEW v_price_trend AS
SELECT
    window_start,
    window_end,
    fuel_type,
    location,
    region,
    avg_price,
    min_price,
    max_price,
    record_count,
    ROUND((max_price - min_price)::numeric, 2) AS price_spread
FROM fuel_price_window_agg
WHERE window_start >= NOW() - INTERVAL '7 days'
ORDER BY window_start DESC;

-- View 4: So sánh giá theo địa điểm hôm nay — dùng cho bar chart
CREATE OR REPLACE VIEW v_location_comparison_today AS
SELECT
    fuel_type,
    price_unit,
    location,
    region,
    ROUND(AVG(price)::numeric, 4) AS avg_price_today
FROM fuel_prices_raw
WHERE DATE(event_timestamp) = CURRENT_DATE
GROUP BY fuel_type, price_unit, location, region
ORDER BY fuel_type, avg_price_today DESC;

-- View 5: Cảnh báo gần đây — dùng cho alert table
CREATE OR REPLACE VIEW v_recent_alerts AS
SELECT
    alert_timestamp,
    fuel_type,
    location,
    region,
    previous_price,
    current_price,
    ROUND(change_percent::numeric, 2) AS change_percent,
    alert_type
FROM fuel_price_alerts
WHERE alert_timestamp >= NOW() - INTERVAL '24 hours'
ORDER BY alert_timestamp DESC;

-- View 6: Thống kê tổng quan hệ thống — dùng cho Data Quality dashboard
CREATE OR REPLACE VIEW v_pipeline_health AS
SELECT
    DATE(ingested_at)                               AS ingest_date,
    COUNT(*)                                        AS total_records,
    COUNT(DISTINCT location)                        AS locations_active,
    COUNT(DISTINCT fuel_type)                       AS fuel_types_active,
    ROUND(AVG(price)::numeric, 4)                   AS overall_avg_price,
    MIN(event_timestamp)                            AS earliest_event,
    MAX(event_timestamp)                            AS latest_event,
    EXTRACT(EPOCH FROM (MAX(ingested_at) - MIN(ingested_at))) AS pipeline_span_seconds
FROM fuel_prices_raw
WHERE ingested_at >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY DATE(ingested_at)
ORDER BY ingest_date DESC;

-- =============================================================
-- DỮ LIỆU MẪU (SEED DATA)
-- Cho phép Người 4 test dashboard ngay cả khi producer chưa chạy.
-- =============================================================

INSERT INTO fuel_prices_raw (event_timestamp, fuel_type, price, price_unit, location, region, source)
SELECT
    NOW() - (seq * INTERVAL '1 minute'),
    fuel_type,
    ROUND((base_price + (RANDOM() * base_price * 0.01 - base_price * 0.005))::numeric, 4),
    price_unit,
    location,
    region,
    'seed-data'
FROM
    generate_series(0, 59) AS seq,
    (VALUES
        -- WTI Crude Oil (USD/barrel, NYMEX)
        ('WTI_CRUDE',   78.50, 'USD/barrel', 'New York',  'North America'),
        ('WTI_CRUDE',   78.30, 'USD/barrel', 'Houston',   'North America'),
        -- Brent Crude (USD/barrel, ICE)
        ('BRENT_CRUDE', 82.30, 'USD/barrel', 'London',    'Europe'),
        ('BRENT_CRUDE', 82.50, 'USD/barrel', 'Dubai',     'Middle East'),
        ('BRENT_CRUDE', 82.10, 'USD/barrel', 'Singapore', 'Asia Pacific'),
        -- Gasoline (USD/gallon)
        ('GASOLINE',     2.42, 'USD/gallon', 'New York',  'North America'),
        ('GASOLINE',     2.38, 'USD/gallon', 'Houston',   'North America'),
        -- Diesel (USD/gallon)
        ('DIESEL',       2.65, 'USD/gallon', 'New York',  'North America'),
        ('DIESEL',       2.60, 'USD/gallon', 'London',    'Europe')
    ) AS t(fuel_type, base_price, price_unit, location, region);

-- Seed window aggregation (1-hour windows for last 24 hours)
INSERT INTO fuel_price_window_agg (window_start, window_end, fuel_type, location, region, avg_price, min_price, max_price, record_count)
SELECT
    DATE_TRUNC('hour', NOW()) - (hr * INTERVAL '1 hour'),
    DATE_TRUNC('hour', NOW()) - (hr * INTERVAL '1 hour') + INTERVAL '1 hour',
    fuel_type,
    location,
    region,
    ROUND((base_price * (1 + RANDOM() * 0.01 - 0.005))::numeric, 4),
    ROUND((base_price * 0.995)::numeric, 4),
    ROUND((base_price * 1.005)::numeric, 4),
    FLOOR(RANDOM() * 50 + 10)::INTEGER
FROM
    generate_series(0, 23) AS hr,
    (VALUES
        ('WTI_CRUDE',   78.50, 'New York',  'North America'),
        ('BRENT_CRUDE', 82.30, 'London',    'Europe'),
        ('GASOLINE',     2.42, 'New York',  'North America'),
        ('DIESEL',       2.65, 'New York',  'North America')
    ) AS t(fuel_type, base_price, location, region);

-- Seed một vài alerts
INSERT INTO fuel_price_alerts (alert_timestamp, fuel_type, location, region, previous_price, current_price, change_percent, alert_type)
VALUES
    (NOW() - INTERVAL '2 hour',  'WTI_CRUDE',   'New York',  'North America', 78.10, 80.65, 3.26, 'PRICE_SPIKE'),
    (NOW() - INTERVAL '5 hour',  'BRENT_CRUDE',  'London',   'Europe',        83.20, 80.55, -3.19, 'PRICE_DROP'),
    (NOW() - INTERVAL '10 hour', 'GASOLINE',     'New York',  'North America',  2.35, 2.43,  3.40, 'PRICE_SPIKE');
