package vn.edu.ves.desktop.util;

import vn.edu.ves.desktop.model.Role;
import vn.edu.ves.desktop.model.User;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Singleton giữ user đang đăng nhập trong scope JVM (1 desktop app = 1 user tại 1 thời điểm).
 *
 * <p>Pattern: Singleton eager init + thread-safe slot dùng {@link AtomicReference}.
 * Tránh dùng <code>volatile</code> field thuần vì assignment & read cần atomic semantic
 * khi controller chạy trên JavaFX Application Thread còn background task chạy thread khác.</p>
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private final AtomicReference<User> currentUser = new AtomicReference<>();

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    /** Set user đăng nhập (gọi từ AuthServiceImpl sau khi BCrypt verify OK). */
    public void setCurrentUser(User user) {
        currentUser.set(user);
    }

    /** Trả về user hiện tại; có thể null nếu chưa login. */
    public User getCurrentUser() {
        return currentUser.get();
    }

    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser.get());
    }

    /** Clear session (gọi từ logout / window close). */
    public void clear() {
        currentUser.set(null);
    }

    public boolean isLoggedIn() {
        return currentUser.get() != null;
    }

    public boolean isAdmin() {
        User u = currentUser.get();
        return u != null && u.getRole() == Role.ADMIN;
    }

    public boolean isManager() {
        User u = currentUser.get();
        return u != null && u.getRole() == Role.MANAGER;
    }

    /** TRUE nếu có quyền ghi (ADMIN hoặc MANAGER). VIEWER → FALSE. */
    public boolean canWrite() {
        User u = currentUser.get();
        return u != null && (u.getRole() == Role.ADMIN || u.getRole() == Role.MANAGER);
    }
}
