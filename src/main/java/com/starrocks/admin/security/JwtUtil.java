package com.starrocks.admin.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtUtil(@Value("${app.auth.jwt-secret}") String secret,
                   @Value("${app.auth.jwt-expires-in:24h}") String expiresIn) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = parseExpiration(expiresIn);
    }

    private static long parseExpiration(String expiresIn) {
        try {
            if (expiresIn.endsWith("h")) {
                return Duration.ofHours(Long.parseLong(expiresIn.substring(0, expiresIn.length() - 1))).toMillis();
            } else if (expiresIn.endsWith("d")) {
                return Duration.ofDays(Long.parseLong(expiresIn.substring(0, expiresIn.length() - 1))).toMillis();
            } else {
                return Duration.ofHours(24).toMillis();
            }
        } catch (NumberFormatException e) {
            return Duration.ofHours(24).toMillis();
        }
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }

    public Claims verifyToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", e.getMessage());
            throw new com.starrocks.admin.exception.ApiException(com.starrocks.admin.exception.ApiException.tokenExpired().getCode(), "Token expired");
        } catch (JwtException e) {
            log.warn("Token verification failed: {}", e.getMessage());
            throw new com.starrocks.admin.exception.ApiException(com.starrocks.admin.exception.ApiException.unauthorized("Invalid token").getCode(), "Invalid token");
        }
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }
}
