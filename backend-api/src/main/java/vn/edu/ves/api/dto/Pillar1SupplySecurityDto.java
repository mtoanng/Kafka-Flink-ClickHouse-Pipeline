package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 1 row của {@code v_pillar1_supply_security} (Phase 7.1 IEA/APERC Availability). */
@Data
@Builder
@AllArgsConstructor
public class Pillar1SupplySecurityDto {
    private String          regionCode;
    private String          fuelType;
    private BigDecimal      idr;
    private BigDecimal      sfri;
    private BigDecimal      hhiSupply;
    private BigDecimal      n1Resilience;
    private BigDecimal      pillar1Score;
    private String          status;
    private OffsetDateTime  computedAt;
}
