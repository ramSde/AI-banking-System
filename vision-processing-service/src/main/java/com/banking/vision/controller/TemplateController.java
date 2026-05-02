package com.banking.vision.controller;

import com.banking.vision.domain.DocumentType;
import com.banking.vision.domain.ExtractionTemplate;
import com.banking.vision.dto.ApiResponse;
import com.banking.vision.repository.ExtractionTemplateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing extraction templates.
 * Provides admin endpoints for CRUD operations on document extraction templates.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@RestController
@RequestMapping("/v1/vision/templates")
@RequiredArgsConstructor
@Tag(name = "Template Management", description = "Admin APIs for managing extraction templates")
@SecurityRequirement(name = "bearerAuth")
public class TemplateController {

    private final ExtractionTemplateRepository templateRepository;

    /**
     * Get all extraction templates.
     *
     * @return List of all templates
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Get all templates", description = "Retrieve all extraction templates")
    public ResponseEntity<ApiResponse<List<ExtractionTemplate>>> getAllTemplates() {
        log.info("Fetching all extraction templates");

        List<ExtractionTemplate> templates = templateRepository.findAll();

        return ResponseEntity.ok(ApiResponse.success(
                templates,
                "Retrieved " + templates.size() + " templates"
        ));
    }

    /**
     * Get template by ID.
     *
     * @param id Template ID
     * @return Template details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Get template by ID", description = "Retrieve a specific extraction template")
    public ResponseEntity<ApiResponse<ExtractionTemplate>> getTemplateById(@PathVariable Long id) {
        log.info("Fetching template with ID: {}", id);

        return templateRepository.findById(id)
                .map(template -> ResponseEntity.ok(ApiResponse.success(
                        template,
                        "Template retrieved successfully"
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found with ID: " + id)));
    }

    /**
     * Get templates by document type.
     *
     * @param documentType Document type
     * @return List of templates for the document type
     */
    @GetMapping("/type/{documentType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Get templates by type", description = "Retrieve templates for a specific document type")
    public ResponseEntity<ApiResponse<List<ExtractionTemplate>>> getTemplatesByType(
            @PathVariable DocumentType documentType) {
        log.info("Fetching templates for document type: {}", documentType);

        List<ExtractionTemplate> templates = templateRepository.findByDocumentType(documentType);

        return ResponseEntity.ok(ApiResponse.success(
                templates,
                "Retrieved " + templates.size() + " templates for " + documentType
        ));
    }

    /**
     * Get active templates by document type.
     *
     * @param documentType Document type
     * @return List of active templates
     */
    @GetMapping("/type/{documentType}/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @Operation(summary = "Get active templates", description = "Retrieve active templates for a document type")
    public ResponseEntity<ApiResponse<List<ExtractionTemplate>>> getActiveTemplatesByType(
            @PathVariable DocumentType documentType) {
        log.info("Fetching active templates for document type: {}", documentType);

        List<ExtractionTemplate> templates = templateRepository.findByDocumentTypeAndIsActiveTrue(documentType);

        return ResponseEntity.ok(ApiResponse.success(
                templates,
                "Retrieved " + templates.size() + " active templates"
        ));
    }

    /**
     * Create new extraction template.
     *
     * @param request Template creation request
     * @return Created template
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create template", description = "Create a new extraction template")
    public ResponseEntity<ApiResponse<ExtractionTemplate>> createTemplate(
            @Valid @RequestBody TemplateRequest request) {
        log.info("Creating new template for document type: {}", request.documentType());

        ExtractionTemplate template = ExtractionTemplate.builder()
                .name(request.name())
                .documentType(request.documentType())
                .fieldPatterns(request.fieldPatterns())
                .isActive(request.isActive() != null ? request.isActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ExtractionTemplate savedTemplate = templateRepository.save(template);

        log.info("Created template with ID: {}", savedTemplate.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        savedTemplate,
                        "Template created successfully"
                ));
    }

    /**
     * Update existing template.
     *
     * @param id      Template ID
     * @param request Template update request
     * @return Updated template
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update template", description = "Update an existing extraction template")
    public ResponseEntity<ApiResponse<ExtractionTemplate>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRequest request) {
        log.info("Updating template with ID: {}", id);

        return templateRepository.findById(id)
                .map(template -> {
                    template.setName(request.name());
                    template.setDocumentType(request.documentType());
                    template.setFieldPatterns(request.fieldPatterns());
                    if (request.isActive() != null) {
                        template.setActive(request.isActive());
                    }
                    template.setUpdatedAt(LocalDateTime.now());

                    ExtractionTemplate updatedTemplate = templateRepository.save(template);

                    return ResponseEntity.ok(ApiResponse.success(
                            updatedTemplate,
                            "Template updated successfully"
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found with ID: " + id)));
    }

    /**
     * Activate/deactivate template.
     *
     * @param id       Template ID
     * @param isActive Active status
     * @return Updated template
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update template status", description = "Activate or deactivate a template")
    public ResponseEntity<ApiResponse<ExtractionTemplate>> updateTemplateStatus(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        log.info("Updating template {} status to: {}", id, isActive);

        return templateRepository.findById(id)
                .map(template -> {
                    template.setActive(isActive);
                    template.setUpdatedAt(LocalDateTime.now());

                    ExtractionTemplate updatedTemplate = templateRepository.save(template);

                    return ResponseEntity.ok(ApiResponse.success(
                            updatedTemplate,
                            "Template status updated successfully"
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Template not found with ID: " + id)));
    }

    /**
     * Delete template.
     *
     * @param id Template ID
     * @return Success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete template", description = "Delete an extraction template")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("Deleting template with ID: {}", id);

        if (!templateRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Template not found with ID: " + id));
        }

        templateRepository.deleteById(id);

        return ResponseEntity.ok(ApiResponse.success(
                null,
                "Template deleted successfully"
        ));
    }

    /**
     * Template request DTO.
     */
    public record TemplateRequest(
            String name,
            DocumentType documentType,
            Map<String, String> fieldPatterns,
            Boolean isActive
    ) {
    }
}
