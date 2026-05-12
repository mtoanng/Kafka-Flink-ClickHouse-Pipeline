package vn.edu.ves.desktop.dao;

import vn.edu.ves.desktop.model.Region;
import vn.edu.ves.desktop.util.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO cho bảng <code>regions</code>.
 */
public class RegionDao extends BaseDao {

    private final DatabaseConfig dbConfig;

    public RegionDao() {
        this(DatabaseConfig.getInstance());
    }

    public RegionDao(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public List<Region> findAll() {
        final String sql = "SELECT id, code, name, vn_zone, country_code, description, created_at " +
                "FROM regions ORDER BY id";
        List<Region> out = new ArrayList<>();
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("findAll() lỗi: {}", e.getMessage(), e);
        }
        return out;
    }

    public Optional<Region> findById(long id) {
        return findBy("id = ?", id);
    }

    public Optional<Region> findByCode(String code) {
        return findBy("code = ?", code);
    }

    private Optional<Region> findBy(String whereClause, Object param) {
        final String sql = "SELECT id, code, name, vn_zone, country_code, description, created_at " +
                "FROM regions WHERE " + whereClause;
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param instanceof Long) {
                ps.setLong(1, (Long) param);
            } else if (param instanceof String) {
                ps.setString(1, (String) param);
            } else {
                ps.setObject(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            log.error("findBy({}) lỗi: {}", whereClause, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /** Insert (id=0) hoặc Update. Trả về Region đã save (id được set khi insert). */
    public Region save(Region region) {
        if (region == null) {
            throw new IllegalArgumentException("region null");
        }
        return region.getId() == 0 ? insert(region) : update(region);
    }

    private Region insert(Region r) {
        final String sql = "INSERT INTO regions (code, name, vn_zone, country_code, description) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getCode());
            ps.setString(2, r.getName());
            setStringOrNull(ps, 3, r.getVnZone());
            ps.setString(4, r.getCountryCode() == null ? "VN" : r.getCountryCode());
            setStringOrNull(ps, 5, r.getDescription());
            int n = ps.executeUpdate();
            if (n == 0) return null;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setId(keys.getLong(1));
            }
            return r;
        } catch (SQLException e) {
            log.error("insert region({}) lỗi: {}", r.getCode(), e.getMessage(), e);
            return null;
        }
    }

    private Region update(Region r) {
        final String sql = "UPDATE regions SET code = ?, name = ?, vn_zone = ?, country_code = ?, " +
                "description = ? WHERE id = ?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getCode());
            ps.setString(2, r.getName());
            setStringOrNull(ps, 3, r.getVnZone());
            ps.setString(4, r.getCountryCode() == null ? "VN" : r.getCountryCode());
            setStringOrNull(ps, 5, r.getDescription());
            ps.setLong(6, r.getId());
            int n = ps.executeUpdate();
            return n > 0 ? r : null;
        } catch (SQLException e) {
            log.error("update region(id={}) lỗi: {}", r.getId(), e.getMessage(), e);
            return null;
        }
    }

    public boolean delete(long id) {
        final String sql = "DELETE FROM regions WHERE id = ?";
        try (Connection c = dbConfig.openConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("delete region(id={}) lỗi: {}", id, e.getMessage(), e);
            return false;
        }
    }

    private Region mapRow(ResultSet rs) throws SQLException {
        Region r = new Region();
        r.setId(rs.getLong("id"));
        r.setCode(rs.getString("code"));
        r.setName(getStringOrNull(rs, "name"));
        r.setVnZone(getStringOrNull(rs, "vn_zone"));
        r.setCountryCode(getStringOrNull(rs, "country_code"));
        r.setDescription(getStringOrNull(rs, "description"));
        r.setCreatedAt(getLocalDateTimeOrNull(rs, "created_at"));
        return r;
    }
}
