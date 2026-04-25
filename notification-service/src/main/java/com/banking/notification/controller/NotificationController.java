package com.banking.notification.controller;

import com.banking.notification.dto.ApiResponse;
import com.banking.notification.dto.NotificationHistoryResponse;
import com.banking.notification.dto.NotificationQueryRequest;
import com.banking.notification.dto.NotificationStatsResponse;
import com.banking.notification.service.NotificationService;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/notifications")
@Tag(name = "Notifications", description = "Notification history and statistics API")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final Tracer tracer;

    public NotificationController(NotificationService notificationService, Tracer tracer) {
        this.notificationService = notificationService;
        this.tracer = tracer;
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get notification history", description = "Retrieve notification history for the authenticated user")
    public ResponseEntity<ApiResponse<Page<NotificationHistoryResponse>>> getNotificationHistory(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID traceId = getTraceId();

        log.info("Fetching notification history for userId: {}, traceId: {}", userId, traceId);

        NotificationQueryRequest request = new NotificationQueryRequest(
                userId,
                channel != null ? com.banking.notification.domain.NotificationChannel.valueOf(channel) : null,
                status != null ? com.banking.notification.domain.NotificationStatus.valueOf(status) : null,
                startDate != null ? java.time.Instant.parse(startDate) : null,
                endDate != null ? java.time.Instant.parse(endDate) : null,
                page,
                size
        );

        Page<NotificationHistoryResponse> history = notificationService.getNotificationHistory(request);

        return ResponseEntity.ok(ApiResponse.success(history, traceId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get notification by ID", description = "Retrieve a specific notification by its ID")
    public ResponseEntity<ApiResponse<NotificationHistoryResponse>> getNotificationById(
            @PathVariable UUID id
    ) {
        UUID traceId = getTraceId();
        log.info("Fetching notification by ID: {}, traceId: {}", id, traceId);

        NotificationHistoryResponse notification = notificationService.getNotificationById(id);

        return ResponseEntity.ok(ApiResponse.success(notification, traceId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get notification statistics", description = "Retrieve notification statistics for the authenticated user")
    public ResponseEntity<ApiResponse<NotificationStatsResponse>> getNotificationStats(
            Authentication authentication
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID traceId = getTraceId();

        log.info("Fetching notification stats for userId: {}, traceId: {}", userId, traceId);

        NotificationStatsResponse stats = notificationService.getNotificationStats(userId);

        return ResponseEntity.ok(ApiResponse.success(stats, traceId));
    }

    private UUID getTraceId() {
        try {
            if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
                String traceIdHex = tracer.currentSpan().context().traceId();
                return UUID.fromString(
                        traceIdHex.substring(0, 8) + "-" +
                                traceIdHex.substring(8, 12) + "-" +
                                traceIdHex.substring(12, 16) + "-" +
                                traceIdHex.substring(16, 20) + "-" +
                                traceIdHex.substring(20, 32)
                );
            }
        } catch (Exception e) {
            log.debug("Could not extract trace ID from tracer", e);
        }
        return UUID.randomUUID();
    }
}
