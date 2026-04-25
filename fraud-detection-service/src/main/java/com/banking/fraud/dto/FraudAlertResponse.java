package com.banking.fraud.dto;

import com.banking.fraud.domain.AlertStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Fraud Alert Response DTO
 * 
 * @param id Alert ID
 * @param fraudCheckId Fraud check ID
 * @param transactionId Transaction ID
 * @param userId User ID
 * @param alertType Alert type
 * @param severity Severity level
 * @param status Alert status
 * @param description Alert description
 * @param assignedTo Assigned user ID
 * @param resolvedAt Resolution timestamp
 * @param resolutionNotes Resolution notes
 * @param createdAt Creation timestamp
 * @param updatedAt Update timestamp
 */
public record FraudAlertResponse(
        UUID id,
        UUID fraudCheckId,
        UUID transactionId,
        UUID userId,
        String alertType,
        String severity,
        AlertStatus status,
        String description,
        UUID assignedTo,
        Instant resolvedAt,
        String resolutionNotes,
        Instant createdAt,
        Instant updatedAt
) {
}
