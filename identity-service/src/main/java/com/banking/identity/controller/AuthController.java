package com.banking.identity.controller;

import com.banking.identity.dto.*;
import com.banking.identity.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authentication Controller
 * 
 * Handles user registration, login, token refresh, and logout operations.
 */
@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register new user", description = "Create a new user account with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email or username already exists")
    })
    public com.banking.identity.dto.ApiResponse<String> register(
            @Valid @RequestBody final RegisterRequest request,
            final HttpServletRequest httpRequest) {
        
        log.info("Registration request received for email: {}", request.email());
        
        final String userId = authService.register(request);
        final String traceId = getTraceId(httpRequest);
        
        return com.banking.identity.dto.ApiResponse.success(userId, traceId);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "423", description = "Account locked")
    })
    public com.banking.identity.dto.ApiResponse<LoginResponse> login(
            @Valid @RequestBody final LoginRequest request,
            final HttpServletRequest httpRequest) {
        
        log.info("Login request received for email: {}", request.email());
        
        final String ipAddress = getClientIpAddress(httpRequest);
        final String userAgent = httpRequest.getHeader("User-Agent");
        
        final LoginRequest enrichedRequest = new LoginRequest(
                request.email(),
                request.password(),
                request.deviceId(),
                ipAddress,
                userAgent
        );
        
        final LoginResponse response = authService.login(enrichedRequest);
        final String traceId = getTraceId(httpRequest);
        
        return com.banking.identity.dto.ApiResponse.success(response, traceId);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Obtain new access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    public com.banking.identity.dto.ApiResponse<LoginResponse> refreshToken(
            @Valid @RequestBody final RefreshTokenRequest request,
            final HttpServletRequest httpRequest) {
        
        log.info("Token refresh request received");
        
        final String ipAddress = getClientIpAddress(httpRequest);
        final String userAgent = httpRequest.getHeader("User-Agent");
        
        final RefreshTokenRequest enrichedRequest = new RefreshTokenRequest(
                request.refreshToken(),
                request.deviceId(),
                ipAddress,
                userAgent
        );
        
        final LoginResponse response = authService.refreshToken(enrichedRequest);
        final String traceId = getTraceId(httpRequest);
        
        return com.banking.identity.dto.ApiResponse.success(response, traceId);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Revoke all refresh tokens for the user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public com.banking.identity.dto.ApiResponse<Void> logout(
            @RequestParam final String userId,
            final HttpServletRequest httpRequest) {
        
        log.info("Logout request received for user: {}", userId);
        
        authService.logout(userId);
        final String traceId = getTraceId(httpRequest);
        
        return com.banking.identity.dto.ApiResponse.success(traceId);
    }

    @PostMapping("/revoke")
    @Operation(summary = "Revoke refresh token", description = "Revoke a specific refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token revoked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public com.banking.identity.dto.ApiResponse<Void> revokeToken(
            @RequestParam final String refreshToken,
            final HttpServletRequest httpRequest) {
        
        log.info("Token revocation request received");
        
        authService.revokeRefreshToken(refreshToken);
        final String traceId = getTraceId(httpRequest);
        
        return com.banking.identity.dto.ApiResponse.success(traceId);
    }

    private String getClientIpAddress(final HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String getTraceId(final HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }
        return traceId;
    }
}
