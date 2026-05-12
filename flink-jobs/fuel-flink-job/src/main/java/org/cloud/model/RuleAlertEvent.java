package org.cloud.model;

import java.io.Serializable;

/**
 * Cảnh báo rule-based — kết quả AlertDetectionFunction emit.
 *
 * Khác với PriceAlert (giá biến động > 3%), RuleAlertEvent được tạo khi
 * một event vi phạm 1 alert_rule cụ thể trong DB (e.g. WTI vượt 90 USD).
 *
 * Được ghi vào bảng `alerts` (Phase 2 schema). Khi severity = CRITICAL,
 * cũng được dùng để sinh `recommendations` (cooldown 30 phút SQL-level).
 */
public class RuleAlertEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public long    ruleId;
    public String  ruleName;
    public String  fuelType;
    public String  location;
    public String  region;
    public double  triggeredPrice;
    public double  threshold;
    public String  operator;
    public String  severity;       // INFO | WARNING | CRITICAL
    public String  message;
    public String  eventTimestamp; // ISO 8601 từ FuelPrice gốc

    public RuleAlertEvent() {}

    public RuleAlertEvent(AlertRule rule, FuelPrice event) {
        this.ruleId         = rule.id;
        this.ruleName       = rule.ruleName;
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

    /** action_type cho recommendation (chỉ dùng khi severity=CRITICAL). */
    public String actionTypeForRecommendation() {
        if (">".equals(operator) || ">=".equals(operator)) {
            return "HEDGE_IMPORT";   // giá vượt ngưỡng cao → mua tăng dự trữ
        }
        return "PRICE_MONITORING";    // giá xuống thấp bất thường → theo dõi cơ hội
    }

    @Override
    public String toString() {
        return String.format("[RULE-ALERT %s] rule=%s %s @ %s price=%.4f threshold=%.4f",
                severity, ruleName, fuelType, location, triggeredPrice, threshold);
    }
}
