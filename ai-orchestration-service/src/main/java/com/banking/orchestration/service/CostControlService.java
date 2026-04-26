package com.banking.orchestration.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface CostControlService {

    void checkBudget(UUID userId, BigDecimal estimatedCost);

    void recordCost(UUID userId, BigDecimal actualCost);

    BigDecimal calculateCost(String modelName, Integer inputTokens, Integer outputTokens);

    void resetDailyBudgets();

    void resetMonthlyBudgets();
}
