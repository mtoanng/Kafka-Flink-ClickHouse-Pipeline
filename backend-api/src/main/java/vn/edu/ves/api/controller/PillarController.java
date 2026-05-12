package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ves.api.dao.PillarDao;
import vn.edu.ves.api.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/pillars")
@Tag(name = "Pillars", description = "4 dashboards cho 4 pillars an ninh năng lượng")
@SecurityRequirement(name = "bearerAuth")
public class PillarController {

    private final PillarDao dao;

    public PillarController(PillarDao dao) {
        this.dao = dao;
    }

    @GetMapping("/1/outlook")
    @Operation(summary = "Pillar 1 — Supply Outlook (stock days + critical forecast)")
    public List<Pillar1OutlookDto> pillar1() {
        return dao.pillar1Outlook();
    }

    @GetMapping("/2/volatility")
    @Operation(summary = "Pillar 2 — Price Volatility Signal (rolling σ)")
    public List<Pillar2VolatilityDto> pillar2() {
        return dao.pillar2Volatility();
    }

    @GetMapping("/3/shedding-plan")
    @Operation(summary = "Pillar 3 — Load Shedding Plan khi load ≥ 85%")
    public List<Pillar3SheddingDto> pillar3() {
        return dao.pillar3Shedding();
    }

    @GetMapping("/4/net-zero")
    @Operation(summary = "Pillar 4 — Renewable share vs roadmap 2026/2030")
    public List<Pillar4NetZeroDto> pillar4() {
        return dao.pillar4NetZero();
    }
}
