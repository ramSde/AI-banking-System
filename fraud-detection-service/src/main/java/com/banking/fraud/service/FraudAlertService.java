package com.banking.fraud.service;

import com.banking.fraud.domain.AlertStatus;
import com.banking.fraud.dto.FraudAlertResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Fraud Alert Service Interface
 * 
 * Manages fraud alerts and investigations.
 */
public interface FraudAlertService {

    /**
     * Get fraud alert by ID
     * 
     * @param alertId Alert ID
     * @return Fraud alert response
     */
    FraudAlertResponse getAlertById(UUID alertId);

    /**
     * Get all fraud alerts with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlertResponse> getAllAlerts(Pageable pageable);

    /**
     * Get alerts by status
     * 
     * @param status Alert status
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlertResponse> getAlertsByStatus(AlertStatus status, Pageable pageable);

    /**
     * Get alerts by user
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlertResponse> getAlertsByUser(UUID userId, Pageable pageable);

    /**
     * Assign alert to user
     * 
     * @param alertId Alert ID
     * @param assignedTo User ID to assign to
     * @return Updated fraud alert response
     */
    FraudAlertResponse assignAlert(UUID alertId, UUID assignedTo);

    /**
     * Update alert status
     * 
     * @param alertId Alert ID
     * @param status New status
     * @param resolutionNotes Resolution notes
     * @return Updated fraud alert response
     */
    FraudAlertResponse updateAlertStatus(UUID alertId, AlertStatus status, String resolutionNotes);

    /**
     * Get open alerts
     * 
     * @param pageable Pagination parameters
     * @return Page of open fraud alerts
     */
    Page<FraudAlertResponse> getOpenAlerts(Pageable pageable);
}
