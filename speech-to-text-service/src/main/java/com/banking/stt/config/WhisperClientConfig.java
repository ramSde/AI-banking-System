package com.banking.stt.config;

import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for OpenAI Whisper API client.
 * Configures the OpenAI service for speech-to-text operations.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Configuration
@RequiredArgsConstructor
public class WhisperClientConfig {

    private final SttProperties sttProperties;

    @Bean
    public OpenAiService openAiService() {
        String apiKey = sttProperties.getWhisper().getApiKey();
        Duration timeout = Duration.ofSeconds(sttProperties.getWhisper().getTimeoutSeconds());
        
        return new OpenAiService(apiKey, timeout);
    }
}
