package com.banking.identity.service;

import java.util.UUID;

/**
 * Token Service Interface
 * 
 * Handles JWT token generation, validation, and refresh token management.
 */
public interface TokenService {

    /**
     * Generate JWT access token for user
     * 
     * @param userId User ID
     * @param email User email
     * @return JWT access token string
     */
    String generateAccessToken(UUID userId, String email);

    /**
     * Generate refresh token for user
     * 
     * @param userId User ID
     * @param deviceId Device identifier
     * @param ipAddress IP address
     * @param userAgent User agent string
     * @return Refresh token string
     */
    String generateRefreshToken(UUID userId, String deviceId, String ipAddress, String userAgent);

    /**
     * Validate and parse JWT access token
     * 
     * @param token JWT token string
     * @return User ID extracted from token
     */
    UUID validateAccessToken(String token);

    /**
     * Validate refresh token and return user ID
     * 
     * @param refreshToken Refresh token string
     * @return User ID associated with the token
     */
    UUID validateRefreshToken(String refreshToken);

    /**
     * Rotate refresh token (revoke old, issue new)
     * 
     * @param oldRefreshToken Old refresh token to revoke
     * @param userId User ID
     * @param deviceId Device identifier
     * @param ipAddress IP address
     * @param userAgent User agent string
     * @return New refresh token string
     */
    String rotateRefreshToken(String oldRefreshToken, UUID userId, String deviceId, String ipAddress, String userAgent);

    /**
     * Revoke refresh token
     * 
     * @param refreshToken Refresh token to revoke
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * Revoke all refresh tokens for user
     * 
     * @param userId User ID
     */
    void revokeAllUserTokens(UUID userId);

    /**
     * Get access token TTL in seconds
     * 
     * @return TTL in seconds
     */
    long getAccessTokenTtlSeconds();
}
