package vn.edu.ves.desktop.service;

import vn.edu.ves.desktop.dao.RegionDao;
import vn.edu.ves.desktop.model.Region;

import java.util.List;
import java.util.Optional;

public class RegionServiceImpl implements RegionService {

    private final RegionDao regionDao;

    public RegionServiceImpl() {
        this(new RegionDao());
    }

    public RegionServiceImpl(RegionDao regionDao) {
        this.regionDao = regionDao;
    }

    @Override
    public List<Region> findAll() {
        return regionDao.findAll();
    }

    @Override
    public Optional<Region> findById(long id) {
        return regionDao.findById(id);
    }

    @Override
    public Optional<Region> findByCode(String code) {
        return regionDao.findByCode(code);
    }

    @Override
    public Region save(Region region) {
        if (region == null) {
            throw new IllegalArgumentException("region null");
        }
        if (region.getCode() != null) {
            region.setCode(region.getCode().trim().toUpperCase());
        }
        return regionDao.save(region);
    }

    @Override
    public boolean delete(long id) {
        return regionDao.delete(id);
    }
}
