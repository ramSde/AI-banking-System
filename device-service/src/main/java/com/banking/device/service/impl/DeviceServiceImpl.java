package com.banking.device.service.impl;

import com.banking.device.domain.*;
import com.banking.device.dto.DeviceRegistrationRequest;
import com.banking.device.dto.DeviceResponse;
import com.banking.device.dto.DeviceTrustUpdateRequest;
import com.banking.device.exception.DeviceAlreadyExistsException;
import com.banking.device.exception.DeviceNotFoundException;
import com.banking.device.mapper.DeviceMapper;
import com.banking.device.repository.DeviceHistoryRepository;
import com.banking.device.repository.DeviceRepository;
import com.banking.device.service.DeviceService;
import com.banking.device.util.FingerprintGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of DeviceService for device management operations.
 * Handles device registration, trust scoring, and lifecycle management with caching and event publishing.
 */
@Service
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceHistoryRepository deviceHistoryRepository;
    private final DeviceMapper deviceMapper;
    private final FingerprintGenerator fingerprintGenerator;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DeviceServiceImpl(
            DeviceRepository deviceRepository,
            DeviceHistoryRepository deviceHistoryRepository,
            DeviceMapper deviceMapper,
            FingerprintGenerator fingerprintGenerator,
            KafkaTemplate<String, Object> kafkaTemplate) {
        this.deviceRepository = deviceRepository;
        this.deviceHistoryRepository = deviceHistoryRepository;
        this.deviceMapper = deviceMapper;
        this.fingerprintGenerator = fingerprintGenerator;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public DeviceResponse registerDevice(DeviceRegistrationRequest request) {
        log.info("Registering device for user: {}", request.userId());

        String fingerprintHash = fingerprintGenerator.generateFingerprint(
                request.userAgent(),
                request.screenResolution(),
                request.timezone(),
                request.language()
        );

        Optional<Device> existingDevice = deviceRepository.findByFingerprintHashAndDeletedAtIsNull(fingerprintHash);
        if (existingDevice.isPresent()) {
            log.warn("Device already exists with fingerprint: {}", fingerprintHash);
            throw new DeviceAlreadyExistsException("Device already registered");
        }

        Device device = Device.builder()
                .userId(request.userId())
                .fingerprintHash(fingerprintHash)
                .deviceType(request.deviceType())
                .deviceStatus(DeviceStatus.ACTIVE)
                .trustScore(30)
                .lastSeenAt(Instant.now())
                .browser(request.browser())
                .browserVersion(request.browserVersion())
                .os(request.os())
                .osVersion(request.osVersion())
                .deviceName(request.deviceName())
                .ipAddress(request.ipAddress())
                .geolocation(request.geolocation())
                .screenResolution(request.screenResolution())
                .timezone(request.timezone())
                .language(request.language())
                .userAgent(request.userAgent())
                .build();

        Device savedDevice = deviceRepository.save(device);

        DeviceHistory history = DeviceHistory.builder()
                .deviceId(savedDevice.getId())
                .userId(savedDevice.getUserId())
                .eventType(EventType.REGISTERED)
                .ipAddress(request.ipAddress())
                .geolocation(request.geolocation())
                .userAgent(request.userAgent())
                .success(true)
                .build();
        deviceHistoryRepository.save(history);

        log.info("Device registered successfully: {}", savedDevice.getId());
        return deviceMapper.toResponse(savedDevice);
    }

    @Override
    @Cacheable(value = "device", key = "#deviceId")
    public Optional<DeviceResponse> getDeviceById(UUID deviceId) {
        log.debug("Fetching device by ID: {}", deviceId);
        return deviceRepository.findById(deviceId)
                .filter(device -> device.getDeletedAt() == null)
                .map(deviceMapper::toResponse);
    }

    @Override
    @Cacheable(value = "device", key = "#fingerprintHash")
    public Optional<DeviceResponse> getDeviceByFingerprint(String fingerprintHash) {
        log.debug("Fetching device by fingerprint: {}", fingerprintHash);
        return deviceRepository.findByFingerprintHashAndDeletedAtIsNull(fingerprintHash)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getDevicesByUser(UUID userId, Pageable pageable) {
        log.debug("Fetching devices for user: {}", userId);
        return deviceRepository.findByUserIdAndNotDeleted(userId, pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getDevicesByStatus(DeviceStatus status, Pageable pageable) {
        log.debug("Fetching devices by status: {}", status);
        return deviceRepository.findByUserIdAndStatus(null, status, pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getDevicesByType(DeviceType deviceType, Pageable pageable) {
        log.debug("Fetching devices by type: {}", deviceType);
        return deviceRepository.findByDeviceType(deviceType, pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getDevicesByTrustRange(Integer minScore, Integer maxScore, Pageable pageable) {
        log.debug("Fetching devices by trust range: {} - {}", minScore, maxScore);
        return deviceRepository.findByTrustScoreRange(minScore, maxScore, pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getSuspiciousDevices(Pageable pageable) {
        log.debug("Fetching suspicious devices");
        return deviceRepository.findSuspiciousDevices(pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getTrustedDevices(Pageable pageable) {
        log.debug("Fetching trusted devices");
        return deviceRepository.findTrustedDevices(pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getInactiveDevices(Instant since, Pageable pageable) {
        log.debug("Fetching inactive devices since: {}", since);
        return deviceRepository.findDevicesNotSeenSince(since, pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> getRecentlyActiveDevices(Instant since, Pageable pageable) {
        log.debug("Fetching recently active devices since: {}", since);
        return deviceRepository.findRecentlyActiveDevices(since, pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "device", key = "#deviceId")
    public DeviceResponse updateTrustScore(UUID deviceId, DeviceTrustUpdateRequest request) {
        log.info("Updating trust score for device: {}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        Integer oldScore = device.getTrustScore();
        device.setTrustScore(request.trustScore());

        Device updatedDevice = deviceRepository.save(device);

        EventType eventType = request.trustScore() > oldScore ? EventType.TRUST_INCREASED : EventType.TRUST_DECREASED;
        DeviceHistory history = DeviceHistory.builder()
                .deviceId(device.getId())
                .userId(device.getUserId())
                .eventType(eventType)
                .success(true)
                .metadata(String.format("{\"old_score\": %d, \"new_score\": %d, \"reason\": \"%s\"}", 
                        oldScore, request.trustScore(), request.reason()))
                .build();
        deviceHistoryRepository.save(history);

        log.info("Trust score updated for device: {} from {} to {}", deviceId, oldScore, request.trustScore());
        return deviceMapper.toResponse(updatedDevice);
    }

    @Override
    @Transactional
    @CacheEvict(value = "device", key = "#deviceId")
    public DeviceResponse updateDeviceStatus(UUID deviceId, DeviceStatus status) {
        log.info("Updating device status: {} to {}", deviceId, status);

        Device device = deviceRepository.findById(deviceId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        device.setDeviceStatus(status);
        Device updatedDevice = deviceRepository.save(device);

        EventType eventType = status == DeviceStatus.BLOCKED ? EventType.BLOCKED : EventType.UNBLOCKED;
        DeviceHistory history = DeviceHistory.builder()
                .deviceId(device.getId())
                .userId(device.getUserId())
                .eventType(eventType)
                .success(true)
                .build();
        deviceHistoryRepository.save(history);

        log.info("Device status updated: {}", deviceId);
        return deviceMapper.toResponse(updatedDevice);
    }

    @Override
    @Transactional
    @CacheEvict(value = "device", key = "#deviceId")
    public void updateLastSeen(UUID deviceId) {
        log.debug("Updating last seen for device: {}", deviceId);
        deviceRepository.updateLastSeenAt(deviceId, Instant.now(), Instant.now());
    }

    @Override
    @Transactional
    @CacheEvict(value = "device", key = "#deviceId")
    public DeviceResponse blockDevice(UUID deviceId, String reason) {
        log.warn("Blocking device: {} - Reason: {}", deviceId, reason);

        Device device = deviceRepository.findById(deviceId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        device.setDeviceStatus(DeviceStatus.BLOCKED);
        Device updatedDevice = deviceRepository.save(device);

        DeviceHistory history = DeviceHistory.builder()
                .deviceId(device.getId())
                .userId(device.getUserId())
                .eventType(EventType.BLOCKED)
                .success(true)
                .failureReason(reason)
                .build();
        deviceHistoryRepository.save(history);

        log.info("Device blocked: {}", deviceId);
        return deviceMapper.toResponse(updatedDevice);
    }

    @Override
    @Transactional
    @CacheEvict(value = "device", key = "#deviceId")
    public DeviceResponse unblockDevice(UUID deviceId, String reason) {
        log.info("Unblocking device: {} - Reason: {}", deviceId, reason);

        Device device = deviceRepository.findById(deviceId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        device.setDeviceStatus(DeviceStatus.ACTIVE);
        Device updatedDevice = deviceRepository.save(device);

        DeviceHistory history = DeviceHistory.builder()
                .deviceId(device.getId())
                .userId(device.getUserId())
                .eventType(EventType.UNBLOCKED)
                .success(true)
                .metadata(String.format("{\"reason\": \"%s\"}", reason))
                .build();
        deviceHistoryRepository.save(history);

        log.info("Device unblocked: {}", deviceId);
        return deviceMapper.toResponse(updatedDevice);
    }

    @Override
    @Transactional
    @CacheEvict(value = "device", key = "#deviceId")
    public void deleteDevice(UUID deviceId) {
        log.info("Soft deleting device: {}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        deviceRepository.softDelete(deviceId, Instant.now(), Instant.now());
        log.info("Device soft deleted: {}", deviceId);
    }

    @Override
    public Long getDeviceCountByUser(UUID userId) {
        log.debug("Counting devices for user: {}", userId);
        return deviceRepository.countByUserId(userId);
    }

    @Override
    public Long getDeviceCountByStatus(DeviceStatus status) {
        log.debug("Counting devices by status: {}", status);
        return deviceRepository.countByStatus(status);
    }

    @Override
    public boolean deviceExistsForUser(UUID userId, String fingerprintHash) {
        log.debug("Checking if device exists for user: {}", userId);
        return deviceRepository.findByFingerprintHashAndDeletedAtIsNull(fingerprintHash)
                .map(device -> device.getUserId().equals(userId))
                .orElse(false);
    }

    @Override
    public Optional<DeviceResponse> getMostTrustedDevice(UUID userId) {
        log.debug("Fetching most trusted device for user: {}", userId);
        return deviceRepository.findMostTrustedDeviceByUser(userId)
                .map(deviceMapper::toResponse);
    }

    @Override
    public boolean isFingerprintUsedByDifferentUser(String fingerprintHash, UUID userId) {
        log.debug("Checking if fingerprint is used by different user");
        return deviceRepository.existsByFingerprintHashAndDifferentUser(fingerprintHash, userId);
    }

    @Override
    public Integer calculateTrustScore(UUID deviceId) {
        log.debug("Calculating trust score for device: {}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        Long successfulLogins = deviceHistoryRepository.countSuccessfulEventsByDevice(deviceId);
        Long failedLogins = deviceHistoryRepository.countFailedEventsByDevice(deviceId);

        int baseScore = 30;
        int successBonus = Math.min(successfulLogins.intValue() * 5, 50);
        int failurePenalty = Math.min(failedLogins.intValue() * 10, 30);

        int calculatedScore = Math.max(0, Math.min(100, baseScore + successBonus - failurePenalty));

        log.debug("Calculated trust score for device {}: {}", deviceId, calculatedScore);
        return calculatedScore;
    }

    @Override
    public Page<DeviceResponse> getDevicesWithLocation(Pageable pageable) {
        log.debug("Fetching devices with location data");
        return deviceRepository.findDevicesWithLocation(pageable)
                .map(deviceMapper::toResponse);
    }

    @Override
    public Page<DeviceResponse> searchDevicesByUserAgent(String pattern, Pageable pageable) {
        log.debug("Searching devices by user agent pattern: {}", pattern);
        return deviceRepository.findByUserAgentPattern(pattern, pageable)
                .map(deviceMapper::toResponse);
    }
}