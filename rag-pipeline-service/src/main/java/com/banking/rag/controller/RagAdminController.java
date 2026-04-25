package com.banking.rag.controller;

import com.banking.rag.dto.ApiResponse;
import com.banking.rag.dto.CacheStatsResponse;
import com.banking.rag.service.SemanticCacheService;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/rag/admin")
@Tag(name = "RAG Admin", description = "RAG administration endpoints")
public class RagAdminController {

    private final SemanticCacheService semanticCacheService;
    private final Tracer tracer;

    public RagAdminController(SemanticCacheService semanticCacheService, Tracer tracer) {
        this.semanticCacheService = semanticCacheService;
        this.tracer = tracer;
    }

    @GetMapping("/cache/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cache statistics", description = "Retrieves semantic cache statistics")
    public ResponseEntity<ApiResponse<CacheStatsResponse>> getCacheStats() {
        String traceId = getTraceId();
        log.info("Fetching cache stats, traceId: {}", traceId);

        CacheStatsResponse stats = semanticCacheService.getCacheStats();
        return ResponseEntity.ok(ApiResponse.success(stats, traceId));
    }

    @DeleteMapping("/cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear cache", description = "Clears all semantic cache entries")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        String traceId = getTraceId();
        log.info("Clearing cache, traceId: {}", traceId);

        semanticCacheService.clearCache();
        return ResponseEntity.ok(ApiResponse.success("Cache cleared successfully", traceId));
    }

    @DeleteMapping("/cache/expired")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove expired cache entries", description = "Removes expired cache entries")
    public ResponseEntity<ApiResponse<String>> removeExpiredEntries() {
        String traceId = getTraceId();
        log.info("Removing expired cache entries, traceId: {}", traceId);

        int removed = semanticCacheService.removeExpiredEntries();
        return ResponseEntity.ok(ApiResponse.success("Removed " + removed + " expired entries", traceId));
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "no-trace-id";
    }
}
