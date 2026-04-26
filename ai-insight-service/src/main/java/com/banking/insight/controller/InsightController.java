package com.banking.insight.controller;

import com.banking.insight.dto.*;
import com.banking.insight.mapper.InsightMapper;
import com.banking.insight.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/insights")
public class InsightController {

    private final InsightService insightService;
    private final PatternAnalysisService patternAnalysisService;
    private final AnomalyDetectionService anomalyDetectionService;
    private final RecommendationService recommendationService;
    private final ForecastService forecastService;
    private final InsightMapper insightMapper;

    public InsightController(
        final InsightService insightService,
        final PatternAnalysisService patternAnalysisService,
        final AnomalyDetectionService anomalyDetectionService,
        final RecommendationService recommendationService,
        final ForecastService forecastService,
        final InsightMapper insightMapper
    ) {
        this.insightService = insightService;
        this.patternAnalysisService = patternAnalysisService;
        this.anomalyDetectionService = anomalyDetectionService;
        this.recommendationService = recommendationService;
        this.forecastService = forecastService;
        this.insightMapper = insightMapper;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InsightResponse>> generateInsights(
        @Valid @RequestBody final InsightRequest request,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var insight = insightService.generateInsights(request);
        return ResponseEntity.ok(ApiResponse.success("Insights generated successfully", insightMapper.toResponse(insight)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<InsightResponse>>> getUserInsights(
        @RequestParam final UUID userId,
        final Pageable pageable,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var insights = insightService.getUserInsights(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(insights.map(insightMapper::toResponse)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InsightResponse>> getInsightById(
        @PathVariable final UUID id,
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var insight = insightService.getInsightById(id, userId);
        return ResponseEntity.ok(ApiResponse.success(insightMapper.toResponse(insight)));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InsightSummaryResponse>> getInsightSummary(
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var summary = insightService.getInsightSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @GetMapping("/spending-patterns")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SpendingPatternResponse>>> getSpendingPatterns(
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var patterns = patternAnalysisService.getRecurringPatterns(userId);
        return ResponseEntity.ok(ApiResponse.success(patterns.stream().map(insightMapper::toPatternResponse).toList()));
    }

    @GetMapping("/anomalies")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> getAnomalies(
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var anomalies = anomalyDetectionService.getUnacknowledgedAnomalies(userId);
        return ResponseEntity.ok(ApiResponse.success(anomalies.stream().map(insightMapper::toAnomalyResponse).toList()));
    }

    @PostMapping("/anomalies/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AnomalyResponse>> acknowledgeAnomaly(
        @PathVariable final UUID id,
        @RequestParam final UUID userId,
        @RequestParam(required = false) final String notes,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var anomaly = anomalyDetectionService.acknowledgeAnomaly(id, userId, notes);
        return ResponseEntity.ok(ApiResponse.success("Anomaly acknowledged", insightMapper.toAnomalyResponse(anomaly)));
    }

    @GetMapping("/recommendations")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RecommendationResponse>>> getRecommendations(
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var recommendations = recommendationService.getActionableRecommendations(userId);
        return ResponseEntity.ok(ApiResponse.success(recommendations.stream().map(insightMapper::toRecommendationResponse).toList()));
    }

    @PostMapping("/recommendations/{id}/accept")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RecommendationResponse>> acceptRecommendation(
        @PathVariable final UUID id,
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var recommendation = recommendationService.acceptRecommendation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recommendation accepted", insightMapper.toRecommendationResponse(recommendation)));
    }

    @PostMapping("/recommendations/{id}/dismiss")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<RecommendationResponse>> dismissRecommendation(
        @PathVariable final UUID id,
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var recommendation = recommendationService.dismissRecommendation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Recommendation dismissed", insightMapper.toRecommendationResponse(recommendation)));
    }

    @GetMapping("/forecast")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ForecastResponse>>> getForecast(
        @RequestParam final UUID userId,
        @RequestParam(defaultValue = "3") final int months,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var forecasts = forecastService.forecastMonthlySpending(userId, months);
        return ResponseEntity.ok(ApiResponse.success(forecasts));
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<InsightResponse>> markAsRead(
        @PathVariable final UUID id,
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        final var insight = insightService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Insight marked as read", insightMapper.toResponse(insight)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInsight(
        @PathVariable final UUID id,
        @RequestParam final UUID userId,
        @AuthenticationPrincipal final UserDetails userDetails
    ) {
        insightService.deleteInsight(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Insight deleted successfully", null));
    }
}
