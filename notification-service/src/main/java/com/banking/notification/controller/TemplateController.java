package com.banking.notification.controller;

import com.banking.notification.dto.ApiResponse;
import com.banking.notification.dto.TemplateCreateRequest;
import com.banking.notification.dto.TemplateResponse;
import com.banking.notification.dto.TemplateUpdateRequest;
import com.banking.notification.service.TemplateService;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/templates")
@Tag(name = "Templates", description = "Notification template management API")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class TemplateController {

    private final TemplateService templateService;
    private final Tracer tracer;

    public TemplateController(TemplateService templateService, Tracer tracer) {
        this.templateService = templateService;
        this.tracer = tracer;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create notification template", description = "Create a new notification template")
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request
    ) {
        UUID traceId = getTraceId();
        log.info("Creating template with code: {}, traceId: {}", request.templateCode(), traceId);

        TemplateResponse response = templateService.createTemplate(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, traceId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update notification template", description = "Update an existing notification template")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody TemplateUpdateRequest request
    ) {
        UUID traceId = getTraceId();
        log.info("Updating template with ID: {}, traceId: {}", id, traceId);

        TemplateResponse response = templateService.updateTemplate(id, request);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get template by ID", description = "Retrieve a notification template by its ID")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplateById(
            @PathVariable UUID id
    ) {
        UUID traceId = getTraceId();
        log.info("Fetching template by ID: {}, traceId: {}", id, traceId);

        TemplateResponse response = templateService.getTemplateById(id);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/code/{templateCode}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get template by code", description = "Retrieve a notification template by its code")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplateByCode(
            @PathVariable String templateCode
    ) {
        UUID traceId = getTraceId();
        log.info("Fetching template by code: {}, traceId: {}", templateCode, traceId);

        TemplateResponse response = templateService.getTemplateByCode(templateCode);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all templates", description = "Retrieve all notification templates with pagination")
    public ResponseEntity<ApiResponse<Page<TemplateResponse>>> getAllTemplates(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        UUID traceId = getTraceId();
        log.info("Fetching all templates, page: {}, size: {}, traceId: {}", page, size, traceId);

        Pageable pageable = PageRequest.of(page, size);
        Page<TemplateResponse> templates = templateService.getAllTemplates(pageable);

        return ResponseEntity.ok(ApiResponse.success(templates, traceId));
    }

    @GetMapping("/channel/{channel}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get templates by channel", description = "Retrieve notification templates by channel")
    public ResponseEntity<ApiResponse<Page<TemplateResponse>>> getTemplatesByChannel(
            @PathVariable String channel,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        UUID traceId = getTraceId();
        log.info("Fetching templates by channel: {}, traceId: {}", channel, traceId);

        Pageable pageable = PageRequest.of(page, size);
        Page<TemplateResponse> templates = templateService.getTemplatesByChannel(
                com.banking.notification.domain.NotificationChannel.valueOf(channel), pageable);

        return ResponseEntity.ok(ApiResponse.success(templates, traceId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete template", description = "Soft delete a notification template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(
            @PathVariable UUID id
    ) {
        UUID traceId = getTraceId();
        log.info("Deleting template with ID: {}, traceId: {}", id, traceId);

        templateService.deleteTemplate(id);

        return ResponseEntity.ok(ApiResponse.success(null, traceId));
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
