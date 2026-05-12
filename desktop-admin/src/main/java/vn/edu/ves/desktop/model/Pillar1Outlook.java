package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row của <code>v_pillar1_supply_outlook</code>.
 *
 * <p>Columns:</p>
 * <pre>
 *   region_code, region_name, fuel_type, stock_volume_kl, daily_consumption_kl,
 *   stock_days, target_days, days_to_critical, days_above_target,
 *   target_achievement_pct, status, recommendation_text,
 *   suggested_donor_region, reported_at
 * </pre>
 */
public class Pillar1Outlook {

    private String regionCode;
    private String regionName;
    private String fuelType;
    private BigDecimal stockVolumeKl;
    private BigDecimal dailyConsumptionKl;
    private BigDecimal stockDays;
    private Integer targetDays;
    private BigDecimal daysToCritical;
    private BigDecimal daysAboveTarget;
    private BigDecimal targetAchievementPct;
    private String status;
    private String recommendationText;
    private String suggestedDonorRegion;
    private LocalDateTime reportedAt;

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public BigDecimal getStockVolumeKl() { return stockVolumeKl; }
    public void setStockVolumeKl(BigDecimal stockVolumeKl) { this.stockVolumeKl = stockVolumeKl; }

    public BigDecimal getDailyConsumptionKl() { return dailyConsumptionKl; }
    public void setDailyConsumptionKl(BigDecimal dailyConsumptionKl) { this.dailyConsumptionKl = dailyConsumptionKl; }

    public BigDecimal getStockDays() { return stockDays; }
    public void setStockDays(BigDecimal stockDays) { this.stockDays = stockDays; }

    public Integer getTargetDays() { return targetDays; }
    public void setTargetDays(Integer targetDays) { this.targetDays = targetDays; }

    public BigDecimal getDaysToCritical() { return daysToCritical; }
    public void setDaysToCritical(BigDecimal daysToCritical) { this.daysToCritical = daysToCritical; }

    public BigDecimal getDaysAboveTarget() { return daysAboveTarget; }
    public void setDaysAboveTarget(BigDecimal daysAboveTarget) { this.daysAboveTarget = daysAboveTarget; }

    public BigDecimal getTargetAchievementPct() { return targetAchievementPct; }
    public void setTargetAchievementPct(BigDecimal targetAchievementPct) { this.targetAchievementPct = targetAchievementPct; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecommendationText() { return recommendationText; }
    public void setRecommendationText(String recommendationText) { this.recommendationText = recommendationText; }

    public String getSuggestedDonorRegion() { return suggestedDonorRegion; }
    public void setSuggestedDonorRegion(String suggestedDonorRegion) { this.suggestedDonorRegion = suggestedDonorRegion; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }
}
