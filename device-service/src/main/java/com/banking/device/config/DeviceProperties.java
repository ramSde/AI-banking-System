package com.banking.device.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for device service.
 * Binds device-specific configuration from application.yml.
 */
@Configuration
@ConfigurationProperties(prefix = "device")
@Data
public class DeviceProperties {

    private Fingerprint fingerprint = new Fingerprint();
    private Trust trust = new Trust();
    private Cache cache = new Cache();
    private Anomaly anomaly = new Anomaly();

    @Data
    public static class Fingerprint {
        private String algorithm = "SHA-256";
        private String salt = "banking-device-salt-2024";
    }

    @Data
    public static class Trust {
        private Integer initialScore = 30;
        private Integer maxScore = 100;
        private Integer minScore = 0;
        private Integer incrementOnSuccess = 5;
        private Integer decrementOnFailure = 10;
    }

    @Data
    public static class Cache {
        private Integer ttlMinutes = 5;
    }

    @Data
    public static class Anomaly {
        private Integer impossibleTravelSpeedKmh = 800;
        private Integer maxLocationChangeKm = 500;
    }
}