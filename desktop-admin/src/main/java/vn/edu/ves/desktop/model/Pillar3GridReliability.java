package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row của <code>v_pillar3_grid_reliability</code> (Phase 7.1, NERC/IEEE framework).
 *
 * <p>Pillar 3 — Grid Reliability (Accessibility). Sub-indicators:</p>
 * <ul>
 *   <li>{@code reserve_margin_pct} — (capacity - load) / capacity × 100.</li>
 *   <li>{@code peak_load_factor}   — peak_load / avg_load (1h window).</li>
 *   <li>{@code shedding_prob}      — P(load_pct &gt; 95) over 1h (0.0–1.0).</li>
 *   <li>{@code freq_stability_idx} — 100 - stddev(load_pct) × 10 (proxy 0-100).</li>
 * </ul>
 */
public class Pillar3GridReliability {

    private String regionCode;
    private BigDecimal reserveMarginPct;
    private BigDecimal peakLoadFactor;
    private BigDecimal sheddingProb;
    private BigDecimal freqStabilityIdx;
    private BigDecimal pillar3Score;
    private String status;
    private LocalDateTime computedAt;

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public BigDecimal getReserveMarginPct() { return reserveMarginPct; }
    public void setReserveMarginPct(BigDecimal reserveMarginPct) { this.reserveMarginPct = reserveMarginPct; }

    public BigDecimal getPeakLoadFactor() { return peakLoadFactor; }
    public void setPeakLoadFactor(BigDecimal peakLoadFactor) { this.peakLoadFactor = peakLoadFactor; }

    public BigDecimal getSheddingProb() { return sheddingProb; }
    public void setSheddingProb(BigDecimal sheddingProb) { this.sheddingProb = sheddingProb; }

    public BigDecimal getFreqStabilityIdx() { return freqStabilityIdx; }
    public void setFreqStabilityIdx(BigDecimal freqStabilityIdx) { this.freqStabilityIdx = freqStabilityIdx; }

    public BigDecimal getPillar3Score() { return pillar3Score; }
    public void setPillar3Score(BigDecimal pillar3Score) { this.pillar3Score = pillar3Score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}
