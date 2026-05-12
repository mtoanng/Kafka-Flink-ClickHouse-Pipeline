package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.exception.AuthenticationException;
import vn.edu.ves.desktop.model.User;

/**
 * Service xác thực — abstraction để LoginController không lệ thuộc UserDao + BCrypt.
 */
public interface AuthService {

    /**
     * Verify username/password. Nếu OK: set vào {@link vn.edu.ves.desktop.util.SessionManager}
     * + update last_login_at, return {@link User}.
     *
     * @throws AuthenticationException khi user không tồn tại, account disabled, hoặc password sai.
     */
    User login(String username, String password) throws AuthenticationException;

    /** Clear session. Không tương tác DB. */
    void logout();

    /** Tiện ích — trả về user đang login (null nếu chưa). */
    User getCurrentUser();
}
