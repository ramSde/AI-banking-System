package com.banking.identity.service.impl;

import com.banking.identity.domain.User;
import com.banking.identity.dto.LoginRequest;
import com.banking.identity.dto.LoginResponse;
import com.banking.identity.dto.RefreshTokenRequest;
import com.banking.identity.dto.RegisterRequest;
import com.banking.identity.service.AuthService;
import com.banking.identity.service.TokenService;
import com.banking.identity.service.UserCredentialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Authentication Service Implementation
 * 
 * Orchestrates user registration, login, token refresh, and logout operations.
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserCredentialService userCredentialService;
    private final TokenService tokenService;

    public AuthServiceImpl(
            final UserCredentialService userCredentialService,
            final TokenService tokenService) {
        this.userCredentialService = userCredentialService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public String register(final RegisterRequest request) {
        log.info("Registering new user with email: {}", request.email());

        final User user = userCredentialService.createUser(
                request.email(),
                request.password(),
                request.phoneNumber(),
                request.username()
        );

        log.info("User registered successfully with ID: {}", user.getId());
        return user.getId().toString();
    }

    @Override
    @Transactional
    public LoginResponse login(final LoginRequest request) {
        log.info("User login attempt for email: {}", request.email());

        final User user = userCredentialService.verifyCredentials(request.email(), request.password());

        userCredentialService.handleSuccessfulLogin(user, request.ipAddress());

        final String accessToken = tokenService.generateAccessToken(user.getId(), user.getEmail());
        final String refreshToken = tokenService.generateRefreshToken(
                user.getId(),
                request.deviceId(),
                request.ipAddress(),
                request.userAgent()
        );

        final long expiresIn = tokenService.getAccessTokenTtlSeconds();

        log.info("User logged in successfully: {}", user.getId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                expiresIn,
                user.getId().toString(),
                user.getEmail()
        );
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(final RefreshTokenRequest request) {
        log.info("Refreshing access token");

        final UUID userId = tokenService.validateRefreshToken(request.refreshToken());

        final User user = userCredentialService.findUserById(userId);

        final String newAccessToken = tokenService.generateAccessToken(user.getId(), user.getEmail());
        final String newRefreshToken = tokenService.rotateRefreshToken(
                request.refreshToken(),
                user.getId(),
                request.deviceId(),
                request.ipAddress(),
                request.userAgent()
        );

        final long expiresIn = tokenService.getAccessTokenTtlSeconds();

        log.info("Access token refreshed successfully for user: {}", userId);

        return new LoginResponse(
                newAccessToken,
                newRefreshToken,
                expiresIn,
                user.getId().toString(),
                user.getEmail()
        );
    }

    @Override
    @Transactional
    public void logout(final String userId) {
        log.info("Logging out user: {}", userId);

        final UUID userUuid = UUID.fromString(userId);
        tokenService.revokeAllUserTokens(userUuid);

        log.info("User logged out successfully: {}", userId);
    }

    @Override
    @Transactional
    public void revokeRefreshToken(final String refreshToken) {
        log.info("Revoking specific refresh token");
        tokenService.revokeRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(final String userId) {
        log.info("Revoking all tokens for user: {}", userId);
        final UUID userUuid = UUID.fromString(userId);
        tokenService.revokeAllUserTokens(userUuid);
    }
}
