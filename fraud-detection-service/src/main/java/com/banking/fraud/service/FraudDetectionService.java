package com.banking.fraud.service;

import com.banking.fraud.dto.FraudCheckRequest;
import com.banking.fraud.dto.RiskScoreResponse;

import java.util.Map;
import java.util.UUID;

/**
 * Fraud Detection Service Interface
 * 
 * Core fraud detection logic for analyzing transactions.
 */
public interface FraudDetectionService {

    /**
     * Perform fraud check on transaction
     * 
     * @param request Fraud check request
     * @return Risk score response
     */
    RiskScoreResponse performFraudCheck(FraudCheckRequest request);

    /**
     * Process transaction event from Kafka
     * 
     * @param transactionEvent Transaction event data
     */
    void processTransactionEvent(Map<String, Object> transactionEvent);

    /**
     * Get cached risk score for transaction
     * 
     * @param transactionId Transaction ID
     * @return Risk score response or null if not cached
     */
    RiskScoreResponse getCachedRiskScore(UUID transactionId);
}
