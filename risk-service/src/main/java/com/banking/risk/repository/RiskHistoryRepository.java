package com.banking.risk.repository;

import com.banking.risk.domain.RiskHistory;
import com.banking.risk.domain.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for RiskHistory entity.
 * Provides data access methods for risk history tracking and analytics.
 */
@Repository
public interface RiskHistoryRepository extends JpaRepository<RiskHistory, UUID> {

    /**
     * Find risk history for a user.
     */
    @Query("SELECT rh FROM RiskHistory rh WHERE rh.userId = :userId " +
           "AND rh.deletedAt IS NULL ORDER BY rh.createdAt DESC")
    Page<RiskHistory> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find risk history by assessment ID.
     */
    @Query("SELECT rh FROM RiskHistory rh WHERE rh.assessmentId = :assessmentId AND rh.deletedAt IS NULL")
    List<RiskHistory> findByAssessmentId(@Param("assessmentId") UUID assessmentId);

    /**
     * Find risk history by user and date range.
     */
    @Query("SELECT rh FROM RiskHistory rh WHERE rh.userId = :userId " +
           "AND rh.createdAt BETWEEN :startDate AND :endDate " +
           "AND rh.deletedAt IS NULL ORDER BY rh.createdAt DESC")
    Page<RiskHistory> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Count risk history records by risk level for a user.
     */
    @Query("SELECT COUNT(rh) FROM RiskHistory rh WHERE rh.userId = :userId " +
           "AND rh.riskLevel = :riskLevel AND rh.deletedAt IS NULL")
    long countByUserIdAndRiskLevel(@Param("userId") UUID userId, @Param("riskLevel") RiskLevel riskLevel);

    /**
     * Find recent risk history for analytics (last N days).
     */
    @Query("SELECT rh FROM RiskHistory rh WHERE rh.createdAt >= :since " +
           "AND rh.deletedAt IS NULL ORDER BY rh.createdAt DESC")
    List<RiskHistory> findRecentHistory(@Param("since") Instant since);

    /**
     * Get average risk score for a user over time.
     */
    @Query("SELECT AVG(rh.riskScore) FROM RiskHistory rh WHERE rh.userId = :userId " +
           "AND rh.createdAt >= :since AND rh.deletedAt IS NULL")
    Double getAverageRiskScoreByUserId(@Param("userId") UUID userId, @Param("since") Instant since);
}
