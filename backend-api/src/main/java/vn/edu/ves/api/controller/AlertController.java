package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ves.api.dao.AlertDao;
import vn.edu.ves.api.dto.AlertDto;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Alerts", description = "Cảnh báo rule-based đa pillar (chưa acknowledge)")
@SecurityRequirement(name = "bearerAuth")
public class AlertController {

    private final AlertDao dao;

    public AlertController(AlertDao dao) {
        this.dao = dao;
    }

    @GetMapping("/active")
    @Operation(summary = "Top N cảnh báo đang hoạt động, sort theo thời gian giảm dần")
    public List<AlertDto> active(@RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return dao.active(safeLimit);
    }
}
