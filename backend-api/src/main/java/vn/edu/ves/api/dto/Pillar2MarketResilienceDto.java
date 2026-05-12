package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 1 row của {@code v_pillar2_market_resilience} (Phase 7.1 IEA/IMF Affordability). */
@Data
@Builder
@AllArgsConstructor
public class Pillar2MarketResilienceDto {
    private String          fuelType;
    private BigDecimal      sigma30d;
    private BigDecimal      priceGapPct;
    private BigDecimal      betaCrude;
    private BigDecimal      affordabilityIdx;
    private BigDecimal      pillar2Score;
    private String          status;
    private OffsetDateTime  computedAt;
}
