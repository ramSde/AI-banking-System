package com.banking.chat.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "chat")
@Getter
@Setter
public class ChatProperties {

    private Integer maxHistoryMessages = 20;
    private Integer sessionTimeoutMinutes = 30;
    private Integer maxMessageLength = 4000;
    private Integer contextWindowTokens = 8000;
    private Boolean enableStreaming = true;
    private RateLimit rateLimit = new RateLimit();

    @Getter
    @Setter
    public static class RateLimit {
        private Integer messagesPerMinute = 20;
        private Integer messagesPerHour = 100;
    }
}
