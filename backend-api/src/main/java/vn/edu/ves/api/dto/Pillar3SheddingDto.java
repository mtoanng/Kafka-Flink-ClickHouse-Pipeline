package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 row của {@code v_pillar3_load_shedding_plan}. */
@Data
@Builder
@AllArgsConstructor
public class Pillar3SheddingDto {
    private long         priorityLevel;
    private String       regionCode;
    private String       regionName;
    private BigDecimal   loadMw;
    private BigDecimal   capacityMw;
    private BigDecimal   loadPct;
    private boolean      peakHour;
    private BigDecimal   suggestedShedMw;
    private String       actionType;
    private String       recommendationText;
    private LocalDateTime eventTime;
}
