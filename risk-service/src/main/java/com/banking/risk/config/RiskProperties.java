package com.banking.risk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for risk assessment.
 * Maps risk.* properties from application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "risk")
@Data
public class RiskProperties {

    private Thresholds thresholds = new Thresholds();
    private Cache cache = new Cache();
    private Weights weights = new Weights();

    @Data
    public static class Thresholds {
        private int low = 30;
        private int medium = 60;
        private int high = 100;
    }

    @Data
    public static class Cache {
        private int ttlSeconds = 300; // 5 minutes
    }

    @Data
    public static class Weights {
        private int newDevice = 25;
        private int newLocation = 20;
        private int velocity = 15;
        private int timeOfDay = 10;
        private int failedAttempts = 30;
    }
}
