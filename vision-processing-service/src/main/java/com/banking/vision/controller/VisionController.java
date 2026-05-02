package com.banking.vision.controller;

import com.banking.vision.domain.DocumentType;
import com.banking.vision.domain.ProcessingStatus;
import com.banking.vision.domain.VisionDocument;
import com.banking.vision.dto.*;
import com.banking.vision.service.VisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for vision processing operations.
 * 
 * Provides endpoints for:
 * - Document upload
 * - Processing status tracking
 * - OCR results retrieval
 * - Extracted data retrieval
 * - Document management
 */
@Slf4j
@RestController
@RequestMapping("/v1/vision")
@RequiredArgsConstructor
@Tag(name = "Vision Processing", description = "Document OCR and vision processing endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VisionController {

    private final VisionService visionService;

    /**
     * Upload document for processing.
     * 
     * Accepts multipart/form-data with file and document type.
     * Returns document ID and initial status.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "Upload document for OCR processing",
        description = "Upload a document (PDF, PNG, JPG, JPEG, TIFF) for OCR and data extraction. " +
                     "Files larger than 1MB are processed asynchronously."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document uploaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> uploadDocument(
        @Parameter(description = "Document file (max 10MB)", required = true)
        @RequestParam("file") MultipartFile file,
        
        @Parameter(description = "Document type", required = true)
        @RequestParam("documentType") DocumentType documentType,
        
        @Parameter(description = "Optional description")
        @RequestParam(value = "description", required = false) String description,
        
        @AuthenticationPrincipal UUID userId
    ) {
        log.info("Upload request from user: {}, type: {}, file: {}", 
            userId, documentType, file.getOriginalFilename());
        
        String traceId = UUID.randomUUID().toString();
        
        VisionDocument document = visionService.uploadDocument(
            file,
            documentType,
            userId,
            description != null ? Map.of("description", description) : null
        );
        
        DocumentUploadResponse response = DocumentUploadResponse.builder()
            .documentId(document.getId())
            .documentType(document.getDocumentType())
            .filename(document.getOriginalFilename())
            .fileSize(document.getFileSize())
            .status(document.getProcessingStatus())
            .uploadedAt(document.getCreatedAt())
            .estimatedCompletionTime(document.getCreatedAt().plusSeconds(30))
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    /**
     * Get document by ID.
     */
    @GetMapping("/documents/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get document by ID", description = "Retrieve document metadata by ID")
    public ResponseEntity<ApiResponse<VisionDocument>> getDocument(
        @Parameter(description = "Document ID", required = true)
        @PathVariable UUID id,
        
        @AuthenticationPrincipal UUID userId
    ) {
        log.debug("Get document request: {} from user: {}", id, userId);
        
        String traceId = UUID.randomUUID().toString();
        VisionDocument document = visionService.getDocument(id, userId);
        
        return ResponseEntity.ok(ApiResponse.success(document, traceId));
    }

    /**
     * Get processing status.
     */
    @GetMapping("/documents/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get processing status", description = "Check document processing status")
    public ResponseEntity<ApiResponse<ProcessingStatusResponse>> getProcessingStatus(
        @Parameter(description = "Document ID", required = true)
        @PathVariable UUID id,
        
        @AuthenticationPrincipal UUID userId
    ) {
        log.debug("Get status request: {} from user: {}", id, userId);
        
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> status = visionService.getProcessingStatus(id, userId);
        
        ProcessingStatusResponse response = ProcessingStatusResponse.builder()
            .documentId((UUID) status.get("documentId"))
            .status((ProcessingStatus) status.get("status"))
            .confidenceScore((Double) status.get("confidenceScore"))
            .pageCount(((Long) status.get("pageCount")).intValue())
            .errorMessage((String) status.get("errorMessage"))
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    /**
     * Get OCR results.
     */
    @GetMapping("/documents/{id}/ocr")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get OCR results", description = "Retrieve raw OCR text extraction results")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOcrResults(
        @Parameter(description = "Document ID", required = true)
        @PathVariable UUID id,
        
        @AuthenticationPrincipal UUID userId
    ) {
        log.debug("Get OCR results request: {} from user: {}", id, userId);
        
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> results = visionService.getOcrResults(id, userId);
        
        return ResponseEntity.ok(ApiResponse.success(results, traceId));
    }

    /**
     * Get extracted structured data.
     */
    @GetMapping("/documents/{id}/extracted")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get extracted data", description = "Retrieve structured data extracted from document")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExtractedData(
        @Parameter(description = "Document ID", required = true)
        @PathVariable UUID id,
        
        @AuthenticationPrincipal UUID userId
    ) {
        log.debug("Get extracted data request: {} from user: {}", id, userId);
        
        String traceId = UUID.randomUUID().toString();
        Map<String, Object> data = visionService.getExtractedData(id, userId);
        
        return ResponseEntity.ok(ApiResponse.success(data, traceId));
    }

    /**
     * List user's documents (paginated).
     */
    @GetMapping("/documents")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "List documents", description = "Get paginated list of user's documents")
    public ResponseEntity<ApiResponse<Page<VisionDocument>>> listDocuments(
        @Parameter(description = "Document type filter")
        @RequestParam(required = false) DocumentType type,
        
        @Parameter(description = "Processing status filter")
        @RequestParam(required = false) ProcessingStatus status,
        
        @Parameter(description = "Start date filter")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
        
        @Parameter(description = "End date filter")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
        
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        
        @AuthenticationPrincipal UUID userId
    ) {
        log.debug("List documents request from user: {}", userId);
        
        String traceId = UUID.randomUUID().toString();
        Page<VisionDocument> documents;
        
        if (type != null) {
            documents = visionService.getUserDocumentsByType(userId, type, pageable);
        } else if (status != null) {
            documents = visionService.getUserDocumentsByStatus(userId, status, pageable);
        } else if (startDate != null && endDate != null) {
            documents = visionService.getUserDocumentsByDateRange(userId, startDate, endDate, pageable);
        } else {
            documents = visionService.getUserDocuments(userId, pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(documents, traceId));
    }

    /**
     * Delete document (soft delete).
     */
    @DeleteMapping("/documents/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Delete document", description = "Soft delete a document")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
        @Parameter(description = "Document ID", required = true)
        @PathVariable UUID id,
        
        @AuthenticationPrincipal UUID userId
    ) {
        log.info("Delete document request: {} from user: {}", id, userId);
        
        String traceId = UUID.randomUUID().toString();
        visionService.deleteDocument(id, userId);
        
        return ResponseEntity.ok(ApiResponse.success(null, traceId));
    }
}
