package com.banking.transaction.service.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Account Service Client
 * 
 * REST client for Account Service integration with circuit breaker and retry.
 * Handles account validation and balance updates.
 */
@Service
public class AccountServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceClient.class);

    private final RestClient restClient;

    @Value("${external.account-service.url}")
    private String accountServiceUrl;

    public AccountServiceClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "getAccountFallback")
    @Retry(name = "accountService")
    public Map<String, Object> getAccount(UUID accountId, String jwtToken) {
        log.debug("Fetching account: {}", accountId);
        try {
            ResponseEntity<Map> response = restClient.get()
                    .uri("/v1/accounts/{id}", accountId)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .toEntity(Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("data")) {
                    return (Map<String, Object>) body.get("data");
                }
                return body;
            }
            throw new RestClientException("Failed to fetch account: " + accountId);
        } catch (Exception e) {
            log.error("Error fetching account: {}", accountId, e);
            throw new RestClientException("Account service unavailable", e);
        }
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "updateBalanceFallback")
    @Retry(name = "accountService")
    public void updateBalance(UUID accountId, BigDecimal amount, String operation, String jwtToken) {
        log.debug("Updating balance for account: {} by {} ({})", accountId, amount, operation);
        try {
            Map<String, Object> request = Map.of(
                    "amount", amount,
                    "operation", operation
            );

            ResponseEntity<Void> response = restClient.put()
                    .uri("/v1/accounts/{id}/balance", accountId)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.NO_CONTENT) {
                throw new RestClientException("Failed to update balance for account: " + accountId);
            }
            log.debug("Balance updated successfully for account: {}", accountId);
        } catch (Exception e) {
            log.error("Error updating balance for account: {}", accountId, e);
            throw new RestClientException("Account service unavailable", e);
        }
    }

    private Map<String, Object> getAccountFallback(UUID accountId, String jwtToken, Exception e) {
        log.error("Circuit breaker fallback triggered for getAccount: {}", accountId, e);
        throw new RestClientException("Account service is currently unavailable. Please try again later.");
    }

    private void updateBalanceFallback(UUID accountId, BigDecimal amount, String operation, String jwtToken, Exception e) {
        log.error("Circuit breaker fallback triggered for updateBalance: {}", accountId, e);
        throw new RestClientException("Account service is currently unavailable. Please try again later.");
    }
}
