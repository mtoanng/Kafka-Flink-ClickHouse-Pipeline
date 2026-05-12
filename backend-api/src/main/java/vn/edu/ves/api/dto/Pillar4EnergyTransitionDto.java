package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** 1 row của {@code v_pillar4_energy_transition} (Phase 7.1 IPCC Acceptability). */
@Data
@Builder
@AllArgsConstructor
public class Pillar4EnergyTransitionDto {
    private String          regionCode;
    private BigDecimal      renewablePct;
    private BigDecimal      co2Intensity;
    private BigDecimal      curtailmentRate;
    private BigDecimal      netzeroProgress;
    private BigDecimal      pillar4Score;
    private String          status;
    private OffsetDateTime  computedAt;
}
