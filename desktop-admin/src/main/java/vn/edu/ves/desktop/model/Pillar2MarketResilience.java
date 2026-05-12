package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row của <code>v_pillar2_market_resilience</code> (Phase 7.1, IEA/IMF framework).
 *
 * <p>Pillar 2 — Market Resilience (Affordability). Sub-indicators:</p>
 * <ul>
 *   <li>{@code sigma_30d}        — 30-day rolling stddev of price (proxy via 1h window).</li>
 *   <li>{@code price_gap_pct}    — (VN price - benchmark) / benchmark × 100.</li>
 *   <li>{@code beta_crude}       — OLS slope of fuel returns vs Brent crude returns.</li>
 *   <li>{@code affordability_idx} — 100 - price/threshold × 10 (normalised 0-100).</li>
 * </ul>
 */
public class Pillar2MarketResilience {

    private String fuelType;
    private BigDecimal sigma30d;
    private BigDecimal priceGapPct;
    private BigDecimal betaCrude;
    private BigDecimal affordabilityIdx;
    private BigDecimal pillar2Score;
    private String status;
    private LocalDateTime computedAt;

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public BigDecimal getSigma30d() { return sigma30d; }
    public void setSigma30d(BigDecimal sigma30d) { this.sigma30d = sigma30d; }

    public BigDecimal getPriceGapPct() { return priceGapPct; }
    public void setPriceGapPct(BigDecimal priceGapPct) { this.priceGapPct = priceGapPct; }

    public BigDecimal getBetaCrude() { return betaCrude; }
    public void setBetaCrude(BigDecimal betaCrude) { this.betaCrude = betaCrude; }

    public BigDecimal getAffordabilityIdx() { return affordabilityIdx; }
    public void setAffordabilityIdx(BigDecimal affordabilityIdx) { this.affordabilityIdx = affordabilityIdx; }

    public BigDecimal getPillar2Score() { return pillar2Score; }
    public void setPillar2Score(BigDecimal pillar2Score) { this.pillar2Score = pillar2Score; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getComputedAt() { return computedAt; }
    public void setComputedAt(LocalDateTime computedAt) { this.computedAt = computedAt; }
}
