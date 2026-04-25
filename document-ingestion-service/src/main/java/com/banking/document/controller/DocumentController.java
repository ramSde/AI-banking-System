package com.banking.document.controller;

import com.banking.document.domain.DocumentType;
import com.banking.document.domain.ProcessingStatus;
import com.banking.document.dto.*;
import com.banking.document.service.DocumentService;
import com.banking.document.util.JwtValidator;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/documents")
@Tag(name = "Document Management", description = "APIs for document upload, processing, and retrieval")
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final JwtValidator jwtValidator;
    private final Tracer tracer;

    public DocumentController(DocumentService documentService, JwtValidator jwtValidator, Tracer tracer) {
        this.documentService = documentService;
        this.jwtValidator = jwtValidator;
        this.tracer = tracer;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Upload a document", description = "Upload a document for processing and vector storage")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Document uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413", description = "File size exceeded")
    })
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @Parameter(description = "Document file to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Document type") @RequestParam("documentType") DocumentType documentType,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        log.info("Upload document request - userId: {}, filename: {}, type: {}, traceId: {}",
                userId, file.getOriginalFilename(), documentType, traceId);

        DocumentResponse response = documentService.uploadDocument(file, documentType, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, traceId));
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get document by ID", description = "Retrieve document metadata by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(
            @Parameter(description = "Document ID") @PathVariable UUID documentId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        log.info("Get document request - documentId: {}, userId: {}, traceId: {}", documentId, userId, traceId);

        DocumentResponse response = documentService.getDocument(documentId, userId);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get user documents", description = "Retrieve paginated list of user documents")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getUserDocuments(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") Sort.Direction direction,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<DocumentResponse> response = documentService.getUserDocuments(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get documents by status", description = "Retrieve documents filtered by processing status")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getDocumentsByStatus(
            @Parameter(description = "Processing status") @PathVariable ProcessingStatus status,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentResponse> response = documentService.getUserDocumentsByStatus(userId, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/type/{documentType}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get documents by type", description = "Retrieve documents filtered by document type")
    public ResponseEntity<ApiResponse<Page<DocumentResponse>>> getDocumentsByType(
            @Parameter(description = "Document type") @PathVariable DocumentType documentType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<DocumentResponse> response = documentService.getUserDocumentsByType(userId, documentType, pageable);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @PostMapping("/search")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Search documents", description = "Search documents using semantic similarity")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> searchDocuments(
            @Valid @RequestBody DocumentSearchRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        log.info("Search documents request - userId: {}, query: {}, traceId: {}", userId, request.getQuery(), traceId);

        List<DocumentResponse> response = documentService.searchDocuments(request, userId);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get document statistics", description = "Retrieve document statistics for the user")
    public ResponseEntity<ApiResponse<DocumentStatsResponse>> getDocumentStats(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        DocumentStatsResponse response = documentService.getUserDocumentStats(userId);

        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/{documentId}/download-url")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get document download URL", description = "Generate a pre-signed URL for document download")
    public ResponseEntity<ApiResponse<String>> getDownloadUrl(
            @Parameter(description = "Document ID") @PathVariable UUID documentId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        String downloadUrl = documentService.getDocumentDownloadUrl(documentId, userId);

        return ResponseEntity.ok(ApiResponse.success(downloadUrl, traceId));
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete document", description = "Soft delete a document and its associated data")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Document deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Document not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable UUID documentId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtValidator.extractUserId(authHeader);
        String traceId = getTraceId();

        log.info("Delete document request - documentId: {}, userId: {}, traceId: {}", documentId, userId, traceId);

        documentService.deleteDocument(documentId, userId);

        return ResponseEntity.noContent().build();
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
