package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/** 1 row của {@code v_pillar4_net_zero_progress}. */
@Data
@Builder
@AllArgsConstructor
public class Pillar4NetZeroDto {
    private String     regionCode;
    private String     regionName;
    private BigDecimal renewableMw;
    private BigDecimal avgLoadMw;
    private BigDecimal currentRenewableSharePct;
    private BigDecimal target2026Pct;
    private BigDecimal target2030Pct;
    private String     status;
    private String     recommendationText;
}
