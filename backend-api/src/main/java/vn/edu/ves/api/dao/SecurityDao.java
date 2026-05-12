package vn.edu.ves.api.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.edu.ves.api.dto.CascadeRiskDto;
import vn.edu.ves.api.dto.SecurityScoreDto;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Cross-pillar:
 *   - v_security_score   : 1 row tổng (Light ESI)
 *   - v_cascade_risks    : 0-N risk
 */
@Repository
public class SecurityDao {

    private final JdbcTemplate jdbc;

    public SecurityDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<SecurityScoreDto> SCORE_MAPPER = (rs, i) -> {
        Timestamp ts = rs.getTimestamp("computed_at");
        OffsetDateTime computedAt = ts == null
                ? OffsetDateTime.now()
                : ts.toInstant().atOffset(ZoneOffset.UTC);
        return SecurityScoreDto.builder()
                .pillar1Score(rs.getBigDecimal("pillar1_score"))
                .pillar2Score(rs.getBigDecimal("pillar2_score"))
                .pillar3Score(rs.getBigDecimal("pillar3_score"))
                .pillar4Score(rs.getBigDecimal("pillar4_score"))
                .overallScore(rs.getBigDecimal("overall_score"))
                .status(rs.getString("status"))
                .computedAt(computedAt)
                .build();
    };

    public Optional<SecurityScoreDto> score() {
        List<SecurityScoreDto> rows = jdbc.query("SELECT * FROM v_security_score LIMIT 1", SCORE_MAPPER);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    private static final RowMapper<CascadeRiskDto> RISK_MAPPER = (rs, i) -> CascadeRiskDto.builder()
            .riskType(rs.getString("risk_type"))
            .severity(rs.getString("severity"))
            .description(rs.getString("description"))
            .details(rs.getString("details"))   // JSONB → text pass-through
            .build();

    public List<CascadeRiskDto> cascadeRisks() {
        return jdbc.query(
                "SELECT risk_type, severity, description, details::text AS details FROM v_cascade_risks",
                RISK_MAPPER);
    }
}
