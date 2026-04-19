package com.banking.device.dto;

import com.banking.device.domain.DeviceStatus;
import com.banking.device.domain.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for device information.
 * Contains device details with sensitive data masked.
 */
@Schema(description = "Device information response")
public record DeviceResponse(
        
        @Schema(description = "Device ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,
        
        @Schema(description = "Device fingerprint hash (masked)", example = "sha256:abc123...xyz789")
        String fingerprintHash,
        
        @Schema(description = "Device type", example = "MOBILE")
        DeviceType deviceType,
        
        @Schema(description = "Device status", example = "ACTIVE")
        DeviceStatus deviceStatus,
        
        @Schema(description = "Trust score (0-100)", example = "75")
        Integer trustScore,
        
        @Schema(description = "Last seen timestamp", example = "2024-01-15T10:30:00Z")
        Instant lastSeenAt,
        
        @Schema(description = "Browser name", example = "Chrome")
        String browser,
        
        @Schema(description = "Browser version", example = "120.0.0.0")
        String browserVersion,
        
        @Schema(description = "Operating system", example = "Windows")
        String os,
        
        @Schema(description = "OS version", example = "11")
        String osVersion,
        
        @Schema(description = "Device name", example = "John's iPhone")
        String deviceName,
        
        @Schema(description = "IP address (masked)", example = "192.168.1.***")
        String ipAddress,
        
        @Schema(description = "Screen resolution", example = "1920x1080")
        String screenResolution,
        
        @Schema(description = "Timezone", example = "America/New_York")
        String timezone,
        
        @Schema(description = "Language", example = "en-US")
        String language,
        
        @Schema(description = "User agent (truncated)", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...")
        String userAgent,
        
        @Schema(description = "Geolocation data", example = "{\"city\": \"New York\", \"country\": \"US\"}")
        String geolocation,
        
        @Schema(description = "Creation timestamp", example = "2024-01-01T00:00:00Z")
        Instant createdAt,
        
        @Schema(description = "Last update timestamp", example = "2024-01-15T10:30:00Z")
        Instant updatedAt
) {
}