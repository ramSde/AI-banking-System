package com.banking.fraud.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Fraud Detection Configuration Properties
 * 
 * Externalized configuration for fraud detection thresholds and rules.
 */
@Configuration
@ConfigurationProperties(prefix = "fraud")
@Data
public class FraudProperties {

    private Scoring scoring = new Scoring();
    private Velocity velocity = new Velocity();
    private Amount amount = new Amount();

    @Data
    public static class Scoring {
        /**
         * High risk threshold (default: 70)
         */
        private int highRiskThreshold = 70;

        /**
         * Medium risk threshold (default: 30)
         */
        private int mediumRiskThreshold = 30;

        /**
         * Auto-block threshold (default: 85)
         */
        private int autoBlockThreshold = 85;
    }

    @Data
    public static class Velocity {
        /**
         * Transaction count window in minutes (default: 60)
         */
        private int transactionCountWindowMinutes = 60;

        /**
         * Max transactions per window (default: 10)
         */
        private int maxTransactionsPerWindow = 10;
    }

    @Data
    public static class Amount {
        /**
         * Large transaction threshold (default: 10000.00)
         */
        private BigDecimal largeTransactionThreshold = new BigDecimal("10000.00");

        /**
         * Suspicious amount threshold (default: 50000.00)
         */
        private BigDecimal suspiciousAmountThreshold = new BigDecimal("50000.00");
    }
}
