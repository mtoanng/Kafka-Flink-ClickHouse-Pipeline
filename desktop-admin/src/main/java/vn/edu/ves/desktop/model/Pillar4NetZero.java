package vn.edu.ves.desktop.model;

import java.math.BigDecimal;

/**
 * Row của <code>v_pillar4_net_zero_progress</code>.
 *
 * <p>Columns: region_code, region_name, renewable_mw, avg_load_mw,
 * current_renewable_share_pct, target_2026_pct, target_2030_pct,
 * status, recommendation_text.</p>
 */
public class Pillar4NetZero {

    private String regionCode;
    private String regionName;
    private BigDecimal renewableMw;
    private BigDecimal avgLoadMw;
    private BigDecimal currentRenewableSharePct;
    private BigDecimal target2026Pct;
    private BigDecimal target2030Pct;
    private String status;
    private String recommendationText;

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getRegionName() { return regionName; }
    public void setRegionName(String regionName) { this.regionName = regionName; }

    public BigDecimal getRenewableMw() { return renewableMw; }
    public void setRenewableMw(BigDecimal renewableMw) { this.renewableMw = renewableMw; }

    public BigDecimal getAvgLoadMw() { return avgLoadMw; }
    public void setAvgLoadMw(BigDecimal avgLoadMw) { this.avgLoadMw = avgLoadMw; }

    public BigDecimal getCurrentRenewableSharePct() { return currentRenewableSharePct; }
    public void setCurrentRenewableSharePct(BigDecimal currentRenewableSharePct) { this.currentRenewableSharePct = currentRenewableSharePct; }

    public BigDecimal getTarget2026Pct() { return target2026Pct; }
    public void setTarget2026Pct(BigDecimal target2026Pct) { this.target2026Pct = target2026Pct; }

    public BigDecimal getTarget2030Pct() { return target2030Pct; }
    public void setTarget2030Pct(BigDecimal target2030Pct) { this.target2030Pct = target2030Pct; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecommendationText() { return recommendationText; }
    public void setRecommendationText(String recommendationText) { this.recommendationText = recommendationText; }
}
