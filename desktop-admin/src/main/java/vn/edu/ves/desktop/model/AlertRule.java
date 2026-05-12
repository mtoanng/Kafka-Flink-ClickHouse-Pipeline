package vn.edu.ves.desktop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * POJO map bảng <code>alert_rules</code> (03_init_alerts.sql + 07_init_security_features.sql).
 *
 * <pre>
 *   id BIGSERIAL              → id
 *   rule_name VARCHAR(150)    → ruleName
 *   fuel_type VARCHAR(50) nullable → fuelType
 *   location VARCHAR(100) nullable → location
 *   operator VARCHAR(5)       → operator (enum)
 *   threshold NUMERIC(12,4)   → threshold
 *   severity VARCHAR(20)      → severity (enum)
 *   enabled BOOLEAN           → enabled
 *   created_by BIGINT nullable→ createdBy
 *   created_at / updated_at   → createdAt / updatedAt
 *   metric_type VARCHAR(50)   → metricType (enum, default FUEL_PRICE)
 *   region_code VARCHAR(20) FK regions(code) nullable → regionCode
 * </pre>
 */
public class AlertRule {

    private long id;
    private String ruleName;
    private MetricType metricType = MetricType.FUEL_PRICE;
    private String fuelType;
    private String regionCode;
    private String location;
    private Operator operator = Operator.GT;
    private BigDecimal threshold;
    private Severity severity = Severity.WARNING;
    private boolean enabled = true;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AlertRule() {
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public MetricType getMetricType() { return metricType; }
    public void setMetricType(MetricType metricType) {
        this.metricType = metricType != null ? metricType : MetricType.FUEL_PRICE;
    }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getRegionCode() { return regionCode; }
    public void setRegionCode(String regionCode) { this.regionCode = regionCode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Operator getOperator() { return operator; }
    public void setOperator(Operator operator) {
        this.operator = operator != null ? operator : Operator.GT;
    }

    public BigDecimal getThreshold() { return threshold; }
    public void setThreshold(BigDecimal threshold) { this.threshold = threshold; }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) {
        this.severity = severity != null ? severity : Severity.WARNING;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** Tiện cho TableView CellValueFactory: display "metric op threshold". */
    public String getDisplayCondition() {
        return metricType + " " + operator.toSymbol() + " " + (threshold == null ? "?" : threshold);
    }

    @Override
    public String toString() {
        return "AlertRule{id=" + id + ", rule='" + ruleName + "', metric=" + metricType +
                ", op=" + operator + ", threshold=" + threshold + ", severity=" + severity + '}';
    }
}
