package vn.edu.ves.desktop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.edu.ves.desktop.dao.UserDao;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.util.PasswordUtil;
import vn.edu.ves.desktop.util.SessionManager;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDao userDao;
    private final SessionManager session;

    public UserServiceImpl() {
        this(new UserDao(), SessionManager.getInstance());
    }

    public UserServiceImpl(UserDao userDao, SessionManager session) {
        this.userDao = userDao;
        this.session = session;
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll();
    }

    @Override
    public Optional<User> findById(long id) {
        return userDao.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public User save(User user, String newPasswordOrNull) {
        if (user == null) throw new IllegalArgumentException("user null");
        if (user.getUsername() != null) {
            user.setUsername(user.getUsername().trim());
        }
        String hash = null;
        if (newPasswordOrNull != null && !newPasswordOrNull.isBlank()) {
            hash = PasswordUtil.hash(newPasswordOrNull);
        }
        return userDao.save(user, hash);
    }

    @Override
    public boolean delete(long id) {
        if (!session.isAdmin()) {
            log.warn("Non-admin tried to delete user id={} (current={})",
                    id, session.getCurrentUser() == null ? "anonymous"
                            : session.getCurrentUser().getUsername());
            return false;
        }
        if (session.getCurrentUser() != null && session.getCurrentUser().getId() == id) {
            log.warn("Admin {} tried to delete chính mình (id={}) — reject để tránh khóa hệ thống",
                    session.getCurrentUser().getUsername(), id);
            return false;
        }
        return userDao.delete(id);
    }

    @Override
    public boolean setEnabled(long id, boolean enabled) {
        return userDao.setEnabled(id, enabled);
    }
}
