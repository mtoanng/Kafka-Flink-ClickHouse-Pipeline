package vn.edu.ves.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger UI: http://localhost:8090/swagger-ui.html
 * - Khai báo Bearer JWT để UI có nút "Authorize".
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI vesApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("VES-Monitor REST API")
                        .description("Vietnam Energy Security Real-time Monitor — 13 canonical endpoints (4 pillars + Security + alerts + recommendations + raw data + auth + health). Each pillar additionally exposes a Phase 2.5/2.6 legacy alias (see PillarController).")
                        .version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Lấy JWT qua POST /api/auth/login, bỏ vào header: Authorization: Bearer <token>")));
    }
}
