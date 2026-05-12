package vn.edu.ves.desktop.dao;

import vn.edu.ves.desktop.model.Pillar1Outlook;
import vn.edu.ves.desktop.model.Pillar2Volatility;
import vn.edu.ves.desktop.model.Pillar3Shedding;
import vn.edu.ves.desktop.model.Pillar4NetZero;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;
import vn.edu.ves.desktop.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Read-only DAO cho 6 view "actionable" của Phase 2.5/2.6:
 *
 * <ul>
 *   <li><code>v_security_score</code> — overall score gauge.</li>
 *   <li><code>v_pillar1_supply_outlook</code> — Pillar 1 inventory + recommendation.</li>
 *   <li><code>v_pillar2_volatility_signal</code> — Pillar 2 σ rolling 1h.</li>
 *   <li><code>v_pillar3_load_shedding_plan</code> — Pillar 3 overload + shed plan.</li>
 *   <li><code>v_pillar4_net_zero_progress</code> — Pillar 4 renewable share roadmap.</li>
 *   <li><code>v_active_recommendations</code> — recommendations PENDING.</li>
 * </ul>
 */
public class ViewsDao extends BaseDao {

    private final DatabaseConfig dbConfig;

    public ViewsDao() {
        this(DatabaseConfig.getInstance());
    }

    public ViewsDao(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Optional<SecurityScore> fetchSecurityScore() {
        final String sql = "SELECT pillar1_score, pillar2_score, pillar3_score, pillar4_score, " +
                "overall_score, status, computed_at FROM v_security_score";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                SecurityScore s = new SecurityScore();
                s.setPillar1Score(rs.getBigDecimal("pillar1_score"));
                s.setPillar2Score(rs.getBigDecimal("pillar2_score"));
                s.setPillar3Score(rs.getBigDecimal("pillar3_score"));
                s.setPillar4Score(rs.getBigDecimal("pillar4_score"));
                s.setOverallScore(rs.getBigDecimal("overall_score"));
                s.setStatus(getStringOrNull(rs, "status"));
                s.setComputedAt(getLocalDateTimeOrNull(rs, "computed_at"));
                return Optional.of(s);
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error("fetchSecurityScore() lỗi: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public List<Pillar1Outlook> fetchPillar1Outlook() {
        final String sql = "SELECT region_code, region_name, fuel_type, stock_volume_kl, " +
                "daily_consumption_kl, stock_days, target_days, days_to_critical, " +
                "days_above_target, target_achievement_pct, status, recommendation_text, " +
                "suggested_donor_region, reported_at FROM v_pillar1_supply_outlook";
        List<Pillar1Outlook> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar1Outlook p = new Pillar1Outlook();
                p.setRegionCode(rs.getString("region_code"));
                p.setRegionName(getStringOrNull(rs, "region_name"));
                p.setFuelType(rs.getString("fuel_type"));
                p.setStockVolumeKl(rs.getBigDecimal("stock_volume_kl"));
                p.setDailyConsumptionKl(rs.getBigDecimal("daily_consumption_kl"));
                p.setStockDays(rs.getBigDecimal("stock_days"));
                p.setTargetDays(rs.getInt("target_days"));
                p.setDaysToCritical(rs.getBigDecimal("days_to_critical"));
                p.setDaysAboveTarget(rs.getBigDecimal("days_above_target"));
                p.setTargetAchievementPct(rs.getBigDecimal("target_achievement_pct"));
                p.setStatus(getStringOrNull(rs, "status"));
                p.setRecommendationText(getStringOrNull(rs, "recommendation_text"));
                p.setSuggestedDonorRegion(getStringOrNull(rs, "suggested_donor_region"));
                p.setReportedAt(getLocalDateTimeOrNull(rs, "reported_at"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar1Outlook() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<Pillar2Volatility> fetchPillar2Volatility() {
        final String sql = "SELECT fuel_type, location, sample_count, avg_price, sigma, " +
                "relative_volatility_pct, range_abs, signal, last_event " +
                "FROM v_pillar2_volatility_signal";
        List<Pillar2Volatility> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar2Volatility p = new Pillar2Volatility();
                p.setFuelType(rs.getString("fuel_type"));
                p.setLocation(getStringOrNull(rs, "location"));
                p.setSampleCount(rs.getInt("sample_count"));
                p.setAvgPrice(rs.getBigDecimal("avg_price"));
                p.setSigma(rs.getBigDecimal("sigma"));
                p.setRelativeVolatilityPct(rs.getBigDecimal("relative_volatility_pct"));
                p.setRangeAbs(rs.getBigDecimal("range_abs"));
                p.setSignal(getStringOrNull(rs, "signal"));
                p.setLastEvent(getLocalDateTimeOrNull(rs, "last_event"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar2Volatility() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<Pillar3Shedding> fetchPillar3Shedding() {
        final String sql = "SELECT priority_level, region_code, region_name, load_mw, capacity_mw, " +
                "load_pct, is_peak_hour, suggested_shed_mw, action_type, recommendation_text, event_time " +
                "FROM v_pillar3_load_shedding_plan";
        List<Pillar3Shedding> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar3Shedding p = new Pillar3Shedding();
                p.setPriorityLevel(rs.getLong("priority_level"));
                p.setRegionCode(rs.getString("region_code"));
                p.setRegionName(getStringOrNull(rs, "region_name"));
                p.setLoadMw(rs.getBigDecimal("load_mw"));
                p.setCapacityMw(rs.getBigDecimal("capacity_mw"));
                p.setLoadPct(rs.getBigDecimal("load_pct"));
                p.setPeakHour(rs.getBoolean("is_peak_hour"));
                p.setSuggestedShedMw(rs.getBigDecimal("suggested_shed_mw"));
                p.setActionType(getStringOrNull(rs, "action_type"));
                p.setRecommendationText(getStringOrNull(rs, "recommendation_text"));
                p.setEventTime(getLocalDateTimeOrNull(rs, "event_time"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar3Shedding() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<Pillar4NetZero> fetchPillar4NetZero() {
        final String sql = "SELECT region_code, region_name, renewable_mw, avg_load_mw, " +
                "current_renewable_share_pct, target_2026_pct, target_2030_pct, status, recommendation_text " +
                "FROM v_pillar4_net_zero_progress";
        List<Pillar4NetZero> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar4NetZero p = new Pillar4NetZero();
                p.setRegionCode(rs.getString("region_code"));
                p.setRegionName(getStringOrNull(rs, "region_name"));
                p.setRenewableMw(rs.getBigDecimal("renewable_mw"));
                p.setAvgLoadMw(rs.getBigDecimal("avg_load_mw"));
                p.setCurrentRenewableSharePct(rs.getBigDecimal("current_renewable_share_pct"));
                p.setTarget2026Pct(rs.getBigDecimal("target_2026_pct"));
                p.setTarget2030Pct(rs.getBigDecimal("target_2030_pct"));
                p.setStatus(getStringOrNull(rs, "status"));
                p.setRecommendationText(getStringOrNull(rs, "recommendation_text"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar4NetZero() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<Recommendation> fetchActiveRecommendations() {
        final String sql = "SELECT id, pillar, action_type, severity, title, message, " +
                "suggested_data::text AS suggested_data_text, suggested_at, age_seconds, " +
                "expires_at, is_expired FROM v_active_recommendations";
        List<Recommendation> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Recommendation r = new Recommendation();
                r.setId(rs.getLong("id"));
                r.setPillar(rs.getInt("pillar"));
                r.setActionType(getStringOrNull(rs, "action_type"));
                r.setSeverity(getStringOrNull(rs, "severity"));
                r.setTitle(getStringOrNull(rs, "title"));
                r.setMessage(getStringOrNull(rs, "message"));
                r.setSuggestedDataJson(getStringOrNull(rs, "suggested_data_text"));
                r.setSuggestedAt(getLocalDateTimeOrNull(rs, "suggested_at"));
                r.setAgeSeconds(rs.getInt("age_seconds"));
                r.setExpiresAt(getLocalDateTimeOrNull(rs, "expires_at"));
                r.setExpired(rs.getBoolean("is_expired"));
                out.add(r);
            }
        } catch (SQLException e) {
            log.error("fetchActiveRecommendations() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }
}
