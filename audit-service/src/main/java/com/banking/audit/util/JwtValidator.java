package com.banking.audit.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtValidator {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);
    private final SecretKey secretKey;

    public JwtValidator(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public UUID extractUserId(Claims claims) {
        String userIdStr = claims.get("userId", String.class);
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    public List<String> extractRoles(Claims claims) {
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : List.of();
    }
}
