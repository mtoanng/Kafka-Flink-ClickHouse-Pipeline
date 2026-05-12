package vn.edu.ves.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 row của {@code v_pillar1_supply_outlook}. */
@Data
@Builder
@AllArgsConstructor
public class Pillar1OutlookDto {
    private String       regionCode;
    private String       regionName;
    private String       fuelType;
    private BigDecimal   stockVolumeKl;
    private BigDecimal   dailyConsumptionKl;
    private BigDecimal   stockDays;
    private Integer      targetDays;
    private BigDecimal   daysToCritical;
    private BigDecimal   daysAboveTarget;
    private BigDecimal   targetAchievementPct;
    private String       status;
    private String       recommendationText;
    private String       suggestedDonorRegion;
    private LocalDateTime reportedAt;
}
