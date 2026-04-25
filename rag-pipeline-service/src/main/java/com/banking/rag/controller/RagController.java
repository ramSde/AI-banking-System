package com.banking.rag.controller;

import com.banking.rag.dto.ApiResponse;
import com.banking.rag.dto.RetrievalRequest;
import com.banking.rag.dto.RetrievalResponse;
import com.banking.rag.service.RagService;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/rag")
@Tag(name = "RAG Pipeline", description = "RAG retrieval, reranking, and context assembly endpoints")
public class RagController {

    private final RagService ragService;
    private final Tracer tracer;

    public RagController(RagService ragService, Tracer tracer) {
        this.ragService = ragService;
        this.tracer = tracer;
    }

    @PostMapping("/retrieve")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Retrieve relevant documents", description = "Performs RAG retrieval with optional reranking and caching")
    public ResponseEntity<ApiResponse<RetrievalResponse>> retrieve(@Valid @RequestBody RetrievalRequest request) {
        String traceId = getTraceId();
        log.info("Received retrieval request, traceId: {}", traceId);

        RetrievalResponse response = ragService.retrieve(request);
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    @GetMapping("/queries/{queryId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get query details", description = "Retrieves query details by ID")
    public ResponseEntity<ApiResponse<RetrievalResponse>> getQuery(@PathVariable UUID queryId) {
        String traceId = getTraceId();
        log.info("Fetching query {}, traceId: {}", queryId, traceId);

        RetrievalResponse response = ragService.getQueryById(queryId);
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }
}
