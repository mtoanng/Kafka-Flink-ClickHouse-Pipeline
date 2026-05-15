package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public — không cần JWT. Dùng cho:
 *   - Healthcheck script ({@code scripts/healthcheck.sh})
 *   - Reverse tunnel (Cloudflare) probe
 *   - JavaFX/Android: ping trước khi prompt login
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Self-check (public)")
public class HealthController {

    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    @Operation(
            summary = "Luôn trả về 200 OK. Field `status` = `UP` nếu DB reachable, `DEGRADED` nếu không.",
            security = {})
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service",   "ves-backend-api");
        body.put("timestamp", OffsetDateTime.now().toString());
        try {
            Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
            body.put("db", one != null && one == 1 ? "UP" : "DEGRADED");
            body.put("status", "UP");
        } catch (Exception ex) {
            body.put("db", "DOWN");
            body.put("status", "DEGRADED");
            body.put("error", ex.getMessage());
        }
        return body;
    }
}
