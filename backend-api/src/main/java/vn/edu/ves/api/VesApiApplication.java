package vn.edu.ves.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VES-Monitor Backend REST API — Spring Boot 2.7 / Java 11.
 *
 * Endpoint groups (14 total):
 *   POST  /api/auth/login                       Đăng nhập → JWT
 *   GET   /api/auth/me                          User hiện tại
 *
 *   GET   /api/pillars/1/outlook                Pillar 1 — supply outlook + recommendation
 *   GET   /api/pillars/2/volatility             Pillar 2 — rolling σ + signal
 *   GET   /api/pillars/3/shedding-plan          Pillar 3 — load shedding priority
 *   GET   /api/pillars/4/net-zero               Pillar 4 — renewable share vs roadmap
 *
 *   GET   /api/security/score                   Energy Security Score 0-100 (Light ESI)
 *   GET   /api/security/cascade-risks           Compound risks multi-pillar
 *   GET   /api/recommendations?status=PENDING   Active recommendations
 *   POST  /api/recommendations/{id}/acknowledge ACK + audit (user + note)
 *
 *   GET   /api/alerts/active                    Cảnh báo chưa acknowledge
 *   GET   /api/fuel-prices/latest               Pillar 2 — giá nhiên liệu mới nhất
 *   GET   /api/grid-load/latest                 Pillar 3 — phụ tải mới nhất per region
 *
 *   GET   /api/health                           Self-check (không cần JWT)
 */
@SpringBootApplication
public class VesApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VesApiApplication.class, args);
    }
}
