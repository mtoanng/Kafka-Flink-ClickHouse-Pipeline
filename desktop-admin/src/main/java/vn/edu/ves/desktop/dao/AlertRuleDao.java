package vn.edu.ves.desktop.dao;

import vn.edu.ves.desktop.model.AlertRule;
import vn.edu.ves.desktop.model.MetricType;
import vn.edu.ves.desktop.model.Operator;
import vn.edu.ves.desktop.model.Severity;
import vn.edu.ves.desktop.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO cho bảng <code>alert_rules</code>.
 */
public class AlertRuleDao extends BaseDao {

    private final DatabaseConfig dbConfig;

    public AlertRuleDao() {
        this(DatabaseConfig.getInstance());
    }

    public AlertRuleDao(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    private static final String COLS =
            "id, rule_name, metric_type, fuel_type, region_code, location, operator, threshold, " +
            "severity, enabled, created_by, created_at, updated_at";

    public List<AlertRule> findAll() {
        final String sql = "SELECT " + COLS + " FROM alert_rules ORDER BY id";
        List<AlertRule> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public List<AlertRule> findByMetricType(MetricType metricType) {
        final String sql = "SELECT " + COLS + " FROM alert_rules WHERE metric_type = ? ORDER BY id";
        List<AlertRule> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, metricType.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("findByMetricType({}) lỗi: {}", metricType, e.getMessage(), e);
        }
        return out;
    }

    public Optional<AlertRule> findById(long id) {
        final String sql = "SELECT " + COLS + " FROM alert_rules WHERE id = ?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("findById({}) lỗi: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public AlertRule save(AlertRule rule) {
        if (rule == null) throw new IllegalArgumentException("rule null");
        return rule.getId() == 0 ? insert(rule) : update(rule);
    }

    private AlertRule insert(AlertRule r) {
        final String sql = "INSERT INTO alert_rules " +
                "(rule_name, metric_type, fuel_type, region_code, location, operator, threshold, " +
                " severity, enabled, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getRuleName());
            ps.setString(2, r.getMetricType().name());
            setStringOrNull(ps, 3, r.getFuelType());
            setStringOrNull(ps, 4, r.getRegionCode());
            setStringOrNull(ps, 5, r.getLocation());
            ps.setString(6, r.getOperator().toSymbol());
            ps.setBigDecimal(7, r.getThreshold());
            ps.setString(8, r.getSeverity().name());
            ps.setBoolean(9, r.isEnabled());
            setLongOrNull(ps, 10, r.getCreatedBy());
            int n = ps.executeUpdate();
            if (n == 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getLong(1));
            }
            return r;
        } catch (SQLException e) {
            log.error("insert alert_rule({}) lỗi: {}", r.getRuleName(), e.getMessage(), e);
            return null;
        }
    }

    private AlertRule update(AlertRule r) {
        final String sql = "UPDATE alert_rules SET rule_name=?, metric_type=?, fuel_type=?, " +
                "region_code=?, location=?, operator=?, threshold=?, severity=?, enabled=?, " +
                "updated_at=? WHERE id=?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getRuleName());
            ps.setString(2, r.getMetricType().name());
            setStringOrNull(ps, 3, r.getFuelType());
            setStringOrNull(ps, 4, r.getRegionCode());
            setStringOrNull(ps, 5, r.getLocation());
            ps.setString(6, r.getOperator().toSymbol());
            ps.setBigDecimal(7, r.getThreshold());
            ps.setString(8, r.getSeverity().name());
            ps.setBoolean(9, r.isEnabled());
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(11, r.getId());
            int n = ps.executeUpdate();
            return n > 0 ? r : null;
        } catch (SQLException e) {
            log.error("update alert_rule(id={}) lỗi: {}", r.getId(), e.getMessage(), e);
            return null;
        }
    }

    public boolean delete(long id) {
        final String sql = "DELETE FROM alert_rules WHERE id = ?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete alert_rule(id={}) lỗi: {}", id, e.getMessage(), e);
            return false;
        }
    }

    public boolean setEnabled(long id, boolean enabled) {
        final String sql = "UPDATE alert_rules SET enabled = ?, updated_at = ? WHERE id = ?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, enabled);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("setEnabled(id={}, {}) lỗi: {}", id, enabled, e.getMessage(), e);
            return false;
        }
    }

    private AlertRule mapRow(ResultSet rs) throws SQLException {
        AlertRule r = new AlertRule();
        r.setId(rs.getLong("id"));
        r.setRuleName(rs.getString("rule_name"));
        r.setMetricType(MetricType.fromString(rs.getString("metric_type")));
        r.setFuelType(getStringOrNull(rs, "fuel_type"));
        r.setRegionCode(getStringOrNull(rs, "region_code"));
        r.setLocation(getStringOrNull(rs, "location"));
        r.setOperator(Operator.fromSymbol(rs.getString("operator")));
        r.setThreshold(rs.getBigDecimal("threshold"));
        r.setSeverity(Severity.fromString(rs.getString("severity")));
        r.setEnabled(rs.getBoolean("enabled"));
        r.setCreatedBy(getLongOrNull(rs, "created_by"));
        r.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        r.setUpdatedAt(getLocalDateTimeOrNull(rs, "updated_at"));
        return r;
    }
}
