package com.banking.fraud.dto;

import com.banking.fraud.domain.RiskLevel;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Fraud Check Response DTO
 * 
 * @param id Fraud check ID
 * @param transactionId Transaction ID
 * @param userId User ID
 * @param riskScore Risk score (0-100)
 * @param riskLevel Risk level classification
 * @param blocked Whether transaction was blocked
 * @param rulesTriggered Rules that triggered
 * @param checkDetails Check details
 * @param checkedAt Check timestamp
 */
public record FraudCheckResponse(
        UUID id,
        UUID transactionId,
        UUID userId,
        Integer riskScore,
        RiskLevel riskLevel,
        Boolean blocked,
        Map<String, Object> rulesTriggered,
        Map<String, Object> checkDetails,
        Instant checkedAt
) {
}
