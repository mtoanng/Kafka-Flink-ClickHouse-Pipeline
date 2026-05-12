package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ves.api.dao.PillarDao;
import vn.edu.ves.api.dto.FuelPriceDto;
import vn.edu.ves.api.dto.GridLoadLatestDto;

import java.util.List;

/**
 * Endpoint cho raw / latest data — phục vụ JavaFX chart "live" và Android list.
 */
@RestController
@Tag(name = "Raw data", description = "Fuel prices stream + grid load latest")
@SecurityRequirement(name = "bearerAuth")
public class RawDataController {

    private final PillarDao dao;

    public RawDataController(PillarDao dao) {
        this.dao = dao;
    }

    @GetMapping("/api/fuel-prices/latest")
    @Operation(summary = "Pillar 2 — N giá nhiên liệu mới nhất (tuỳ chọn lọc theo fuel_type)")
    public List<FuelPriceDto> latestFuel(@RequestParam(name = "fuel_type", required = false) String fuelType,
                                          @RequestParam(name = "limit", defaultValue = "20") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        return dao.latestFuelPrices(fuelType, safeLimit);
    }

    @GetMapping("/api/grid-load/latest")
    @Operation(summary = "Pillar 3 — phụ tải mới nhất theo region")
    public List<GridLoadLatestDto> latestGridLoad() {
        return dao.gridLoadLatest();
    }
}
