package org.cloud.model;

import java.io.Serializable;

/**
 * Quy tắc cảnh báo (rule-based detection).
 * Mỗi row trong bảng `alert_rules` ánh xạ vào 1 AlertRule object.
 *
 * Phase 3 chỉ xử lý rule có metric_type = FUEL_PRICE. Các metric_type khác
 * (INVENTORY_DAYS, GRID_LOAD_PCT, RENEWABLE_PCT, EMISSION_INTENSITY) sẽ
 * được Phase 4 mở rộng khi có generator/stream tương ứng.
 *
 * Cách match một FuelPrice event với rule:
 *   - rule.fuelType == null   → match mọi fuel
 *   - rule.fuelType == event.fuelType
 *   - rule.location == null   → match mọi location
 *   - rule.location == event.location
 *   - operator (>, <, >=, <=, =) so sánh event.price với rule.threshold
 */
public class AlertRule implements Serializable {

    private static final long serialVersionUID = 1L;

    public long    id;
    public String  ruleName;
    public String  metricType;     // FUEL_PRICE | INVENTORY_DAYS | GRID_LOAD_PCT | ...
    public String  fuelType;       // null = mọi fuel
    public String  location;       // null = mọi location
    public String  regionCode;     // null = mọi region (NULL trong DB)
    public String  operator;       // >, <, >=, <=, =
    public double  threshold;
    public String  severity;       // INFO | WARNING | CRITICAL
    public boolean enabled;

    public AlertRule() {}

    /** Kiểm tra rule có khớp event không (chưa so sánh giá). */
    public boolean appliesTo(FuelPrice event) {
        if (!enabled) return false;
        if (fuelType != null && !fuelType.equals(event.fuelType)) return false;
        if (location != null && !location.equals(event.location)) return false;
        return true;
    }

    /** So sánh giá event với threshold theo operator. */
    public boolean isTriggered(double price) {
        switch (operator) {
            case ">":  return price >  threshold;
            case "<":  return price <  threshold;
            case ">=": return price >= threshold;
            case "<=": return price <= threshold;
            case "=":  return Math.abs(price - threshold) < 1e-9;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return String.format("Rule[id=%d, %s, %s %s %.4f %s, fuel=%s, loc=%s]",
                id, ruleName, metricType, operator, threshold, severity,
                fuelType == null ? "*" : fuelType,
                location == null ? "*" : location);
    }
}
