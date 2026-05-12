package vn.edu.ves.api.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 1 row của {@code v_active_recommendations}. */
@Data
@Builder
@AllArgsConstructor
public class RecommendationDto {
    private long          id;
    private short         pillar;          // 0-4
    private String        actionType;      // TRANSFER_STOCK | HEDGE_IMPORT | PEAK_SHAVING | ...
    private String        severity;        // INFO | WARNING | CRITICAL
    private String        title;
    private String        message;
    @JsonRawValue
    private String        suggestedData;   // JSONB pass-through
    private LocalDateTime suggestedAt;
    private int           ageSeconds;
    private LocalDateTime expiresAt;
    private boolean       expired;
}
