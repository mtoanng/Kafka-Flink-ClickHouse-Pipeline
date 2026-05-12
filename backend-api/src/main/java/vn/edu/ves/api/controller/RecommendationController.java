package vn.edu.ves.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.ves.api.dao.RecommendationDao;
import vn.edu.ves.api.dto.AckRequest;
import vn.edu.ves.api.dto.RecommendationDto;
import vn.edu.ves.api.exception.ApiException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recommendations", description = "Gợi ý hành động + acknowledge audit trail")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACKNOWLEDGED", "DISMISSED");

    private final RecommendationDao dao;

    public RecommendationController(RecommendationDao dao) {
        this.dao = dao;
    }

    @GetMapping
    @Operation(summary = "Danh sách recommendation đang hoạt động (status=PENDING & chưa expired)")
    public List<RecommendationDto> list(@RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return dao.active(safeLimit);
    }

    @PostMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge (mặc định) hoặc Dismiss một recommendation")
    public Map<String, Object> acknowledge(@PathVariable("id") long id,
                                            @RequestBody(required = false) AckRequest req) {
        String status = req == null || req.getStatus() == null ? "ACKNOWLEDGED" : req.getStatus().toUpperCase();
        if (!ALLOWED_STATUSES.contains(status)) {
            throw ApiException.badRequest("status phải là ACKNOWLEDGED hoặc DISMISSED");
        }
        String note = req == null ? null : req.getNote();

        Long userId = currentUserId();
        int updated = dao.acknowledge(id, userId, status, note);
        if (updated == 0) {
            throw ApiException.notFound("Recommendation #" + id + " không tồn tại hoặc đã được xử lý trước đó");
        }
        return Map.of(
                "id", id,
                "newStatus", status,
                "acknowledgedBy", userId);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getDetails() == null) {
            throw ApiException.unauthorized("Thiếu user context");
        }
        return (Long) auth.getDetails();
    }
}
