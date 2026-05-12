package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 1 row của {@code v_pillar3_grid_reliability} (Phase 7.1 NERC/IEEE Accessibility). */
@Data
@Builder
@AllArgsConstructor
public class Pillar3GridReliabilityDto {
    private String          regionCode;
    private BigDecimal      reserveMarginPct;
    private BigDecimal      peakLoadFactor;
    private BigDecimal      sheddingProb;
    private BigDecimal      freqStabilityIdx;
    private BigDecimal      pillar3Score;
    private String          status;
    private OffsetDateTime  computedAt;
}
