package vn.edu.ves.desktop.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.dao.UserDao;
import vn.edu.ves.desktop.exception.AuthenticationException;
import vn.edu.ves.desktop.model.Role;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.util.PasswordUtil;
import vn.edu.ves.desktop.util.SessionManager;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests cho {@link AuthServiceImpl} với Mockito mock {@link UserDao}.
 */
public class AuthServiceTest {

    private UserDao userDao;
    private SessionManager session;
    private AuthService auth;

    @Before
    public void setUp() {
        userDao = mock(UserDao.class);
        session = SessionManager.getInstance();
        session.clear();
        auth = new AuthServiceImpl(userDao, session);
    }

    @After
    public void tearDown() {
        session.clear();
    }

    @Test
    public void login_validCredentials_returnsUserAndSetsSession() {
        String plain = "demoPass123";
        User stored = makeUser(10L, "demoAdmin", Role.ADMIN, true, PasswordUtil.hash(plain));
        when(userDao.findByUsername("demoAdmin")).thenReturn(Optional.of(stored));
        when(userDao.updateLastLogin(10L)).thenReturn(true);

        User result = auth.login("demoAdmin", plain);

        assertNotNull(result);
        assertEquals("demoAdmin", result.getUsername());
        assertEquals(Role.ADMIN, result.getRole());
        assertTrue(session.isLoggedIn());
        assertEquals(stored, session.getCurrentUser());
        verify(userDao, times(1)).updateLastLogin(10L);
    }

    @Test
    public void login_wrongPassword_throwsAndKeepsSessionEmpty() {
        User stored = makeUser(11L, "demo", Role.VIEWER, true, PasswordUtil.hash("realSecret"));
        when(userDao.findByUsername("demo")).thenReturn(Optional.of(stored));

        try {
            auth.login("demo", "wrongSecret");
            fail("Phải ném AuthenticationException khi sai password");
        } catch (AuthenticationException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("sai"));
        }
        assertFalse(session.isLoggedIn());
        verify(userDao, never()).updateLastLogin(anyLong());
    }

    @Test
    public void login_userNotFound_throwsGenericMessage() {
        when(userDao.findByUsername("ghost")).thenReturn(Optional.empty());
        try {
            auth.login("ghost", "anything");
            fail("Phải ném AuthenticationException khi user không tồn tại");
        } catch (AuthenticationException ex) {
            assertNotNull(ex.getMessage());
        }
        assertFalse(session.isLoggedIn());
        verify(userDao, never()).updateLastLogin(anyLong());
    }

    @Test
    public void login_userDisabled_throwsLockedMessage() {
        User stored = makeUser(12L, "disabled", Role.VIEWER, false, PasswordUtil.hash("p"));
        when(userDao.findByUsername("disabled")).thenReturn(Optional.of(stored));
        try {
            auth.login("disabled", "p");
            fail("User bị disable phải ném exception");
        } catch (AuthenticationException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("khóa")
                    || ex.getMessage().toLowerCase().contains("disable"));
        }
        assertFalse(session.isLoggedIn());
    }

    @Test
    public void login_emptyInput_throwsImmediately() {
        try {
            auth.login("", "");
            fail("Empty input phải throw ngay");
        } catch (AuthenticationException ex) {
            // expected
        }
        verify(userDao, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    public void logout_clearsSession() {
        User stored = makeUser(20L, "manager", Role.MANAGER, true, PasswordUtil.hash("m"));
        when(userDao.findByUsername("manager")).thenReturn(Optional.of(stored));
        when(userDao.updateLastLogin(20L)).thenReturn(true);
        auth.login("manager", "m");
        assertTrue(session.isLoggedIn());

        auth.logout();
        assertFalse(session.isLoggedIn());
        assertNull(auth.getCurrentUser());
    }

    private User makeUser(long id, String username, Role role, boolean enabled, String hash) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setRole(role);
        u.setEnabled(enabled);
        u.setPasswordHash(hash);
        u.setFullName(username + " full");
        return u;
    }
}
