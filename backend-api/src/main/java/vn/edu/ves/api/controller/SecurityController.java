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
@Tag(name = "Security", description = "Cross-pillar Energy Security Index (Phase 7.1 IEA weights)")
@SecurityRequirement(name = "bearerAuth")
public class SecurityController {

    private final SecurityDao dao;

    public SecurityController(SecurityDao dao) {
        this.dao = dao;
    }

    @GetMapping("/score")
    @Operation(summary = "Composite ESI 0-100 (IEA weights 0.30/0.20/0.30/0.20) + status SECURE/ELEVATED/STRESSED/CRITICAL")
    public SecurityScoreDto score() {
        return dao.score().orElseThrow(() -> ApiException.notFound("v_security_score chưa có dữ liệu"));
    }

    /**
     * Backward-compat endpoint. View {@code v_cascade_risks} đã bị drop ở Phase 7.1.
     * Endpoint vẫn trả 200 với mảng rỗng để client cũ (JavaFX desktop, Android app)
     * không vỡ trong giai đoạn migration. Cascade analysis sẽ được re-implement
     * ở phase sau bằng cách correlate alerts đa-pillar.
     */
    @GetMapping("/cascade-risks")
    @Operation(summary = "[DEPRECATED] Cascade risks — view dropped in Phase 7.1, returns empty array")
    public List<CascadeRiskDto> cascadeRisks() {
        return dao.cascadeRisks();
    }
}
