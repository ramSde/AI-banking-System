package com.banking.device.repository;

import com.banking.device.domain.Device;
import com.banking.device.domain.DeviceStatus;
import com.banking.device.domain.DeviceType;
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
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Device entity operations.
 * Provides data access methods for device management and queries.
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    /**
     * Find device by fingerprint hash
     */
    Optional<Device> findByFingerprintHashAndDeletedAtIsNull(String fingerprintHash);

    /**
     * Find all devices for a user (not deleted)
     */
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.deletedAt IS NULL ORDER BY d.lastSeenAt DESC")
    Page<Device> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find devices by user and status
     */
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.deviceStatus = :status AND d.deletedAt IS NULL ORDER BY d.lastSeenAt DESC")
    Page<Device> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") DeviceStatus status, Pageable pageable);

    /**
     * Find devices by trust score range
     */
    @Query("SELECT d FROM Device d WHERE d.trustScore BETWEEN :minScore AND :maxScore AND d.deletedAt IS NULL ORDER BY d.trustScore DESC")
    Page<Device> findByTrustScoreRange(@Param("minScore") Integer minScore, @Param("maxScore") Integer maxScore, Pageable pageable);

    /**
     * Find suspicious devices (low trust score)
     */
    @Query("SELECT d FROM Device d WHERE d.trustScore <= 30 AND d.deletedAt IS NULL ORDER BY d.trustScore ASC")
    Page<Device> findSuspiciousDevices(Pageable pageable);

    /**
     * Find trusted devices (high trust score)
     */
    @Query("SELECT d FROM Device d WHERE d.trustScore >= 70 AND d.deletedAt IS NULL ORDER BY d.trustScore DESC")
    Page<Device> findTrustedDevices(Pageable pageable);

    /**
     * Find devices by type
     */
    @Query("SELECT d FROM Device d WHERE d.deviceType = :deviceType AND d.deletedAt IS NULL ORDER BY d.lastSeenAt DESC")
    Page<Device> findByDeviceType(@Param("deviceType") DeviceType deviceType, Pageable pageable);

    /**
     * Find devices not seen since specified time
     */
    @Query("SELECT d FROM Device d WHERE d.lastSeenAt < :since AND d.deletedAt IS NULL ORDER BY d.lastSeenAt ASC")
    Page<Device> findDevicesNotSeenSince(@Param("since") Instant since, Pageable pageable);

    /**
     * Find recently active devices
     */
    @Query("SELECT d FROM Device d WHERE d.lastSeenAt >= :since AND d.deletedAt IS NULL ORDER BY d.lastSeenAt DESC")
    Page<Device> findRecentlyActiveDevices(@Param("since") Instant since, Pageable pageable);

    /**
     * Count devices by user
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.userId = :userId AND d.deletedAt IS NULL")
    Long countByUserId(@Param("userId") UUID userId);

    /**
     * Count devices by status
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.deviceStatus = :status AND d.deletedAt IS NULL")
    Long countByStatus(@Param("status") DeviceStatus status);

    /**
     * Find devices by IP address
     */
    @Query("SELECT d FROM Device d WHERE d.ipAddress = :ipAddress AND d.deletedAt IS NULL ORDER BY d.lastSeenAt DESC")
    List<Device> findByIpAddress(@Param("ipAddress") String ipAddress);

    /**
     * Update device last seen timestamp
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.lastSeenAt = :lastSeenAt, d.updatedAt = :updatedAt WHERE d.id = :deviceId")
    void updateLastSeenAt(@Param("deviceId") UUID deviceId, @Param("lastSeenAt") Instant lastSeenAt, @Param("updatedAt") Instant updatedAt);

    /**
     * Update device trust score
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.trustScore = :trustScore, d.updatedAt = :updatedAt WHERE d.id = :deviceId")
    void updateTrustScore(@Param("deviceId") UUID deviceId, @Param("trustScore") Integer trustScore, @Param("updatedAt") Instant updatedAt);

    /**
     * Update device status
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.deviceStatus = :status, d.updatedAt = :updatedAt WHERE d.id = :deviceId")
    void updateDeviceStatus(@Param("deviceId") UUID deviceId, @Param("status") DeviceStatus status, @Param("updatedAt") Instant updatedAt);

    /**
     * Soft delete device
     */
    @Modifying
    @Transactional
    @Query("UPDATE Device d SET d.deletedAt = :deletedAt, d.updatedAt = :updatedAt WHERE d.id = :deviceId")
    void softDelete(@Param("deviceId") UUID deviceId, @Param("deletedAt") Instant deletedAt, @Param("updatedAt") Instant updatedAt);

    /**
     * Find devices with geolocation data
     */
    @Query("SELECT d FROM Device d WHERE d.geolocation IS NOT NULL AND d.deletedAt IS NULL ORDER BY d.lastSeenAt DESC")
    Page<Device> findDevicesWithLocation(Pageable pageable);

    /**
     * Search devices by user agent pattern
     */
    @Query("SELECT d FROM Device d WHERE d.userAgent ILIKE %:pattern% AND d.deletedAt IS NULL ORDER BY d.lastSeenAt DESC")
    Page<Device> findByUserAgentPattern(@Param("pattern") String pattern, Pageable pageable);

    /**
     * Find user's most trusted device
     */
    @Query("SELECT d FROM Device d WHERE d.userId = :userId AND d.deletedAt IS NULL ORDER BY d.trustScore DESC LIMIT 1")
    Optional<Device> findMostTrustedDeviceByUser(@Param("userId") UUID userId);

    /**
     * Check if fingerprint exists for different user
     */
    @Query("SELECT COUNT(d) > 0 FROM Device d WHERE d.fingerprintHash = :fingerprintHash AND d.userId != :userId AND d.deletedAt IS NULL")
    boolean existsByFingerprintHashAndDifferentUser(@Param("fingerprintHash") String fingerprintHash, @Param("userId") UUID userId);
}