package com.banking.fraud.repository;

import com.banking.fraud.domain.AlertStatus;
import com.banking.fraud.domain.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Fraud Alert entities
 */
@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {

    /**
     * Find alert by fraud check ID
     * 
     * @param fraudCheckId Fraud check ID
     * @return Optional fraud alert
     */
    Optional<FraudAlert> findByFraudCheckIdAndDeletedAtIsNull(UUID fraudCheckId);

    /**
     * Find alerts by user ID
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlert> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find alerts by status
     * 
     * @param status Alert status
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlert> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(AlertStatus status, Pageable pageable);

    /**
     * Find alerts by severity
     * 
     * @param severity Severity level
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlert> findBySeverityAndDeletedAtIsNullOrderByCreatedAtDesc(String severity, Pageable pageable);

    /**
     * Find alerts assigned to user
     * 
     * @param assignedTo Assigned user ID
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlert> findByAssignedToAndDeletedAtIsNullOrderByCreatedAtDesc(UUID assignedTo, Pageable pageable);

    /**
     * Find open alerts
     * 
     * @param pageable Pagination parameters
     * @return Page of open fraud alerts
     */
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.status IN ('OPEN', 'INVESTIGATING') AND fa.deletedAt IS NULL ORDER BY fa.createdAt DESC")
    Page<FraudAlert> findOpenAlerts(Pageable pageable);

    /**
     * Count open alerts for user
     * 
     * @param userId User ID
     * @return Count of open alerts
     */
    @Query("SELECT COUNT(fa) FROM FraudAlert fa WHERE fa.userId = :userId AND fa.status IN ('OPEN', 'INVESTIGATING') AND fa.deletedAt IS NULL")
    long countOpenAlertsByUserId(@Param("userId") UUID userId);

    /**
     * Find all alerts with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.deletedAt IS NULL ORDER BY fa.createdAt DESC")
    Page<FraudAlert> findAllNotDeleted(Pageable pageable);

    /**
     * Find alerts by status and severity
     * 
     * @param status Alert status
     * @param severity Severity level
     * @param pageable Pagination parameters
     * @return Page of fraud alerts
     */
    Page<FraudAlert> findByStatusAndSeverityAndDeletedAtIsNullOrderByCreatedAtDesc(AlertStatus status, String severity, Pageable pageable);

    /**
     * Find recent alerts for user
     * 
     * @param userId User ID
     * @param startTime Start time
     * @return List of fraud alerts
     */
    @Query("SELECT fa FROM FraudAlert fa WHERE fa.userId = :userId AND fa.createdAt >= :startTime AND fa.deletedAt IS NULL ORDER BY fa.createdAt DESC")
    List<FraudAlert> findRecentAlertsByUserId(@Param("userId") UUID userId, @Param("startTime") Instant startTime);
}
