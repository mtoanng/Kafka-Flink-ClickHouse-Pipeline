package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row của <code>v_pillar3_load_shedding_plan</code>.
 *
 * <p>Columns: priority_level, region_code, region_name, load_mw, capacity_mw,
 * load_pct, is_peak_hour, suggested_shed_mw, action_type, recommendation_text, event_time.</p>
 */
public class Pillar3Shedding {

    private long priorityLevel;
    private String regionCode;
    private String regionName;
    private BigDecimal loadMw;
    private BigDecimal capacityMw;
    private BigDecimal loadPct;
    private boolean peakHour;
    private BigDecimal suggestedShedMw;
    private String actionType;
    private String recommendationText;
    private LocalDateTime eventTime;

    public long getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(long priorityLevel) { this.priorityLevel = priorityLevel; }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public BigDecimal getLoadMw() { return loadMw; }
    public void setLoadMw(BigDecimal loadMw) { this.loadMw = loadMw; }

    public BigDecimal getCapacityMw() { return capacityMw; }
    public void setCapacityMw(BigDecimal capacityMw) { this.capacityMw = capacityMw; }

    public BigDecimal getLoadPct() { return loadPct; }
    public void setLoadPct(BigDecimal loadPct) { this.loadPct = loadPct; }

    public boolean isPeakHour() { return peakHour; }
    public void setPeakHour(boolean peakHour) { this.peakHour = peakHour; }

    public BigDecimal getSuggestedShedMw() { return suggestedShedMw; }
    public void setSuggestedShedMw(BigDecimal suggestedShedMw) { this.suggestedShedMw = suggestedShedMw; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getRecommendationText() { return recommendationText; }
    public void setRecommendationText(String recommendationText) { this.recommendationText = recommendationText; }

    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
