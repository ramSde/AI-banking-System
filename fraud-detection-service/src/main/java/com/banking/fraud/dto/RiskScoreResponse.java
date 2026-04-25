package com.banking.fraud.dto;

import com.banking.fraud.domain.RiskLevel;

import java.util.List;
import java.util.UUID;

/**
 * Risk Score Response DTO
 * 
 * @param transactionId Transaction ID
 * @param userId User ID
 * @param riskScore Calculated risk score (0-100)
 * @param riskLevel Risk level classification
 * @param blocked Whether transaction should be blocked
 * @param triggeredRules List of triggered rule names
 * @param recommendation Action recommendation
 */
public record RiskScoreResponse(
        UUID transactionId,
        UUID userId,
        Integer riskScore,
        RiskLevel riskLevel,
        Boolean blocked,
        List<String> triggeredRules,
        String recommendation
) {
}
