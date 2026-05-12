package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 row của {@code v_pillar2_volatility_signal}. */
@Data
@Builder
@AllArgsConstructor
public class Pillar2VolatilityDto {
    private String       fuelType;
    private String       location;
    private long         sampleCount;
    private BigDecimal   avgPrice;
    private BigDecimal   sigma;
    private BigDecimal   relativeVolatilityPct;
    private BigDecimal   rangeAbs;
    private String       signal;        // STABLE | ELEVATED | VOLATILE
    private LocalDateTime lastEvent;
}
