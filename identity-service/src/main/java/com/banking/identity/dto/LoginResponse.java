package com.banking.identity.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Login Response DTO
 * 
 * Response payload containing JWT access token and refresh token.
 */
@Schema(description = "User login response with JWT tokens")
public record LoginResponse(

    @Schema(description = "JWT access token (RS256, 15-minute TTL)", example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...")
    String accessToken,

    @Schema(description = "Refresh token for obtaining new access tokens (7-day TTL)", example = "rt_abc123def456...")
    String refreshToken,

    @Schema(description = "Token type", example = "Bearer")
    String tokenType,

    @Schema(description = "Access token expiration time in seconds", example = "900")
    Long expiresIn,

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    String userId,

    @Schema(description = "User email", example = "user@example.com")
    String email
) {
    public LoginResponse(String accessToken, String refreshToken, Long expiresIn, String userId, String email) {
        this(accessToken, refreshToken, "Bearer", expiresIn, userId, email);
    }
}
