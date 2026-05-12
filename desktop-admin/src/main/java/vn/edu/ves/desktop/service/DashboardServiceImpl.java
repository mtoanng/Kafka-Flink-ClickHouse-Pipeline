package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.dao.ViewsDao;
import vn.edu.ves.desktop.model.Pillar1SupplySecurity;
import vn.edu.ves.desktop.model.Pillar2MarketResilience;
import vn.edu.ves.desktop.model.Pillar3GridReliability;
import vn.edu.ves.desktop.model.Pillar4EnergyTransition;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;

import java.util.List;
import java.util.Optional;

/**
 * Thin delegation tới {@link ViewsDao}.
 *
 * <p>Phase 7.1: rewired to IEA/APERC framework. Service layer kept thin so we can
 * later wrap caching / business rules without churning the controller.</p>
 */
public class DashboardServiceImpl implements DashboardService {

    private final ViewsDao viewsDao;

    public DashboardServiceImpl() {
        this(new ViewsDao());
    }

    public DashboardServiceImpl(ViewsDao viewsDao) {
        this.viewsDao = viewsDao;
    }

    @Override
    public Optional<SecurityScore> getSecurityScore() {
        return viewsDao.fetchSecurityScore();
    }

    @Override
    public List<Pillar1SupplySecurity> getSupplySecurity() {
        return viewsDao.fetchPillar1SupplySecurity();
    }

    @Override
    public List<Pillar2MarketResilience> getMarketResilience() {
        return viewsDao.fetchPillar2MarketResilience();
    }

    @Override
    public List<Pillar3GridReliability> getGridReliability() {
        return viewsDao.fetchPillar3GridReliability();
    }

    @Override
    public List<Pillar4EnergyTransition> getEnergyTransition() {
        return viewsDao.fetchPillar4EnergyTransition();
    }

    @Override
    public List<Recommendation> getActiveRecommendations() {
        return viewsDao.fetchActiveRecommendations();
    }
}
