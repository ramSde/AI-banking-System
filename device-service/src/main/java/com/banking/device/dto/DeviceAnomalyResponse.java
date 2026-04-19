package com.banking.device.dto;

import com.banking.device.domain.AnomalyType;
import com.banking.device.domain.Severity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for device anomaly information.
 * Contains anomaly details and risk assessment.
 */
@Schema(description = "Device anomaly response")
public record DeviceAnomalyResponse(
        
        @Schema(description = "Anomaly ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "Device ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID deviceId,
        
        @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,
        
        @Schema(description = "Anomaly type", example = "IMPOSSIBLE_TRAVEL")
        AnomalyType anomalyType,
        
        @Schema(description = "Severity level", example = "HIGH")
        Severity severity,
        
        @Schema(description = "Anomaly description", example = "User traveled from New York to London in 2 hours")
        String description,
        
        @Schema(description = "Detection timestamp", example = "2024-01-15T10:30:00Z")
        Instant detectedAt,
        
        @Schema(description = "Resolution timestamp", example = "2024-01-15T11:00:00Z")
        Instant resolvedAt,
        
        @Schema(description = "Resolution notes", example = "False positive - user took flight")
        String resolutionNotes,
        
        @Schema(description = "IP address (masked)", example = "192.168.1.***")
        String ipAddress,
        
        @Schema(description = "Geolocation data", example = "{\"city\": \"London\", \"country\": \"UK\"}")
        String geolocation,
        
        @Schema(description = "User agent (truncated)", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)...")
        String userAgent,
        
        @Schema(description = "Risk score (0-100)", example = "85")
        Integer riskScore,
        
        @Schema(description = "Confidence level (0.0-1.0)", example = "0.92")
        BigDecimal confidenceLevel,
        
        @Schema(description = "Additional anomaly metadata", example = "{\"travel_speed_kmh\": 1200}")
        String metadata,
        
        @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00Z")
        Instant createdAt,
        
        @Schema(description = "Last update timestamp", example = "2024-01-15T11:00:00Z")
        Instant updatedAt,
        
        @Schema(description = "Resolution status", example = "true")
        Boolean resolved
) {
}