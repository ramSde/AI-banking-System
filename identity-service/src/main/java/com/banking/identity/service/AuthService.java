package com.banking.identity.service;

import com.banking.identity.dto.LoginRequest;
import com.banking.identity.dto.LoginResponse;
import com.banking.identity.dto.RefreshTokenRequest;
import com.banking.identity.dto.RegisterRequest;

/**
 * Authentication Service Interface
 * 
 * Handles user registration, login, token refresh, and logout operations.
 */
public interface AuthService {

    /**
     * Register a new user
     * 
     * @param request Registration request with email, password, and optional phone/username
     * @return User ID of the newly created user
     */
    String register(RegisterRequest request);

    /**
     * Authenticate user and issue JWT tokens
     * 
     * @param request Login request with email, password, and device metadata
     * @return Login response with access token and refresh token
     */
    LoginResponse login(LoginRequest request);

    /**
     * Refresh access token using refresh token
     * 
     * @param request Refresh token request with refresh token and device metadata
     * @return New login response with rotated tokens
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout user and revoke all active refresh tokens
     * 
     * @param userId User ID to logout
     */
    void logout(String userId);

    /**
     * Revoke a specific refresh token
     * 
     * @param refreshToken Refresh token to revoke
     */
    void revokeRefreshToken(String refreshToken);

    /**
     * Revoke all refresh tokens for a user
     * 
     * @param userId User ID whose tokens should be revoked
     */
    void revokeAllUserTokens(String userId);
}
