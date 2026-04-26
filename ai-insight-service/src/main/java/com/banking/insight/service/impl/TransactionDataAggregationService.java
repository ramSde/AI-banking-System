package com.banking.insight.service.impl;

import com.banking.insight.exception.ServiceUnavailableException;
import com.banking.insight.service.DataAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionDataAggregationService implements DataAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDataAggregationService.class);

    private final WebClient transactionServiceWebClient;
    private final WebClient accountServiceWebClient;

    public TransactionDataAggregationService(
        @Qualifier("transactionServiceWebClient") final WebClient transactionServiceWebClient,
        @Qualifier("accountServiceWebClient") final WebClient accountServiceWebClient
    ) {
        this.transactionServiceWebClient = transactionServiceWebClient;
        this.accountServiceWebClient = accountServiceWebClient;
    }

    @Override
    public List<Map<String, Object>> getUserTransactions(
        final UUID userId,
        final Instant startDate,
        final Instant endDate
    ) {
        logger.debug("Fetching transactions for user: {}", userId);
        
        return transactionServiceWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/transactions")
                .queryParam("userId", userId)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
            .onErrorResume(e -> {
                logger.error("Failed to fetch transactions for user: {}", userId, e);
                return Mono.error(new ServiceUnavailableException("Transaction service unavailable", e));
            })
            .block();
    }

    @Override
    public Map<String, BigDecimal> getCategorySpending(
        final UUID userId,
        final Instant startDate,
        final Instant endDate
    ) {
        logger.debug("Fetching category spending for user: {}", userId);
        
        return transactionServiceWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/transactions/category-spending")
                .queryParam("userId", userId)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, BigDecimal>>() {})
            .onErrorResume(e -> {
                logger.error("Failed to fetch category spending for user: {}", userId, e);
                return Mono.error(new ServiceUnavailableException("Transaction service unavailable", e));
            })
            .block();
    }

    @Override
    public Map<String, Object> getUserProfile(final UUID userId) {
        logger.debug("Fetching user profile: {}", userId);
        
        return accountServiceWebClient
            .get()
            .uri("/v1/users/{userId}", userId)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .onErrorResume(e -> {
                logger.error("Failed to fetch user profile: {}", userId, e);
                return Mono.error(new ServiceUnavailableException("Account service unavailable", e));
            })
            .block();
    }

    @Override
    public List<Map<String, Object>> getUserAccounts(final UUID userId) {
        logger.debug("Fetching accounts for user: {}", userId);
        
        return accountServiceWebClient
            .get()
            .uri("/v1/accounts?userId={userId}", userId)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
            .onErrorResume(e -> {
                logger.error("Failed to fetch accounts for user: {}", userId, e);
                return Mono.error(new ServiceUnavailableException("Account service unavailable", e));
            })
            .block();
    }

    @Override
    public BigDecimal getTotalBalance(final UUID userId) {
        logger.debug("Fetching total balance for user: {}", userId);
        
        final List<Map<String, Object>> accounts = getUserAccounts(userId);
        return accounts.stream()
            .map(account -> new BigDecimal(account.get("balance").toString()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Integer getTransactionCount(
        final UUID userId,
        final Instant startDate,
        final Instant endDate
    ) {
        logger.debug("Fetching transaction count for user: {}", userId);
        
        return transactionServiceWebClient
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/transactions/count")
                .queryParam("userId", userId)
                .queryParam("startDate", startDate)
                .queryParam("endDate", endDate)
                .build())
            .retrieve()
            .bodyToMono(Integer.class)
            .onErrorResume(e -> {
                logger.error("Failed to fetch transaction count for user: {}", userId, e);
                return Mono.error(new ServiceUnavailableException("Transaction service unavailable", e));
            })
            .block();
    }
}
