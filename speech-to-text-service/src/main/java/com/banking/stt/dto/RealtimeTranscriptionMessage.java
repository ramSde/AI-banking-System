package com.banking.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for real-time transcription messages via WebSocket.
 * Represents partial or complete transcription results.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeTranscriptionMessage {

    /**
     * Message type: PARTIAL, FINAL, ERROR
     */
    private MessageType type;

    /**
     * Transcribed text
     */
    private String text;

    /**
     * Confidence score (0-100)
     */
    private BigDecimal confidence;

    /**
     * Is this the final result
     */
    private Boolean isFinal;

    /**
     * Detected language
     */
    private String language;

    /**
     * Timestamp
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Error message (if type is ERROR)
     */
    private String errorMessage;

    /**
     * Message type enumeration
     */
    public enum MessageType {
        PARTIAL,  // Partial transcription result
        FINAL,    // Final transcription result
        ERROR     // Error occurred
    }
}
