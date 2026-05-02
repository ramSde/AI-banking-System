package com.banking.stt.exception;

/**
 * Exception thrown when an unsupported audio format is provided.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public class UnsupportedAudioFormatException extends SttException {

    public UnsupportedAudioFormatException(String format) {
        super("Unsupported audio format: " + format + ". Supported formats: mp3, wav, m4a, flac, ogg, webm",
                "UNSUPPORTED_AUDIO_FORMAT");
    }

    public UnsupportedAudioFormatException(String message, Throwable cause) {
        super(message, "UNSUPPORTED_AUDIO_FORMAT", cause);
    }
}
