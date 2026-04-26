package com.banking.rag.controller;

import com.banking.rag.domain.RagQuery;
import com.banking.rag.dto.ApiResponse;
import com.banking.rag.exception.QueryNotFoundException;
import com.banking.rag.repository.RagQueryRepository;
import com.banking.rag.util.SecurityUtil;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/rag/queries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Query History", description = "Query history and analytics endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class QueryController {

    private final RagQueryRepository queryRepository;
    private final Tracer tracer;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get query by ID", description = "Get query details by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Query retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Query not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<RagQuery>> getQueryById(@PathVariable UUID id) {
        log.info("Fetching query: {}", id);

        RagQuery query = queryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new QueryNotFoundException(id));

        return ResponseEntity.ok(ApiResponse.success(query, getTraceId()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get query history", description = "Get paginated query history for current user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Queries retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<Page<RagQuery>>> getQueryHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Fetching query history for user");

        UUID userId = SecurityUtil.getCurrentUserId();
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RagQuery> queries = queryRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(queries, getTraceId()));
    }

    private String getTraceId() {
        if (tracer.currentSpan() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
