package com.banking.risk.repository;

import com.banking.risk.domain.RiskAssessment;
import com.banking.risk.domain.RiskLevel;
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
 * Repository for RiskAssessment entity.
 * Provides data access methods for risk assessment operations.
 */
@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {

    /**
     * Find risk assessment by ID excluding soft-deleted records.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.id = :id AND ra.deletedAt IS NULL")
    Optional<RiskAssessment> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * Find all risk assessments for a user.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.userId = :userId AND ra.deletedAt IS NULL ORDER BY ra.assessedAt DESC")
    Page<RiskAssessment> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find risk assessments by user and date range.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.userId = :userId " +
           "AND ra.assessedAt BETWEEN :startDate AND :endDate " +
           "AND ra.deletedAt IS NULL ORDER BY ra.assessedAt DESC")
    Page<RiskAssessment> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Find risk assessments by risk level.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.riskLevel = :riskLevel " +
           "AND ra.deletedAt IS NULL ORDER BY ra.assessedAt DESC")
    Page<RiskAssessment> findByRiskLevel(@Param("riskLevel") RiskLevel riskLevel, Pageable pageable);

    /**
     * Find recent risk assessments for a user (last N days).
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.userId = :userId " +
           "AND ra.assessedAt >= :since AND ra.deletedAt IS NULL ORDER BY ra.assessedAt DESC")
    List<RiskAssessment> findRecentByUserId(@Param("userId") UUID userId, @Param("since") Instant since);

    /**
     * Find risk assessments by device fingerprint.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.deviceFingerprint = :deviceFingerprint " +
           "AND ra.deletedAt IS NULL ORDER BY ra.assessedAt DESC")
    List<RiskAssessment> findByDeviceFingerprint(@Param("deviceFingerprint") String deviceFingerprint);

    /**
     * Find risk assessments by IP address.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.ipAddress = :ipAddress " +
           "AND ra.deletedAt IS NULL ORDER BY ra.assessedAt DESC")
    List<RiskAssessment> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * Count high-risk assessments for a user in a time window.
     */
    @Query("SELECT COUNT(ra) FROM RiskAssessment ra WHERE ra.userId = :userId " +
           "AND ra.riskLevel = 'HIGH' AND ra.assessedAt >= :since AND ra.deletedAt IS NULL")
    long countHighRiskByUserIdSince(@Param("userId") UUID userId, @Param("since") Instant since);

    /**
     * Find risk assessment by session ID.
     */
    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.sessionId = :sessionId AND ra.deletedAt IS NULL")
    Optional<RiskAssessment> findBySessionId(@Param("sessionId") UUID sessionId);
}
