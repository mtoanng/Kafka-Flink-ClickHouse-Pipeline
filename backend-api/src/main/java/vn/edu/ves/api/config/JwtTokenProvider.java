package vn.edu.ves.api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Sinh + parse JWT HS256.
 *
 * Claims tối thiểu:
 *   sub = username
 *   role = USER role  ("ADMIN" | "MANAGER" | "VIEWER")
 *   uid = user.id
 *
 * KHÔNG dùng SecurityContextHolder ở đây — JwtAuthFilter sẽ build
 * Authentication object từ claims sau khi token được verify.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(@Value("${ves.security.jwt.secret}") String secret,
                            @Value("${ves.security.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(long userId, String username, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim("uid", userId)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Trả về null nếu token sai/expired thay vì throw — dễ filter. */
    public Claims parseSilently(String token) {
        try {
            return parse(token);
        } catch (Exception ex) {
            log.debug("JWT parse fail: {}", ex.getMessage());
            return null;
        }
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
