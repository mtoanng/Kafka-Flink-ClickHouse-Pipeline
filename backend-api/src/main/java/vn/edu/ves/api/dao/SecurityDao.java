package vn.edu.ves.api.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.edu.ves.api.dto.CascadeRiskDto;
import vn.edu.ves.api.dto.SecurityScoreDto;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Cross-pillar:
 *   - {@code v_security_score}  : 1 row tổng (Light ESI) — Phase 7.1 columns
 *     {@code overall_score, status, pillar1..4_score, computed_at}.
 *   - {@code v_cascade_risks}   : DROPPED in Phase 7.1.  Cascade analysis sẽ
 *     được re-implement ở phase sau (cần multi-pillar correlation từ Flink job
 *     mới). Endpoint cũ vẫn trả 200 với mảng rỗng để giữ backward-compat.
 */
@Repository
public class SecurityDao {

    private static final Logger log = LoggerFactory.getLogger(SecurityDao.class);

    private final JdbcTemplate jdbc;

    public SecurityDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<SecurityScoreDto> SCORE_MAPPER = (rs, i) -> SecurityScoreDto.builder()
            .pillar1Score(rs.getBigDecimal("pillar1_score"))
            .pillar2Score(rs.getBigDecimal("pillar2_score"))
            .pillar3Score(rs.getBigDecimal("pillar3_score"))
            .pillar4Score(rs.getBigDecimal("pillar4_score"))
            .overallScore(rs.getBigDecimal("overall_score"))
            .status(rs.getString("status"))
            .computedAt(toOffset(rs.getTimestamp("computed_at")))
            .build();

    public Optional<SecurityScoreDto> score() {
        List<SecurityScoreDto> rows = jdbc.query(
                "SELECT pillar1_score, pillar2_score, pillar3_score, pillar4_score, " +
                "overall_score, status, computed_at FROM v_security_score LIMIT 1",
                SCORE_MAPPER);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    /**
     * @deprecated {@code v_cascade_risks} đã bị drop ở Phase 7.1. Không còn dữ liệu
     *             cascade nào được xuất bản. Endpoint giữ lại chỉ để frontend cũ
     *             không vỡ; sẽ trả về danh sách rỗng đến khi cascade analysis
     *             được re-implement (planned post-Phase 8).
     */
    @Deprecated
    public List<CascadeRiskDto> cascadeRisks() {
        log.warn("cascadeRisks() called — view v_cascade_risks dropped in Phase 7.1; returning empty list");
        return Collections.emptyList();
    }

    /**
     * Defensive consistency with {@code PillarDao.toOffset(...)}: return {@code null}
     * when the column is NULL instead of fabricating "now". The view always emits
     * non-null via {@code COALESCE} so this branch is unreachable in practice;
     * aligning the two helpers prevents a future caller from mistaking a freshly-
     * generated timestamp for a real database value.
     */
    private static OffsetDateTime toOffset(Timestamp ts) {
        return ts == null ? null : ts.toInstant().atOffset(ZoneOffset.UTC);
    }
}
