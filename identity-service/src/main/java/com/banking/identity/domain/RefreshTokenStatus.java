package com.banking.identity.domain;

/**
 * Refresh Token Status Enum
 * 
 * Represents the current status of a refresh token.
 */
public enum RefreshTokenStatus {
    /**
     * Token is active and can be used for refresh
     */
    ACTIVE,

    /**
     * Token has been revoked (user logout, security event)
     */
    REVOKED,

    /**
     * Token has expired (TTL exceeded)
     */
    EXPIRED,

    /**
     * Token has been replaced by a new token (rotation)
     */
    REPLACED
}
