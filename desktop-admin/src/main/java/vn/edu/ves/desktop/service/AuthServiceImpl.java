package vn.edu.ves.desktop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.dao.UserDao;
import vn.edu.ves.desktop.exception.AuthenticationException;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.util.PasswordUtil;
import vn.edu.ves.desktop.util.SessionManager;

import java.util.Optional;

/**
 * Implementation gom DAO + BCrypt + SessionManager.
 *
 * <p>Flow login:</p>
 * <ol>
 *   <li>Validate input (trim, non-blank).</li>
 *   <li>{@link UserDao#findByUsername(String)} → nếu rỗng → throw "Sai tài khoản hoặc mật khẩu".</li>
 *   <li>Check {@code enabled} → false → throw "Account bị khóa".</li>
 *   <li>{@link PasswordUtil#verify(String, String)} → false → throw "Sai tài khoản hoặc mật khẩu".</li>
 *   <li>{@link SessionManager#setCurrentUser(User)}.</li>
 *   <li>{@link UserDao#updateLastLogin(long)} (best-effort, không throw nếu fail).</li>
 * </ol>
 */
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String GENERIC_BAD_CREDENTIALS = "Sai tài khoản hoặc mật khẩu";

    private final UserDao userDao;
    private final SessionManager session;

    public AuthServiceImpl() {
        this(new UserDao(), SessionManager.getInstance());
    }

    /** Constructor cho test (inject mock UserDao). */
    public AuthServiceImpl(UserDao userDao, SessionManager session) {
        this.userDao = userDao;
        this.session = session;
    }

    @Override
    public User login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new AuthenticationException("Vui lòng nhập đầy đủ tài khoản và mật khẩu");
        }
        String uname = username.trim();
        Optional<User> userOpt = userDao.findByUsername(uname);
        if (userOpt.isEmpty()) {
            log.warn("Login fail — username [{}] không tồn tại", uname);
            throw new AuthenticationException(GENERIC_BAD_CREDENTIALS);
        }
        User user = userOpt.get();
        if (!user.isEnabled()) {
            log.warn("Login fail — username [{}] đã bị disable", uname);
            throw new AuthenticationException("Tài khoản đang bị khóa, vui lòng liên hệ admin");
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            log.warn("Login fail — sai mật khẩu cho username [{}]", uname);
            throw new AuthenticationException(GENERIC_BAD_CREDENTIALS);
        }
        session.setCurrentUser(user);
        boolean updated = userDao.updateLastLogin(user.getId());
        if (!updated) {
            log.warn("updateLastLogin({}) trả false — bỏ qua, login vẫn coi như OK", user.getId());
        }
        log.info("User [{}] (role={}) login thành công", user.getUsername(), user.getRole());
        return user;
    }

    @Override
    public void logout() {
        User u = session.getCurrentUser();
        session.clear();
        if (u != null) {
            log.info("User [{}] logout", u.getUsername());
        }
    }

    @Override
    public User getCurrentUser() {
        return session.getCurrentUser();
    }
}
