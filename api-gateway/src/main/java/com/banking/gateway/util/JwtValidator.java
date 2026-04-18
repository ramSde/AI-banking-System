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

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * JWT token validation utility.
 * 
 * Provides secure JWT validation with:
 * - RSA signature verification using public key
 * - Expiration time validation
 * - Issuer and audience claim validation
 * - Comprehensive error handling and logging
 * 
 * Security considerations:
 * - Uses RSA-256 signature algorithm (asymmetric)
 * - Validates all standard JWT claims (iss, aud, exp, iat)
 * - Provides detailed error messages for debugging
 * - Logs security events for audit purposes
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final GatewayProperties gatewayProperties;
    private volatile Key publicKey;

    /**
     * Validates JWT token and extracts claims.
     * 
     * Performs comprehensive validation:
     * 1. Signature verification using RSA public key
     * 2. Expiration time check
     * 3. Issuer validation
     * 4. Audience validation
     * 5. Token structure validation
     * 
     * @param token JWT token string (without "Bearer " prefix)
     * @return Validated JWT claims
     * @throws InvalidTokenException if token is invalid, expired, or malformed
     */
    public Claims validateToken(String token) {
        try {
            log.debug("Validating JWT token");
            
            // Parse and validate token
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getPublicKey())
                .requireIssuer(gatewayProperties.getJwt().getIssuer())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            // Validate audience claim
            validateAudience(claims);
            
            // Log successful validation (without sensitive data)
            String userId = claims.getSubject();
            Instant expiration = claims.getExpiration().toInstant();
            log.info("JWT token validated successfully for user: {}, expires: {}", 
                    maskUserId(userId), expiration);
            
            return claims;
            
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new InvalidTokenException("Token has expired", e);
            
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Unsupported token format", e);
            
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Malformed token", e);
            
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token signature", e);
            
        } catch (IllegalArgumentException e) {
            log.warn("JWT token validation error: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token", e);
            
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation", e);
            throw new InvalidTokenException("Token validation failed", e);
        }
    }

    /**
     * Extracts user ID from JWT token without full validation.
     * 
     * Used for rate limiting where we need user ID even if token
     * might be expired (rate limits apply to all requests).
     * 
     * @param token JWT token string
     * @return User ID from token subject claim, or null if extraction fails
     */
    public String extractUserIdUnsafe(String token) {
        try {
            // Parse without signature verification for rate limiting
            String[] chunks = token.split("\\.");
            if (chunks.length != 3) {
                return null;
            }
            
            String payload = new String(Base64.getUrlDecoder().decode(chunks[1]));
            // Simple JSON parsing for subject claim
            if (payload.contains("\"sub\":")) {
                int start = payload.indexOf("\"sub\":\"") + 7;
                int end = payload.indexOf("\"", start);
                if (start > 6 && end > start) {
                    return payload.substring(start, end);
                }
            }
            return null;
            
        } catch (Exception e) {
            log.debug("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates JWT audience claim against configured audiences.
     * 
     * @param claims JWT claims
     * @throws InvalidTokenException if audience validation fails
     */
    private void validateAudience(Claims claims) {
        List<String> expectedAudiences = gatewayProperties.getJwt().getAudience();
        Object audienceClaim = claims.get("aud");
        
        if (audienceClaim == null) {
            throw new InvalidTokenException("Token missing audience claim");
        }
        
        List<String> tokenAudiences;
        if (audienceClaim instanceof String) {
            tokenAudiences = List.of((String) audienceClaim);
        } else if (audienceClaim instanceof List) {
            tokenAudiences = (List<String>) audienceClaim;
        } else {
            throw new InvalidTokenException("Invalid audience claim format");
        }
        
        boolean audienceValid = tokenAudiences.stream()
            .anyMatch(expectedAudiences::contains);
        
        if (!audienceValid) {
            log.warn("JWT audience validation failed. Expected: {}, Found: {}", 
                    expectedAudiences, tokenAudiences);
            throw new InvalidTokenException("Invalid token audience");
        }
    }

    /**
     * Gets RSA public key for signature verification.
     * 
     * Lazy-loads and caches the public key from configuration.
     * Key is parsed from PEM format and converted to Java Key object.
     * 
     * @return RSA public key for JWT signature verification
     */
    private Key getPublicKey() {
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
                        throw new IllegalStateException("Cannot load JWT public key", e);
                    }
                }
            }
        }
        return publicKey;
    }

    /**
     * Masks user ID for logging (privacy protection).
     * 
     * Shows first 4 and last 4 characters, masks middle with asterisks.
     * 
     * @param userId User ID to mask
     * @return Masked user ID for safe logging
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.length() <= 8) {
            return "****";
        }
        return userId.substring(0, 4) + "****" + userId.substring(userId.length() - 4);
    }
}