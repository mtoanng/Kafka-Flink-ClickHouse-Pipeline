package vn.edu.ves.desktop.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * POJO mapping bảng <code>users</code> (02_init_users_regions.sql).
 *
 * <p>Schema columns:</p>
 * <pre>
 *   id BIGSERIAL          → {@link #id}
 *   username VARCHAR(50)  → {@link #username}
 *   password_hash         → {@link #passwordHash} (BCrypt $2a$/$2b$)
 *   full_name             → {@link #fullName}
 *   email                 → {@link #email}
 *   role VARCHAR(20)      → {@link #role} (enum)
 *   enabled BOOLEAN       → {@link #enabled}
 *   created_at TIMESTAMP  → {@link #createdAt}
 *   updated_at TIMESTAMP  → {@link #updatedAt}
 *   last_login_at         → {@link #lastLoginAt}
 * </pre>
 *
 * <p>POJO thuần (không Lombok — module desktop-admin không khai báo lombok dep).</p>
 */
public class User {

    private long id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private Role role = Role.VIEWER;
    private boolean enabled = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    public User() {
    }

    public User(long id, String username, String passwordHash, String fullName,
                String email, Role role, boolean enabled,
                LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null ? role : Role.VIEWER;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role != null ? role : Role.VIEWER; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "User{id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", enabled=" + enabled + '}';
    }
}
