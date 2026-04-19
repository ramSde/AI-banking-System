package com.banking.device.dto;

import com.banking.device.domain.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for device history information.
 * Contains audit trail of device events.
 */
@Schema(description = "Device history entry response")
public record DeviceHistoryResponse(
        
        @Schema(description = "History entry ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "Device ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID deviceId,
        
        @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,
        
        @Schema(description = "Event type", example = "LOGIN_SUCCESS")
        EventType eventType,
        
        @Schema(description = "IP address (masked)", example = "192.168.1.***")
        String ipAddress,
        
        @Schema(description = "Geolocation data", example = "{\"city\": \"New York\", \"country\": \"US\"}")
        String geolocation,
        
        @Schema(description = "User agent (truncated)", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...")
        String userAgent,
        
        @Schema(description = "Event success status", example = "true")
        Boolean success,
        
        @Schema(description = "Failure reason if unsuccessful", example = "Invalid credentials")
        String failureReason,
        
        @Schema(description = "Additional event metadata", example = "{\"login_method\": \"password\"}")
        String metadata,
        
        @Schema(description = "Event timestamp", example = "2024-01-15T10:30:00Z")
        Instant createdAt
) {
}