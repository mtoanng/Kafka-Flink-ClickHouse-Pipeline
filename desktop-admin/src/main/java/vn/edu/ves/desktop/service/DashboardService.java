package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.model.Pillar1Outlook;
import vn.edu.ves.desktop.model.Pillar2Volatility;
import vn.edu.ves.desktop.model.Pillar3Shedding;
import vn.edu.ves.desktop.model.Pillar4NetZero;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;

import java.util.List;
import java.util.Optional;

/**
 * Service tổng hợp data cho màn Dashboard.
 *
 * <p>Tách interface để DashboardController có thể inject mock khi test
 * và để dễ thay DAO source về sau (REST API thay JDBC chẳng hạn).</p>
 */
public interface DashboardService {

    Optional<SecurityScore> getSecurityScore();

    List<Pillar1Outlook> getPillar1();

    List<Pillar2Volatility> getPillar2();

    List<Pillar3Shedding> getPillar3();

    List<Pillar4NetZero> getPillar4();

    List<Recommendation> getActiveRecommendations();
}
