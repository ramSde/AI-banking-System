package com.banking.fraud.util;

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

/**
 * JWT Validator Utility
 * 
 * Validates and extracts claims from JWT tokens.
 */
@Component
public class JwtValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtValidator.class);

    private final SecretKey secretKey;

    public JwtValidator(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validate JWT token
     * 
     * @param token JWT token
     * @return True if valid
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !isTokenExpired(claims);
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user ID from token
     * 
     * @param token JWT token
     * @return User ID
     */
    public String extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    /**
     * Extract roles from token
     * 
     * @param token JWT token
     * @return List of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObj = claims.get("roles");
        
        if (rolesObj instanceof List<?>) {
            return (List<String>) rolesObj;
        }
        
        return List.of();
    }

    /**
     * Extract all claims from token
     * 
     * @param token JWT token
     * @return Claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Check if token is expired
     * 
     * @param claims Token claims
     * @return True if expired
     */
    private boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}
