package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 row của {@code v_pillar3_grid_load_latest}. */
@Data
@Builder
@AllArgsConstructor
public class GridLoadLatestDto {
    private String        regionCode;
    private String        regionName;
    private BigDecimal    loadMw;
    private BigDecimal    capacityMw;
    private BigDecimal    loadPct;
    private boolean       peakHour;
    private String        status;
    private LocalDateTime eventTime;
}
