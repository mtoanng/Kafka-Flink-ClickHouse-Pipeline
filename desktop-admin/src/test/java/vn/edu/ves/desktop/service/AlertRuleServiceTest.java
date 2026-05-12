package vn.edu.ves.desktop.service;

import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.dao.AlertRuleDao;
import vn.edu.ves.desktop.model.AlertRule;
import vn.edu.ves.desktop.model.MetricType;
import vn.edu.ves.desktop.model.Operator;
import vn.edu.ves.desktop.model.Severity;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlertRuleServiceTest {

    private AlertRuleDao dao;
    private AlertRuleService service;

    @Before
    public void setUp() {
        dao = mock(AlertRuleDao.class);
        service = new AlertRuleServiceImpl(dao);
    }

    @Test
    public void save_normalizesFuelAndRegionAndTrimsName() {
        AlertRule input = new AlertRule();
        input.setRuleName("  WTI rule  ");
        input.setMetricType(MetricType.FUEL_PRICE);
        input.setFuelType("wti_crude");
        input.setRegionCode("vn_north");
        input.setOperator(Operator.GT);
        input.setThreshold(new BigDecimal("90.0"));
        input.setSeverity(Severity.WARNING);
        AlertRule fromDao = new AlertRule();
        fromDao.setId(42);
        when(dao.save(any(AlertRule.class))).thenReturn(fromDao);

        AlertRule saved = service.save(input);
        assertSame(fromDao, saved);
        assertEquals("WTI rule", input.getRuleName());
        assertEquals("WTI_CRUDE", input.getFuelType());
        assertEquals("VN_NORTH", input.getRegionCode());
    }

    @Test
    public void setEnabled_passesThrough() {
        when(dao.setEnabled(7L, false)).thenReturn(true);
        assertTrue(service.setEnabled(7L, false));
        verify(dao).setEnabled(7L, false);
    }

    @Test
    public void findByMetricType_filterDelegated() {
        when(dao.findByMetricType(MetricType.GRID_LOAD_PCT))
                .thenReturn(java.util.Arrays.asList(new AlertRule(), new AlertRule()));
        assertEquals(2, service.findByMetricType(MetricType.GRID_LOAD_PCT).size());
    }
}
