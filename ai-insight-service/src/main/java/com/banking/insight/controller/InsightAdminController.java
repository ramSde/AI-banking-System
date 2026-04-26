package com.banking.insight.controller;

import com.banking.insight.dto.ApiResponse;
import com.banking.insight.dto.InsightResponse;
import com.banking.insight.mapper.InsightMapper;
import com.banking.insight.service.InsightService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/insights/admin")
@PreAuthorize("hasRole('ADMIN')")
public class InsightAdminController {

    private final InsightService insightService;
    private final InsightMapper insightMapper;

    public InsightAdminController(
        final InsightService insightService,
        final InsightMapper insightMapper
    ) {
        this.insightService = insightService;
        this.insightMapper = insightMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InsightResponse>>> getAllInsights(final Pageable pageable) {
        final var insights = insightService.getAllInsights(pageable);
        return ResponseEntity.ok(ApiResponse.success(insights.map(insightMapper::toResponse)));
    }
}
