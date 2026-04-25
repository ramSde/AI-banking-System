package com.banking.audit.controller;

import com.banking.audit.domain.AuditEvent;
import com.banking.audit.domain.EntityType;
import com.banking.audit.domain.EventType;
import com.banking.audit.dto.ApiResponse;
import com.banking.audit.dto.AuditEventResponse;
import com.banking.audit.dto.AuditQueryRequest;
import com.banking.audit.mapper.AuditMapper;
import com.banking.audit.service.AuditService;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/audit")
@Tag(name = "Audit", description = "Audit trail management API")
@SecurityRequirement(name = "Bearer Authentication")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);
    private final AuditService auditService;
    private final AuditMapper auditMapper;
    private final Tracer tracer;

    public AuditController(AuditService auditService, AuditMapper auditMapper, Tracer tracer) {
        this.auditService = auditService;
        this.auditMapper = auditMapper;
        this.tracer = tracer;
    }

    @PostMapping("/events/query")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    @Operation(summary = "Query audit events", description = "Query audit events with flexible filtering criteria")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit events"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid query parameters"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<AuditEventResponse>>> queryAuditEvents(
            @Valid @RequestBody AuditQueryRequest request
    ) {
        logger.info("Querying audit events with request: {}", request);

        Page<AuditEvent> auditEvents = auditService.queryAuditEvents(request);
        Page<AuditEventResponse> response = auditEvents.map(auditMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/events/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit event by ID", description = "Retrieve a specific audit event by its ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit event"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Audit event not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<AuditEventResponse>> getAuditEventById(
            @Parameter(description = "Audit event ID") @PathVariable UUID id
    ) {
        logger.info("Fetching audit event by id: {}", id);

        AuditEvent auditEvent = auditService.getAuditEventById(id);
        AuditEventResponse response = auditMapper.toResponse(auditEvent);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/events/event-id/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit event by event ID", description = "Retrieve a specific audit event by its event ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit event"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Audit event not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<AuditEventResponse>> getAuditEventByEventId(
            @Parameter(description = "Event ID") @PathVariable String eventId
    ) {
        logger.info("Fetching audit event by eventId: {}", eventId);

        AuditEvent auditEvent = auditService.getAuditEventByEventId(eventId);
        AuditEventResponse response = auditMapper.toResponse(auditEvent);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/events/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit events by entity", description = "Retrieve audit events for a specific entity")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit events"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<AuditEventResponse>>> getAuditEventsByEntity(
            @Parameter(description = "Entity type") @PathVariable EntityType entityType,
            @Parameter(description = "Entity ID") @PathVariable String entityId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("Fetching audit events by entity: entityType={}, entityId={}, page={}, size={}",
                entityType, entityId, page, size);

        Page<AuditEvent> auditEvents = auditService.getAuditEventsByEntity(entityType, entityId, page, size);
        Page<AuditEventResponse> response = auditEvents.map(auditMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/events/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit events by user", description = "Retrieve audit events for a specific user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit events"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<AuditEventResponse>>> getAuditEventsByUser(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("Fetching audit events by user: userId={}, page={}, size={}", userId, page, size);

        Page<AuditEvent> auditEvents = auditService.getAuditEventsByUser(userId, page, size);
        Page<AuditEventResponse> response = auditEvents.map(auditMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/events/type/{eventType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit events by event type", description = "Retrieve audit events by event type")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit events"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Page<AuditEventResponse>>> getAuditEventsByEventType(
            @Parameter(description = "Event type") @PathVariable EventType eventType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        logger.info("Fetching audit events by event type: eventType={}, page={}, size={}", eventType, page, size);

        Page<AuditEvent> auditEvents = auditService.getAuditEventsByEventType(eventType, page, size);
        Page<AuditEventResponse> response = auditEvents.map(auditMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/trace/{traceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit events by trace ID", description = "Retrieve all audit events for a distributed trace")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit events"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<AuditEventResponse>>> getAuditEventsByTraceId(
            @Parameter(description = "Trace ID") @PathVariable String traceId
    ) {
        logger.info("Fetching audit events by trace ID: traceId={}", traceId);

        List<AuditEvent> auditEvents = auditService.getAuditEventsByTraceId(traceId);
        List<AuditEventResponse> response = auditMapper.toResponseList(auditEvents);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/correlation/{correlationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPORT')")
    @Operation(summary = "Get audit events by correlation ID", description = "Retrieve all related audit events by correlation ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved audit events"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<List<AuditEventResponse>>> getAuditEventsByCorrelationId(
            @Parameter(description = "Correlation ID") @PathVariable String correlationId
    ) {
        logger.info("Fetching audit events by correlation ID: correlationId={}", correlationId);

        List<AuditEvent> auditEvents = auditService.getAuditEventsByCorrelationId(correlationId);
        List<AuditEventResponse> response = auditMapper.toResponseList(auditEvents);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/admin/count/entity/{entityType}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count audit events by entity", description = "Count total audit events for a specific entity")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved count"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Long>> countAuditEventsByEntity(
            @Parameter(description = "Entity type") @PathVariable EntityType entityType,
            @Parameter(description = "Entity ID") @PathVariable String entityId
    ) {
        logger.info("Counting audit events by entity: entityType={}, entityId={}", entityType, entityId);

        long count = auditService.countAuditEventsByEntity(entityType, entityId);

        return ResponseEntity.ok(ApiResponse.success(count, getTraceId()));
    }

    @GetMapping("/admin/count/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Count audit events by user and date range", description = "Count audit events for a user within a date range")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved count"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Long>> countAuditEventsByUserAndDateRange(
            @Parameter(description = "User ID") @PathVariable UUID userId,
            @Parameter(description = "From date (ISO 8601)") @RequestParam Instant fromDate,
            @Parameter(description = "To date (ISO 8601)") @RequestParam Instant toDate
    ) {
        logger.info("Counting audit events by user and date range: userId={}, fromDate={}, toDate={}",
                userId, fromDate, toDate);

        long count = auditService.countAuditEventsByUserAndDateRange(userId, fromDate, toDate);

        return ResponseEntity.ok(ApiResponse.success(count, getTraceId()));
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }
}
