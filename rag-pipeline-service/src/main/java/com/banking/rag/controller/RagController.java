package com.banking.rag.controller;

import com.banking.rag.dto.*;
import com.banking.rag.service.RetrievalService;
import com.banking.rag.service.RerankingService;
import com.banking.rag.service.SemanticCacheService;
import com.banking.rag.util.SecurityUtil;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/rag")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RAG Pipeline", description = "RAG retrieval, reranking, and context assembly endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class RagController {

    private final RetrievalService retrievalService;
    private final RerankingService rerankingService;
    private final SemanticCacheService semanticCacheService;
    private final Tracer tracer;

    @PostMapping("/retrieve")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Retrieve relevant documents", description = "Retrieve and rank relevant documents for a query")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<RetrievalResponse>> retrieve(@Valid @RequestBody RetrievalRequest request) {
        log.info("Received retrieval request: {}", request.queryText());

        UUID userId = SecurityUtil.getCurrentUserId();
        RetrievalResponse response = retrievalService.retrieve(request, userId);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @PostMapping("/rerank")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Rerank documents", description = "Rerank a list of documents based on relevance to query")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Documents reranked successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<RerankResponse>> rerank(@Valid @RequestBody RerankRequest request) {
        log.info("Received rerank request for {} documents", request.documents().size());

        RerankResponse response = rerankingService.rerank(request);

        return ResponseEntity.ok(ApiResponse.success(response, getTraceId()));
    }

    @GetMapping("/cache/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get cache statistics", description = "Get semantic cache statistics")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cache stats retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<CacheStatsResponse>> getCacheStats() {
        log.info("Fetching cache statistics");

        CacheStatsResponse stats = semanticCacheService.getCacheStats();

        return ResponseEntity.ok(ApiResponse.success(stats, getTraceId()));
    }

    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear cache", description = "Clear all semantic cache entries (Admin only)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cache cleared successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<ApiResponse<Void>> clearCache() {
        log.info("Clearing semantic cache");

        semanticCacheService.clearCache();

        return ResponseEntity.ok(ApiResponse.success(null, getTraceId()));
    }

    private String getTraceId() {
        if (tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
