package vn.edu.ves.api.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.edu.ves.api.dto.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * DAO cho 4 pillar views (Phase 7.1 IEA/APERC framework).
 *
 * <p>Mỗi view trả về 4 sub-indicator + composite {@code pillarN_score}
 * + {@code status} (SECURE / ELEVATED / STRESSED / CRITICAL) + {@code computed_at}.
 * Controller chỉ forward kết quả; không tính toán thêm.</p>
 *
 * <p>Phase 2.5 helper view {@code v_pillar3_grid_load_latest} vẫn tồn tại
 * và được dùng bởi {@link vn.edu.ves.api.controller.RawDataController}.</p>
 */
@Repository
public class PillarDao {

    private final JdbcTemplate jdbc;

    public PillarDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- Pillar 1 — Supply Security (Availability) ----------

    private static final RowMapper<Pillar1SupplySecurityDto> PILLAR1_MAPPER = (rs, i) ->
            Pillar1SupplySecurityDto.builder()
                    .regionCode(rs.getString("region_code"))
                    .fuelType(rs.getString("fuel_type"))
                    .idr(rs.getBigDecimal("idr"))
                    .sfri(rs.getBigDecimal("sfri"))
                    .hhiSupply(rs.getBigDecimal("hhi_supply"))
                    .n1Resilience(rs.getBigDecimal("n1_resilience"))
                    .pillar1Score(rs.getBigDecimal("pillar1_score"))
                    .status(rs.getString("status"))
                    .computedAt(toOffset(rs.getTimestamp("computed_at")))
                    .build();

    public List<Pillar1SupplySecurityDto> findPillar1SupplySecurity() {
        return jdbc.query(
                "SELECT region_code, fuel_type, idr, sfri, hhi_supply, n1_resilience, " +
                "pillar1_score, status, computed_at " +
                "FROM v_pillar1_supply_security " +
                "ORDER BY pillar1_score ASC NULLS LAST, region_code, fuel_type",
                PILLAR1_MAPPER);
    }

    // ---------- Pillar 2 — Market Resilience (Affordability) ----------

    private static final RowMapper<Pillar2MarketResilienceDto> PILLAR2_MAPPER = (rs, i) ->
            Pillar2MarketResilienceDto.builder()
                    .fuelType(rs.getString("fuel_type"))
                    .sigma30d(rs.getBigDecimal("sigma_30d"))
                    .priceGapPct(rs.getBigDecimal("price_gap_pct"))
                    .betaCrude(rs.getBigDecimal("beta_crude"))
                    .affordabilityIdx(rs.getBigDecimal("affordability_idx"))
                    .pillar2Score(rs.getBigDecimal("pillar2_score"))
                    .status(rs.getString("status"))
                    .computedAt(toOffset(rs.getTimestamp("computed_at")))
                    .build();

    public List<Pillar2MarketResilienceDto> findPillar2MarketResilience() {
        return jdbc.query(
                "SELECT fuel_type, sigma_30d, price_gap_pct, beta_crude, affordability_idx, " +
                "pillar2_score, status, computed_at " +
                "FROM v_pillar2_market_resilience " +
                "ORDER BY pillar2_score ASC NULLS LAST, fuel_type",
                PILLAR2_MAPPER);
    }

    // ---------- Pillar 3 — Grid Reliability (Accessibility) ----------

    private static final RowMapper<Pillar3GridReliabilityDto> PILLAR3_MAPPER = (rs, i) ->
            Pillar3GridReliabilityDto.builder()
                    .regionCode(rs.getString("region_code"))
                    .reserveMarginPct(rs.getBigDecimal("reserve_margin_pct"))
                    .peakLoadFactor(rs.getBigDecimal("peak_load_factor"))
                    .sheddingProb(rs.getBigDecimal("shedding_prob"))
                    .freqStabilityIdx(rs.getBigDecimal("freq_stability_idx"))
                    .pillar3Score(rs.getBigDecimal("pillar3_score"))
                    .status(rs.getString("status"))
                    .computedAt(toOffset(rs.getTimestamp("computed_at")))
                    .build();

    public List<Pillar3GridReliabilityDto> findPillar3GridReliability() {
        return jdbc.query(
                "SELECT region_code, reserve_margin_pct, peak_load_factor, shedding_prob, " +
                "freq_stability_idx, pillar3_score, status, computed_at " +
                "FROM v_pillar3_grid_reliability " +
                "ORDER BY pillar3_score ASC NULLS LAST, region_code",
                PILLAR3_MAPPER);
    }

    // ---------- Pillar 4 — Energy Transition (Acceptability) ----------

    private static final RowMapper<Pillar4EnergyTransitionDto> PILLAR4_MAPPER = (rs, i) ->
            Pillar4EnergyTransitionDto.builder()
                    .regionCode(rs.getString("region_code"))
                    .renewablePct(rs.getBigDecimal("renewable_pct"))
                    .co2Intensity(rs.getBigDecimal("co2_intensity"))
                    .curtailmentRate(rs.getBigDecimal("curtailment_rate"))
                    .netzeroProgress(rs.getBigDecimal("netzero_progress"))
                    .pillar4Score(rs.getBigDecimal("pillar4_score"))
                    .status(rs.getString("status"))
                    .computedAt(toOffset(rs.getTimestamp("computed_at")))
                    .build();

    public List<Pillar4EnergyTransitionDto> findPillar4EnergyTransition() {
        return jdbc.query(
                "SELECT region_code, renewable_pct, co2_intensity, curtailment_rate, " +
                "netzero_progress, pillar4_score, status, computed_at " +
                "FROM v_pillar4_energy_transition " +
                "ORDER BY pillar4_score DESC NULLS LAST, region_code",
                PILLAR4_MAPPER);
    }

    // ---------- Pillar 3 helper — grid load latest (Phase 2.5, kept for raw view) ----------

    private static final RowMapper<GridLoadLatestDto> GRID_LATEST_MAPPER = (rs, i) -> GridLoadLatestDto.builder()
            .regionCode(rs.getString("region_code"))
            .regionName(rs.getString("region_name"))
            .loadMw(rs.getBigDecimal("load_mw"))
            .capacityMw(rs.getBigDecimal("capacity_mw"))
            .loadPct(rs.getBigDecimal("load_pct"))
            .peakHour(rs.getBoolean("is_peak_hour"))
            .status(rs.getString("status"))
            .eventTime(toLocal(rs.getTimestamp("event_time")))
            .build();

    public List<GridLoadLatestDto> gridLoadLatest() {
        return jdbc.query(
                "SELECT region_code, region_name, load_mw, capacity_mw, load_pct, " +
                "is_peak_hour, status, event_time " +
                "FROM v_pillar3_grid_load_latest ORDER BY load_pct DESC",
                GRID_LATEST_MAPPER);
    }

    // ---------- Fuel prices raw (Pillar 2 chi tiết) ----------

    private static final RowMapper<FuelPriceDto> FUEL_MAPPER = (rs, i) -> FuelPriceDto.builder()
            .id(rs.getLong("id"))
            .eventTimestamp(toLocal(rs.getTimestamp("event_timestamp")))
            .fuelType(rs.getString("fuel_type"))
            .price(rs.getBigDecimal("price"))
            .priceUnit(rs.getString("price_unit"))
            .location(rs.getString("location"))
            .region(rs.getString("region"))
            .source(rs.getString("source"))
            .build();

    public List<FuelPriceDto> latestFuelPrices(String fuelType, int limit) {
        if (fuelType == null || fuelType.isBlank()) {
            return jdbc.query(
                    "SELECT id, event_timestamp, fuel_type, price, price_unit, location, region, source " +
                    "FROM fuel_prices_raw ORDER BY event_timestamp DESC LIMIT ?",
                    FUEL_MAPPER, limit);
        }
        return jdbc.query(
                "SELECT id, event_timestamp, fuel_type, price, price_unit, location, region, source " +
                "FROM fuel_prices_raw WHERE fuel_type = ? ORDER BY event_timestamp DESC LIMIT ?",
                FUEL_MAPPER, fuelType, limit);
    }

    // ---------- helpers ----------

    private static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static OffsetDateTime toOffset(Timestamp ts) {
        return ts == null ? null : ts.toInstant().atOffset(ZoneOffset.UTC);
    }
}
