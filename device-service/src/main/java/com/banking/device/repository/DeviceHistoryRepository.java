package com.banking.device.repository;

import com.banking.device.domain.DeviceHistory;
import com.banking.device.domain.EventType;
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
 * Repository interface for DeviceHistory entity operations.
 * Provides data access methods for device history and audit trail queries.
 */
@Repository
public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, UUID> {

    /**
     * Find history by device ID
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.deviceId = :deviceId ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findByDeviceId(@Param("deviceId") UUID deviceId, Pageable pageable);

    /**
     * Find history by user ID
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.userId = :userId ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find history by event type
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.eventType = :eventType ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findByEventType(@Param("eventType") EventType eventType, Pageable pageable);

    /**
     * Find history by device and event type
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.deviceId = :deviceId AND dh.eventType = :eventType ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findByDeviceIdAndEventType(@Param("deviceId") UUID deviceId, @Param("eventType") EventType eventType, Pageable pageable);

    /**
     * Find history by user and event type
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.userId = :userId AND dh.eventType = :eventType ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findByUserIdAndEventType(@Param("userId") UUID userId, @Param("eventType") EventType eventType, Pageable pageable);

    /**
     * Find successful events
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.success = true ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findSuccessfulEvents(Pageable pageable);

    /**
     * Find failed events
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.success = false ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findFailedEvents(Pageable pageable);

    /**
     * Find history within date range
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.createdAt BETWEEN :startDate AND :endDate ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);

    /**
     * Find recent history for device
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.deviceId = :deviceId AND dh.createdAt >= :since ORDER BY dh.createdAt DESC")
    List<DeviceHistory> findRecentHistoryByDevice(@Param("deviceId") UUID deviceId, @Param("since") Instant since);

    /**
     * Find recent history for user
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.userId = :userId AND dh.createdAt >= :since ORDER BY dh.createdAt DESC")
    List<DeviceHistory> findRecentHistoryByUser(@Param("userId") UUID userId, @Param("since") Instant since);

    /**
     * Count events by device
     */
    @Query("SELECT COUNT(dh) FROM DeviceHistory dh WHERE dh.deviceId = :deviceId")
    Long countByDeviceId(@Param("deviceId") UUID deviceId);

    /**
     * Count events by user
     */
    @Query("SELECT COUNT(dh) FROM DeviceHistory dh WHERE dh.userId = :userId")
    Long countByUserId(@Param("userId") UUID userId);

    /**
     * Count events by type
     */
    @Query("SELECT COUNT(dh) FROM DeviceHistory dh WHERE dh.eventType = :eventType")
    Long countByEventType(@Param("eventType") EventType eventType);

    /**
     * Count successful events for device
     */
    @Query("SELECT COUNT(dh) FROM DeviceHistory dh WHERE dh.deviceId = :deviceId AND dh.success = true")
    Long countSuccessfulEventsByDevice(@Param("deviceId") UUID deviceId);

    /**
     * Count failed events for device
     */
    @Query("SELECT COUNT(dh) FROM DeviceHistory dh WHERE dh.deviceId = :deviceId AND dh.success = false")
    Long countFailedEventsByDevice(@Param("deviceId") UUID deviceId);

    /**
     * Find history by IP address
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.ipAddress = :ipAddress ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findByIpAddress(@Param("ipAddress") String ipAddress, Pageable pageable);

    /**
     * Find login attempts for device
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.deviceId = :deviceId AND dh.eventType IN ('LOGIN_SUCCESS', 'LOGIN_FAILED') ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findLoginAttemptsByDevice(@Param("deviceId") UUID deviceId, Pageable pageable);

    /**
     * Find trust changes for device
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.deviceId = :deviceId AND dh.eventType IN ('TRUST_INCREASED', 'TRUST_DECREASED') ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findTrustChangesByDevice(@Param("deviceId") UUID deviceId, Pageable pageable);

    /**
     * Find last successful login for device
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.deviceId = :deviceId AND dh.eventType = 'LOGIN_SUCCESS' AND dh.success = true ORDER BY dh.createdAt DESC LIMIT 1")
    DeviceHistory findLastSuccessfulLoginByDevice(@Param("deviceId") UUID deviceId);

    /**
     * Find events with geolocation data
     */
    @Query("SELECT dh FROM DeviceHistory dh WHERE dh.geolocation IS NOT NULL ORDER BY dh.createdAt DESC")
    Page<DeviceHistory> findEventsWithLocation(Pageable pageable);
}