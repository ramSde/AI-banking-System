package com.banking.stt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for exporting transcription.
 * Specifies the export format and options.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionExportRequest {

    /**
     * Export format: pdf, txt, json, srt, vtt
     */
    @NotBlank(message = "Format is required")
    @Pattern(regexp = "^(pdf|txt|json|srt|vtt)$", 
             message = "Format must be one of: pdf, txt, json, srt, vtt")
    private String format;

    /**
     * Include timestamps in export
     */
    @Builder.Default
    private Boolean includeTimestamps = true;

    /**
     * Include speaker labels in export
     */
    @Builder.Default
    private Boolean includeSpeakers = true;

    /**
     * Include confidence scores in export
     */
    @Builder.Default
    private Boolean includeConfidence = false;

    /**
     * Include metadata (filename, duration, etc.)
     */
    @Builder.Default
    private Boolean includeMetadata = true;
}
