package com.banking.risk.service;

import com.banking.risk.dto.RiskAssessmentRequest;
import com.banking.risk.dto.RiskAssessmentResponse;
import com.banking.risk.dto.RiskHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/**
 * Service interface for risk assessment operations.
 * Provides methods for assessing authentication risk and retrieving assessment history.
 */
public interface RiskAssessmentService {

    /**
     * Assess authentication risk based on provided context.
     *
     * @param request Risk assessment request
     * @return Risk assessment response with score and recommended action
     */
    RiskAssessmentResponse assessRisk(RiskAssessmentRequest request);

    /**
     * Get risk assessment by ID.
     *
     * @param assessmentId Assessment ID
     * @return Risk assessment response
     */
    RiskAssessmentResponse getAssessmentById(UUID assessmentId);

    /**
     * Get risk assessment history for a user.
     *
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of risk history records
     */
    Page<RiskHistoryResponse> getUserRiskHistory(UUID userId, Pageable pageable);

    /**
     * Get risk assessment history for a user within a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination parameters
     * @return Page of risk history records
     */
    Page<RiskHistoryResponse> getUserRiskHistoryByDateRange(
            UUID userId,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    );
}
