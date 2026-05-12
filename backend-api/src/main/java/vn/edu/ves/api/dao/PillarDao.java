package vn.edu.ves.api.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.edu.ves.api.dto.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tất cả truy vấn cho 4 pillar views.
 *
 * Trỏ trực tiếp vào các view đã build sẵn ở Phase 2.5/2.6 — controller chỉ
 * forward kết quả, không tính toán thêm.
 */
@Repository
public class PillarDao {

    private final JdbcTemplate jdbc;

    public PillarDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---------- Pillar 1 ----------

    private static final RowMapper<Pillar1OutlookDto> PILLAR1_MAPPER = (rs, i) -> Pillar1OutlookDto.builder()
            .regionCode(rs.getString("region_code"))
            .regionName(rs.getString("region_name"))
            .fuelType(rs.getString("fuel_type"))
            .stockVolumeKl(rs.getBigDecimal("stock_volume_kl"))
            .dailyConsumptionKl(rs.getBigDecimal("daily_consumption_kl"))
            .stockDays(rs.getBigDecimal("stock_days"))
            .targetDays((Integer) rs.getObject("target_days"))
            .daysToCritical(rs.getBigDecimal("days_to_critical"))
            .daysAboveTarget(rs.getBigDecimal("days_above_target"))
            .targetAchievementPct(rs.getBigDecimal("target_achievement_pct"))
            .status(rs.getString("status"))
            .recommendationText(rs.getString("recommendation_text"))
            .suggestedDonorRegion(rs.getString("suggested_donor_region"))
            .reportedAt(toLocal(rs.getTimestamp("reported_at")))
            .build();

    public List<Pillar1OutlookDto> pillar1Outlook() {
        return jdbc.query(
                "SELECT * FROM v_pillar1_supply_outlook ORDER BY status, days_to_critical NULLS LAST",
                PILLAR1_MAPPER);
    }

    // ---------- Pillar 2 ----------

    private static final RowMapper<Pillar2VolatilityDto> PILLAR2_MAPPER = (rs, i) -> Pillar2VolatilityDto.builder()
            .fuelType(rs.getString("fuel_type"))
            .location(rs.getString("location"))
            .sampleCount(rs.getLong("sample_count"))
            .avgPrice(rs.getBigDecimal("avg_price"))
            .sigma(rs.getBigDecimal("sigma"))
            .relativeVolatilityPct(rs.getBigDecimal("relative_volatility_pct"))
            .rangeAbs(rs.getBigDecimal("range_abs"))
            .signal(rs.getString("signal"))
            .lastEvent(toLocal(rs.getTimestamp("last_event")))
            .build();

    public List<Pillar2VolatilityDto> pillar2Volatility() {
        return jdbc.query(
                "SELECT * FROM v_pillar2_volatility_signal ORDER BY relative_volatility_pct DESC NULLS LAST",
                PILLAR2_MAPPER);
    }

    // ---------- Pillar 3 ----------

    private static final RowMapper<Pillar3SheddingDto> PILLAR3_MAPPER = (rs, i) -> Pillar3SheddingDto.builder()
            .priorityLevel(rs.getLong("priority_level"))
            .regionCode(rs.getString("region_code"))
            .regionName(rs.getString("region_name"))
            .loadMw(rs.getBigDecimal("load_mw"))
            .capacityMw(rs.getBigDecimal("capacity_mw"))
            .loadPct(rs.getBigDecimal("load_pct"))
            .peakHour(rs.getBoolean("is_peak_hour"))
            .suggestedShedMw(rs.getBigDecimal("suggested_shed_mw"))
            .actionType(rs.getString("action_type"))
            .recommendationText(rs.getString("recommendation_text"))
            .eventTime(toLocal(rs.getTimestamp("event_time")))
            .build();

    public List<Pillar3SheddingDto> pillar3Shedding() {
        return jdbc.query(
                "SELECT * FROM v_pillar3_load_shedding_plan ORDER BY priority_level",
                PILLAR3_MAPPER);
    }

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
        return jdbc.query("SELECT * FROM v_pillar3_grid_load_latest ORDER BY load_pct DESC", GRID_LATEST_MAPPER);
    }

    // ---------- Pillar 4 ----------

    private static final RowMapper<Pillar4NetZeroDto> PILLAR4_MAPPER = (rs, i) -> Pillar4NetZeroDto.builder()
            .regionCode(rs.getString("region_code"))
            .regionName(rs.getString("region_name"))
            .renewableMw(rs.getBigDecimal("renewable_mw"))
            .avgLoadMw(rs.getBigDecimal("avg_load_mw"))
            .currentRenewableSharePct(rs.getBigDecimal("current_renewable_share_pct"))
            .target2026Pct(rs.getBigDecimal("target_2026_pct"))
            .target2030Pct(rs.getBigDecimal("target_2030_pct"))
            .status(rs.getString("status"))
            .recommendationText(rs.getString("recommendation_text"))
            .build();

    public List<Pillar4NetZeroDto> pillar4NetZero() {
        return jdbc.query(
                "SELECT * FROM v_pillar4_net_zero_progress ORDER BY current_renewable_share_pct DESC NULLS LAST",
                PILLAR4_MAPPER);
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
}
