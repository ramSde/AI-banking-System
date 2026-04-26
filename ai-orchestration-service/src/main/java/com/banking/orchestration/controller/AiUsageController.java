package com.banking.orchestration.controller;

import com.banking.orchestration.domain.AiUsage;
import com.banking.orchestration.dto.ApiResponse;
import com.banking.orchestration.dto.BudgetStatusResponse;
import com.banking.orchestration.dto.UsageStatsResponse;
import com.banking.orchestration.mapper.AiUsageMapper;
import com.banking.orchestration.repository.AiBudgetRepository;
import com.banking.orchestration.repository.AiQuotaRepository;
import com.banking.orchestration.service.QuotaManagementService;
import com.banking.orchestration.service.UsageTrackingService;
import io.micrometer.tracing.Tracer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ai")
@Tag(name = "AI Usage", description = "AI usage tracking and budget management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AiUsageController {

    private static final Logger logger = LoggerFactory.getLogger(AiUsageController.class);
    
    private final UsageTrackingService usageTrackingService;
    private final QuotaManagementService quotaManagementService;
    private final AiBudgetRepository aiBudgetRepository;
    private final AiQuotaRepository aiQuotaRepository;
    private final AiUsageMapper aiUsageMapper;
    private final Tracer tracer;

    public AiUsageController(UsageTrackingService usageTrackingService,
                             QuotaManagementService quotaManagementService,
                             AiBudgetRepository aiBudgetRepository,
                             AiQuotaRepository aiQuotaRepository,
                             AiUsageMapper aiUsageMapper,
                             Tracer tracer) {
        this.usageTrackingService = usageTrackingService;
        this.quotaManagementService = quotaManagementService;
        this.aiBudgetRepository = aiBudgetRepository;
        this.aiQuotaRepository = aiQuotaRepository;
        this.aiUsageMapper = aiUsageMapper;
        this.tracer = tracer;
    }

    @GetMapping("/usage/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user's AI usage", description = "Returns paginated AI usage history for the authenticated user")
    public ResponseEntity<ApiResponse<Page<AiUsage>>> getUserUsage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Authentication authentication) {
        
        String traceId = getTraceId();
        UUID userId = UUID.fromString(authentication.getName());
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        
        Page<AiUsage> usage;
        if (startDate != null && endDate != null) {
            usage = usageTrackingService.getUserUsageByDateRange(userId, startDate, endDate, pageable);
        } else {
            usage = usageTrackingService.getUserUsage(userId, pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(usage, traceId));
    }

    @GetMapping("/usage/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user's usage statistics", description = "Returns aggregated usage statistics for the authenticated user")
    public ResponseEntity<ApiResponse<UsageStatsResponse>> getUserStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            Authentication authentication) {
        
        String traceId = getTraceId();
        UUID userId = UUID.fromString(authentication.getName());
        
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }
        
        UsageStatsResponse stats = usageTrackingService.getUserStats(userId, startDate, endDate);
        
        return ResponseEntity.ok(ApiResponse.success(stats, traceId));
    }

    @GetMapping("/usage/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users' AI usage", description = "Returns paginated AI usage for all users (admin only)")
    public ResponseEntity<ApiResponse<Page<AiUsage>>> getAllUsage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        String traceId = getTraceId();
        
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        Page<AiUsage> usage = usageTrackingService.getAllUsage(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(usage, traceId));
    }

    @GetMapping("/budget/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user's budget status", description = "Returns current budget and quota status for the authenticated user")
    public ResponseEntity<ApiResponse<BudgetStatusResponse>> getBudgetStatus(Authentication authentication) {
        String traceId = getTraceId();
        UUID userId = UUID.fromString(authentication.getName());
        
        var budget = aiBudgetRepository.findByUserIdAndNotDeleted(userId).orElse(null);
        var quota = aiQuotaRepository.findByUserIdAndNotDeleted(userId).orElse(null);
        
        if (budget == null || quota == null) {
            return ResponseEntity.ok(ApiResponse.success(null, traceId));
        }
        
        BigDecimal dailyRemaining = budget.getDailyBudgetUsd().subtract(budget.getDailySpentUsd());
        BigDecimal monthlyRemaining = budget.getMonthlyBudgetUsd().subtract(budget.getMonthlySpentUsd());
        
        BigDecimal dailyUsagePercentage = budget.getDailySpentUsd()
                .divide(budget.getDailyBudgetUsd(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        BigDecimal monthlyUsagePercentage = budget.getMonthlySpentUsd()
                .divide(budget.getMonthlyBudgetUsd(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        Integer dailyTokensRemaining = quotaManagementService.getRemainingDailyTokens(userId);
        Integer monthlyTokensRemaining = quotaManagementService.getRemainingMonthlyTokens(userId);
        
        BudgetStatusResponse response = new BudgetStatusResponse(
                budget.getDailyBudgetUsd(),
                budget.getDailySpentUsd(),
                dailyRemaining,
                dailyUsagePercentage,
                budget.getMonthlyBudgetUsd(),
                budget.getMonthlySpentUsd(),
                monthlyRemaining,
                monthlyUsagePercentage,
                quota.getDailyTokenLimit(),
                quota.getDailyTokensUsed(),
                dailyTokensRemaining,
                quota.getMonthlyTokenLimit(),
                quota.getMonthlyTokensUsed(),
                monthlyTokensRemaining,
                quota.getUserTier(),
                dailyRemaining.compareTo(BigDecimal.ZERO) <= 0 || monthlyRemaining.compareTo(BigDecimal.ZERO) <= 0,
                dailyTokensRemaining <= 0 || monthlyTokensRemaining <= 0,
                budget.getDailyResetAt(),
                budget.getMonthlyResetAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, traceId));
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
