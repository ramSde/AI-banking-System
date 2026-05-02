package com.banking.stt.exception;

/**
 * Exception thrown when transcription processing fails.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public class TranscriptionFailedException extends SttException {

    public TranscriptionFailedException(String message) {
        super(message, "TRANSCRIPTION_FAILED");
    }

    public TranscriptionFailedException(String message, Throwable cause) {
        super(message, "TRANSCRIPTION_FAILED", cause);
    }
}
