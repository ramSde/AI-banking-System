package com.banking.otp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for OTP service
 */
@Configuration
@ConfigurationProperties(prefix = "otp")
@Data
public class OtpProperties {

    private int length = 6;
    private int ttlSeconds = 300;
    private int maxAttempts = 3;
    private RateLimit rateLimit = new RateLimit();
    private Totp totp = new Totp();
    private BackupCode backupCode = new BackupCode();
    private Bcrypt bcrypt = new Bcrypt();

    @Data
    public static class RateLimit {
        private int perUser = 5;
        private int windowSeconds = 300;
    }

    @Data
    public static class Totp {
        private String issuer = "BankingPlatform";
        private int periodSeconds = 30;
        private int digits = 6;
        private String algorithm = "HmacSHA1";
        private int timeStepTolerance = 1;
    }

    @Data
    public static class BackupCode {
        private int length = 12;
        private int count = 8;
    }

    @Data
    public static class Bcrypt {
        private int strength = 12;
    }
}
