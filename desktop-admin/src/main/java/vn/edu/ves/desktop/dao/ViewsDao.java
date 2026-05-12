package vn.edu.ves.desktop.dao;

import vn.edu.ves.desktop.model.Pillar1SupplySecurity;
import vn.edu.ves.desktop.model.Pillar2MarketResilience;
import vn.edu.ves.desktop.model.Pillar3GridReliability;
import vn.edu.ves.desktop.model.Pillar4EnergyTransition;
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
 * Read-only DAO cho 6 view của Phase 7.1 (IEA/APERC framework).
 *
 * <ul>
 *   <li><code>v_security_score</code> — composite ESI (top-bar gauge).</li>
 *   <li><code>v_pillar1_supply_security</code> — Availability (IDR/SFRI/HHI/N-1).</li>
 *   <li><code>v_pillar2_market_resilience</code> — Affordability (σ30d/gap/β/affordability).</li>
 *   <li><code>v_pillar3_grid_reliability</code> — Accessibility (reserve/peak/shed/freq).</li>
 *   <li><code>v_pillar4_energy_transition</code> — Acceptability (renewable/CO2/curtail/netzero).</li>
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

    public List<Pillar1SupplySecurity> fetchPillar1SupplySecurity() {
        final String sql = "SELECT region_code, fuel_type, idr, sfri, hhi_supply, n1_resilience, " +
                "pillar1_score, status, computed_at FROM v_pillar1_supply_security";
        List<Pillar1SupplySecurity> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar1SupplySecurity p = new Pillar1SupplySecurity();
                p.setRegionCode(rs.getString("region_code"));
                p.setFuelType(rs.getString("fuel_type"));
                p.setIdr(rs.getBigDecimal("idr"));
                p.setSfri(rs.getBigDecimal("sfri"));
                p.setHhiSupply(rs.getBigDecimal("hhi_supply"));
                p.setN1Resilience(rs.getBigDecimal("n1_resilience"));
                p.setPillar1Score(rs.getBigDecimal("pillar1_score"));
                p.setStatus(getStringOrNull(rs, "status"));
                p.setComputedAt(getLocalDateTimeOrNull(rs, "computed_at"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar1SupplySecurity() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<Pillar2MarketResilience> fetchPillar2MarketResilience() {
        final String sql = "SELECT fuel_type, sigma_30d, price_gap_pct, beta_crude, affordability_idx, " +
                "pillar2_score, status, computed_at FROM v_pillar2_market_resilience";
        List<Pillar2MarketResilience> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar2MarketResilience p = new Pillar2MarketResilience();
                p.setFuelType(rs.getString("fuel_type"));
                p.setSigma30d(rs.getBigDecimal("sigma_30d"));
                p.setPriceGapPct(rs.getBigDecimal("price_gap_pct"));
                p.setBetaCrude(rs.getBigDecimal("beta_crude"));
                p.setAffordabilityIdx(rs.getBigDecimal("affordability_idx"));
                p.setPillar2Score(rs.getBigDecimal("pillar2_score"));
                p.setStatus(getStringOrNull(rs, "status"));
                p.setComputedAt(getLocalDateTimeOrNull(rs, "computed_at"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar2MarketResilience() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<Pillar3GridReliability> fetchPillar3GridReliability() {
        final String sql = "SELECT region_code, reserve_margin_pct, peak_load_factor, shedding_prob, " +
                "freq_stability_idx, pillar3_score, status, computed_at FROM v_pillar3_grid_reliability";
        List<Pillar3GridReliability> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar3GridReliability p = new Pillar3GridReliability();
                p.setRegionCode(rs.getString("region_code"));
                p.setReserveMarginPct(rs.getBigDecimal("reserve_margin_pct"));
                p.setPeakLoadFactor(rs.getBigDecimal("peak_load_factor"));
                p.setSheddingProb(rs.getBigDecimal("shedding_prob"));
                p.setFreqStabilityIdx(rs.getBigDecimal("freq_stability_idx"));
                p.setPillar3Score(rs.getBigDecimal("pillar3_score"));
                p.setStatus(getStringOrNull(rs, "status"));
                p.setComputedAt(getLocalDateTimeOrNull(rs, "computed_at"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar3GridReliability() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<Pillar4EnergyTransition> fetchPillar4EnergyTransition() {
        final String sql = "SELECT region_code, renewable_pct, co2_intensity, curtailment_rate, " +
                "netzero_progress, pillar4_score, status, computed_at FROM v_pillar4_energy_transition";
        List<Pillar4EnergyTransition> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pillar4EnergyTransition p = new Pillar4EnergyTransition();
                p.setRegionCode(rs.getString("region_code"));
                p.setRenewablePct(rs.getBigDecimal("renewable_pct"));
                p.setCo2Intensity(rs.getBigDecimal("co2_intensity"));
                p.setCurtailmentRate(rs.getBigDecimal("curtailment_rate"));
                p.setNetzeroProgress(rs.getBigDecimal("netzero_progress"));
                p.setPillar4Score(rs.getBigDecimal("pillar4_score"));
                p.setStatus(getStringOrNull(rs, "status"));
                p.setComputedAt(getLocalDateTimeOrNull(rs, "computed_at"));
                out.add(p);
            }
        } catch (SQLException e) {
            log.error("fetchPillar4EnergyTransition() lỗi: {}", e.getMessage(), e);
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
