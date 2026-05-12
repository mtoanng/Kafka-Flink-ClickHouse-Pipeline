package vn.edu.ves.desktop.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * POJO bảng <code>regions</code> (02_init_users_regions.sql).
 *
 * <pre>
 *   id BIGSERIAL          → {@link #id}
 *   code VARCHAR(20)      → {@link #code}      (unique, vd VN_NORTH)
 *   name VARCHAR(100)     → {@link #name}
 *   vn_zone VARCHAR(20)   → {@link #vnZone}    (BAC/TRUNG/NAM/NULL)
 *   country_code VARCHAR  → {@link #countryCode} (default VN)
 *   description TEXT      → {@link #description}
 *   created_at TIMESTAMP  → {@link #createdAt}
 * </pre>
 */
public class Region {

    private long id;
    private String code;
    private String name;
    private String vnZone;
    private String countryCode = "VN";
    private String description;
    private LocalDateTime createdAt;

    public Region() {
    }

    public Region(long id, String code, String name, String vnZone,
                  String countryCode, String description, LocalDateTime createdAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.vnZone = vnZone;
        this.countryCode = countryCode == null ? "VN" : countryCode;
        this.description = description;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVnZone() { return vnZone; }
    public void setVnZone(String vnZone) { this.vnZone = vnZone; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;
        Region r = (Region) o;
        return id == r.id && Objects.equals(code, r.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }

    @Override
    public String toString() {
        return "Region{id=" + id + ", code='" + code + "', name='" + name + "'}";
    }
}
