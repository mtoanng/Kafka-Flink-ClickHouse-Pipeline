package vn.edu.ves.desktop.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.model.Role;
import vn.edu.ves.desktop.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests cho {@link SessionManager} (Singleton, AtomicReference-backed).
 */
public class SessionManagerTest {

    private SessionManager session;

    @Before
    public void setUp() {
        session = SessionManager.getInstance();
        session.clear();
    }

    @After
    public void tearDown() {
        session.clear();
    }

    @Test
    public void getInstance_returnsSameInstance() {
        SessionManager a = SessionManager.getInstance();
        SessionManager b = SessionManager.getInstance();
        assertTrue("Singleton phải luôn trả về cùng 1 instance", a == b);
    }

    @Test
    public void notLoggedInByDefault() {
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());
        assertFalse(session.isAdmin());
        assertFalse(session.canWrite());
    }

    @Test
    public void setCurrentUser_admin_setsRoleAndLoggedIn() {
        User u = new User();
        u.setId(1);
        u.setUsername("admin");
        u.setRole(Role.ADMIN);
        session.setCurrentUser(u);
        assertTrue(session.isLoggedIn());
        assertTrue(session.isAdmin());
        assertTrue(session.canWrite());
        assertFalse(session.isManager());
        assertEquals("admin", session.getCurrentUser().getUsername());
    }

    @Test
    public void setCurrentUser_manager_isManagerAndCanWrite() {
        User u = new User();
        u.setId(2);
        u.setUsername("manager");
        u.setRole(Role.MANAGER);
        session.setCurrentUser(u);
        assertTrue(session.isManager());
        assertTrue(session.canWrite());
        assertFalse(session.isAdmin());
    }

    @Test
    public void setCurrentUser_viewer_cannotWrite() {
        User u = new User();
        u.setId(3);
        u.setUsername("viewer");
        u.setRole(Role.VIEWER);
        session.setCurrentUser(u);
        assertTrue(session.isLoggedIn());
        assertFalse(session.canWrite());
        assertFalse(session.isAdmin());
        assertFalse(session.isManager());
    }

    @Test
    public void clear_logsOut() {
        User u = new User();
        u.setUsername("x");
        u.setRole(Role.ADMIN);
        session.setCurrentUser(u);
        assertTrue(session.isLoggedIn());
        session.clear();
        assertFalse(session.isLoggedIn());
        assertNull(session.getCurrentUser());
    }
}
