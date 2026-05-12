package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ves.api.dao.SecurityDao;
import vn.edu.ves.api.dto.CascadeRiskDto;
import vn.edu.ves.api.dto.SecurityScoreDto;
import vn.edu.ves.api.exception.ApiException;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@Tag(name = "Security", description = "Cross-pillar Energy Security Index & cascade risks")
@SecurityRequirement(name = "bearerAuth")
public class SecurityController {

    private final SecurityDao dao;

    public SecurityController(SecurityDao dao) {
        this.dao = dao;
    }

    @GetMapping("/score")
    @Operation(summary = "Light ESI 0-100 (weighted 25%/pillar) + status SECURE/STABLE/AT_RISK/CRITICAL")
    public SecurityScoreDto score() {
        return dao.score().orElseThrow(() -> ApiException.notFound("v_security_score chưa có dữ liệu"));
    }

    @GetMapping("/cascade-risks")
    @Operation(summary = "Cảnh báo cascade đa-pillar (FUEL_SHORTAGE_RISK, CARBON_COST_RISK, …)")
    public List<CascadeRiskDto> cascadeRisks() {
        return dao.cascadeRisks();
    }
}
