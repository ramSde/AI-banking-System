package com.banking.stt.service;

import com.banking.stt.dto.LanguageDetectionResponse;

import java.io.File;
import java.util.List;

/**
 * Service interface for language detection.
 * Detects the language spoken in audio files.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
public interface LanguageDetectionService {

    /**
     * Detect language from audio file.
     *
     * @param audioFile Audio file
     * @return Detected language code (ISO 639-1)
     */
    String detectLanguage(File audioFile);

    /**
     * Detect language with confidence scores.
     *
     * @param audioFile Audio file
     * @return Language detection response with confidence
     */
    LanguageDetectionResponse detectLanguageWithConfidence(File audioFile);

    /**
     * Get list of supported languages.
     *
     * @return List of supported language codes
     */
    List<String> getSupportedLanguages();

    /**
     * Check if language is supported.
     *
     * @param languageCode Language code to check
     * @return true if supported
     */
    boolean isLanguageSupported(String languageCode);
}
