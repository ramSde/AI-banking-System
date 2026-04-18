package com.banking.identity.service.impl;

import com.banking.identity.config.IdentityProperties;
import com.banking.identity.domain.RefreshTokenAudit;
import com.banking.identity.domain.RefreshTokenStatus;
import com.banking.identity.exception.InvalidTokenException;
import com.banking.identity.repository.RefreshTokenAuditRepository;
import com.banking.identity.service.PasswordService;
import com.banking.identity.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Token Service Implementation
 * 
 * Handles JWT token generation, validation, and refresh token management with RS256 signing.
 */
@Service
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenAuditRepository refreshTokenAuditRepository;
    private final PasswordService passwordService;
    private final IdentityProperties identityProperties;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public TokenServiceImpl(
            final RefreshTokenAuditRepository refreshTokenAuditRepository,
            final PasswordService passwordService,
            final IdentityProperties identityProperties) {
        this.refreshTokenAuditRepository = refreshTokenAuditRepository;
        this.passwordService = passwordService;
        this.identityProperties = identityProperties;
        this.privateKey = loadPrivateKey(identityProperties.getJwt().getPrivateKey());
        this.publicKey = loadPublicKey(identityProperties.getJwt().getPublicKey());
    }

    @Override
    public String generateAccessToken(final UUID userId, final String email) {
        log.debug("Generating access token for user: {}", userId);

        final Instant now = Instant.now();
        final long ttlMinutes = identityProperties.getJwt().getAccessTokenTtlMinutes();
        final Instant expiration = now.plusSeconds(ttlMinutes * 60);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuer(identityProperties.getJwt().getIssuer())
                .setAudience(identityProperties.getJwt().getAudience())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    @Override
    @Transactional
    public String generateRefreshToken(final UUID userId, final String deviceId, 
                                      final String ipAddress, final String userAgent) {
        log.debug("Generating refresh token for user: {}", userId);

        final String rawToken = "rt_" + UUID.randomUUID().toString().replace("-", "");
        final String tokenHash = passwordService.hashPassword(rawToken);

        final Instant now = Instant.now();
        final long ttlDays = identityProperties.getJwt().getRefreshTokenTtlDays();
        final Instant expiration = now.plusSeconds(ttlDays * 24 * 60 * 60);

        final RefreshTokenAudit tokenAudit = new RefreshTokenAudit();
        tokenAudit.setUserId(userId);
        tokenAudit.setTokenHash(tokenHash);
        tokenAudit.setTokenFamilyId(UUID.randomUUID());
        tokenAudit.setIssuedAt(now);
        tokenAudit.setExpiresAt(expiration);
        tokenAudit.setDeviceId(deviceId);
        tokenAudit.setIpAddress(ipAddress);
        tokenAudit.setUserAgent(userAgent);
        tokenAudit.setStatus(RefreshTokenStatus.ACTIVE);

        refreshTokenAuditRepository.save(tokenAudit);
        log.info("Refresh token generated for user: {}", userId);

        return rawToken;
    }

    @Override
    public UUID validateAccessToken(final String token) {
        try {
            final Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            final String userIdStr = claims.getSubject();
            return UUID.fromString(userIdStr);
        } catch (final Exception e) {
            log.error("Invalid access token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid or expired access token");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UUID validateRefreshToken(final String refreshToken) {
        log.debug("Validating refresh token");

        final RefreshTokenAudit tokenAudit = findTokenAuditByRawToken(refreshToken);

        if (!tokenAudit.isActive()) {
            log.warn("Refresh token is not active. Status: {}", tokenAudit.getStatus());
            
            if (tokenAudit.getStatus() == RefreshTokenStatus.REPLACED) {
                revokeTokenFamily(tokenAudit.getTokenFamilyId());
                throw new InvalidTokenException("Token reuse detected. All tokens in family revoked for security");
            }
            
            throw new InvalidTokenException("Refresh token is not active");
        }

        if (tokenAudit.isExpired()) {
            log.warn("Refresh token has expired");
            throw new InvalidTokenException("Refresh token has expired");
        }

        return tokenAudit.getUserId();
    }

    @Override
    @Transactional
    public String rotateRefreshToken(final String oldRefreshToken, final UUID userId, 
                                    final String deviceId, final String ipAddress, final String userAgent) {
        log.info("Rotating refresh token for user: {}", userId);

        final RefreshTokenAudit oldTokenAudit = findTokenAuditByRawToken(oldRefreshToken);

        if (!oldTokenAudit.isActive()) {
            log.error("Attempted to rotate inactive token. Possible token reuse attack");
            revokeTokenFamily(oldTokenAudit.getTokenFamilyId());
            throw new InvalidTokenException("Token reuse detected. All tokens revoked for security");
        }

        final String newRawToken = "rt_" + UUID.randomUUID().toString().replace("-", "");
        final String newTokenHash = passwordService.hashPassword(newRawToken);

        final Instant now = Instant.now();
        final long ttlDays = identityProperties.getJwt().getRefreshTokenTtlDays();
        final Instant expiration = now.plusSeconds(ttlDays * 24 * 60 * 60);

        final RefreshTokenAudit newTokenAudit = new RefreshTokenAudit();
        newTokenAudit.setUserId(userId);
        newTokenAudit.setTokenHash(newTokenHash);
        newTokenAudit.setTokenFamilyId(oldTokenAudit.getTokenFamilyId());
        newTokenAudit.setIssuedAt(now);
        newTokenAudit.setExpiresAt(expiration);
        newTokenAudit.setDeviceId(deviceId);
        newTokenAudit.setIpAddress(ipAddress);
        newTokenAudit.setUserAgent(userAgent);
        newTokenAudit.setStatus(RefreshTokenStatus.ACTIVE);

        final RefreshTokenAudit savedNewToken = refreshTokenAuditRepository.save(newTokenAudit);

        oldTokenAudit.markAsReplaced(savedNewToken.getId());
        refreshTokenAuditRepository.save(oldTokenAudit);

        log.info("Refresh token rotated successfully for user: {}", userId);
        return newRawToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(final String refreshToken) {
        log.info("Revoking refresh token");

        final RefreshTokenAudit tokenAudit = findTokenAuditByRawToken(refreshToken);
        tokenAudit.revoke();
        refreshTokenAuditRepository.save(tokenAudit);

        log.info("Refresh token revoked successfully");
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(final UUID userId) {
        log.info("Revoking all refresh tokens for user: {}", userId);

        final int revokedCount = refreshTokenAuditRepository.revokeAllTokensByUserId(userId, Instant.now());
        log.info("Revoked {} refresh tokens for user: {}", revokedCount, userId);
    }

    @Override
    public long getAccessTokenTtlSeconds() {
        return identityProperties.getJwt().getAccessTokenTtlMinutes() * 60L;
    }

    private RefreshTokenAudit findTokenAuditByRawToken(final String rawToken) {
        return refreshTokenAuditRepository.findAll().stream()
                .filter(audit -> passwordService.verifyPassword(rawToken, audit.getTokenHash()))
                .findFirst()
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
    }

    private void revokeTokenFamily(final UUID tokenFamilyId) {
        log.warn("Revoking entire token family: {}", tokenFamilyId);
        refreshTokenAuditRepository.revokeTokenFamily(tokenFamilyId, Instant.now());
    }

    private PrivateKey loadPrivateKey(final String privateKeyPem) {
        try {
            final String privateKeyContent = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            final byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to load private key", e);
        }
    }

    private PublicKey loadPublicKey(final String publicKeyPem) {
        try {
            final String publicKeyContent = publicKeyPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            final byte[] keyBytes = Base64.getDecoder().decode(publicKeyContent);
            final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to load public key", e);
        }
    }
}
