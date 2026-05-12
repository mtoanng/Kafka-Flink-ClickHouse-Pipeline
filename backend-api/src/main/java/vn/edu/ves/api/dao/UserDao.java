package vn.edu.ves.api.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DAO cho bảng {@code users} (Phase 2 schema).
 *
 * Auth flow:
 *   1. AuthController nhận username + password
 *   2. {@link #findByUsername(String)} trả về row (gồm password_hash bcrypt)
 *   3. Controller dùng BCryptPasswordEncoder.matches để verify
 *   4. Nếu OK → sinh JWT từ JwtTokenProvider
 */
@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Row đầy đủ (kèm password_hash) — chỉ dùng nội bộ để verify, KHÔNG return cho client. */
    public static class UserRecord {
        public long    id;
        public String  username;
        public String  passwordHash;
        public String  fullName;
        public String  email;
        public String  role;
        public boolean enabled;
    }

    private static final RowMapper<UserRecord> MAPPER = (rs, i) -> {
        UserRecord r = new UserRecord();
        r.id           = rs.getLong("id");
        r.username     = rs.getString("username");
        r.passwordHash = rs.getString("password_hash");
        r.fullName     = rs.getString("full_name");
        r.email        = rs.getString("email");
        r.role         = rs.getString("role");
        r.enabled      = rs.getBoolean("enabled");
        return r;
    };

    public Optional<UserRecord> findByUsername(String username) {
        try {
            UserRecord r = jdbc.queryForObject(
                    "SELECT id, username, password_hash, full_name, email, role, enabled " +
                    "FROM users WHERE username = ?",
                    MAPPER, username);
            return Optional.ofNullable(r);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public Optional<UserRecord> findById(long id) {
        try {
            UserRecord r = jdbc.queryForObject(
                    "SELECT id, username, password_hash, full_name, email, role, enabled " +
                    "FROM users WHERE id = ?",
                    MAPPER, id);
            return Optional.ofNullable(r);
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    public void touchLastLogin(long userId) {
        jdbc.update("UPDATE users SET last_login_at = NOW() WHERE id = ?", userId);
    }
}
