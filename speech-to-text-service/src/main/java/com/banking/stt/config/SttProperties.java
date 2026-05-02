package com.banking.stt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for Speech-to-Text Service.
 * Binds application.yml properties to Java objects.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "stt")
public class SttProperties {

    private WhisperConfig whisper = new WhisperConfig();
    private AudioConfig audio = new AudioConfig();
    private StorageConfig storage = new StorageConfig();
    private ProcessingConfig processing = new ProcessingConfig();

    @Data
    public static class WhisperConfig {
        private String apiKey;
        private String apiUrl = "https://api.openai.com/v1/audio/transcriptions";
        private String model = "whisper-1";
        private Integer timeoutSeconds = 120;
        private Integer maxRetries = 3;
        private Integer retryDelayMs = 1000;
    }

    @Data
    public static class AudioConfig {
        private Integer maxFileSizeMb = 25;
        private Integer maxDurationMinutes = 30;
        private List<String> supportedFormats = List.of("mp3", "wav", "m4a", "flac", "ogg", "webm");
        private Integer sampleRate = 16000;
        private Integer channels = 1;
    }

    @Data
    public static class StorageConfig {
        private String type = "minio";
        private String endpoint;
        private String bucket = "banking-audio-files";
        private String accessKey;
        private String secretKey;
        private Integer urlExpirationSeconds = 3600;
    }

    @Data
    public static class ProcessingConfig {
        private Integer maxConcurrentJobs = 10;
        private Boolean enableDiarizationByDefault = false;
        private Integer defaultExpectedSpeakers = 2;
        private Double minConfidenceThreshold = 50.0;
    }
}
