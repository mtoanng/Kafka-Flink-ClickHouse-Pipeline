package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row của <code>v_pillar1_supply_security</code> (Phase 7.1, IEA/APERC framework).
 *
 * <p>Pillar 1 — Supply Security (Availability). Sub-indicators:</p>
 * <ul>
 *   <li>{@code idr}            — Import Dependency Ratio (0.0–1.0, lower better).</li>
 *   <li>{@code sfri}           — Strategic Fuel Reserve Index = days of cover (IEA target ≥ 90).</li>
 *   <li>{@code hhi_supply}     — Herfindahl-Hirschman Index of fuel mix concentration (0–10000).</li>
 *   <li>{@code n1_resilience}  — N-1 days of cover if largest source disrupted.</li>
 * </ul>
 * <p>{@code pillar1_score} is a 0-100 composite of all 4. Status ∈ {SECURE, ELEVATED, STRESSED, CRITICAL}.</p>
 */
public class Pillar1SupplySecurity {

    private String regionCode;
    private String fuelType;
    private BigDecimal idr;
    private BigDecimal sfri;
    private BigDecimal hhiSupply;
    private BigDecimal n1Resilience;
    private BigDecimal pillar1Score;
    private String status;
    private LocalDateTime computedAt;

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public BigDecimal getIdr() { return idr; }
    public void setIdr(BigDecimal idr) { this.idr = idr; }

    public BigDecimal getSfri() { return sfri; }
    public void setSfri(BigDecimal sfri) { this.sfri = sfri; }

    public BigDecimal getHhiSupply() { return hhiSupply; }
    public void setHhiSupply(BigDecimal hhiSupply) { this.hhiSupply = hhiSupply; }

    public BigDecimal getN1Resilience() { return n1Resilience; }
    public void setN1Resilience(BigDecimal n1Resilience) { this.n1Resilience = n1Resilience; }

    public BigDecimal getPillar1Score() { return pillar1Score; }
    public void setPillar1Score(BigDecimal pillar1Score) { this.pillar1Score = pillar1Score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}
