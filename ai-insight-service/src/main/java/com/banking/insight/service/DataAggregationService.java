package com.banking.insight.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface DataAggregationService {

    List<Map<String, Object>> getUserTransactions(UUID userId, Instant startDate, Instant endDate);

    Map<String, BigDecimal> getCategorySpending(UUID userId, Instant startDate, Instant endDate);

    Map<String, Object> getUserProfile(UUID userId);

    List<Map<String, Object>> getUserAccounts(UUID userId);

    BigDecimal getTotalBalance(UUID userId);

    Integer getTransactionCount(UUID userId, Instant startDate, Instant endDate);
}
