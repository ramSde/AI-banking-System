package com.banking.device.service;

import com.banking.device.domain.Device;
import com.banking.device.domain.DeviceStatus;
import com.banking.device.domain.DeviceType;
import com.banking.device.dto.DeviceRegistrationRequest;
import com.banking.device.dto.DeviceResponse;
import com.banking.device.dto.DeviceTrustUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for device management operations.
 * Handles device registration, trust scoring, and lifecycle management.
 */
public interface DeviceService {

    /**
     * Register a new device for a user
     */
    DeviceResponse registerDevice(DeviceRegistrationRequest request);

    /**
     * Get device by ID
     */
    Optional<DeviceResponse> getDeviceById(UUID deviceId);

    /**
     * Get device by fingerprint hash
     */
    Optional<DeviceResponse> getDeviceByFingerprint(String fingerprintHash);

    /**
     * Get all devices for a user
     */
    Page<DeviceResponse> getDevicesByUser(UUID userId, Pageable pageable);

    /**
     * Get devices by status
     */
    Page<DeviceResponse> getDevicesByStatus(DeviceStatus status, Pageable pageable);

    /**
     * Get devices by type
     */
    Page<DeviceResponse> getDevicesByType(DeviceType deviceType, Pageable pageable);

    /**
     * Get devices by trust score range
     */
    Page<DeviceResponse> getDevicesByTrustRange(Integer minScore, Integer maxScore, Pageable pageable);

    /**
     * Get suspicious devices (low trust)
     */
    Page<DeviceResponse> getSuspiciousDevices(Pageable pageable);

    /**
     * Get trusted devices (high trust)
     */
    Page<DeviceResponse> getTrustedDevices(Pageable pageable);

    /**
     * Get devices not seen since specified time
     */
    Page<DeviceResponse> getInactiveDevices(Instant since, Pageable pageable);

    /**
     * Get recently active devices
     */
    Page<DeviceResponse> getRecentlyActiveDevices(Instant since, Pageable pageable);

    /**
     * Update device trust score
     */
    DeviceResponse updateTrustScore(UUID deviceId, DeviceTrustUpdateRequest request);

    /**
     * Update device status
     */
    DeviceResponse updateDeviceStatus(UUID deviceId, DeviceStatus status);

    /**
     * Update device last seen timestamp
     */
    void updateLastSeen(UUID deviceId);

    /**
     * Block device
     */
    DeviceResponse blockDevice(UUID deviceId, String reason);

    /**
     * Unblock device
     */
    DeviceResponse unblockDevice(UUID deviceId, String reason);

    /**
     * Delete device (soft delete)
     */
    void deleteDevice(UUID deviceId);

    /**
     * Get device count by user
     */
    Long getDeviceCountByUser(UUID userId);

    /**
     * Get device count by status
     */
    Long getDeviceCountByStatus(DeviceStatus status);

    /**
     * Check if device exists for user
     */
    boolean deviceExistsForUser(UUID userId, String fingerprintHash);

    /**
     * Get user's most trusted device
     */
    Optional<DeviceResponse> getMostTrustedDevice(UUID userId);

    /**
     * Check if fingerprint is used by different user
     */
    boolean isFingerprintUsedByDifferentUser(String fingerprintHash, UUID userId);

    /**
     * Calculate device trust score based on history
     */
    Integer calculateTrustScore(UUID deviceId);

    /**
     * Get devices with location data
     */
    Page<DeviceResponse> getDevicesWithLocation(Pageable pageable);

    /**
     * Search devices by user agent pattern
     */
    Page<DeviceResponse> searchDevicesByUserAgent(String pattern, Pageable pageable);
}