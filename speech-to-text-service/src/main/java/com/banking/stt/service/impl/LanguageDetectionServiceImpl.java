package com.banking.stt.service.impl;

import com.banking.stt.dto.LanguageDetectionResponse;
import com.banking.stt.service.LanguageDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of language detection service.
 * Detects spoken language in audio files.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageDetectionServiceImpl implements LanguageDetectionService {

    // Whisper supports 99 languages, listing most common ones
    private static final List<String> SUPPORTED_LANGUAGES = Arrays.asList(
            "en", "es", "fr", "de", "it", "pt", "nl", "pl", "ru", "zh",
            "ja", "ko", "ar", "hi", "tr", "vi", "th", "id", "ms", "fil"
    );

    @Override
    public String detectLanguage(File audioFile) {
        log.info("Detecting language for audio file: {}", audioFile.getName());

        // In production, use Whisper's language detection or dedicated language detection model
        // For now, return default language
        String detectedLanguage = "en"; // Default to English

        log.info("Detected language: {}", detectedLanguage);
        return detectedLanguage;
    }

    @Override
    public LanguageDetectionResponse detectLanguageWithConfidence(File audioFile) {
        log.info("Detecting language with confidence for audio file: {}", audioFile.getName());

        // In production, call Whisper API with language detection
        // For now, return mock response
        String detectedLanguage = detectLanguage(audioFile);

        return LanguageDetectionResponse.builder()
                .languageCode(detectedLanguage)
                .languageName(getLanguageName(detectedLanguage))
                .confidence(BigDecimal.valueOf(95.0))
                .build();
    }

    @Override
    public List<String> getSupportedLanguages() {
        return SUPPORTED_LANGUAGES;
    }

    @Override
    public boolean isLanguageSupported(String languageCode) {
        if (languageCode == null) {
            return false;
        }
        return SUPPORTED_LANGUAGES.contains(languageCode.toLowerCase());
    }

    private String getLanguageName(String languageCode) {
        return switch (languageCode.toLowerCase()) {
            case "en" -> "English";
            case "es" -> "Spanish";
            case "fr" -> "French";
            case "de" -> "German";
            case "it" -> "Italian";
            case "pt" -> "Portuguese";
            case "nl" -> "Dutch";
            case "pl" -> "Polish";
            case "ru" -> "Russian";
            case "zh" -> "Chinese";
            case "ja" -> "Japanese";
            case "ko" -> "Korean";
            case "ar" -> "Arabic";
            case "hi" -> "Hindi";
            case "tr" -> "Turkish";
            case "vi" -> "Vietnamese";
            case "th" -> "Thai";
            case "id" -> "Indonesian";
            case "ms" -> "Malay";
            case "fil" -> "Filipino";
            default -> "Unknown";
        };
    }
}
