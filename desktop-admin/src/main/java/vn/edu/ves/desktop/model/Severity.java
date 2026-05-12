package vn.edu.ves.desktop.model;

/**
 * Severity (khớp constraint chk_alert_rules_severity / chk_alerts_severity).
 */
public enum Severity {
    INFO,
    WARNING,
    CRITICAL;

    public static Severity fromString(String raw) {
        if (raw == null) return INFO;
        try {
            return Severity.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return INFO;
        }
    }
}
