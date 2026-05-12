package vn.edu.ves.desktop.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.dao.UserDao;
import vn.edu.ves.desktop.model.Role;
import vn.edu.ves.desktop.model.User;
import vn.edu.ves.desktop.util.PasswordUtil;
import vn.edu.ves.desktop.util.SessionManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserDao dao;
    private SessionManager session;
    private UserService service;

    @Before
    public void setUp() {
        dao = mock(UserDao.class);
        session = SessionManager.getInstance();
        session.clear();
        service = new UserServiceImpl(dao, session);
    }

    @After
    public void tearDown() {
        session.clear();
    }

    @Test
    public void save_withNewPassword_hashesBeforeDao() {
        User input = new User();
        input.setUsername(" alice ");
        input.setRole(Role.MANAGER);
        User dbReturn = new User();
        dbReturn.setId(10L);
        dbReturn.setUsername("alice");
        dbReturn.setRole(Role.MANAGER);
        when(dao.save(any(User.class), any(String.class))).thenAnswer(inv -> {
            String hash = inv.getArgument(1);
            assertNotNull("Hash phải non-null khi password mới được set", hash);
            assertTrue("Hash phải bắt đầu $2 (BCrypt)", hash.startsWith("$2"));
            assertTrue(PasswordUtil.verify("secretA1", hash));
            return dbReturn;
        });

        User saved = service.save(input, "secretA1");
        assertNotNull(saved);
        assertEquals("alice", input.getUsername());
    }

    @Test
    public void save_withoutPassword_passesNullToDao() {
        User input = new User();
        input.setUsername("bob");
        input.setId(20L);
        input.setRole(Role.VIEWER);
        when(dao.save(any(User.class), isNull())).thenReturn(input);

        User saved = service.save(input, "");
        assertEquals("bob", saved.getUsername());
        verify(dao, times(1)).save(eq(input), isNull());
    }

    @Test
    public void delete_nonAdmin_rejected() {
        User viewer = new User();
        viewer.setId(99L);
        viewer.setUsername("viewer");
        viewer.setRole(Role.VIEWER);
        session.setCurrentUser(viewer);

        boolean result = service.delete(50L);
        assertFalse("VIEWER không được phép xóa user", result);
        verify(dao, never()).delete(anyLong());
    }

    @Test
    public void delete_admin_canDeleteOthers() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        session.setCurrentUser(admin);
        when(dao.delete(50L)).thenReturn(true);

        boolean result = service.delete(50L);
        assertTrue(result);
        verify(dao, times(1)).delete(50L);
    }

    @Test
    public void delete_admin_cannotDeleteSelf() {
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);
        session.setCurrentUser(admin);

        boolean result = service.delete(1L);
        assertFalse("Không cho admin tự xóa chính mình (tránh khóa hệ thống)", result);
        verify(dao, never()).delete(1L);
    }
}
