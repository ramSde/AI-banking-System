package com.banking.insight.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtValidator {

    private final SecretKey secretKey;

    public JwtValidator(@Value("${jwt.secret}") final String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims validateToken(final String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isTokenValid(final String token) {
        try {
            final Claims claims = validateToken(token);
            return !isTokenExpired(claims);
        } catch (final Exception e) {
            return false;
        }
    }

    public UUID getUserIdFromToken(final String token) {
        final Claims claims = validateToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getUsernameFromToken(final String token) {
        final Claims claims = validateToken(token);
        return claims.get("username", String.class);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(final String token) {
        final Claims claims = validateToken(token);
        return claims.get("roles", List.class);
    }

    private boolean isTokenExpired(final Claims claims) {
        final Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }
}
