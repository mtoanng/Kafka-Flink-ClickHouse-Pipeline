package vn.edu.ves.desktop.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.model.Pillar1SupplySecurity;
import vn.edu.ves.desktop.model.Pillar2MarketResilience;
import vn.edu.ves.desktop.model.Pillar3GridReliability;
import vn.edu.ves.desktop.model.Pillar4EnergyTransition;
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
 * H2 in-memory tests cho {@link ViewsDao} (Phase 7.1, IEA/APERC pillar shape).
 *
 * <p>Vì view SQL trên Postgres rất phức tạp (REGR_SLOPE, STDDEV_SAMP, FULL OUTER JOIN),
 * ta tạo bảng giả lập có cùng SHAPE column thay vì re-implement view trong H2.
 * Mỗi DAO chỉ làm SELECT * FROM v_… nên đủ để chứng minh column mapping đúng.</p>
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
        assertEquals("ELEVATED", s.getStatus());
        assertTrue(s.getOverallScore().doubleValue() > 0);
    }

    @Test
    public void fetchPillar1SupplySecurity_returnsRowsWithIeaIndicators() {
        List<Pillar1SupplySecurity> rows = dao.fetchPillar1SupplySecurity();
        assertEquals(2, rows.size());
        Pillar1SupplySecurity first = rows.get(0);
        assertEquals("VN_NORTH", first.getRegionCode());
        assertNotNull(first.getIdr());
        assertNotNull(first.getSfri());
        assertNotNull(first.getHhiSupply());
        assertNotNull(first.getN1Resilience());
        assertNotNull(first.getPillar1Score());
        assertEquals("ELEVATED", first.getStatus());
    }

    @Test
    public void fetchPillar2MarketResilience_returnsRows() {
        List<Pillar2MarketResilience> rows = dao.fetchPillar2MarketResilience();
        assertEquals(1, rows.size());
        Pillar2MarketResilience p = rows.get(0);
        assertEquals("WTI_CRUDE", p.getFuelType());
        assertEquals("SECURE", p.getStatus());
        assertNotNull(p.getSigma30d());
        assertNotNull(p.getPriceGapPct());
        assertNotNull(p.getBetaCrude());
        assertNotNull(p.getAffordabilityIdx());
    }

    @Test
    public void fetchPillar3GridReliability_returnsRowsWithReserveMargin() {
        List<Pillar3GridReliability> rows = dao.fetchPillar3GridReliability();
        assertEquals(2, rows.size());
        Pillar3GridReliability p = rows.get(0);
        assertEquals("VN_NORTH", p.getRegionCode());
        assertNotNull(p.getReserveMarginPct());
        assertNotNull(p.getPeakLoadFactor());
        assertNotNull(p.getSheddingProb());
        assertNotNull(p.getFreqStabilityIdx());
        assertEquals("ELEVATED", p.getStatus());
    }

    @Test
    public void fetchPillar4EnergyTransition_returnsRowsWithStatus() {
        List<Pillar4EnergyTransition> rows = dao.fetchPillar4EnergyTransition();
        assertEquals(2, rows.size());
        Pillar4EnergyTransition p = rows.get(0);
        assertEquals("VN_NORTH", p.getRegionCode());
        assertNotNull(p.getRenewablePct());
        assertNotNull(p.getCo2Intensity());
        assertNotNull(p.getCurtailmentRate());
        assertNotNull(p.getNetzeroProgress());
        assertEquals("ELEVATED", p.getStatus());
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
            "v_pillar1_supply_security",
            "v_pillar2_market_resilience",
            "v_pillar3_grid_reliability",
            "v_pillar4_energy_transition",
            "v_active_recommendations"
    };

    private static void dropAll(Connection c) throws Exception {
        for (String v : VIEW_NAMES) {
            try {
                H2TestSupport.exec(c, "DROP VIEW IF EXISTS " + v);
            } catch (Exception ignore) {
                // H2 IF EXISTS may throw when object exists as TABLE — drop next.
            }
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
                "INSERT INTO v_security_score VALUES (64.83, 64.06, 90.93, 72.15, 73.97, 'ELEVATED', CURRENT_TIMESTAMP)");

        // v_pillar1_supply_security
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar1_supply_security (" +
                " region_code VARCHAR(20), fuel_type VARCHAR(50)," +
                " idr NUMERIC(5,3), sfri NUMERIC(6,1)," +
                " hhi_supply NUMERIC(10,2), n1_resilience NUMERIC(6,1)," +
                " pillar1_score NUMERIC(5,2), status VARCHAR(20), computed_at TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar1_supply_security VALUES " +
                "('VN_NORTH','GASOLINE',0.650,75.0,3450.0,40.5,65.20,'ELEVATED',CURRENT_TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar1_supply_security VALUES " +
                "('VN_SOUTH','DIESEL',0.580,82.0,2900.0,52.0,68.10,'ELEVATED',CURRENT_TIMESTAMP)");

        // v_pillar2_market_resilience
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar2_market_resilience (" +
                " fuel_type VARCHAR(50), sigma_30d NUMERIC(12,4), price_gap_pct NUMERIC(6,2)," +
                " beta_crude NUMERIC(6,3), affordability_idx NUMERIC(5,2)," +
                " pillar2_score NUMERIC(5,2), status VARCHAR(20), computed_at TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar2_market_resilience VALUES " +
                "('WTI_CRUDE',0.4200,-0.50,1.020,80.50,82.10,'SECURE',CURRENT_TIMESTAMP)");

        // v_pillar3_grid_reliability
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar3_grid_reliability (" +
                " region_code VARCHAR(20), reserve_margin_pct NUMERIC(5,2)," +
                " peak_load_factor NUMERIC(6,3), shedding_prob NUMERIC(6,4)," +
                " freq_stability_idx NUMERIC(5,2)," +
                " pillar3_score NUMERIC(5,2), status VARCHAR(20), computed_at TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar3_grid_reliability VALUES " +
                "('VN_NORTH',15.50,1.120,0.0500,87.50,72.40,'ELEVATED',CURRENT_TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar3_grid_reliability VALUES " +
                "('VN_SOUTH',8.20,1.250,0.1200,76.30,55.10,'STRESSED',CURRENT_TIMESTAMP)");

        // v_pillar4_energy_transition
        H2TestSupport.exec(c,
                "CREATE TABLE v_pillar4_energy_transition (" +
                " region_code VARCHAR(20), renewable_pct NUMERIC(5,2)," +
                " co2_intensity NUMERIC(10,2), curtailment_rate NUMERIC(5,2)," +
                " netzero_progress NUMERIC(5,2)," +
                " pillar4_score NUMERIC(5,2), status VARCHAR(20), computed_at TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar4_energy_transition VALUES " +
                "('VN_NORTH',32.50,420.00,5.20,46.43,72.30,'ELEVATED',CURRENT_TIMESTAMP)");
        H2TestSupport.exec(c,
                "INSERT INTO v_pillar4_energy_transition VALUES " +
                "('VN_SOUTH',18.20,580.00,2.10,26.00,55.80,'STRESSED',CURRENT_TIMESTAMP)");

        // v_active_recommendations — DAO query uses 'suggested_data::text AS suggested_data_text'
        H2TestSupport.exec(c,
                "CREATE TABLE v_active_recommendations (" +
                " id BIGINT, pillar SMALLINT, action_type VARCHAR(50), severity VARCHAR(20)," +
                " title VARCHAR(200), message VARCHAR(500), suggested_data VARCHAR(500)," +
                " suggested_at TIMESTAMP, age_seconds INT, expires_at TIMESTAMP, is_expired BOOLEAN)");
        H2TestSupport.exec(c,
                "INSERT INTO v_active_recommendations VALUES " +
                "(1,1,'TRANSFER_STOCK','WARNING','Supply Security low'," +
                " 'Region VN_NORTH SFRI 75 days below IEA 90 threshold','{}',CURRENT_TIMESTAMP,120,NULL,FALSE)");
        H2TestSupport.exec(c,
                "INSERT INTO v_active_recommendations VALUES " +
                "(2,3,'PEAK_SHAVING','CRITICAL','Grid Reliability stressed'," +
                " 'Region VN_SOUTH reserve margin 8.2%','{}',CURRENT_TIMESTAMP,30,NULL,FALSE)");
    }
}
