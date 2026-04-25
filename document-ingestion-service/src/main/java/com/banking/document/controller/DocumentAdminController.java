package com.banking.document.controller;

import com.banking.document.domain.ProcessingStatus;
import com.banking.document.dto.ApiResponse;
import com.banking.document.dto.DocumentResponse;
import com.banking.document.repository.DocumentRepository;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/admin/documents")
@Tag(name = "Document Administration", description = "Admin APIs for document management")
@Slf4j
public class DocumentAdminController {

    private final DocumentRepository documentRepository;
    private final Tracer tracer;

    public DocumentAdminController(DocumentRepository documentRepository, Tracer tracer) {
        this.documentRepository = documentRepository;
        this.tracer = tracer;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all documents by status", description = "Admin endpoint to retrieve all documents by processing status")
    public ResponseEntity<ApiResponse<List<com.banking.document.domain.Document>>> getAllDocumentsByStatus(
            @Parameter(description = "Processing status") @PathVariable ProcessingStatus status) {

        String traceId = getTraceId();
        log.info("Admin: Get all documents by status - status: {}, traceId: {}", status, traceId);

        List<com.banking.document.domain.Document> documents = documentRepository.findByProcessingStatusAndNotDeleted(status);

        return ResponseEntity.ok(ApiResponse.success(documents, traceId));
    }

    @PostMapping("/{documentId}/reprocess")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reprocess document", description = "Admin endpoint to trigger document reprocessing")
    public ResponseEntity<ApiResponse<String>> reprocessDocument(
            @Parameter(description = "Document ID") @PathVariable UUID documentId) {

        String traceId = getTraceId();
        log.info("Admin: Reprocess document - documentId: {}, traceId: {}", documentId, traceId);

        return ResponseEntity.ok(ApiResponse.success("Document reprocessing triggered", traceId));
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
