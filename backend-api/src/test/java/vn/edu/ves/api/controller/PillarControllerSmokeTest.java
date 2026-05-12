package vn.edu.ves.api.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import vn.edu.ves.api.config.JwtTokenProvider;
import vn.edu.ves.api.dao.PillarDao;
import vn.edu.ves.api.dto.Pillar1SupplySecurityDto;
import vn.edu.ves.api.dto.Pillar2MarketResilienceDto;
import vn.edu.ves.api.dto.Pillar3GridReliabilityDto;
import vn.edu.ves.api.dto.Pillar4EnergyTransitionDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke tests cho {@link PillarController} — Phase 7.6 backend pillar migration.
 *
 * <p>Mock {@link PillarDao} → assert mỗi pillar endpoint:</p>
 * <ul>
 *   <li>HTTP 200 trên cả path cũ (Phase 2.5/2.6) lẫn path mới (Phase 7.1).</li>
 *   <li>JSON body chứa các field IEA/APERC sub-indicator + composite score.</li>
 * </ul>
 *
 * <p>JUnit 4 + SpringRunner để khớp surefire 2.12.4 (xem {@code backend-api/pom.xml}).
 * {@code addFilters = false} để bỏ JWT/security filter — controller-level smoke
 * không cần auth.</p>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PillarController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PillarControllerSmokeTest {

    private static final OffsetDateTime FIXED_TS =
            OffsetDateTime.of(2026, 5, 13, 4, 0, 0, 0, ZoneOffset.UTC);

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PillarDao dao;

    /** Stub: SecurityConfig vẫn được load (mặc dù addFilters=false), cần JwtTokenProvider để inject vào JwtAuthFilter. */
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // ---------- Pillar 1 ----------

    private Pillar1SupplySecurityDto sampleP1() {
        return Pillar1SupplySecurityDto.builder()
                .regionCode("VN_NORTH")
                .fuelType("DIESEL")
                .idr(new BigDecimal("0.850"))
                .sfri(new BigDecimal("75.5"))
                .hhiSupply(new BigDecimal("4250.10"))
                .n1Resilience(new BigDecimal("42.0"))
                .pillar1Score(new BigDecimal("64.83"))
                .status("ELEVATED")
                .computedAt(FIXED_TS)
                .build();
    }

    @Test
    public void pillar1_oldPath_outlook_returns_200() throws Exception {
        when(dao.findPillar1SupplySecurity()).thenReturn(List.of(sampleP1()));
        mvc.perform(get("/api/pillars/1/outlook"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionCode").value("VN_NORTH"))
                .andExpect(jsonPath("$[0].fuelType").value("DIESEL"))
                .andExpect(jsonPath("$[0].pillar1Score").value(64.83))
                .andExpect(jsonPath("$[0].status").value("ELEVATED"));
    }

    @Test
    public void pillar1_newPath_supply_security_returns_200() throws Exception {
        when(dao.findPillar1SupplySecurity()).thenReturn(List.of(sampleP1()));
        mvc.perform(get("/api/pillars/1/supply-security"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idr").value(0.85))
                .andExpect(jsonPath("$[0].sfri").value(75.5))
                .andExpect(jsonPath("$[0].n1Resilience").value(42.0));
    }

    // ---------- Pillar 2 ----------

    private Pillar2MarketResilienceDto sampleP2() {
        return Pillar2MarketResilienceDto.builder()
                .fuelType("WTI_CRUDE")
                .sigma30d(new BigDecimal("0.4321"))
                .priceGapPct(new BigDecimal("3.25"))
                .betaCrude(new BigDecimal("1.020"))
                .affordabilityIdx(new BigDecimal("82.45"))
                .pillar2Score(new BigDecimal("56.18"))
                .status("STRESSED")
                .computedAt(FIXED_TS)
                .build();
    }

    @Test
    public void pillar2_oldPath_volatility_returns_200() throws Exception {
        when(dao.findPillar2MarketResilience()).thenReturn(List.of(sampleP2()));
        mvc.perform(get("/api/pillars/2/volatility"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fuelType").value("WTI_CRUDE"))
                .andExpect(jsonPath("$[0].pillar2Score").value(56.18))
                .andExpect(jsonPath("$[0].status").value("STRESSED"));
    }

    @Test
    public void pillar2_newPath_market_resilience_returns_200() throws Exception {
        when(dao.findPillar2MarketResilience()).thenReturn(List.of(sampleP2()));
        mvc.perform(get("/api/pillars/2/market-resilience"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sigma30d").value(0.4321))
                .andExpect(jsonPath("$[0].priceGapPct").value(3.25))
                .andExpect(jsonPath("$[0].affordabilityIdx").value(82.45));
    }

    // ---------- Pillar 3 ----------

    private Pillar3GridReliabilityDto sampleP3() {
        return Pillar3GridReliabilityDto.builder()
                .regionCode("VN_SOUTH")
                .reserveMarginPct(new BigDecimal("18.50"))
                .peakLoadFactor(new BigDecimal("1.120"))
                .sheddingProb(new BigDecimal("0.0250"))
                .freqStabilityIdx(new BigDecimal("88.30"))
                .pillar3Score(new BigDecimal("90.16"))
                .status("SECURE")
                .computedAt(FIXED_TS)
                .build();
    }

    @Test
    public void pillar3_oldPath_shedding_returns_200() throws Exception {
        when(dao.findPillar3GridReliability()).thenReturn(List.of(sampleP3()));
        mvc.perform(get("/api/pillars/3/shedding"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionCode").value("VN_SOUTH"))
                .andExpect(jsonPath("$[0].pillar3Score").value(90.16))
                .andExpect(jsonPath("$[0].status").value("SECURE"));
    }

    @Test
    public void pillar3_newPath_grid_reliability_returns_200() throws Exception {
        when(dao.findPillar3GridReliability()).thenReturn(List.of(sampleP3()));
        mvc.perform(get("/api/pillars/3/grid-reliability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reserveMarginPct").value(18.50))
                .andExpect(jsonPath("$[0].sheddingProb").value(0.0250));
    }

    // ---------- Pillar 4 ----------

    private Pillar4EnergyTransitionDto sampleP4() {
        return Pillar4EnergyTransitionDto.builder()
                .regionCode("VN_CENTRAL")
                .renewablePct(new BigDecimal("38.20"))
                .co2Intensity(new BigDecimal("412.50"))
                .curtailmentRate(new BigDecimal("5.80"))
                .netzeroProgress(new BigDecimal("54.57"))
                .pillar4Score(new BigDecimal("72.06"))
                .status("ELEVATED")
                .computedAt(FIXED_TS)
                .build();
    }

    @Test
    public void pillar4_oldPath_netzero_returns_200() throws Exception {
        when(dao.findPillar4EnergyTransition()).thenReturn(List.of(sampleP4()));
        mvc.perform(get("/api/pillars/4/netzero"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].regionCode").value("VN_CENTRAL"))
                .andExpect(jsonPath("$[0].pillar4Score").value(72.06));
    }

    @Test
    public void pillar4_newPath_energy_transition_returns_200() throws Exception {
        when(dao.findPillar4EnergyTransition()).thenReturn(List.of(sampleP4()));
        mvc.perform(get("/api/pillars/4/energy-transition"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].renewablePct").value(38.20))
                .andExpect(jsonPath("$[0].co2Intensity").value(412.50))
                .andExpect(jsonPath("$[0].netzeroProgress").value(54.57));
    }
}
