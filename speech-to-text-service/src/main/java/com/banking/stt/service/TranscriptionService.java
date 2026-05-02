package com.banking.stt.service;

import java.util.UUID;

/**
 * Service interface for transcription operations.
 * Handles interaction with Whisper API for speech-to-text conversion.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public interface TranscriptionService {

    /**
     * Transcribe audio file using Whisper API.
     * This is an async operation that processes the audio and saves results.
     *
     * @param transcriptionId Transcription ID
     */
    void transcribe(UUID transcriptionId);

    /**
     * Retry failed transcription.
     *
     * @param transcriptionId Transcription ID
     */
    void retryTranscription(UUID transcriptionId);

    /**
     * Cancel ongoing transcription.
     *
     * @param transcriptionId Transcription ID
     */
    void cancelTranscription(UUID transcriptionId);
}
