package com.banking.fraud.repository;

import com.banking.fraud.domain.FraudCheck;
import com.banking.fraud.domain.RiskLevel;
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
 * Repository for Fraud Check entities
 */
@Repository
public interface FraudCheckRepository extends JpaRepository<FraudCheck, UUID> {

    /**
     * Find fraud check by transaction ID
     * 
     * @param transactionId Transaction ID
     * @return Optional fraud check
     */
    Optional<FraudCheck> findByTransactionIdAndDeletedAtIsNull(UUID transactionId);

    /**
     * Find all fraud checks for a user
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of fraud checks
     */
    Page<FraudCheck> findByUserIdAndDeletedAtIsNullOrderByCheckedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find fraud checks by risk level
     * 
     * @param riskLevel Risk level
     * @param pageable Pagination parameters
     * @return Page of fraud checks
     */
    Page<FraudCheck> findByRiskLevelAndDeletedAtIsNullOrderByCheckedAtDesc(RiskLevel riskLevel, Pageable pageable);

    /**
     * Find blocked transactions
     * 
     * @param pageable Pagination parameters
     * @return Page of blocked fraud checks
     */
    Page<FraudCheck> findByBlockedTrueAndDeletedAtIsNullOrderByCheckedAtDesc(Pageable pageable);

    /**
     * Count fraud checks for user in time window
     * 
     * @param userId User ID
     * @param startTime Start of time window
     * @return Count of fraud checks
     */
    @Query("SELECT COUNT(fc) FROM FraudCheck fc WHERE fc.userId = :userId AND fc.checkedAt >= :startTime AND fc.deletedAt IS NULL")
    long countByUserIdAndCheckedAtAfter(@Param("userId") UUID userId, @Param("startTime") Instant startTime);

    /**
     * Find recent high-risk checks for user
     * 
     * @param userId User ID
     * @param riskLevel Risk level
     * @param startTime Start time
     * @return List of fraud checks
     */
    @Query("SELECT fc FROM FraudCheck fc WHERE fc.userId = :userId AND fc.riskLevel = :riskLevel AND fc.checkedAt >= :startTime AND fc.deletedAt IS NULL ORDER BY fc.checkedAt DESC")
    List<FraudCheck> findRecentHighRiskChecks(@Param("userId") UUID userId, @Param("riskLevel") RiskLevel riskLevel, @Param("startTime") Instant startTime);

    /**
     * Find all fraud checks with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of fraud checks
     */
    @Query("SELECT fc FROM FraudCheck fc WHERE fc.deletedAt IS NULL ORDER BY fc.checkedAt DESC")
    Page<FraudCheck> findAllNotDeleted(Pageable pageable);

    /**
     * Get average risk score for user
     * 
     * @param userId User ID
     * @param startTime Start time
     * @return Average risk score
     */
    @Query("SELECT AVG(fc.riskScore) FROM FraudCheck fc WHERE fc.userId = :userId AND fc.checkedAt >= :startTime AND fc.deletedAt IS NULL")
    Double getAverageRiskScoreForUser(@Param("userId") UUID userId, @Param("startTime") Instant startTime);
}
