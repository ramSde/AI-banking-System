package com.banking.stt.controller;

import com.banking.stt.dto.RealtimeTranscriptionMessage;
import com.banking.stt.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

/**
 * WebSocket controller for real-time speech-to-text transcription.
 * Handles streaming audio transcription via WebSocket connections.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RealtimeTranscriptionController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/transcribe")
    @SendTo("/topic/transcriptions")
    public WebSocketMessage<RealtimeTranscriptionMessage> handleTranscription(
            byte[] audioData,
            SimpMessageHeaderAccessor headerAccessor) {

        log.info("Received real-time transcription request");

        try {
            // Extract user from session
            String sessionId = headerAccessor.getSessionId();
            UUID userId = extractUserIdFromSession(headerAccessor);

            log.info("Processing audio chunk for session: {}, user: {}", sessionId, userId);

            // In production, this would:
            // 1. Buffer audio chunks
            // 2. Send to Whisper API in real-time mode
            // 3. Return partial transcriptions as they become available

            // For now, return a mock response
            RealtimeTranscriptionMessage transcription = RealtimeTranscriptionMessage.builder()
                    .sessionId(sessionId)
                    .text("Real-time transcription in progress...")
                    .isFinal(false)
                    .confidence(0.85)
                    .timestamp(Instant.now())
                    .build();

            return WebSocketMessage.<RealtimeTranscriptionMessage>builder()
                    .type("TRANSCRIPTION")
                    .payload(transcription)
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Real-time transcription failed", e);

            return WebSocketMessage.<RealtimeTranscriptionMessage>builder()
                    .type("ERROR")
                    .error("Transcription failed: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @MessageMapping("/transcribe/start")
    public void startTranscription(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Starting real-time transcription session: {}", sessionId);

        // Initialize transcription session
        WebSocketMessage<String> response = WebSocketMessage.<String>builder()
                .type("SESSION_STARTED")
                .payload("Transcription session started")
                .timestamp(Instant.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/transcriptions",
                response
        );
    }

    @MessageMapping("/transcribe/stop")
    public void stopTranscription(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        log.info("Stopping real-time transcription session: {}", sessionId);

        // Cleanup transcription session
        WebSocketMessage<String> response = WebSocketMessage.<String>builder()
                .type("SESSION_STOPPED")
                .payload("Transcription session stopped")
                .timestamp(Instant.now())
                .build();

        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/transcriptions",
                response
        );
    }

    private UUID extractUserIdFromSession(SimpMessageHeaderAccessor headerAccessor) {
        // In production, extract from JWT token in WebSocket handshake
        // For now, generate a session-based UUID
        String sessionId = headerAccessor.getSessionId();
        return UUID.nameUUIDFromBytes(sessionId.getBytes());
    }
}
