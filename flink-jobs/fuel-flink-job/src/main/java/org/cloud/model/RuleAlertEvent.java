package org.cloud.model;

import java.io.Serializable;

/**
 * Cảnh báo rule-based — kết quả AlertDetectionFunction emit.
 *
 * Đa-pillar (Phase 4):
 *   - FUEL_PRICE         : từ FuelPrice (location = tên sàn, fuel_type ≠ null)
 *   - GRID_LOAD_PCT      : từ GridLoadEvent (region_code, fuel_type = null)
 *   - EMISSION_INTENSITY : từ EmissionEvent (region_code, fuel_type = null)
 *
 * Được ghi vào bảng {@code alerts} (schema mở rộng ở 09_alter_alerts_multi_pillar.sql).
 * Khi severity = CRITICAL → cũng sinh {@code recommendations} (dedup SQL-level).
 */
public class RuleAlertEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public long    ruleId;
    public String  ruleName;
    public String  metricType;     // FUEL_PRICE | GRID_LOAD_PCT | EMISSION_INTENSITY | ...
    public String  fuelType;       // null cho Pillar 3/4
    public String  location;       // tên sàn (Pillar 2) HOẶC region_code (Pillar 3/4)
    public String  region;         // region/region_code
    public double  triggeredPrice; // giá / load_pct / intensity tuỳ metric
    public double  threshold;
    public String  operator;
    public String  severity;
    public String  message;
    public String  eventTimestamp;

    public RuleAlertEvent() {}

    /** Pillar 2 — FUEL_PRICE alert. Giữ nguyên signature cho code Phase 3. */
    public RuleAlertEvent(AlertRule rule, FuelPrice event) {
        this.ruleId         = rule.id;
        this.ruleName       = rule.ruleName;
        this.metricType     = "FUEL_PRICE";
        this.fuelType       = event.fuelType;
        this.location       = event.location;
        this.region         = event.region;
        this.triggeredPrice = event.price;
        this.threshold      = rule.threshold;
        this.operator       = rule.operator;
        this.severity       = rule.severity;
        this.message        = String.format(
                "Giá %s tại %s = %.4f %s threshold %.4f (rule: %s)",
                event.fuelType, event.location, event.price,
                rule.operator, rule.threshold, rule.ruleName);
        this.eventTimestamp = event.timestamp;
    }

    /** Pillar 3 — GRID_LOAD_PCT alert. */
    public RuleAlertEvent(AlertRule rule, GridLoadEvent event) {
        this.ruleId         = rule.id;
        this.ruleName       = rule.ruleName;
        this.metricType     = "GRID_LOAD_PCT";
        this.fuelType       = null;
        this.location       = event.regionCode;     // dùng region_code làm "location"
        this.region         = event.regionCode;
        this.triggeredPrice = event.getLoadPct();
        this.threshold      = rule.threshold;
        this.operator       = rule.operator;
        this.severity       = rule.severity;
        this.message        = String.format(
                "Phụ tải %s = %.1f%% %s threshold %.1f%% (capacity=%.0f MW) [rule: %s]",
                event.regionCode, event.getLoadPct(),
                rule.operator, rule.threshold, event.capacityMw, rule.ruleName);
        this.eventTimestamp = event.eventTime;
    }

    /** Pillar 4 — EMISSION_INTENSITY alert. */
    public RuleAlertEvent(AlertRule rule, EmissionEvent event) {
        this.ruleId         = rule.id;
        this.ruleName       = rule.ruleName;
        this.metricType     = "EMISSION_INTENSITY";
        this.fuelType       = null;
        this.location       = event.regionCode;
        this.region         = event.regionCode;
        this.triggeredPrice = event.getIntensityKgPerMwh();
        this.threshold      = rule.threshold;
        this.operator       = rule.operator;
        this.severity       = rule.severity;
        this.message        = String.format(
                "Phát thải %s = %.2f kg/MWh %s threshold %.2f (window co2=%.0f kg / %.0f MWh) [rule: %s]",
                event.regionCode, event.getIntensityKgPerMwh(),
                rule.operator, rule.threshold,
                event.co2Kg, event.energyMwh, rule.ruleName);
        this.eventTimestamp = event.eventTime;
    }

    /**
     * Pillar tương ứng cho recommendation (1-4) — dùng SMALLINT trong DB.
     * Mặc định 2 (giữ tương thích logic Phase 3 cho FUEL_PRICE).
     */
    public short getPillar() {
        if (metricType == null) return 2;
        switch (metricType) {
            case "INVENTORY_DAYS":      return 1;
            case "FUEL_PRICE":          return 2;
            case "GRID_LOAD_PCT":       return 3;
            case "RENEWABLE_PCT":
            case "EMISSION_INTENSITY":  return 4;
            default:                    return 2;
        }
    }

    /** action_type cho recommendation (chỉ dùng khi severity=CRITICAL). */
    public String actionTypeForRecommendation() {
        if (metricType == null) metricType = "FUEL_PRICE";
        switch (metricType) {
            case "FUEL_PRICE":
                return (">".equals(operator) || ">=".equals(operator))
                        ? "HEDGE_IMPORT"
                        : "PRICE_MONITORING";
            case "GRID_LOAD_PCT":
                return "PEAK_SHAVING";          // cắt đỉnh / điều phối tải
            case "EMISSION_INTENSITY":
                return "DISPATCH_RENEWABLE";    // tăng huy động năng lượng sạch
            case "INVENTORY_DAYS":
                return "TRANSFER_STOCK";
            default:
                return "INVESTIGATE";
        }
    }

    /** Title ngắn dùng cho recommendation (cũng làm khoá dedup SQL-level). */
    public String recommendationTitle() {
        switch (metricType == null ? "FUEL_PRICE" : metricType) {
            case "GRID_LOAD_PCT":
                return String.format("Auto: Phụ tải %s vượt %.0f%%", region, threshold);
            case "EMISSION_INTENSITY":
                return String.format("Auto: Intensity CO2 %s vượt %.0f kg/MWh", region, threshold);
            case "FUEL_PRICE":
            default:
                return String.format("Auto: %s %s vượt ngưỡng tại %s", fuelType, operator, location);
        }
    }

    @Override
    public String toString() {
        return String.format("[RULE-ALERT %s] %s rule=%s key=%s value=%.4f threshold=%.4f",
                severity, metricType, ruleName,
                fuelType != null ? fuelType + "@" + location : location,
                triggeredPrice, threshold);
    }
}
