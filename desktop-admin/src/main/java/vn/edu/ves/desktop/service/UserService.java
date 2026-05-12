package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service quản lý user — ADMIN-only screen ở Phase 5.4.
 *
 * <p>Quy ước:</p>
 * <ul>
 *   <li>{@code newPasswordOrNull}: nếu non-null/non-blank thì sinh BCrypt hash mới
 *       qua {@link vn.edu.ves.desktop.util.PasswordUtil#hash(String)} và set vào DB.
 *       Khi update mà user không đổi password thì truyền null/blank.</li>
 *   <li>{@code delete(id)}: chỉ ADMIN gọi được. Caller phải check
 *       {@link vn.edu.ves.desktop.util.SessionManager#isAdmin()} trước.</li>
 * </ul>
 */
public interface UserService {

    List<User> findAll();

    Optional<User> findById(long id);

    Optional<User> findByUsername(String username);

    /** Save với option đổi mật khẩu. Trả về user đã save hoặc null nếu fail. */
    User save(User user, String newPasswordOrNull);

    boolean delete(long id);

    boolean setEnabled(long id, boolean enabled);
}
