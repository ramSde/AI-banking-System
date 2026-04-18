package com.banking.identity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Identity Service Configuration Properties
 * 
 * Binds configuration from application.yml banking.identity.* properties.
 */
@Configuration
@ConfigurationProperties(prefix = "banking.identity")
@Getter
@Setter
public class IdentityProperties {

    private JwtProperties jwt = new JwtProperties();
    private PasswordProperties password = new PasswordProperties();
    private SecurityProperties security = new SecurityProperties();

    @Getter
    @Setter
    public static class JwtProperties {
        private String privateKey;
        private String publicKey;
        private String issuer;
        private String audience;
        private Integer accessTokenTtlMinutes;
        private Integer refreshTokenTtlDays;
    }

    @Getter
    @Setter
    public static class PasswordProperties {
        private Integer bcryptStrength;
        private Integer minLength;
        private Boolean requireUppercase;
        private Boolean requireLowercase;
        private Boolean requireDigit;
        private Boolean requireSpecial;
    }

    @Getter
    @Setter
    public static class SecurityProperties {
        private Integer maxLoginAttempts;
        private Integer lockoutDurationMinutes;
    }
}
