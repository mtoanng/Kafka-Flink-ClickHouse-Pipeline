package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Row của <code>v_pillar2_volatility_signal</code>.
 *
 * <p>Columns: fuel_type, location, sample_count, avg_price, sigma,
 * relative_volatility_pct, range_abs, signal, last_event.</p>
 */
public class Pillar2Volatility {

    private String fuelType;
    private String location;
    private int sampleCount;
    private BigDecimal avgPrice;
    private BigDecimal sigma;
    private BigDecimal relativeVolatilityPct;
    private BigDecimal rangeAbs;
    private String signal;
    private LocalDateTime lastEvent;

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getSampleCount() { return sampleCount; }
    public void setSampleCount(int sampleCount) { this.sampleCount = sampleCount; }

    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }

    public BigDecimal getSigma() { return sigma; }
    public void setSigma(BigDecimal sigma) { this.sigma = sigma; }

    public BigDecimal getRelativeVolatilityPct() { return relativeVolatilityPct; }
    public void setRelativeVolatilityPct(BigDecimal relativeVolatilityPct) { this.relativeVolatilityPct = relativeVolatilityPct; }

    public BigDecimal getRangeAbs() { return rangeAbs; }
    public void setRangeAbs(BigDecimal rangeAbs) { this.rangeAbs = rangeAbs; }

    public String getSignal() { return signal; }
    public void setSignal(String signal) { this.signal = signal; }

    public LocalDateTime getLastEvent() { return lastEvent; }
    public void setLastEvent(LocalDateTime lastEvent) { this.lastEvent = lastEvent; }
}
