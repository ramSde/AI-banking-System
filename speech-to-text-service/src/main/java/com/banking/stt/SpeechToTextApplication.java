package com.banking.stt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for Speech-to-Text Service.
 * Provides audio transcription and speech recognition capabilities for the banking platform.
 *
 * Features:
 * - Audio file upload and transcription
 * - Real-time speech recognition via WebSocket
 * - Multiple language support
 * - Speaker diarization
 * - Transcript export (PDF, TXT, JSON)
 * - Integration with OpenAI Whisper API
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableKafka
@EnableAsync
public class SpeechToTextApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpeechToTextApplication.class, args);
    }
}
