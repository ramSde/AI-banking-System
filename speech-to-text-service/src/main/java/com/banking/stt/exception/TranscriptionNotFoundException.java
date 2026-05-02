package com.banking.stt.exception;

import java.util.UUID;

/**
 * Exception thrown when a transcription is not found.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public class TranscriptionNotFoundException extends SttException {

    public TranscriptionNotFoundException(UUID transcriptionId) {
        super("Transcription not found with ID: " + transcriptionId, "TRANSCRIPTION_NOT_FOUND");
    }

    public TranscriptionNotFoundException(String message) {
        super(message, "TRANSCRIPTION_NOT_FOUND");
    }
}
