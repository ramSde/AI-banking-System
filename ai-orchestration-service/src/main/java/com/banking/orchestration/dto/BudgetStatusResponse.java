package com.banking.orchestration.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BudgetStatusResponse(
        BigDecimal dailyBudgetUsd,
        BigDecimal dailySpentUsd,
        BigDecimal dailyRemainingUsd,
        BigDecimal dailyUsagePercentage,
        BigDecimal monthlyBudgetUsd,
        BigDecimal monthlySpentUsd,
        BigDecimal monthlyRemainingUsd,
        BigDecimal monthlyUsagePercentage,
        Integer dailyTokenLimit,
        Integer dailyTokensUsed,
        Integer dailyTokensRemaining,
        Integer monthlyTokenLimit,
        Integer monthlyTokensUsed,
        Integer monthlyTokensRemaining,
        String userTier,
        Boolean budgetExceeded,
        Boolean quotaExceeded,
        Instant dailyResetAt,
        Instant monthlyResetAt
) {
}
