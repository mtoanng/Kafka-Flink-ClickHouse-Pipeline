package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.dao.ViewsDao;
import vn.edu.ves.desktop.model.Pillar1Outlook;
import vn.edu.ves.desktop.model.Pillar2Volatility;
import vn.edu.ves.desktop.model.Pillar3Shedding;
import vn.edu.ves.desktop.model.Pillar4NetZero;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;

import java.util.List;
import java.util.Optional;

/**
 * Thin delegation tới {@link ViewsDao}.
 *
 * <p>Phase 5.2: chỉ pass-through. Khi cần thêm caching / business rule (vd auto refresh
 * theo trend / threshold) sẽ wrap vào đây thay vì controller.</p>
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
    public List<Pillar1Outlook> getPillar1() {
        return viewsDao.fetchPillar1Outlook();
    }

    @Override
    public List<Pillar2Volatility> getPillar2() {
        return viewsDao.fetchPillar2Volatility();
    }

    @Override
    public List<Pillar3Shedding> getPillar3() {
        return viewsDao.fetchPillar3Shedding();
    }

    @Override
    public List<Pillar4NetZero> getPillar4() {
        return viewsDao.fetchPillar4NetZero();
    }

    @Override
    public List<Recommendation> getActiveRecommendations() {
        return viewsDao.fetchActiveRecommendations();
    }
}
