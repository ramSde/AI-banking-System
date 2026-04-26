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
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

/**
 * JWT Validator Utility
 * 
 * Validates JWT tokens using RS256 algorithm with public key verification.
 * Extracts user ID and roles from validated tokens.
 * 
 * JWT Structure:
 * - Algorithm: RS256 (RSA Signature with SHA-256)
 * - Header: { "alg": "RS256", "typ": "JWT" }
 * - Payload: { "sub": "userId", "iss": "banking-platform", "aud": "banking-api", 
 *              "exp": timestamp, "iat": timestamp, "roles": ["ROLE_USER"] }
 * - Signature: Verified using public key from Identity Service
 * 
 * Validation Steps:
 * 1. Parse JWT structure
 * 2. Verify signature using RS256 public key
 * 3. Validate issuer claim matches expected issuer
 * 4. Validate audience claim matches expected audience
 * 5. Validate expiration (exp claim)
 * 6. Extract subject (user ID) and roles
 * 
 * Public Key Loading:
 * - Loaded from gateway.jwt.public-key property (PEM format)
 * - Alternative: Load from file specified in gateway.jwt.public-key-location
 * - Public key must be in X.509 format (BEGIN PUBLIC KEY / END PUBLIC KEY)
 * 
 * Error Handling:
 * - Expired token: InvalidTokenException
 * - Invalid signature: InvalidTokenException
 * - Malformed token: InvalidTokenException
 * - Missing claims: InvalidTokenException
 * 
 * @author Banking Platform Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtValidator {

    private final GatewayProperties gatewayProperties;
    private Key publicKey;

    @PostConstruct
    public void init() {
        try {
            String publicKeyPEM = gatewayProperties.getJwt().getPublicKey();
            
            if (publicKeyPEM == null || publicKeyPEM.isEmpty()) {
                log.warn("JWT public key not configured - using default key for development");
                publicKeyPEM = generateDefaultPublicKey();
            }

            publicKeyPEM = publicKeyPEM
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = keyFactory.generatePublic(spec);

            log.info("JWT public key loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load JWT public key", e);
            throw new IllegalStateException("Failed to initialize JWT validator", e);
        }
    }

    public String validateTokenAndGetUserId(String token) throws InvalidTokenException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireIssuer(gatewayProperties.getJwt().getIssuer())
                    .requireAudience(gatewayProperties.getJwt().getAudience())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            if (userId == null || userId.isEmpty()) {
                throw new InvalidTokenException("Token subject (user ID) is missing");
            }

            log.debug("JWT validated successfully for user: {}", userId);
            return userId;

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new InvalidTokenException("Token has expired");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Unsupported token format");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Malformed token");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token signature");
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            throw new InvalidTokenException("Token claims are empty");
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) throws InvalidTokenException {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List) {
                return (List<String>) rolesObj;
            }

            log.debug("No roles found in token, returning empty list");
            return List.of();

        } catch (Exception e) {
            log.warn("Failed to extract roles from token: {}", e.getMessage());
            return List.of();
        }
    }

    private String generateDefaultPublicKey() {
        log.warn("Generating default public key for development - DO NOT USE IN PRODUCTION");
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0Z7p7VqZJ8Qs8xN5K5Ym" +
               "7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym" +
               "7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym" +
               "7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym" +
               "7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym" +
               "7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QJ5K5Ym7QIDAQAB";
    }
}
