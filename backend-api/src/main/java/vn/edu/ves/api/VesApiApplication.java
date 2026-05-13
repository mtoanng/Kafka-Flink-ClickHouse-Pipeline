package vn.edu.ves.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VES-Monitor Backend REST API — Spring Boot 2.7 / Java 11.
 *
 * <p>13 canonical endpoints (post Phase 7.6 IEA/APERC migration). Each
 * pillar endpoint additionally exposes its Phase 2.5/2.6 legacy alias for
 * backward-compat (see {@link vn.edu.ves.api.controller.PillarController}).</p>
 *
 * <pre>
 *   POST  /api/auth/login                       Đăng nhập → JWT (public)
 *   GET   /api/auth/me                          User hiện tại
 *
 *   GET   /api/pillars/1/supply-security        Pillar 1 — IDR / SFRI / HHI / N-1
 *   GET   /api/pillars/2/market-resilience      Pillar 2 — σ30d / price_gap / β_crude / affordability
 *   GET   /api/pillars/3/grid-reliability       Pillar 3 — reserve margin / peak factor / shed prob / freq stability
 *   GET   /api/pillars/4/energy-transition      Pillar 4 — renewable% / CO2 intensity / curtailment / netzero progress
 *
 *   GET   /api/security/score                   Composite ESI 0-100 (IEA weights 0.30/0.20/0.30/0.20)
 *   GET   /api/security/cascade-risks           [DEPRECATED] returns empty array (view dropped Phase 7.1)
 *
 *   GET   /api/recommendations                  Active recommendations (PENDING, not expired)
 *   POST  /api/recommendations/{id}/acknowledge ACK / DISMISS + audit (user + note)
 *
 *   GET   /api/alerts/active                    Cảnh báo chưa acknowledge (multi-pillar)
 *   GET   /api/fuel-prices/latest               Pillar 2 — giá nhiên liệu mới nhất
 *   GET   /api/grid-load/latest                 Pillar 3 — phụ tải mới nhất per region
 *
 *   GET   /api/health                           Self-check (public, không cần JWT)
 * </pre>
 */
@SpringBootApplication
public class VesApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(VesApiApplication.class, args);
    }
}
