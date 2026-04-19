package com.banking.device.controller;

import com.banking.device.domain.DeviceStatus;
import com.banking.device.domain.DeviceType;
import com.banking.device.dto.ApiResponse;
import com.banking.device.dto.DeviceRegistrationRequest;
import com.banking.device.dto.DeviceResponse;
import com.banking.device.dto.DeviceTrustUpdateRequest;
import com.banking.device.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * REST controller for device management operations.
 * Provides endpoints for device registration, trust scoring, and monitoring.
 */
@RestController
@RequestMapping("/v1/devices")
@Tag(name = "Device Management", description = "Device intelligence and trust scoring APIs")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    /**
     * Register a new device
     */
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Register new device", description = "Register a new device with fingerprinting")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Device registered successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Device already exists")
    })
    public ResponseEntity<ApiResponse<DeviceResponse>> registerDevice(
            @Valid @RequestBody DeviceRegistrationRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Registering device - traceId: {}, userId: {}", traceId, request.userId());
        
        DeviceResponse response = deviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, traceId));
    }

    /**
     * Get device by ID
     */
    @GetMapping("/{deviceId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get device by ID", description = "Retrieve device details by device ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Device found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<ApiResponse<DeviceResponse>> getDeviceById(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Fetching device - traceId: {}, deviceId: {}", traceId, deviceId);
        
        return deviceService.getDeviceById(deviceId)
                .map(device -> ResponseEntity.ok(ApiResponse.success(device, traceId)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("DEVICE_NOT_FOUND", "Device not found", traceId)));
    }

    /**
     * Get devices by user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user devices", description = "Retrieve all devices for a specific user")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getDevicesByUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @PageableDefault(size = 20, sort = "lastSeenAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Fetching devices for user - traceId: {}, userId: {}", traceId, userId);
        
        Page<DeviceResponse> devices = deviceService.getDevicesByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(devices, traceId));
    }

    /**
     * Get devices by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get devices by status", description = "Retrieve devices filtered by status (Admin only)")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getDevicesByStatus(
            @Parameter(description = "Device status") @PathVariable DeviceStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Fetching devices by status - traceId: {}, status: {}", traceId, status);
        
        Page<DeviceResponse> devices = deviceService.getDevicesByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(devices, traceId));
    }

    /**
     * Get suspicious devices
     */
    @GetMapping("/suspicious")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get suspicious devices", description = "Retrieve devices with low trust scores (Admin only)")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getSuspiciousDevices(
            @PageableDefault(size = 20) Pageable pageable) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Fetching suspicious devices - traceId: {}", traceId);
        
        Page<DeviceResponse> devices = deviceService.getSuspiciousDevices(pageable);
        return ResponseEntity.ok(ApiResponse.success(devices, traceId));
    }

    /**
     * Get trusted devices
     */
    @GetMapping("/trusted")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get trusted devices", description = "Retrieve devices with high trust scores (Admin only)")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getTrustedDevices(
            @PageableDefault(size = 20) Pageable pageable) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Fetching trusted devices - traceId: {}", traceId);
        
        Page<DeviceResponse> devices = deviceService.getTrustedDevices(pageable);
        return ResponseEntity.ok(ApiResponse.success(devices, traceId));
    }

    /**
     * Update device trust score
     */
    @PutMapping("/{deviceId}/trust")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    @Operation(summary = "Update trust score", description = "Update device trust score (Admin/System only)")
    public ResponseEntity<ApiResponse<DeviceResponse>> updateTrustScore(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Valid @RequestBody DeviceTrustUpdateRequest request) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Updating trust score - traceId: {}, deviceId: {}", traceId, deviceId);
        
        DeviceResponse response = deviceService.updateTrustScore(deviceId, request);
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    /**
     * Block device
     */
    @PostMapping("/{deviceId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Block device", description = "Block a device for security reasons (Admin only)")
    public ResponseEntity<ApiResponse<DeviceResponse>> blockDevice(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Parameter(description = "Block reason") @RequestParam String reason) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Blocking device - traceId: {}, deviceId: {}", traceId, deviceId);
        
        DeviceResponse response = deviceService.blockDevice(deviceId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    /**
     * Unblock device
     */
    @PostMapping("/{deviceId}/unblock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unblock device", description = "Unblock a previously blocked device (Admin only)")
    public ResponseEntity<ApiResponse<DeviceResponse>> unblockDevice(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId,
            @Parameter(description = "Unblock reason") @RequestParam String reason) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Unblocking device - traceId: {}, deviceId: {}", traceId, deviceId);
        
        DeviceResponse response = deviceService.unblockDevice(deviceId, reason);
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    /**
     * Delete device
     */
    @DeleteMapping("/{deviceId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Delete device", description = "Soft delete a device")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @Parameter(description = "Device ID") @PathVariable UUID deviceId) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Deleting device - traceId: {}, deviceId: {}", traceId, deviceId);
        
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.success(traceId));
    }

    /**
     * Get device count by user
     */
    @GetMapping("/user/{userId}/count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get device count", description = "Get total device count for a user")
    public ResponseEntity<ApiResponse<Long>> getDeviceCountByUser(
            @Parameter(description = "User ID") @PathVariable UUID userId) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Counting devices for user - traceId: {}, userId: {}", traceId, userId);
        
        Long count = deviceService.getDeviceCountByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(count, traceId));
    }

    /**
     * Get inactive devices
     */
    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get inactive devices", description = "Retrieve devices not seen in last 30 days (Admin only)")
    public ResponseEntity<ApiResponse<Page<DeviceResponse>>> getInactiveDevices(
            @Parameter(description = "Days of inactivity") @RequestParam(defaultValue = "30") Integer days,
            @PageableDefault(size = 20) Pageable pageable) {
        
        String traceId = UUID.randomUUID().toString();
        log.info("Fetching inactive devices - traceId: {}, days: {}", traceId, days);
        
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        Page<DeviceResponse> devices = deviceService.getInactiveDevices(since, pageable);
        return ResponseEntity.ok(ApiResponse.success(devices, traceId));
    }
}