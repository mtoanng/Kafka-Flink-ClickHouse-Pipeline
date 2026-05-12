package vn.edu.ves.api.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import vn.edu.ves.api.dto.RecommendationDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO cho bảng {@code recommendations} (Phase 2.6).
 *
 * - GET: dùng {@code v_active_recommendations} (đã tính age_seconds + is_expired).
 * - ACK: UPDATE thẳng vào bảng {@code recommendations}.
 */
@Repository
public class RecommendationDao {

    private final JdbcTemplate jdbc;

    public RecommendationDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<RecommendationDto> MAPPER = (rs, i) -> RecommendationDto.builder()
            .id(rs.getLong("id"))
            .pillar(rs.getShort("pillar"))
            .actionType(rs.getString("action_type"))
            .severity(rs.getString("severity"))
            .title(rs.getString("title"))
            .message(rs.getString("message"))
            .suggestedData(rs.getString("suggested_data"))
            .suggestedAt(toLocal(rs.getTimestamp("suggested_at")))
            .ageSeconds(rs.getInt("age_seconds"))
            .expiresAt(toLocal(rs.getTimestamp("expires_at")))
            .expired(rs.getBoolean("is_expired"))
            .build();

    /** Lấy danh sách recommendations PENDING (view đã filter is_expired = false). */
    public List<RecommendationDto> active(int limit) {
        return jdbc.query(
                "SELECT id, pillar, action_type, severity, title, message, " +
                "  suggested_data::text AS suggested_data, suggested_at, age_seconds, " +
                "  expires_at, is_expired " +
                "FROM v_active_recommendations " +
                "ORDER BY severity DESC, suggested_at DESC LIMIT ?",
                MAPPER, limit);
    }

    /** Trả về số row updated (0 nếu rec không tồn tại hoặc đã ACK trước đó). */
    public int acknowledge(long recommendationId, long userId, String status, String note) {
        return jdbc.update(
                "UPDATE recommendations " +
                "SET status = ?, acknowledged_at = NOW(), acknowledged_by = ?, note = ? " +
                "WHERE id = ? AND status = 'PENDING'",
                status, userId, note, recommendationId);
    }

    private static LocalDateTime toLocal(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
