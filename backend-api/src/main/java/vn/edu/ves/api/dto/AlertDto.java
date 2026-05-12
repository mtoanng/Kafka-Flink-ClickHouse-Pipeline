package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 row của {@code v_active_alerts}. */
@Data
@Builder
@AllArgsConstructor
public class AlertDto {
    private long          id;
    private Long          ruleId;
    private String        ruleName;
    private String        metricType;
    private String        fuelType;        // null cho Pillar 3/4
    private String        location;        // null/region_code tuỳ metric
    private String        region;
    private BigDecimal    triggeredPrice;  // gọi là price nhưng có thể là loadPct / intensity
    private BigDecimal    threshold;
    private String        operator;
    private String        severity;
    private String        message;
    private LocalDateTime eventTimestamp;
    private LocalDateTime alertTimestamp;
    private int           ageSeconds;
}
