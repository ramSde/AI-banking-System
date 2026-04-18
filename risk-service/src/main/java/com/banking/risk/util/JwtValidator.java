package com.banking.risk.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Utility class for JWT token validation.
 * Validates JWT tokens using RSA public key.
 */
@Component
public class JwtValidator {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);

    private final PublicKey publicKey;
    private final String issuer;

    public JwtValidator(
            @Value("${jwt.public-key-path}") String publicKeyPath,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.publicKey = loadPublicKey(publicKeyPath);
        this.issuer = issuer;
    }

    /**
     * Validate JWT token and extract claims.
     *
     * @param token JWT token
     * @return Claims if valid
     * @throws RuntimeException if token is invalid
     */
    public Claims validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Check expiration
            if (claims.getExpiration().before(new Date())) {
                throw new RuntimeException("Token has expired");
            }

            return claims;
        } catch (Exception e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Extract user ID from JWT token.
     *
     * @param token JWT token
     * @return User ID
     */
    public String extractUserId(String token) {
        Claims claims = validateToken(token);
        return claims.getSubject();
    }

    /**
     * Extract role from JWT token.
     *
     * @param token JWT token
     * @return User role
     */
    public String extractRole(String token) {
        Claims claims = validateToken(token);
        return claims.get("role", String.class);
    }

    /**
     * Load RSA public key from file.
     */
    private PublicKey loadPublicKey(String path) {
        try {
            String key = new String(Files.readAllBytes(Paths.get(path)))
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            logger.error("Failed to load public key from {}: {}", path, e.getMessage());
            throw new RuntimeException("Failed to load public key", e);
        }
    }
}
