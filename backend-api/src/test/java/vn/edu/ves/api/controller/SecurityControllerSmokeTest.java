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
import vn.edu.ves.api.dao.SecurityDao;
import vn.edu.ves.api.dto.SecurityScoreDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke tests cho {@link SecurityController} — Phase 7.6.
 *
 * <ul>
 *   <li>{@code /api/security/score} → 200 với composite ESI khi có data, 404 khi không.</li>
 *   <li>{@code /api/security/cascade-risks} → 200 + array rỗng (view dropped Phase 7.1).</li>
 * </ul>
 */
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = SecurityController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SecurityControllerSmokeTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SecurityDao dao;

    /** Stub: SecurityConfig vẫn được load (mặc dù addFilters=false), cần JwtTokenProvider để inject vào JwtAuthFilter. */
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    public void security_score_returns_200_with_composite_esi() throws Exception {
        SecurityScoreDto sample = SecurityScoreDto.builder()
                .pillar1Score(new BigDecimal("64.83"))
                .pillar2Score(new BigDecimal("56.18"))
                .pillar3Score(new BigDecimal("90.16"))
                .pillar4Score(new BigDecimal("72.06"))
                .overallScore(new BigDecimal("72.15"))
                .status("ELEVATED")
                .computedAt(OffsetDateTime.of(2026, 5, 13, 4, 0, 0, 0, ZoneOffset.UTC))
                .build();
        when(dao.score()).thenReturn(Optional.of(sample));

        mvc.perform(get("/api/security/score"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallScore").value(72.15))
                .andExpect(jsonPath("$.status").value("ELEVATED"))
                .andExpect(jsonPath("$.pillar1Score").value(64.83))
                .andExpect(jsonPath("$.pillar3Score").value(90.16));
    }

    @Test
    public void security_score_returns_404_when_view_empty() throws Exception {
        when(dao.score()).thenReturn(Optional.empty());
        mvc.perform(get("/api/security/score"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void security_cascade_risks_returns_200_with_empty_array() throws Exception {
        when(dao.cascadeRisks()).thenReturn(Collections.emptyList());
        mvc.perform(get("/api/security/cascade-risks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
