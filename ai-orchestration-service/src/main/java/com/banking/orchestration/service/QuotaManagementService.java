package com.banking.orchestration.service;

import java.util.UUID;

public interface QuotaManagementService {

    void checkQuota(UUID userId, Integer estimatedTokens);

    void recordTokenUsage(UUID userId, Integer actualTokens);

    void resetDailyQuotas();

    void resetMonthlyQuotas();

    Integer getRemainingDailyTokens(UUID userId);

    Integer getRemainingMonthlyTokens(UUID userId);
}
