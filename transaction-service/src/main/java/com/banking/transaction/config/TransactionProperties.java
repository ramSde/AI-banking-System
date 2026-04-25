package com.banking.transaction.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Transaction Service Configuration Properties
 * 
 * Externalized configuration for transaction service behavior.
 */
@Configuration
@ConfigurationProperties(prefix = "transaction")
@Data
public class TransactionProperties {

    private Reference reference = new Reference();
    private Idempotency idempotency = new Idempotency();
    private Limits limits = new Limits();
    private Hold hold = new Hold();

    @Data
    public static class Reference {
        private String prefix = "TXN";
    }

    @Data
    public static class Idempotency {
        private int ttlHours = 24;
    }

    @Data
    public static class Limits {
        private BigDecimal maxAmount = new BigDecimal("1000000.00");
        private BigDecimal dailyLimitUser = new BigDecimal("50000.00");
    }

    @Data
    public static class Hold {
        private int expiryHours = 72;
    }
}
