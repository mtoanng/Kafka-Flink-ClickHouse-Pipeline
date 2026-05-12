package vn.edu.ves.desktop.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * Helper tĩnh cho DAO: bind nullable params, đọc nullable columns, log SQL error.
 *
 * <p>Mọi DAO concrete đều "extends BaseDao" để inherit logger + helper, nhưng vẫn
 * có thể gọi static method trực tiếp ({@code BaseDao.setStringOrNull(...)}).</p>
 */
public abstract class BaseDao {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Bind String nullable: nếu null/blank thì set NULL với type VARCHAR. */
    public static void setStringOrNull(PreparedStatement ps, int idx, String value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.VARCHAR);
        } else {
            ps.setString(idx, value);
        }
    }

    /** Bind Long nullable: 0 hoặc null đều coi như NULL. */
    public static void setLongOrNull(PreparedStatement ps, int idx, Long value) throws SQLException {
        if (value == null || value == 0L) {
            ps.setNull(idx, Types.BIGINT);
        } else {
            ps.setLong(idx, value);
        }
    }

    /** Bind Timestamp từ LocalDateTime nullable. */
    public static void setTimestampOrNull(PreparedStatement ps, int idx, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(idx, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(idx, Timestamp.valueOf(value));
        }
    }

    /** Đọc String nullable. */
    public static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        String s = rs.getString(column);
        return rs.wasNull() ? null : s;
    }

    /** Đọc Long nullable (return null thay vì 0 khi DB NULL). */
    public static Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long v = rs.getLong(column);
        return rs.wasNull() ? null : v;
    }

    /** Đọc LocalDateTime từ TIMESTAMP nullable. */
    public static LocalDateTime getLocalDateTimeOrNull(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }
}
