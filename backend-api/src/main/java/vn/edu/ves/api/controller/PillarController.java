package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ves.api.dao.PillarDao;
import vn.edu.ves.api.dto.Pillar1SupplySecurityDto;
import vn.edu.ves.api.dto.Pillar2MarketResilienceDto;
import vn.edu.ves.api.dto.Pillar3GridReliabilityDto;
import vn.edu.ves.api.dto.Pillar4EnergyTransitionDto;

import java.util.List;

/**
 * 4 pillar dashboards (Phase 7.1 IEA/APERC framework).
 *
 * <p>Mỗi endpoint expose cả tên cũ (Phase 2.5/2.6) lẫn tên mới (Phase 7.1)
 * để client đời cũ không bị break:</p>
 * <ul>
 *   <li>P1: {@code /1/outlook}        + {@code /1/supply-security}</li>
 *   <li>P2: {@code /2/volatility}     + {@code /2/market-resilience}</li>
 *   <li>P3: {@code /3/shedding}       + {@code /3/shedding-plan} + {@code /3/grid-reliability}</li>
 *   <li>P4: {@code /4/netzero}        + {@code /4/net-zero} + {@code /4/energy-transition}</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/pillars")
@Tag(name = "Pillars", description = "4 dashboards cho 4 pillars an ninh năng lượng (IEA/APERC)")
@SecurityRequirement(name = "bearerAuth")
public class PillarController {

    private final PillarDao dao;

    public PillarController(PillarDao dao) {
        this.dao = dao;
    }

    @GetMapping({"/1/supply-security", "/1/outlook"})
    @Operation(summary = "Pillar 1 — Supply Security (IDR / SFRI / HHI / N-1 resilience)")
    public List<Pillar1SupplySecurityDto> pillar1() {
        return dao.findPillar1SupplySecurity();
    }

    @GetMapping({"/2/market-resilience", "/2/volatility"})
    @Operation(summary = "Pillar 2 — Market Resilience (σ30d / price gap / β crude / affordability)")
    public List<Pillar2MarketResilienceDto> pillar2() {
        return dao.findPillar2MarketResilience();
    }

    @GetMapping({"/3/grid-reliability", "/3/shedding", "/3/shedding-plan"})
    @Operation(summary = "Pillar 3 — Grid Reliability (reserve margin / peak load / shedding prob / freq stability)")
    public List<Pillar3GridReliabilityDto> pillar3() {
        return dao.findPillar3GridReliability();
    }

    @GetMapping({"/4/energy-transition", "/4/netzero", "/4/net-zero"})
    @Operation(summary = "Pillar 4 — Energy Transition (renewable % / CO2 intensity / curtailment / netzero progress)")
    public List<Pillar4EnergyTransitionDto> pillar4() {
        return dao.findPillar4EnergyTransition();
    }
}
