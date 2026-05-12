package vn.edu.ves.desktop.dao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import vn.edu.ves.desktop.model.Region;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * H2 in-memory tests cho {@link RegionDao} — full CRUD lifecycle.
 */
public class RegionDaoTest {

    private RegionDao dao;

    @Before
    public void setUp() throws Exception {
        H2TestSupport.overrideSingletonToH2();
        try (Connection c = H2TestSupport.openConnection()) {
            H2TestSupport.exec(c, "DROP TABLE IF EXISTS regions");
            H2TestSupport.exec(c,
                    "CREATE TABLE regions (" +
                    " id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    " code VARCHAR(20) NOT NULL UNIQUE," +
                    " name VARCHAR(100) NOT NULL," +
                    " vn_zone VARCHAR(20)," +
                    " country_code VARCHAR(3) NOT NULL DEFAULT 'VN'," +
                    " description CLOB," +
                    " created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        }
        dao = new RegionDao();
    }

    @After
    public void tearDown() throws Exception {
        try (Connection c = H2TestSupport.openConnection()) {
            H2TestSupport.exec(c, "DROP TABLE IF EXISTS regions");
        }
    }

    @Test
    public void insert_assignsIdAndIsFindable() {
        Region saved = dao.save(newRegion("VN_NORTH", "Miền Bắc", "BAC", "VN", "Hà Nội..."));
        assertNotNull(saved);
        assertTrue("ID phải > 0 sau insert", saved.getId() > 0);

        Optional<Region> fetched = dao.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals("VN_NORTH", fetched.get().getCode());
        assertEquals("BAC", fetched.get().getVnZone());
    }

    @Test
    public void findByCode_returnsExpected() {
        dao.save(newRegion("VN_SOUTH", "Miền Nam", "NAM", "VN", null));
        Optional<Region> r = dao.findByCode("VN_SOUTH");
        assertTrue(r.isPresent());
        assertEquals("Miền Nam", r.get().getName());
    }

    @Test
    public void findByCode_unknown_returnsEmpty() {
        assertFalse(dao.findByCode("UNKNOWN_REGION").isPresent());
    }

    @Test
    public void update_changesPersistedFields() {
        Region saved = dao.save(newRegion("INTL_NA", "North America", null, "US", "NYMEX"));
        long id = saved.getId();
        saved.setName("North America (NYMEX/WTI)");
        saved.setDescription("Updated description");
        Region updated = dao.save(saved);
        assertNotNull(updated);
        Region fetched = dao.findById(id).orElse(null);
        assertNotNull(fetched);
        assertEquals("North America (NYMEX/WTI)", fetched.getName());
        assertEquals("Updated description", fetched.getDescription());
        assertNull("vn_zone vẫn null sau update", fetched.getVnZone());
    }

    @Test
    public void delete_removesRow() {
        Region saved = dao.save(newRegion("DEL_ME", "tmp", null, "VN", null));
        long id = saved.getId();
        assertTrue(dao.delete(id));
        assertFalse(dao.findById(id).isPresent());
    }

    @Test
    public void findAll_returnsAllRowsInsertedSoFar() {
        dao.save(newRegion("R1", "R1 Name", null, "VN", null));
        dao.save(newRegion("R2", "R2 Name", null, "VN", null));
        dao.save(newRegion("R3", "R3 Name", null, "VN", null));
        List<Region> all = dao.findAll();
        assertEquals(3, all.size());
    }

    private Region newRegion(String code, String name, String zone, String country, String desc) {
        Region r = new Region();
        r.setCode(code);
        r.setName(name);
        r.setVnZone(zone);
        r.setCountryCode(country);
        r.setDescription(desc);
        return r;
    }
}
