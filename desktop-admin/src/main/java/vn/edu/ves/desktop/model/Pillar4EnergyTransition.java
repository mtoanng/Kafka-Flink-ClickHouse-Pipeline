package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row của <code>v_pillar4_energy_transition</code> (Phase 7.1, IPCC AR6 framework).
 *
 * <p>Pillar 4 — Energy Transition (Acceptability). Sub-indicators:</p>
 * <ul>
 *   <li>{@code renewable_pct}    — renewable_mw / load_mw × 100.</li>
 *   <li>{@code co2_intensity}    — gCO2 per kWh (kg/MWh equivalent).</li>
 *   <li>{@code curtailment_rate} — (capacity - output) / capacity × 100 for renewables.</li>
 *   <li>{@code netzero_progress} — current_renewable_pct / 70.0 × 100 (linear 2050 path).</li>
 * </ul>
 */
public class Pillar4EnergyTransition {

    private String regionCode;
    private BigDecimal renewablePct;
    private BigDecimal co2Intensity;
    private BigDecimal curtailmentRate;
    private BigDecimal netzeroProgress;
    private BigDecimal pillar4Score;
    private String status;
    private LocalDateTime computedAt;

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public BigDecimal getRenewablePct() { return renewablePct; }
    public void setRenewablePct(BigDecimal renewablePct) { this.renewablePct = renewablePct; }

    public BigDecimal getCo2Intensity() { return co2Intensity; }
    public void setCo2Intensity(BigDecimal co2Intensity) { this.co2Intensity = co2Intensity; }

    public BigDecimal getCurtailmentRate() { return curtailmentRate; }
    public void setCurtailmentRate(BigDecimal curtailmentRate) { this.curtailmentRate = curtailmentRate; }

    public BigDecimal getNetzeroProgress() { return netzeroProgress; }
    public void setNetzeroProgress(BigDecimal netzeroProgress) { this.netzeroProgress = netzeroProgress; }

    public BigDecimal getPillar4Score() { return pillar4Score; }
    public void setPillar4Score(BigDecimal pillar4Score) { this.pillar4Score = pillar4Score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}
