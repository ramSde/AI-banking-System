package com.banking.stt.exception;

/**
 * Exception thrown when an audio file is invalid or corrupted.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public class InvalidAudioFileException extends SttException {

    public InvalidAudioFileException(String message) {
        super(message, "INVALID_AUDIO_FILE");
    }

    public InvalidAudioFileException(String message, Throwable cause) {
        super(message, "INVALID_AUDIO_FILE", cause);
    }
}
