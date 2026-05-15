package vn.edu.ves.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Security config — stateless JWT, no session.
 *
 * - Open: /api/auth/login, /api/health, /v3/api-docs/**, /swagger-ui/**, /swagger-ui.html, OPTIONS preflight
 * - Còn lại: yêu cầu JWT hợp lệ.
 * - CORS: allowedOrigins "*" cho dev (Android emulator + JavaFX desktop từ máy khác).
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers(
                            "/api/auth/login",
                            "/api/health",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/actuator/**").permitAll()
                    .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                    .authenticationEntryPoint(authenticationEntryPoint);
        return http.build();
    }

    /**
     * Auth entry point — emits a strict-JSON 401 body. Uses Jackson rather than manual
     * string concatenation so that exception messages containing quotes, backslashes,
     * or control characters cannot break the JSON shape (or, in the worst case, leak
     * raw user input into a Content-Type=application/json response).
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper mapper) {
        return (req, res, ex) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding("UTF-8");
            Map<String, String> body = new LinkedHashMap<>();
            body.put("error", "UNAUTHORIZED");
            body.put("message", ex.getMessage() == null ? "Unauthorized" : ex.getMessage());
            mapper.writeValue(res.getWriter(), body);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 10 rounds — matches existing seed data ($2b$10$...)
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
