package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.model.Pillar1SupplySecurity;
import vn.edu.ves.desktop.model.Pillar2MarketResilience;
import vn.edu.ves.desktop.model.Pillar3GridReliability;
import vn.edu.ves.desktop.model.Pillar4EnergyTransition;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;

import java.util.List;
import java.util.Optional;

/**
 * Service tổng hợp data cho màn Dashboard.
 *
 * <p>Phase 7.1: rewired to IEA/APERC pillar taxonomy. 4 getters renamed to
 * {@code getSupplySecurity / getMarketResilience / getGridReliability /
 * getEnergyTransition} for clarity.</p>
 */
public interface DashboardService {

    Optional<SecurityScore> getSecurityScore();

    List<Pillar1SupplySecurity> getSupplySecurity();

    List<Pillar2MarketResilience> getMarketResilience();

    List<Pillar3GridReliability> getGridReliability();

    List<Pillar4EnergyTransition> getEnergyTransition();

    List<Recommendation> getActiveRecommendations();

    /**
     * Return latest alerts (mapped to {@link vn.edu.ves.desktop.model.Recommendation}
     * so the dashboard can reuse the same UI widgets for display.
     */
    List<Recommendation> getActiveAlerts();
}
