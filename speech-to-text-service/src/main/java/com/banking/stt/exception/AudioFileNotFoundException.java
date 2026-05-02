package com.banking.stt.exception;

import java.util.UUID;

/**
 * Exception thrown when an audio file is not found.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public class AudioFileNotFoundException extends SttException {

    public AudioFileNotFoundException(UUID audioFileId) {
        super("Audio file not found with ID: " + audioFileId, "AUDIO_FILE_NOT_FOUND");
    }

    public AudioFileNotFoundException(String message) {
        super(message, "AUDIO_FILE_NOT_FOUND");
    }
}
