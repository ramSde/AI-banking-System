package com.banking.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for language detection.
 * Contains detected language and confidence scores.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDetectionResponse {

    /**
     * Detected language code (ISO 639-1)
     */
    private String languageCode;

    /**
     * Language name
     */
    private String languageName;

    /**
     * Confidence score (0-100)
     */
    private BigDecimal confidence;

    /**
     * Alternative language detections
     */
    private List<LanguageCandidate> alternatives;

    /**
     * Language candidate with confidence
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageCandidate {
        private String languageCode;
        private String languageName;
        private BigDecimal confidence;
    }
}
