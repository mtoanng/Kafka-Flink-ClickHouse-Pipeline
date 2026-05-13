package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 1 row của {@code v_security_score} — Light ESI. */
@Data
@Builder
@AllArgsConstructor
public class SecurityScoreDto {
    private BigDecimal pillar1Score;
    private BigDecimal pillar2Score;
    private BigDecimal pillar3Score;
    private BigDecimal pillar4Score;
    private BigDecimal overallScore;
    private String     status;          // SECURE | ELEVATED | STRESSED | CRITICAL (Phase 7.1 IEA enum)
    private OffsetDateTime computedAt;
}
