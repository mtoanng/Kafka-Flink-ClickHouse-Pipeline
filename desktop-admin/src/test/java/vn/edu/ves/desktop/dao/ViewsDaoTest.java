package vn.edu.ves.desktop.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.model.Pillar1Outlook;
import vn.edu.ves.desktop.model.Pillar2Volatility;
import vn.edu.ves.desktop.model.Pillar3Shedding;
import vn.edu.ves.desktop.model.Pillar4NetZero;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * H2 in-memory tests cho {@link ViewsDao}.
 *
 * <p>Dùng MODE=PostgreSQL (H2 2.2.224) để hỗ trợ <code>::text</code> cast.
 * Tạo "view" giả lập bằng cách CREATE TABLE + INSERT data fix → DAO query SELECT
 * như thường (view và table cùng readable).</p>
 */
public class ViewsDaoTest {

    private ViewsDao dao;

    @Before
    public void setUp() throws Exception {
        H2TestSupport.overrideSingletonToH2();
        try (Connection c = H2TestSupport.openConnection()) {
            dropAll(c);
            createFixtures(c);
        }
        dao = new ViewsDao();
    }

    @After
    public void tearDown() throws Exception {
        try (Connection c = H2TestSupport.openConnection()) {
            dropAll(c);
        }
    }

    @Test
    public void fetchSecurityScore_returnsSingleRow() {
        Optional<SecurityScore> opt = dao.fetchSecurityScore();
        assertTrue("v_security_score phải có row", opt.isPresent());
        SecurityScore s = opt.get();
        assertNotNull(s.getOverallScore());
        assertEquals("SECURE", s.getStatus());
        assertTrue(s.getOverallScore().doubleValue() > 0);
    }

    @Test
    public void fetchPillar1Outlook_returnsRowsWithRecommendation() {
        List<Pillar1Outlook> rows = dao.fetchPillar1Outlook();
        assertEquals(2, rows.size());
        Pillar1Outlook first = rows.get(0);
        assertEquals("VN_NORTH", first.getRegionCode());
        assertNotNull(first.getRecommendationText());
    }

    @Test
    public void fetchPillar2Volatility_returnsRows() {
        List<Pillar2Volatility> rows = dao.fetchPillar2Volatility();
        assertEquals(1, rows.size());
        assertEquals("WTI_CRUDE", rows.get(0).getFuelType());
        assertEquals("STABLE", rows.get(0).getSignal());
    }

    @Test
    public void fetchPillar3Shedding_returnsRowsOrderedByPriority() {
        List<Pillar3Shedding> rows = dao.fetchPillar3Shedding();
        assertEquals(2, rows.size());
        assertEquals(1L, rows.get(0).getPriorityLevel());
    }

    @Test
    public void fetchPillar4NetZero_returnsRowsWithStatus() {
        List<Pillar4NetZero> rows = dao.fetchPillar4NetZero();
        assertEquals(2, rows.size());
        assertEquals("ON_TRACK", rows.get(0).getStatus());
    }

    @Test
    public void fetchActiveRecommendations_filtersExpired() {
        List<Recommendation> rows = dao.fetchActiveRecommendations();
        assertEquals(2, rows.size());
        for (Recommendation r : rows) {
            assertFalse("Test fixtures non-expired", r.isExpired());
        }
    }

    /* ------------------------------------------------------- */

    private static final String[] VIEW_NAMES = {
            "v_security_score",
            "v_pillar1_supply_outlook",
            "v_pillar2_volatility_signal",
            "v_pillar3_load_shedding_plan",
            "v_pillar4_net_zero_progress",
            "v_active_recommendations"
    };

    private static void dropAll(Connection c) throws Exception {
        for (String v : VIEW_NAMES) {
            H2TestSupport.exec(c, "DROP VIEW IF EXISTS " + v);
            H2TestSupport.exec(c, "DROP TABLE IF EXISTS " + v);
        }
    }

    private static void createFixtures(Connection c) throws Exception {
        // v_security_score
        H2TestSupport.exec(c,
                "CREATE TABLE v_security_score (" +
                " pillar1_score NUMERIC(5,2), pillar2_score NUMERIC(5,2)," +
                " pillar3_score NUMERIC(5,2), pillar4_score NUMERIC(5,2)," +
                " overall_score NUMERIC(5,2), status VARCHAR(20), computed_at TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_security_score VALUES (80.5, 72.3, 60.1, 85.0, 74.5, 'SECURE', CURRENT_TIMESTAMP)");

        // v_pillar1_supply_outlook
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar1_supply_outlook (" +
                " region_code VARCHAR(20), region_name VARCHAR(100), fuel_type VARCHAR(50)," +
                " stock_volume_kl NUMERIC(14,2), daily_consumption_kl NUMERIC(14,2)," +
                " stock_days NUMERIC(6,1), target_days INT," +
                " days_to_critical NUMERIC(6,1), days_above_target NUMERIC(6,1)," +
                " target_achievement_pct NUMERIC(5,1), status VARCHAR(20)," +
                " recommendation_text VARCHAR(500), suggested_donor_region VARCHAR(20)," +
                " reported_at TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar1_supply_outlook VALUES " +
                "('VN_NORTH','Miền Bắc','GASOLINE',120000.0,2000.0,60.0,90,30.0,0.0,66.7," +
                " 'WARNING','Tăng tồn kho thêm 60000 kl','VN_SOUTH',CURRENT_TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar1_supply_outlook VALUES " +
                "('VN_SOUTH','Miền Nam','GASOLINE',200000.0,2500.0,80.0,90,50.0,0.0,88.9," +
                " 'BELOW_TARGET','Tiến độ 89% mục tiêu',NULL,CURRENT_TIMESTAMP)");

        // v_pillar2_volatility_signal
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar2_volatility_signal (" +
                " fuel_type VARCHAR(50), location VARCHAR(100), sample_count INT," +
                " avg_price NUMERIC(12,4), sigma NUMERIC(12,4)," +
                " relative_volatility_pct NUMERIC(5,2), range_abs NUMERIC(12,4)," +
                " signal VARCHAR(20), last_event TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar2_volatility_signal VALUES " +
                "('WTI_CRUDE','New York',10,82.4500,0.4200,0.51,1.2300,'STABLE',CURRENT_TIMESTAMP)");

        // v_pillar3_load_shedding_plan
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar3_load_shedding_plan (" +
                " priority_level BIGINT, region_code VARCHAR(20), region_name VARCHAR(100)," +
                " load_mw NUMERIC(12,2), capacity_mw NUMERIC(12,2), load_pct NUMERIC(5,2)," +
                " is_peak_hour BOOLEAN, suggested_shed_mw NUMERIC(12,2)," +
                " action_type VARCHAR(50), recommendation_text VARCHAR(500), event_time TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar3_load_shedding_plan VALUES " +
                "(1,'VN_NORTH','Miền Bắc',9200.0,10000.0,92.0,TRUE,1200.0,'PEAK_SHAVING'," +
                " 'WARNING: Kích hoạt peak shaving 1200 MW',CURRENT_TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar3_load_shedding_plan VALUES " +
                "(2,'VN_SOUTH','Miền Nam',8800.0,10000.0,88.0,TRUE,800.0,'DEMAND_RESPONSE'," +
                " 'WATCH',CURRENT_TIMESTAMP)");

        // v_pillar4_net_zero_progress
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar4_net_zero_progress (" +
                " region_code VARCHAR(20), region_name VARCHAR(100)," +
                " renewable_mw NUMERIC(12,2), avg_load_mw NUMERIC(12,2)," +
                " current_renewable_share_pct NUMERIC(5,2)," +
                " target_2026_pct NUMERIC(5,2), target_2030_pct NUMERIC(5,2)," +
                " status VARCHAR(20), recommendation_text VARCHAR(500))");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar4_net_zero_progress VALUES " +
                "('VN_NORTH','Miền Bắc',2500.0,8000.0,31.25,25.0,47.0,'ON_TRACK','ON_TRACK mục tiêu 2026')");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar4_net_zero_progress VALUES " +
                "('VN_SOUTH','Miền Nam',1200.0,9000.0,13.33,25.0,47.0,'BEHIND','Đang BEHIND mục tiêu 2026')");

        // v_active_recommendations — DAO query dùng 'suggested_data::text AS suggested_data_text'
        // → H2 MODE=PostgreSQL 2.x hỗ trợ '::' cast operator → giữ column tên 'suggested_data' VARCHAR.
        H2TestSupport.exec(c,
                "CREATE TABLE v_active_recommendations (" +
                " id BIGINT, pillar SMALLINT, action_type VARCHAR(50), severity VARCHAR(20)," +
                " title VARCHAR(200), message VARCHAR(500), suggested_data VARCHAR(500)," +
                " suggested_at TIMESTAMP, age_seconds INT, expires_at TIMESTAMP, is_expired BOOLEAN)");
        H2TestSupport.exec(c,
                "INSERT INTO v_active_recommendations VALUES " +
                "(1,1,'TRANSFER_STOCK','WARNING','Pillar 1 stock low'," +
                " 'Region VN_NORTH cần thêm 60000 kl','{}',CURRENT_TIMESTAMP,120,NULL,FALSE)");
        H2TestSupport.exec(c,
                "INSERT INTO v_active_recommendations VALUES " +
                "(2,3,'PEAK_SHAVING','CRITICAL','Pillar 3 overload'," +
                " 'Region VN_NORTH load 92%','{}',CURRENT_TIMESTAMP,30,NULL,FALSE)");
    }
}
