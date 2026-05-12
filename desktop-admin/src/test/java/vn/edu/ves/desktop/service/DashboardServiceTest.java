package vn.edu.ves.desktop.service;

import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.dao.ViewsDao;
import vn.edu.ves.desktop.model.Pillar1Outlook;
import vn.edu.ves.desktop.model.Recommendation;
import vn.edu.ves.desktop.model.SecurityScore;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests cho {@link DashboardServiceImpl} với Mockito mock {@link ViewsDao}.
 */
public class DashboardServiceTest {

    private ViewsDao viewsDao;
    private DashboardService service;

    @Before
    public void setUp() {
        viewsDao = mock(ViewsDao.class);
        service = new DashboardServiceImpl(viewsDao);
    }

    @Test
    public void getSecurityScore_delegatesToDao() {
        SecurityScore expected = new SecurityScore();
        expected.setOverallScore(new BigDecimal("75.0"));
        expected.setStatus("STABLE");
        when(viewsDao.fetchSecurityScore()).thenReturn(Optional.of(expected));

        Optional<SecurityScore> result = service.getSecurityScore();
        assertTrue(result.isPresent());
        assertSame(expected, result.get());
        verify(viewsDao, times(1)).fetchSecurityScore();
    }

    @Test
    public void getPillar1_returnsAllRowsFromDao() {
        Pillar1Outlook row = new Pillar1Outlook();
        row.setRegionCode("VN_NORTH");
        row.setFuelType("GASOLINE");
        when(viewsDao.fetchPillar1Outlook()).thenReturn(List.of(row));

        List<Pillar1Outlook> result = service.getPillar1();
        assertEquals(1, result.size());
        assertEquals("VN_NORTH", result.get(0).getRegionCode());
    }

    @Test
    public void getActiveRecommendations_emptyList_isOk() {
        when(viewsDao.fetchActiveRecommendations()).thenReturn(Collections.emptyList());
        List<Recommendation> result = service.getActiveRecommendations();
        assertEquals(0, result.size());
        verify(viewsDao).fetchActiveRecommendations();
    }

    @Test
    public void allDelegations_callDaoExactlyOnce() {
        when(viewsDao.fetchSecurityScore()).thenReturn(Optional.empty());
        when(viewsDao.fetchPillar1Outlook()).thenReturn(Collections.emptyList());
        when(viewsDao.fetchPillar2Volatility()).thenReturn(Collections.emptyList());
        when(viewsDao.fetchPillar3Shedding()).thenReturn(Collections.emptyList());
        when(viewsDao.fetchPillar4NetZero()).thenReturn(Collections.emptyList());
        when(viewsDao.fetchActiveRecommendations()).thenReturn(Collections.emptyList());

        service.getSecurityScore();
        service.getPillar1();
        service.getPillar2();
        service.getPillar3();
        service.getPillar4();
        service.getActiveRecommendations();

        verify(viewsDao).fetchSecurityScore();
        verify(viewsDao).fetchPillar1Outlook();
        verify(viewsDao).fetchPillar2Volatility();
        verify(viewsDao).fetchPillar3Shedding();
        verify(viewsDao).fetchPillar4NetZero();
        verify(viewsDao).fetchActiveRecommendations();
    }
}
