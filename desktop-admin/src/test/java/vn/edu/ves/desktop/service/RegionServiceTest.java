package vn.edu.ves.desktop.service;

import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.dao.RegionDao;
import vn.edu.ves.desktop.model.Region;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests cho {@link RegionServiceImpl}. Verify code chuẩn hóa UPPERCASE + delegation.
 */
public class RegionServiceTest {

    private RegionDao dao;
    private RegionService service;

    @Before
    public void setUp() {
        dao = mock(RegionDao.class);
        service = new RegionServiceImpl(dao);
    }

    @Test
    public void findAll_delegates() {
        when(dao.findAll()).thenReturn(Arrays.asList(new Region(), new Region()));
        List<Region> result = service.findAll();
        assertEquals(2, result.size());
        verify(dao, times(1)).findAll();
    }

    @Test
    public void save_normalizesCodeToUpperCase() {
        Region input = new Region();
        input.setCode("vn_north");
        input.setName("Test");
        Region returned = new Region();
        returned.setId(99);
        returned.setCode("VN_NORTH");
        when(dao.save(any(Region.class))).thenReturn(returned);

        Region saved = service.save(input);
        assertSame(returned, saved);
        assertEquals("Code đã được upper-case TRƯỚC khi gọi DAO", "VN_NORTH", input.getCode());
    }

    @Test
    public void findByCode_passesThrough() {
        Region r = new Region();
        r.setCode("VN_CENTRAL");
        when(dao.findByCode("VN_CENTRAL")).thenReturn(Optional.of(r));
        Optional<Region> got = service.findByCode("VN_CENTRAL");
        assertTrue(got.isPresent());
        assertEquals("VN_CENTRAL", got.get().getCode());
    }

    @Test
    public void delete_returnsDaoResult() {
        when(dao.delete(42L)).thenReturn(true);
        assertTrue(service.delete(42L));
        verify(dao).delete(42L);
    }
}
