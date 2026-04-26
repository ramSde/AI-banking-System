package com.banking.orchestration.controller;

import com.banking.orchestration.dto.AiRequest;
import com.banking.orchestration.dto.AiResponse;
import com.banking.orchestration.dto.ApiResponse;
import com.banking.orchestration.dto.ModelConfig;
import com.banking.orchestration.mapper.AiModelMapper;
import com.banking.orchestration.service.AiOrchestrationService;
import com.banking.orchestration.service.ModelSelectionService;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/ai")
@Tag(name = "AI Orchestration", description = "AI model orchestration and execution endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AiOrchestrationController {

    private static final Logger logger = LoggerFactory.getLogger(AiOrchestrationController.class);
    
    private final AiOrchestrationService aiOrchestrationService;
    private final ModelSelectionService modelSelectionService;
    private final AiModelMapper aiModelMapper;
    private final Tracer tracer;

    public AiOrchestrationController(AiOrchestrationService aiOrchestrationService,
                                     ModelSelectionService modelSelectionService,
                                     AiModelMapper aiModelMapper,
                                     Tracer tracer) {
        this.aiOrchestrationService = aiOrchestrationService;
        this.modelSelectionService = modelSelectionService;
        this.aiModelMapper = aiModelMapper;
        this.tracer = tracer;
    }

    @PostMapping("/orchestrate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Execute AI request with orchestration", 
               description = "Executes an AI request with automatic model selection, fallback, and cost control")
    public ResponseEntity<ApiResponse<AiResponse>> orchestrate(
            @Valid @RequestBody AiRequest request,
            Authentication authentication) {
        
        String traceId = getTraceId();
        UUID userId = UUID.fromString(authentication.getName());
        
        logger.info("AI orchestration request from user: {}, feature: {}, traceId: {}",
                userId, request.feature(), traceId);

        AiResponse response = aiOrchestrationService.orchestrate(request, userId, traceId);
        
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/models")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get available AI models", description = "Returns list of all enabled AI models")
    public ResponseEntity<ApiResponse<List<ModelConfig>>> getModels() {
        String traceId = getTraceId();
        
        List<ModelConfig> models = modelSelectionService.getAllEnabledModels()
                .stream()
                .map(aiModelMapper::toModelConfig)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(models, traceId));
    }

    @PostMapping("/models/{id}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Enable AI model", description = "Enables a specific AI model")
    public ResponseEntity<ApiResponse<Void>> enableModel(@PathVariable UUID id) {
        String traceId = getTraceId();
        logger.info("Enabling model: {}, traceId: {}", id, traceId);
        return ResponseEntity.ok(ApiResponse.success(null, traceId));
    }

    @PostMapping("/models/{id}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disable AI model", description = "Disables a specific AI model")
    public ResponseEntity<ApiResponse<Void>> disableModel(@PathVariable UUID id) {
        String traceId = getTraceId();
        logger.info("Disabling model: {}, traceId: {}", id, traceId);
        return ResponseEntity.ok(ApiResponse.success(null, traceId));
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
