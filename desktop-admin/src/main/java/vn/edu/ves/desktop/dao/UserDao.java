package vn.edu.ves.desktop.dao;

import vn.edu.ves.desktop.model.Role;
import vn.edu.ves.desktop.model.User;
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
 * DAO cho bảng <code>users</code>.
 *
 * <p>Hỗ trợ:</p>
 * <ul>
 *   <li>{@link #findByUsername(String)} — cho login flow.</li>
 *   <li>{@link #findById(long)}, {@link #findAll()} — cho admin User CRUD screen (Phase 5.4).</li>
 *   <li>{@link #updateLastLogin(long)} — đánh dấu thời điểm login thành công.</li>
 *   <li>{@link #save(User, String)} — insert (id=0) hoặc update.</li>
 *   <li>{@link #delete(long)}, {@link #setEnabled(long, boolean)}.</li>
 * </ul>
 *
 * <p>Mọi method dùng try-with-resources để chắc chắn đóng Connection/Statement/ResultSet.</p>
 */
public class UserDao extends BaseDao {

    private final DatabaseConfig dbConfig;

    public UserDao() {
        this(DatabaseConfig.getInstance());
    }

    /** Constructor cho test (inject mock DatabaseConfig hoặc H2 config). */
    public UserDao(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public Optional<User> findByUsername(String username) {
        final String sql = "SELECT id, username, password_hash, full_name, email, role, enabled, " +
                "created_at, updated_at, last_login_at FROM users WHERE username = ?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("findByUsername({}) lỗi: {}", username, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<User> findById(long id) {
        final String sql = "SELECT id, username, password_hash, full_name, email, role, enabled, " +
                "created_at, updated_at, last_login_at FROM users WHERE id = ?";
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

    public List<User> findAll() {
        final String sql = "SELECT id, username, password_hash, full_name, email, role, enabled, " +
                "created_at, updated_at, last_login_at FROM users ORDER BY id";
        List<User> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("findAll() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    /**
     * Update column <code>last_login_at = NOW()</code> sau khi login thành công.
     * Trả về <code>true</code> nếu update OK.
     */
    public boolean updateLastLogin(long userId) {
        final String sql = "UPDATE users SET last_login_at = ?, updated_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(now));
            ps.setTimestamp(2, Timestamp.valueOf(now));
            ps.setLong(3, userId);
            int n = ps.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            log.error("updateLastLogin({}) lỗi: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Insert (nếu id=0) hoặc update. Nếu <code>plainPassword</code> non-null/non-blank thì
     * sinh BCrypt hash mới và lưu (chỉ khi caller chủ động đổi mật khẩu).
     *
     * <p>Caller chịu trách nhiệm hash password trước khi gọi (qua PasswordUtil.hash).
     * Method này nhận trực tiếp hash đã sinh để DAO không phải import jbcrypt.</p>
     *
     * @return user sau khi lưu (id được set với insert), null nếu lỗi.
     */
    public User save(User user, String newPasswordHashOrNull) {
        if (user == null) {
            throw new IllegalArgumentException("user null");
        }
        return user.getId() == 0 ? insert(user, newPasswordHashOrNull)
                                 : update(user, newPasswordHashOrNull);
    }

    private User insert(User user, String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            log.error("insert user mà không có passwordHash → reject");
            return null;
        }
        final String sql = "INSERT INTO users (username, password_hash, full_name, email, role, enabled) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, passwordHash);
            setStringOrNull(ps, 3, user.getFullName());
            setStringOrNull(ps, 4, user.getEmail());
            ps.setString(5, user.getRole().name());
            ps.setBoolean(6, user.isEnabled());
            int n = ps.executeUpdate();
            if (n == 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            user.setPasswordHash(passwordHash);
            return user;
        } catch (SQLException e) {
            log.error("insert user({}) lỗi: {}", user.getUsername(), e.getMessage(), e);
            return null;
        }
    }

    private User update(User user, String newPasswordHashOrNull) {
        boolean withPassword = newPasswordHashOrNull != null && !newPasswordHashOrNull.isBlank();
        String sql = withPassword
                ? "UPDATE users SET username=?, full_name=?, email=?, role=?, enabled=?, password_hash=?, updated_at=? WHERE id=?"
                : "UPDATE users SET username=?, full_name=?, email=?, role=?, enabled=?, updated_at=? WHERE id=?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            setStringOrNull(ps, 2, user.getFullName());
            setStringOrNull(ps, 3, user.getEmail());
            ps.setString(4, user.getRole().name());
            ps.setBoolean(5, user.isEnabled());
            int idx = 6;
            if (withPassword) {
                ps.setString(idx++, newPasswordHashOrNull);
                user.setPasswordHash(newPasswordHashOrNull);
            }
            ps.setTimestamp(idx++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(idx, user.getId());
            int n = ps.executeUpdate();
            return n > 0 ? user : null;
        } catch (SQLException e) {
            log.error("update user(id={}) lỗi: {}", user.getId(), e.getMessage(), e);
            return null;
        }
    }

    public boolean delete(long id) {
        final String sql = "DELETE FROM users WHERE id = ?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete user(id={}) lỗi: {}", id, e.getMessage(), e);
            return false;
        }
    }

    public boolean setEnabled(long id, boolean enabled) {
        final String sql = "UPDATE users SET enabled = ?, updated_at = ? WHERE id = ?";
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

    /* --------------------- mapping --------------------- */

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(getStringOrNull(rs, "full_name"));
        u.setEmail(getStringOrNull(rs, "email"));
        u.setRole(Role.fromString(rs.getString("role")));
        u.setEnabled(rs.getBoolean("enabled"));
        u.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        u.setUpdatedAt(getLocalDateTimeOrNull(rs, "updated_at"));
        u.setLastLoginAt(getLocalDateTimeOrNull(rs, "last_login_at"));
        return u;
    }
}
