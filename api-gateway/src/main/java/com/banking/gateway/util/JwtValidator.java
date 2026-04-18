package com.banking.gateway.util;

import com.banking.gateway.config.GatewayProperties;
import com.banking.gateway.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * JWT token validation utility for the API Gateway.
 * 
 * This component provides:
 * - JWT signature verification using RSA public key
 * - Token expiration validation
 * - Issuer and audience claim validation
 * - Role and permission extraction
 * - Comprehensive error handling with specific exception types
 * 
 * Security Features:
 * - RSA-256 signature verification
 * - Clock skew tolerance for distributed systems
 * - Comprehensive validation of all JWT claims
 * - Secure error handling (no token content in logs)
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final GatewayProperties gatewayProperties;
    private volatile PublicKey publicKey;

    /**
     * Validate JWT token and extract claims.
     * 
     * Validation Steps:
     * 1. Parse and verify JWT signature using RSA public key
     * 2. Check token expiration with clock skew tolerance
     * 3. Validate issuer claim matches expected value
     * 4. Validate audience claim contains expected value
     * 5. Extract user ID, roles, and permissions
     * 
     * @param token JWT token string (without "Bearer " prefix)
     * @return Claims object with validated token claims
     * @throws InvalidTokenException if token is invalid, expired, or malformed
     */
    public Claims validateToken(String token) {
        try {
            // Get or initialize public key
            PublicKey key = getPublicKey();

            // Parse and validate JWT
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(gatewayProperties.getJwt().getIssuer())
                    .setAllowedClockSkewSeconds(30) // 30 seconds tolerance for distributed systems
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Validate audience claim
            validateAudience(claims);

            // Log successful validation (without token content)
            log.debug("JWT token validated successfully for user: {}", claims.getSubject());

            return claims;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired at: {}", e.getClaims().getExpiration());
            throw new InvalidTokenException("Token has expired", e);
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token received");
            throw new InvalidTokenException("Token is malformed", e);
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed");
            throw new InvalidTokenException("Token signature is invalid", e);
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token format");
            throw new InvalidTokenException("Token format is not supported", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT token is null or empty");
            throw new InvalidTokenException("Token is null or empty", e);
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation", e);
            throw new InvalidTokenException("Token validation failed", e);
        }
    }

    /**
     * Extract user ID from JWT claims.
     * 
     * @param claims JWT claims
     * @return User ID from subject claim
     */
    public String extractUserId(Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extract user roles from JWT claims.
     * 
     * @param claims JWT claims
     * @return List of user roles
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?>) {
            return (List<String>) roles;
        }
        return List.of();
    }

    /**
     * Extract user permissions from JWT claims.
     * 
     * @param claims JWT claims
     * @return List of user permissions
     */
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(Claims claims) {
        Object permissions = claims.get("permissions");
        if (permissions instanceof List<?>) {
            return (List<String>) permissions;
        }
        return List.of();
    }

    /**
     * Check if token is expired.
     * 
     * @param claims JWT claims
     * @return true if token is expired
     */
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(java.util.Date.from(Instant.now()));
    }

    /**
     * Get or initialize RSA public key for JWT signature verification.
     * 
     * The public key is loaded from configuration and cached for performance.
     * In production, this key is injected from Kubernetes Secret or Vault.
     * 
     * @return RSA public key for signature verification
     * @throws InvalidTokenException if public key cannot be loaded
     */
    private PublicKey getPublicKey() {
        if (publicKey == null) {
            synchronized (this) {
                if (publicKey == null) {
                    try {
                        String publicKeyPem = gatewayProperties.getJwt().getPublicKey()
                                .replace("-----BEGIN PUBLIC KEY-----", "")
                                .replace("-----END PUBLIC KEY-----", "")
                                .replaceAll("\\s", "");

                        byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);
                        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        publicKey = keyFactory.generatePublic(keySpec);

                        log.info("RSA public key loaded successfully for JWT validation");
                    } catch (Exception e) {
                        log.error("Failed to load RSA public key for JWT validation", e);
                        throw new InvalidTokenException("Failed to initialize JWT validator", e);
                    }
                }
            }
        }
        return publicKey;
    }

    /**
     * Validate JWT audience claim.
     * 
     * The token must contain at least one of the expected audience values.
     * This ensures the token was issued for this specific API.
     * 
     * @param claims JWT claims
     * @throws InvalidTokenException if audience validation fails
     */
    private void validateAudience(Claims claims) {
        Object audienceClaim = claims.get("aud");
        List<String> expectedAudiences = gatewayProperties.getJwt().getAudience();

        if (audienceClaim == null) {
            throw new InvalidTokenException("Token missing audience claim");
        }

        List<String> tokenAudiences;
        if (audienceClaim instanceof String) {
            tokenAudiences = List.of((String) audienceClaim);
        } else if (audienceClaim instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<String> audienceList = (List<String>) audienceClaim;
            tokenAudiences = audienceList;
        } else {
            throw new InvalidTokenException("Invalid audience claim format");
        }

        boolean validAudience = tokenAudiences.stream()
                .anyMatch(expectedAudiences::contains);

        if (!validAudience) {
            log.warn("JWT audience validation failed. Expected: {}, Found: {}", 
                    expectedAudiences, tokenAudiences);
            throw new InvalidTokenException("Token audience is invalid");
        }
    }
}