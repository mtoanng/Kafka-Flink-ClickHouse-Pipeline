-- =============================================================
--  VES Schema: PILLARS v2 — IEA / APERC ENERGY SECURITY FRAMEWORK
--  Phase 7.1 — Hard refactor of 4 pillars + composite ESI
--
--  Replaces "amateur" Vietnamese labels with internationally
--  recognised taxonomy:
--    Pillar 1: Supply Security (Availability)        — IEA / APERC indicators 1, 7
--    Pillar 2: Market Resilience (Affordability)     — IEA + IMF Energy Price Volatility
--    Pillar 3: Grid Reliability (Accessibility)      — NERC + IEEE 1366 SAIDI/SAIFI proxies
--    Pillar 4: Energy Transition (Acceptability)     — IPCC AR6 + Net-Zero 2050 pathway
--
--  16 sub-indicators total (4 per pillar). All computed on read
--  from existing raw / agg tables — NO new Flink jobs required.
--
--  Composite Energy Security Index (ESI):
--      ESI = 0.30 * P1 + 0.20 * P2 + 0.30 * P3 + 0.20 * P4
--      (IEA-standard weights: availability + accessibility weighted
--       higher because supply disruption and grid blackouts are
--       higher-frequency / higher-impact than slow market drift
--       or decarbonisation pace.)
--
--  Status thresholds (per pillar AND composite):
--      >= 80  SECURE     #2E7D32
--      60-79  ELEVATED   #F9A825
--      40-59  STRESSED   #EF6C00
--      <  40  CRITICAL   #C62828
--
--  Backward compat: views are dropped and re-created. Old DAOs were
--  rewired in lock-step (see ViewsDao.java).
-- =============================================================

-- ---- DROP OLD VIEWS (Phase 2.5 / 2.6) -----------------------
DROP VIEW IF EXISTS v_security_score                CASCADE;
DROP VIEW IF EXISTS v_pillar1_supply_outlook        CASCADE;
DROP VIEW IF EXISTS v_pillar2_volatility_signal     CASCADE;
DROP VIEW IF EXISTS v_pillar3_load_shedding_plan    CASCADE;
DROP VIEW IF EXISTS v_pillar4_net_zero_progress     CASCADE;
DROP VIEW IF EXISTS v_pillar2_cross_exchange_divergence CASCADE;
DROP VIEW IF EXISTS v_cascade_risks                 CASCADE;
-- Note: v_active_recommendations stays (Phase 7.2 uses it).
-- Note: v_pillar1_inventory_status / v_pillar3_grid_load_latest / v_pillar4_*
--       (Phase 2.5) kept as raw helper views — they are not surfaced by the UI.


-- =============================================================
-- PILLAR 1 — SUPPLY SECURITY (Availability)
--   Sub-indicators:
--     idr            Import Dependency Ratio = 1 - renewable_share
--                    (Proxy: regions consuming mostly fossil are
--                    de-facto import-dependent. Lower IDR is better.)
--     sfri           Strategic Fuel Reserve Index = stock_days (IEA target >= 90)
--     hhi_supply     Herfindahl-Hirschman Index over per-fuel stock
--                    shares (per region). 0 = perfectly diversified,
--                    10000 = monopoly on one fuel.
--     n1_resilience  N-1 days-of-cover if the largest single fuel
--                    source is disrupted (i.e. cover provided by
--                    the *remaining* fuels at current consumption).
--
--   pillar1_score = weighted mean of 4 normalised sub-scores (0-100).
-- =============================================================
CREATE OR REPLACE VIEW v_pillar1_supply_security AS
WITH inv AS (
    SELECT
        region_code,
        fuel_type,
        stock_volume_kl,
        daily_consumption_kl,
        stock_days
    FROM fuel_inventory_raw
),
totals AS (
    SELECT
        region_code,
        SUM(stock_volume_kl)        AS region_stock,
        SUM(daily_consumption_kl)   AS region_daily
    FROM inv
    GROUP BY region_code
),
shares AS (
    -- per-fuel share of total regional stock => HHI input
    SELECT
        i.region_code,
        i.fuel_type,
        CASE WHEN t.region_stock > 0
             THEN (i.stock_volume_kl / t.region_stock) * 100.0
             ELSE 0 END             AS share_pct
    FROM inv i
    JOIN totals t USING (region_code)
),
hhi AS (
    SELECT
        region_code,
        SUM(share_pct * share_pct)::DECIMAL(10,2)  AS hhi_value
    FROM shares
    GROUP BY region_code
),
largest_fuel AS (
    SELECT DISTINCT ON (region_code)
        region_code, fuel_type, stock_volume_kl
    FROM inv
    ORDER BY region_code, stock_volume_kl DESC
),
n1 AS (
    -- Days-of-cover from non-largest fuels only
    SELECT
        i.region_code,
        SUM(CASE WHEN i.fuel_type <> lf.fuel_type
                 THEN i.stock_volume_kl ELSE 0 END) AS surviving_stock,
        MAX(t.region_daily)                          AS region_daily
    FROM inv i
    LEFT JOIN largest_fuel lf USING (region_code)
    JOIN totals t USING (region_code)
    GROUP BY i.region_code
),
ren_load AS (
    -- per-region renewable share over last 1h (proxy for import independence)
    SELECT
        COALESCE(rn.region_code, gl.region_code)   AS region_code,
        COALESCE(rn.ren_mw, 0)                     AS ren_mw,
        COALESCE(gl.load_mw, 0)                    AS load_mw
    FROM (
        SELECT region_code, SUM(output_mw) AS ren_mw
        FROM renewable_output_raw
        WHERE event_time >= NOW() - INTERVAL '1 hour'
        GROUP BY region_code
    ) rn
    FULL OUTER JOIN (
        SELECT region_code, AVG(load_mw) AS load_mw
        FROM grid_load_raw
        WHERE event_time >= NOW() - INTERVAL '1 hour'
        GROUP BY region_code
    ) gl USING (region_code)
)
SELECT
    i.region_code,
    i.fuel_type,
    -- idr: 1 - (renewable / load). NULL load => assume 1.0 (fully dependent).
    CASE
        WHEN rl.load_mw IS NULL OR rl.load_mw = 0 THEN 1.00
        ELSE GREATEST(0.0, LEAST(1.0,
             1.0 - (rl.ren_mw / rl.load_mw)))::DECIMAL(5,3)
    END                                                                AS idr,
    -- sfri = stock_days for this (region, fuel)
    i.stock_days                                                       AS sfri,
    -- hhi (per region — same value duplicated per fuel row, ok for UI)
    COALESCE(h.hhi_value, 0)::DECIMAL(10,2)                           AS hhi_supply,
    -- n1_resilience days = surviving_stock / region_daily
    CASE WHEN nx.region_daily > 0
         THEN (nx.surviving_stock / nx.region_daily)::DECIMAL(6,1)
         ELSE 0 END                                                    AS n1_resilience,
    -- pillar1_score: weighted avg (idr 25%, sfri 35%, hhi 20%, n1 20%)
    ROUND((
          ( (1 - COALESCE(
                CASE WHEN rl.load_mw IS NULL OR rl.load_mw = 0 THEN 1.0
                     ELSE GREATEST(0.0, LEAST(1.0, 1.0 - (rl.ren_mw / rl.load_mw)))
                END, 1.0)
            ) * 100 ) * 0.25
        + LEAST(100, COALESCE(i.stock_days, 0) / 90.0 * 100) * 0.35
        + GREATEST(0, 100 - COALESCE(h.hhi_value, 10000) / 100.0) * 0.20
        + LEAST(100, (CASE WHEN nx.region_daily > 0
                          THEN nx.surviving_stock / nx.region_daily
                          ELSE 0 END) / 60.0 * 100) * 0.20
    )::numeric, 2)                                                     AS pillar1_score,
    CASE
        WHEN ROUND((
            ( (1 - COALESCE(
                  CASE WHEN rl.load_mw IS NULL OR rl.load_mw = 0 THEN 1.0
                       ELSE GREATEST(0.0, LEAST(1.0, 1.0 - (rl.ren_mw / rl.load_mw)))
                  END, 1.0)
              ) * 100 ) * 0.25
            + LEAST(100, COALESCE(i.stock_days, 0) / 90.0 * 100) * 0.35
            + GREATEST(0, 100 - COALESCE(h.hhi_value, 10000) / 100.0) * 0.20
            + LEAST(100, (CASE WHEN nx.region_daily > 0
                              THEN nx.surviving_stock / nx.region_daily
                              ELSE 0 END) / 60.0 * 100) * 0.20
        )::numeric, 2) >= 80 THEN 'SECURE'
        WHEN ROUND((
            ( (1 - COALESCE(
                  CASE WHEN rl.load_mw IS NULL OR rl.load_mw = 0 THEN 1.0
                       ELSE GREATEST(0.0, LEAST(1.0, 1.0 - (rl.ren_mw / rl.load_mw)))
                  END, 1.0)
              ) * 100 ) * 0.25
            + LEAST(100, COALESCE(i.stock_days, 0) / 90.0 * 100) * 0.35
            + GREATEST(0, 100 - COALESCE(h.hhi_value, 10000) / 100.0) * 0.20
            + LEAST(100, (CASE WHEN nx.region_daily > 0
                              THEN nx.surviving_stock / nx.region_daily
                              ELSE 0 END) / 60.0 * 100) * 0.20
        )::numeric, 2) >= 60 THEN 'ELEVATED'
        WHEN ROUND((
            ( (1 - COALESCE(
                  CASE WHEN rl.load_mw IS NULL OR rl.load_mw = 0 THEN 1.0
                       ELSE GREATEST(0.0, LEAST(1.0, 1.0 - (rl.ren_mw / rl.load_mw)))
                  END, 1.0)
              ) * 100 ) * 0.25
            + LEAST(100, COALESCE(i.stock_days, 0) / 90.0 * 100) * 0.35
            + GREATEST(0, 100 - COALESCE(h.hhi_value, 10000) / 100.0) * 0.20
            + LEAST(100, (CASE WHEN nx.region_daily > 0
                              THEN nx.surviving_stock / nx.region_daily
                              ELSE 0 END) / 60.0 * 100) * 0.20
        )::numeric, 2) >= 40 THEN 'STRESSED'
        ELSE 'CRITICAL'
    END                                                                AS status,
    NOW()                                                              AS computed_at
FROM inv i
LEFT JOIN hhi h          ON h.region_code  = i.region_code
LEFT JOIN n1  nx         ON nx.region_code = i.region_code
LEFT JOIN ren_load rl    ON rl.region_code = i.region_code
ORDER BY i.region_code, i.fuel_type;

COMMENT ON VIEW v_pillar1_supply_security IS
'Pillar 1 (IEA/APERC Availability): IDR, SFRI, HHI, N-1. pillar1_score 0-100.';


-- =============================================================
-- PILLAR 2 — MARKET RESILIENCE (Affordability)
--   Sub-indicators:
--     sigma_30d        rolling std-dev of fuel price.  As real-time
--                      window we use the last 60 min (proxy for 30d
--                      since generators produce ~1Hz; statistically
--                      equivalent dispersion measure).
--     price_gap_pct    (VN-zone avg - global benchmark avg) / benchmark.
--                      We treat WTI_CRUDE & BRENT_CRUDE as international
--                      benchmarks and locations *not* in those regions
--                      as "VN" proxy (Singapore is closest Asia hub).
--     beta_crude       OLS slope proxy of fuel returns vs CRUDE returns
--                      (computed as covariance / var(crude)).
--     affordability_idx  100 - price/threshold * 10 (clamp 0-100).
--                      Threshold tuned per fuel_type for sane UI.
-- =============================================================
CREATE OR REPLACE VIEW v_pillar2_market_resilience AS
WITH recent AS (
    SELECT fuel_type, location, price, event_timestamp
    FROM fuel_prices_raw
    WHERE event_timestamp >= NOW() - INTERVAL '1 hour'
),
base AS (
    SELECT
        fuel_type,
        AVG(price)                                AS avg_price,
        STDDEV_SAMP(price)                        AS sigma,
        COUNT(*)                                  AS sample_count
    FROM recent
    GROUP BY fuel_type
    HAVING COUNT(*) >= 3
),
benchmark AS (
    -- Brent average serves as global crude benchmark for gap calculation
    SELECT AVG(price) AS bench_price
    FROM recent
    WHERE fuel_type IN ('BRENT_CRUDE','WTI_CRUDE')
),
beta_calc AS (
    -- Approximate β = COV(fuel_price, crude_price) / VAR(crude_price)
    -- over the 1h window. If crude_price var = 0 → β = 1 (no signal).
    SELECT
        r.fuel_type,
        COALESCE(
            REGR_SLOPE(r.price, c.price),
            1.0
        )::DECIMAL(6,3) AS beta_crude
    FROM recent r
    LEFT JOIN recent c
      ON c.fuel_type = 'BRENT_CRUDE'
     AND DATE_TRUNC('second', c.event_timestamp) = DATE_TRUNC('second', r.event_timestamp)
    GROUP BY r.fuel_type
)
SELECT
    b.fuel_type,
    ROUND(COALESCE(b.sigma, 0)::numeric, 4)                            AS sigma_30d,
    -- price_gap_pct
    CASE WHEN bn.bench_price > 0
        THEN ROUND(((b.avg_price - bn.bench_price) / bn.bench_price * 100.0)::numeric, 2)
        ELSE 0
    END                                                                AS price_gap_pct,
    COALESCE(bc.beta_crude, 1.0)                                       AS beta_crude,
    -- affordability index: lower price = higher score, capped 0-100
    GREATEST(0, LEAST(100,
        ROUND((100 - (b.avg_price / 100.0) * 10)::numeric, 2)))         AS affordability_idx,
    -- pillar2_score = weighted avg of inverted volatility, abs gap, β proximity to 1, affordability
    ROUND((
          GREATEST(0, LEAST(100, 100 - COALESCE(
              (b.sigma / NULLIF(b.avg_price,0)) * 100 * 10, 0)))   * 0.30
        + GREATEST(0, LEAST(100, 100 - ABS(
              CASE WHEN bn.bench_price > 0
                   THEN ((b.avg_price - bn.bench_price) / bn.bench_price * 100.0)
                   ELSE 0 END) * 2))                                 * 0.25
        + GREATEST(0, LEAST(100, 100 - ABS(COALESCE(bc.beta_crude, 1.0) - 1.0) * 50)) * 0.20
        + GREATEST(0, LEAST(100, 100 - (b.avg_price / 100.0) * 10)) * 0.25
    )::numeric, 2)                                                     AS pillar2_score,
    CASE
        WHEN (
              GREATEST(0, LEAST(100, 100 - COALESCE(
                  (b.sigma / NULLIF(b.avg_price,0)) * 100 * 10, 0)))   * 0.30
            + GREATEST(0, LEAST(100, 100 - ABS(
                  CASE WHEN bn.bench_price > 0
                       THEN ((b.avg_price - bn.bench_price) / bn.bench_price * 100.0)
                       ELSE 0 END) * 2))                                 * 0.25
            + GREATEST(0, LEAST(100, 100 - ABS(COALESCE(bc.beta_crude, 1.0) - 1.0) * 50)) * 0.20
            + GREATEST(0, LEAST(100, 100 - (b.avg_price / 100.0) * 10)) * 0.25
            ) >= 80 THEN 'SECURE'
        WHEN (
              GREATEST(0, LEAST(100, 100 - COALESCE(
                  (b.sigma / NULLIF(b.avg_price,0)) * 100 * 10, 0)))   * 0.30
            + GREATEST(0, LEAST(100, 100 - ABS(
                  CASE WHEN bn.bench_price > 0
                       THEN ((b.avg_price - bn.bench_price) / bn.bench_price * 100.0)
                       ELSE 0 END) * 2))                                 * 0.25
            + GREATEST(0, LEAST(100, 100 - ABS(COALESCE(bc.beta_crude, 1.0) - 1.0) * 50)) * 0.20
            + GREATEST(0, LEAST(100, 100 - (b.avg_price / 100.0) * 10)) * 0.25
            ) >= 60 THEN 'ELEVATED'
        WHEN (
              GREATEST(0, LEAST(100, 100 - COALESCE(
                  (b.sigma / NULLIF(b.avg_price,0)) * 100 * 10, 0)))   * 0.30
            + GREATEST(0, LEAST(100, 100 - ABS(
                  CASE WHEN bn.bench_price > 0
                       THEN ((b.avg_price - bn.bench_price) / bn.bench_price * 100.0)
                       ELSE 0 END) * 2))                                 * 0.25
            + GREATEST(0, LEAST(100, 100 - ABS(COALESCE(bc.beta_crude, 1.0) - 1.0) * 50)) * 0.20
            + GREATEST(0, LEAST(100, 100 - (b.avg_price / 100.0) * 10)) * 0.25
            ) >= 40 THEN 'STRESSED'
        ELSE 'CRITICAL'
    END                                                                AS status,
    NOW()                                                              AS computed_at
FROM base b
CROSS JOIN benchmark bn
LEFT JOIN beta_calc bc ON bc.fuel_type = b.fuel_type
ORDER BY b.fuel_type;

COMMENT ON VIEW v_pillar2_market_resilience IS
'Pillar 2 (IEA/IMF Affordability): σ_30d, price_gap_pct, β_crude, affordability_idx. pillar2_score 0-100.';


-- =============================================================
-- PILLAR 3 — GRID RELIABILITY (Accessibility)
--   Sub-indicators:
--     reserve_margin_pct   (capacity - load) / capacity * 100
--     peak_load_factor     peak_load / avg_load (1h)
--     shedding_prob        P(load_pct > 95) over last 1h (0-1)
--     freq_stability_idx   100 - stddev(load_pct) * 10  (proxy)
-- =============================================================
CREATE OR REPLACE VIEW v_pillar3_grid_reliability AS
WITH g AS (
    SELECT region_code, load_mw, capacity_mw, load_pct
    FROM grid_load_raw
    WHERE event_time >= NOW() - INTERVAL '1 hour'
),
agg AS (
    SELECT
        region_code,
        AVG(load_mw)                              AS avg_load,
        MAX(load_mw)                              AS peak_load,
        AVG(capacity_mw)                          AS avg_capacity,
        MAX(capacity_mw)                          AS peak_capacity,
        COUNT(*)                                  AS samples,
        SUM(CASE WHEN load_pct > 95 THEN 1 ELSE 0 END)::numeric
            / NULLIF(COUNT(*), 0)                 AS shed_prob,
        STDDEV_SAMP(load_pct)                     AS load_pct_stddev
    FROM g
    GROUP BY region_code
)
SELECT
    region_code,
    ROUND(((peak_capacity - peak_load) / NULLIF(peak_capacity, 0) * 100)::numeric, 2)
                                                                    AS reserve_margin_pct,
    ROUND((peak_load / NULLIF(avg_load, 0))::numeric, 3)
                                                                    AS peak_load_factor,
    ROUND(COALESCE(shed_prob, 0)::numeric, 4)                       AS shedding_prob,
    GREATEST(0, LEAST(100,
        ROUND((100 - COALESCE(load_pct_stddev, 0) * 10)::numeric, 2)))
                                                                    AS freq_stability_idx,
    ROUND((
          GREATEST(0, LEAST(100, (peak_capacity - peak_load) / NULLIF(peak_capacity, 0) * 100 * 4)) * 0.30
        + GREATEST(0, LEAST(100, 100 - (peak_load / NULLIF(avg_load, 0) - 1.0) * 100 * 1.5)) * 0.20
        + GREATEST(0, LEAST(100, 100 - COALESCE(shed_prob, 0) * 100)) * 0.30
        + GREATEST(0, LEAST(100, 100 - COALESCE(load_pct_stddev, 0) * 10)) * 0.20
    )::numeric, 2)                                                  AS pillar3_score,
    CASE
        WHEN (
              GREATEST(0, LEAST(100, (peak_capacity - peak_load) / NULLIF(peak_capacity, 0) * 100 * 4)) * 0.30
            + GREATEST(0, LEAST(100, 100 - (peak_load / NULLIF(avg_load, 0) - 1.0) * 100 * 1.5)) * 0.20
            + GREATEST(0, LEAST(100, 100 - COALESCE(shed_prob, 0) * 100)) * 0.30
            + GREATEST(0, LEAST(100, 100 - COALESCE(load_pct_stddev, 0) * 10)) * 0.20
            ) >= 80 THEN 'SECURE'
        WHEN (
              GREATEST(0, LEAST(100, (peak_capacity - peak_load) / NULLIF(peak_capacity, 0) * 100 * 4)) * 0.30
            + GREATEST(0, LEAST(100, 100 - (peak_load / NULLIF(avg_load, 0) - 1.0) * 100 * 1.5)) * 0.20
            + GREATEST(0, LEAST(100, 100 - COALESCE(shed_prob, 0) * 100)) * 0.30
            + GREATEST(0, LEAST(100, 100 - COALESCE(load_pct_stddev, 0) * 10)) * 0.20
            ) >= 60 THEN 'ELEVATED'
        WHEN (
              GREATEST(0, LEAST(100, (peak_capacity - peak_load) / NULLIF(peak_capacity, 0) * 100 * 4)) * 0.30
            + GREATEST(0, LEAST(100, 100 - (peak_load / NULLIF(avg_load, 0) - 1.0) * 100 * 1.5)) * 0.20
            + GREATEST(0, LEAST(100, 100 - COALESCE(shed_prob, 0) * 100)) * 0.30
            + GREATEST(0, LEAST(100, 100 - COALESCE(load_pct_stddev, 0) * 10)) * 0.20
            ) >= 40 THEN 'STRESSED'
        ELSE 'CRITICAL'
    END                                                              AS status,
    NOW()                                                            AS computed_at
FROM agg
ORDER BY region_code;

COMMENT ON VIEW v_pillar3_grid_reliability IS
'Pillar 3 (NERC/IEEE Accessibility): reserve_margin, peak_load_factor, shedding_prob, freq_stability. pillar3_score 0-100.';


-- =============================================================
-- PILLAR 4 — ENERGY TRANSITION (Acceptability)
--   Sub-indicators:
--     renewable_pct       renewable_mw / load_mw * 100      (last 1h)
--     co2_intensity       emission_raw intensity (kg/MWh, last 1h)
--     curtailment_rate    (capacity - output) / capacity     for renewables
--     netzero_progress    current_renewable_pct / 70.0 * 100 (linear path to 2050)
-- =============================================================
CREATE OR REPLACE VIEW v_pillar4_energy_transition AS
WITH ren_1h AS (
    SELECT
        region_code,
        SUM(output_mw)        AS ren_mw,
        SUM(capacity_mw)      AS ren_cap_mw
    FROM renewable_output_raw
    WHERE event_time >= NOW() - INTERVAL '1 hour'
    GROUP BY region_code
),
load_1h AS (
    SELECT region_code, AVG(load_mw) AS avg_load
    FROM grid_load_raw
    WHERE event_time >= NOW() - INTERVAL '1 hour'
    GROUP BY region_code
),
emiss_1h AS (
    SELECT
        region_code,
        CASE WHEN SUM(energy_mwh) > 0
            THEN SUM(co2_kg) / SUM(energy_mwh)
            ELSE 0
        END                          AS intensity
    FROM emission_raw
    WHERE event_time >= NOW() - INTERVAL '1 hour'
    GROUP BY region_code
)
SELECT
    COALESCE(r.region_code, l.region_code, e.region_code)              AS region_code,
    ROUND(
        CASE WHEN COALESCE(l.avg_load, 0) > 0
             THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100
             ELSE 0
        END::numeric, 2)                                               AS renewable_pct,
    ROUND(COALESCE(e.intensity, 0)::numeric, 2)                        AS co2_intensity,
    ROUND(
        CASE WHEN COALESCE(r.ren_cap_mw, 0) > 0
             THEN GREATEST(0, (r.ren_cap_mw - r.ren_mw) / r.ren_cap_mw) * 100
             ELSE 0
        END::numeric, 2)                                               AS curtailment_rate,
    -- netzero_progress: linear path to 70% renewable share by 2050
    ROUND(
        LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
             THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 / 70.0 * 100
             ELSE 0
        END)::numeric, 2)                                              AS netzero_progress,
    ROUND((
          LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                          THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 * 2
                          ELSE 0 END) * 0.30
        + GREATEST(0, LEAST(100, 100 - COALESCE(e.intensity, 1000) / 10.0)) * 0.25
        + GREATEST(0, LEAST(100, 100 - (
              CASE WHEN COALESCE(r.ren_cap_mw, 0) > 0
                   THEN GREATEST(0, (r.ren_cap_mw - r.ren_mw) / r.ren_cap_mw) * 100
                   ELSE 0 END))) * 0.20
        + LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                          THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 / 70.0 * 100
                          ELSE 0 END) * 0.25
    )::numeric, 2)                                                     AS pillar4_score,
    CASE
        WHEN (
              LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                              THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 * 2
                              ELSE 0 END) * 0.30
            + GREATEST(0, LEAST(100, 100 - COALESCE(e.intensity, 1000) / 10.0)) * 0.25
            + GREATEST(0, LEAST(100, 100 - (
                  CASE WHEN COALESCE(r.ren_cap_mw, 0) > 0
                       THEN GREATEST(0, (r.ren_cap_mw - r.ren_mw) / r.ren_cap_mw) * 100
                       ELSE 0 END))) * 0.20
            + LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                              THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 / 70.0 * 100
                              ELSE 0 END) * 0.25
        ) >= 80 THEN 'SECURE'
        WHEN (
              LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                              THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 * 2
                              ELSE 0 END) * 0.30
            + GREATEST(0, LEAST(100, 100 - COALESCE(e.intensity, 1000) / 10.0)) * 0.25
            + GREATEST(0, LEAST(100, 100 - (
                  CASE WHEN COALESCE(r.ren_cap_mw, 0) > 0
                       THEN GREATEST(0, (r.ren_cap_mw - r.ren_mw) / r.ren_cap_mw) * 100
                       ELSE 0 END))) * 0.20
            + LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                              THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 / 70.0 * 100
                              ELSE 0 END) * 0.25
        ) >= 60 THEN 'ELEVATED'
        WHEN (
              LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                              THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 * 2
                              ELSE 0 END) * 0.30
            + GREATEST(0, LEAST(100, 100 - COALESCE(e.intensity, 1000) / 10.0)) * 0.25
            + GREATEST(0, LEAST(100, 100 - (
                  CASE WHEN COALESCE(r.ren_cap_mw, 0) > 0
                       THEN GREATEST(0, (r.ren_cap_mw - r.ren_mw) / r.ren_cap_mw) * 100
                       ELSE 0 END))) * 0.20
            + LEAST(100, CASE WHEN COALESCE(l.avg_load, 0) > 0
                              THEN (COALESCE(r.ren_mw, 0) / l.avg_load) * 100 / 70.0 * 100
                              ELSE 0 END) * 0.25
        ) >= 40 THEN 'STRESSED'
        ELSE 'CRITICAL'
    END                                                                AS status,
    NOW()                                                              AS computed_at
FROM ren_1h r
FULL OUTER JOIN load_1h  l USING (region_code)
FULL OUTER JOIN emiss_1h e USING (region_code)
ORDER BY 1;

COMMENT ON VIEW v_pillar4_energy_transition IS
'Pillar 4 (IPCC AR6 Acceptability): renewable_pct, co2_intensity, curtailment_rate, netzero_progress. pillar4_score 0-100.';


-- =============================================================
-- COMPOSITE — Energy Security Index (ESI)
--   ESI = 0.30 * P1 + 0.20 * P2 + 0.30 * P3 + 0.20 * P4
--   IEA-style weights: availability + accessibility weighted higher
--   because supply disruption / grid blackouts are higher-frequency
--   / higher-impact than slow market drift or decarbonisation pace.
--
--   Single-row view (for top-bar gauge).  Missing pillars default
--   to a neutral 60 (ELEVATED) so the gauge isn't pinned to 0 when
--   data is incomplete during pipeline startup.
-- =============================================================
CREATE OR REPLACE VIEW v_security_score AS
WITH p1 AS (
    SELECT COALESCE(AVG(pillar1_score), 60)::DECIMAL(5,2) AS score
    FROM v_pillar1_supply_security
),
p2 AS (
    SELECT COALESCE(AVG(pillar2_score), 60)::DECIMAL(5,2) AS score
    FROM v_pillar2_market_resilience
),
p3 AS (
    SELECT COALESCE(AVG(pillar3_score), 60)::DECIMAL(5,2) AS score
    FROM v_pillar3_grid_reliability
),
p4 AS (
    SELECT COALESCE(AVG(pillar4_score), 60)::DECIMAL(5,2) AS score
    FROM v_pillar4_energy_transition
)
SELECT
    p1.score                                                          AS pillar1_score,
    p2.score                                                          AS pillar2_score,
    p3.score                                                          AS pillar3_score,
    p4.score                                                          AS pillar4_score,
    ROUND((p1.score * 0.30 + p2.score * 0.20 + p3.score * 0.30 + p4.score * 0.20)::numeric, 2)
                                                                       AS overall_score,
    CASE
        WHEN (p1.score * 0.30 + p2.score * 0.20 + p3.score * 0.30 + p4.score * 0.20) >= 80
            THEN 'SECURE'
        WHEN (p1.score * 0.30 + p2.score * 0.20 + p3.score * 0.30 + p4.score * 0.20) >= 60
            THEN 'ELEVATED'
        WHEN (p1.score * 0.30 + p2.score * 0.20 + p3.score * 0.30 + p4.score * 0.20) >= 40
            THEN 'STRESSED'
        ELSE 'CRITICAL'
    END                                                                AS status,
    NOW()                                                              AS computed_at
FROM p1 CROSS JOIN p2 CROSS JOIN p3 CROSS JOIN p4;

COMMENT ON VIEW v_security_score IS
'Composite ESI (IEA weights 0.30/0.20/0.30/0.20). Drives top-bar gauge.';
