package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.model.Region;

import java.util.List;
import java.util.Optional;

public interface RegionService {

    List<Region> findAll();

    Optional<Region> findById(long id);

    Optional<Region> findByCode(String code);

    /** Insert hoặc update. Trả về region đã save (id set khi insert). */
    Region save(Region region);

    boolean delete(long id);
}
