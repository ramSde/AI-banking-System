package com.banking.device.repository;

import com.banking.device.domain.AnomalyType;
import com.banking.device.domain.DeviceAnomaly;
import com.banking.device.domain.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for DeviceAnomaly entity operations.
 * Provides data access methods for anomaly detection and security monitoring.
 */
@Repository
public interface DeviceAnomalyRepository extends JpaRepository<DeviceAnomaly, UUID> {

    /**
     * Find anomalies by device ID (not deleted)
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.deviceId = :deviceId AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findByDeviceIdAndNotDeleted(@Param("deviceId") UUID deviceId, Pageable pageable);

    /**
     * Find anomalies by user ID (not deleted)
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.userId = :userId AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find anomalies by type
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.anomalyType = :anomalyType AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findByAnomalyType(@Param("anomalyType") AnomalyType anomalyType, Pageable pageable);

    /**
     * Find anomalies by severity
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.severity = :severity AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findBySeverity(@Param("severity") Severity severity, Pageable pageable);

    /**
     * Find unresolved anomalies
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.resolvedAt IS NULL AND da.deletedAt IS NULL ORDER BY da.severity DESC, da.detectedAt DESC")
    Page<DeviceAnomaly> findUnresolvedAnomalies(Pageable pageable);

    /**
     * Find resolved anomalies
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.resolvedAt IS NOT NULL AND da.deletedAt IS NULL ORDER BY da.resolvedAt DESC")
    Page<DeviceAnomaly> findResolvedAnomalies(Pageable pageable);

    /**
     * Find high-risk anomalies
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.riskScore >= 70 AND da.deletedAt IS NULL ORDER BY da.riskScore DESC, da.detectedAt DESC")
    Page<DeviceAnomaly> findHighRiskAnomalies(Pageable pageable);

    /**
     * Find critical anomalies
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.severity = 'CRITICAL' AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findCriticalAnomalies(Pageable pageable);

    /**
     * Find recent anomalies
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.detectedAt >= :since AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findRecentAnomalies(@Param("since") Instant since, Pageable pageable);

    /**
     * Find anomalies within date range
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.detectedAt BETWEEN :startDate AND :endDate AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);

    /**
     * Find anomalies by risk score range
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.riskScore BETWEEN :minScore AND :maxScore AND da.deletedAt IS NULL ORDER BY da.riskScore DESC")
    Page<DeviceAnomaly> findByRiskScoreRange(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore, Pageable pageable);

    /**
     * Count anomalies by device
     */
    @Query("SELECT COUNT(da) FROM DeviceAnomaly da WHERE da.deviceId = :deviceId AND da.deletedAt IS NULL")
    Long countByDeviceId(@Param("deviceId") UUID deviceId);

    /**
     * Count anomalies by user
     */
    @Query("SELECT COUNT(da) FROM DeviceAnomaly da WHERE da.userId = :userId AND da.deletedAt IS NULL")
    Long countByUserId(@Param("userId") UUID userId);

    /**
     * Count unresolved anomalies by device
     */
    @Query("SELECT COUNT(da) FROM DeviceAnomaly da WHERE da.deviceId = :deviceId AND da.resolvedAt IS NULL AND da.deletedAt IS NULL")
    Long countUnresolvedByDeviceId(@Param("deviceId") UUID deviceId);

    /**
     * Count unresolved anomalies by user
     */
    @Query("SELECT COUNT(da) FROM DeviceAnomaly da WHERE da.userId = :userId AND da.resolvedAt IS NULL AND da.deletedAt IS NULL")
    Long countUnresolvedByUserId(@Param("userId") UUID userId);

    /**
     * Count anomalies by type
     */
    @Query("SELECT COUNT(da) FROM DeviceAnomaly da WHERE da.anomalyType = :anomalyType AND da.deletedAt IS NULL")
    Long countByAnomalyType(@Param("anomalyType") AnomalyType anomalyType);

    /**
     * Count anomalies by severity
     */
    @Query("SELECT COUNT(da) FROM DeviceAnomaly da WHERE da.severity = :severity AND da.deletedAt IS NULL")
    Long countBySeverity(@Param("severity") Severity severity);

    /**
     * Find anomalies by IP address
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.ipAddress = :ipAddress AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    List<DeviceAnomaly> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * Find recent anomalies for device
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.deviceId = :deviceId AND da.detectedAt >= :since AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    List<DeviceAnomaly> findRecentAnomaliesByDevice(@Param("deviceId") UUID deviceId, @Param("since") Instant since);

    /**
     * Find recent anomalies for user
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.userId = :userId AND da.detectedAt >= :since AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    List<DeviceAnomaly> findRecentAnomaliesByUser(@Param("userId") UUID userId, @Param("since") Instant since);

    /**
     * Resolve anomaly
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceAnomaly da SET da.resolvedAt = :resolvedAt, da.resolutionNotes = :notes, da.updatedAt = :updatedAt WHERE da.id = :anomalyId")
    void resolveAnomaly(@Param("anomalyId") UUID anomalyId, @Param("resolvedAt") Instant resolvedAt, @Param("notes") String notes, @Param("updatedAt") Instant updatedAt);

    /**
     * Soft delete anomaly
     */
    @Modifying
    @Transactional
    @Query("UPDATE DeviceAnomaly da SET da.deletedAt = :deletedAt, da.updatedAt = :updatedAt WHERE da.id = :anomalyId")
    void softDelete(@Param("anomalyId") UUID anomalyId, @Param("deletedAt") Instant deletedAt, @Param("updatedAt") Instant updatedAt);

    /**
     * Find anomalies with geolocation data
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.geolocation IS NOT NULL AND da.deletedAt IS NULL ORDER BY da.detectedAt DESC")
    Page<DeviceAnomaly> findAnomaliesWithLocation(Pageable pageable);

    /**
     * Find high confidence anomalies
     */
    @Query("SELECT da FROM DeviceAnomaly da WHERE da.confidenceLevel >= 0.8 AND da.deletedAt IS NULL ORDER BY da.confidenceLevel DESC, da.detectedAt DESC")
    Page<DeviceAnomaly> findHighConfidenceAnomalies(Pageable pageable);
}