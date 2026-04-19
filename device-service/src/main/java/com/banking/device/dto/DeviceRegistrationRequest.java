package com.banking.device.dto;

import com.banking.device.domain.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for device registration.
 * Contains device fingerprinting data and characteristics.
 */
@Schema(description = "Device registration request")
public record DeviceRegistrationRequest(
        
        @Schema(description = "User ID who owns this device", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "User ID is required")
        java.util.UUID userId,
        
        @Schema(description = "Device type", example = "MOBILE")
        @NotNull(message = "Device type is required")
        DeviceType deviceType,
        
        @Schema(description = "Browser name", example = "Chrome")
        @Size(max = 100, message = "Browser name must not exceed 100 characters")
        String browser,
        
        @Schema(description = "Browser version", example = "120.0.0.0")
        @Size(max = 50, message = "Browser version must not exceed 50 characters")
        String browserVersion,
        
        @Schema(description = "Operating system", example = "Windows")
        @Size(max = 100, message = "OS name must not exceed 100 characters")
        String os,
        
        @Schema(description = "OS version", example = "11")
        @Size(max = 50, message = "OS version must not exceed 50 characters")
        String osVersion,
        
        @Schema(description = "Device name", example = "John's iPhone")
        @Size(max = 255, message = "Device name must not exceed 255 characters")
        String deviceName,
        
        @Schema(description = "IP address", example = "192.168.1.100")
        @Size(max = 45, message = "IP address must not exceed 45 characters")
        String ipAddress,
        
        @Schema(description = "Screen resolution", example = "1920x1080")
        @Size(max = 50, message = "Screen resolution must not exceed 50 characters")
        String screenResolution,
        
        @Schema(description = "Timezone", example = "America/New_York")
        @Size(max = 100, message = "Timezone must not exceed 100 characters")
        String timezone,
        
        @Schema(description = "Language", example = "en-US")
        @Size(max = 50, message = "Language must not exceed 50 characters")
        String language,
        
        @Schema(description = "User agent string")
        @NotBlank(message = "User agent is required")
        String userAgent,
        
        @Schema(description = "Geolocation data as JSON", example = "{\"latitude\": 40.7128, \"longitude\": -74.0060, \"city\": \"New York\", \"country\": \"US\"}")
        String geolocation
) {
}