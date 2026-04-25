package com.banking.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for User Service.
 * Binds application.yml user.* properties to Java objects.
 */
@Configuration
@ConfigurationProperties(prefix = "user")
@Data
public class UserProperties {

    private Encryption encryption = new Encryption();
    private Cache cache = new Cache();
    private Kyc kyc = new Kyc();

    @Data
    public static class Encryption {
        private String algorithm = "AES/GCM/NoPadding";
        private String key;
    }

    @Data
    public static class Cache {
        private Integer profileTtlMinutes = 15;
        private Integer preferenceTtlMinutes = 30;
    }

    @Data
    public static class Kyc {
        private Integer documentMaxSizeMb = 10;
        private Integer verificationExpiryDays = 365;
    }
}
