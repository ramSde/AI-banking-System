package com.banking.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Generic WebSocket message wrapper.
 * Used for bidirectional communication in real-time transcription.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {

    /**
     * Message type
     */
    private String type;

    /**
     * Message payload (JSON)
     */
    private Object payload;

    /**
     * Session ID
     */
    private String sessionId;

    /**
     * Timestamp
     */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /**
     * Create a message of specific type.
     *
     * @param type    Message type
     * @param payload Payload data
     * @return WebSocketMessage instance
     */
    public static WebSocketMessage of(String type, Object payload) {
        return WebSocketMessage.builder()
                .type(type)
                .payload(payload)
                .timestamp(Instant.now())
                .build();
    }
}
