-- =============================================================
--  VES Schema: USERS + REGIONS  (Phase 2)
--  Chạy sau 01_init_fuel_schema.sql
--  KHÔNG đụng các bảng có sẵn (fuel_prices_raw, ...)
-- =============================================================

-- =============================================================
-- BẢNG users — Đăng nhập cho Backend API + JavaFX + Android
-- Mật khẩu lưu dạng BCrypt ($2a$ hoặc $2b$, tương thích Spring Security)
-- =============================================================
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL    PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,                    -- bcrypt hash
    full_name       VARCHAR(150),
    email           VARCHAR(150),
    role            VARCHAR(20)  NOT NULL DEFAULT 'VIEWER',   -- ADMIN | MANAGER | VIEWER
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_login_at   TIMESTAMP,
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'MANAGER', 'VIEWER'))
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role     ON users(role);

COMMENT ON TABLE users IS 'Người dùng hệ thống. Authentication qua BCrypt (Spring Security tương thích).';
COMMENT ON COLUMN users.role IS 'ADMIN: full quyền | MANAGER: xem + tạo rule | VIEWER: chỉ xem';

-- =============================================================
-- BẢNG regions — Phân vùng địa lý Việt Nam + sàn quốc tế
-- Dùng để gắn facility/location với region cụ thể
-- =============================================================
CREATE TABLE IF NOT EXISTS regions (
    id              BIGSERIAL    PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL UNIQUE,             -- VN_NORTH, VN_CENTRAL, VN_SOUTH, INTL_EU, ...
    name            VARCHAR(100) NOT NULL,
    vn_zone         VARCHAR(20),                              -- BAC | TRUNG | NAM | NULL (quốc tế)
    country_code    VARCHAR(3)   NOT NULL DEFAULT 'VN',       -- ISO-3166 alpha-2/3
    description     TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_regions_code        ON regions(code);
CREATE INDEX IF NOT EXISTS idx_regions_vn_zone     ON regions(vn_zone);
CREATE INDEX IF NOT EXISTS idx_regions_country     ON regions(country_code);

COMMENT ON TABLE regions IS 'Phân vùng địa lý. VN có 3 miền + nhóm quốc tế (cho fuel price từ NYMEX/ICE).';
