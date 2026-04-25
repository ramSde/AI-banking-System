package com.banking.fraud.repository;

import com.banking.fraud.domain.FraudPattern;
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
 * Repository for Fraud Pattern entities
 */
@Repository
public interface FraudPatternRepository extends JpaRepository<FraudPattern, UUID> {

    /**
     * Find pattern by type and user
     * 
     * @param patternType Pattern type
     * @param userId User ID
     * @return Optional fraud pattern
     */
    Optional<FraudPattern> findByPatternTypeAndUserIdAndDeletedAtIsNull(String patternType, UUID userId);

    /**
     * Find active patterns for user
     * 
     * @param userId User ID
     * @return List of active fraud patterns
     */
    @Query("SELECT fp FROM FraudPattern fp WHERE fp.userId = :userId AND fp.active = true AND fp.deletedAt IS NULL ORDER BY fp.lastDetectedAt DESC")
    List<FraudPattern> findActivePatternsByUserId(@Param("userId") UUID userId);

    /**
     * Find patterns by type
     * 
     * @param patternType Pattern type
     * @param pageable Pagination parameters
     * @return Page of fraud patterns
     */
    Page<FraudPattern> findByPatternTypeAndDeletedAtIsNullOrderByLastDetectedAtDesc(String patternType, Pageable pageable);

    /**
     * Find patterns by severity
     * 
     * @param severity Severity level
     * @param pageable Pagination parameters
     * @return Page of fraud patterns
     */
    Page<FraudPattern> findBySeverityAndDeletedAtIsNullOrderByLastDetectedAtDesc(String severity, Pageable pageable);

    /**
     * Find active patterns
     * 
     * @param pageable Pagination parameters
     * @return Page of active fraud patterns
     */
    Page<FraudPattern> findByActiveTrueAndDeletedAtIsNullOrderByLastDetectedAtDesc(Pageable pageable);

    /**
     * Find all patterns with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of fraud patterns
     */
    @Query("SELECT fp FROM FraudPattern fp WHERE fp.deletedAt IS NULL ORDER BY fp.lastDetectedAt DESC")
    Page<FraudPattern> findAllNotDeleted(Pageable pageable);

    /**
     * Find recently detected patterns
     * 
     * @param startTime Start time
     * @param pageable Pagination parameters
     * @return Page of fraud patterns
     */
    @Query("SELECT fp FROM FraudPattern fp WHERE fp.lastDetectedAt >= :startTime AND fp.deletedAt IS NULL ORDER BY fp.lastDetectedAt DESC")
    Page<FraudPattern> findRecentlyDetected(@Param("startTime") Instant startTime, Pageable pageable);

    /**
     * Count active patterns for user
     * 
     * @param userId User ID
     * @return Count of active patterns
     */
    @Query("SELECT COUNT(fp) FROM FraudPattern fp WHERE fp.userId = :userId AND fp.active = true AND fp.deletedAt IS NULL")
    long countActivePatternsByUserId(@Param("userId") UUID userId);

    /**
     * Find patterns by user with pagination
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of fraud patterns
     */
    Page<FraudPattern> findByUserIdAndDeletedAtIsNullOrderByLastDetectedAtDesc(UUID userId, Pageable pageable);
}
