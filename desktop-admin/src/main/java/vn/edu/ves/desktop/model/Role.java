package vn.edu.ves.desktop.model;

import java.util.Locale;

/**
 * Phân quyền user (khớp constraint chk_users_role ở 02_init_users_regions.sql).
 *
 * <ul>
 *   <li>{@link #ADMIN}: full quyền — CRUD User + Region + AlertRule.</li>
 *   <li>{@link #MANAGER}: xem dashboard + CRUD Region/AlertRule, không động User.</li>
 *   <li>{@link #VIEWER}: chỉ xem dashboard, mọi form CRUD đều read-only.</li>
 * </ul>
 */
public enum Role {
    ADMIN,
    MANAGER,
    VIEWER;

    /**
     * Parse role string từ DB. Case-insensitive. Trả về {@link #VIEWER} nếu null/blank/invalid
     * (defensive — DB constraint đã chặn các giá trị xấu rồi, nhưng tránh NPE phía Java).
     */
    public static Role fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return VIEWER;
        }
        try {
            return Role.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return VIEWER;
        }
    }
}
