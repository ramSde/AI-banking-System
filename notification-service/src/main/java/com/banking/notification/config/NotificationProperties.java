package com.banking.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationProperties {

    private EmailConfig email = new EmailConfig();
    private SmsConfig sms = new SmsConfig();
    private PushConfig push = new PushConfig();
    private RetryConfig retry = new RetryConfig();
    private RateLimitConfig rateLimit = new RateLimitConfig();
    private DeduplicationConfig deduplication = new DeduplicationConfig();

    @Data
    public static class EmailConfig {
        private String from;
        private String fromName;
        private Boolean enabled = true;
    }

    @Data
    public static class SmsConfig {
        private Boolean enabled = true;
        private String provider = "twilio";
    }

    @Data
    public static class PushConfig {
        private Boolean enabled = true;
        private String provider = "fcm";
    }

    @Data
    public static class RetryConfig {
        private Integer maxAttempts = 3;
        private Long initialInterval = 1000L;
        private Double multiplier = 2.0;
        private Long maxInterval = 10000L;
    }

    @Data
    public static class RateLimitConfig {
        private Integer emailPerUserPerHour = 50;
        private Integer smsPerUserPerHour = 10;
        private Integer pushPerUserPerHour = 100;
    }

    @Data
    public static class DeduplicationConfig {
        private Integer windowSeconds = 300;
    }
}
