package org.cloud.source;

import org.cloud.Constant;
import org.cloud.model.AlertRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nạp danh sách alert_rules đang enabled từ PostgreSQL.
 *
 * Phase 3 chỉ nạp rule có metric_type = 'FUEL_PRICE' để xử lý fuel-prices
 * stream. Rule nạp 1 lần lúc job khởi động — đơn giản, không cần broadcast
 * state động. Khi đổi rule phải restart Flink job (đủ cho demo, theo §24.5).
 */
public class AlertRulesLoader {

    private static final Logger log = LoggerFactory.getLogger(AlertRulesLoader.class);

    private static final String SQL =
            "SELECT id, rule_name, metric_type, fuel_type, location, region_code, " +
            "       operator, threshold, severity, enabled " +
            "FROM alert_rules " +
            "WHERE enabled = TRUE AND metric_type = ?";

    /** Trả về list rule FUEL_PRICE đang enabled. */
    public static List<AlertRule> loadFuelPriceRules() {
        return loadRulesByMetricType("FUEL_PRICE");
    }

    public static List<AlertRule> loadRulesByMetricType(String metricType) {
        List<AlertRule> rules = new ArrayList<>();

        try {
            Class.forName(Constant.PostgresqlConfig.DRIVER_NAME);
        } catch (ClassNotFoundException e) {
            log.error("PostgreSQL driver không có trên classpath", e);
            return Collections.emptyList();
        }

        try (Connection conn = DriverManager.getConnection(
                    Constant.PostgresqlConfig.JDBC_URL,
                    Constant.PostgresqlConfig.USERNAME,
                    Constant.PostgresqlConfig.PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL)) {

            ps.setString(1, metricType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AlertRule r = new AlertRule();
                    r.id         = rs.getLong("id");
                    r.ruleName   = rs.getString("rule_name");
                    r.metricType = rs.getString("metric_type");
                    r.fuelType   = rs.getString("fuel_type");
                    r.location   = rs.getString("location");
                    r.regionCode = rs.getString("region_code");
                    r.operator   = rs.getString("operator");
                    r.threshold  = rs.getDouble("threshold");
                    r.severity   = rs.getString("severity");
                    r.enabled    = rs.getBoolean("enabled");
                    rules.add(r);
                }
            }

            log.info("Đã nạp {} alert_rule ({} enabled)", rules.size(), metricType);
            for (AlertRule r : rules) {
                log.info("  → {}", r);
            }
        } catch (Exception e) {
            log.error("Lỗi nạp alert_rules (metric_type={}): {}",
                    metricType, e.getMessage(), e);
        }

        return rules;
    }
}
