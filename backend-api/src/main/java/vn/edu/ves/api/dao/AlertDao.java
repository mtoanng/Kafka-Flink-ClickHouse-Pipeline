package vn.edu.ves.api.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.edu.ves.api.dto.AlertDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Đọc {@code v_active_alerts} — alert chưa acknowledged của tất cả pillar.
 */
@Repository
public class AlertDao {

    private final JdbcTemplate jdbc;

    public AlertDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<AlertDto> MAPPER = (rs, i) -> AlertDto.builder()
            .id(rs.getLong("id"))
            .ruleId((Long) rs.getObject("rule_id"))
            .ruleName(rs.getString("rule_name"))
            .metricType(rs.getString("metric_type"))
            .fuelType(rs.getString("fuel_type"))
            .location(rs.getString("location"))
            .region(rs.getString("region"))
            .triggeredPrice(rs.getBigDecimal("triggered_price"))
            .threshold(rs.getBigDecimal("threshold"))
            .operator(rs.getString("operator"))
            .severity(rs.getString("severity"))
            .message(rs.getString("message"))
            .eventTimestamp(toLocal(rs.getTimestamp("event_timestamp")))
            .alertTimestamp(toLocal(rs.getTimestamp("alert_timestamp")))
            .ageSeconds(rs.getInt("age_seconds"))
            .build();

    public List<AlertDto> active(int limit) {
        return jdbc.query(
                "SELECT * FROM v_active_alerts ORDER BY alert_timestamp DESC LIMIT ?",
                MAPPER, limit);
    }

    private static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
