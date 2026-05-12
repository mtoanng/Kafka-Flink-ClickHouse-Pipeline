package vn.edu.ves.desktop.model;

/**
 * Metric type (khớp constraint chk_alert_rules_metric_type ở 07_init_security_features.sql).
 *
 * <ul>
 *   <li>FUEL_PRICE — Pillar 2</li>
 *   <li>INVENTORY_DAYS — Pillar 1</li>
 *   <li>GRID_LOAD_PCT — Pillar 3</li>
 *   <li>RENEWABLE_PCT — Pillar 4</li>
 *   <li>EMISSION_INTENSITY — Pillar 4</li>
 * </ul>
 */
public enum MetricType {
    FUEL_PRICE,
    INVENTORY_DAYS,
    GRID_LOAD_PCT,
    RENEWABLE_PCT,
    EMISSION_INTENSITY;

    public static MetricType fromString(String raw) {
        if (raw == null) return FUEL_PRICE;
        try {
            return MetricType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return FUEL_PRICE;
        }
    }
}
