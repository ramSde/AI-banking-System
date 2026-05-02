package com.banking.stt.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for audio file upload.
 * Represents the metadata accompanying an audio file upload.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioUploadRequest {

    /**
     * Language code (ISO 639-1) - optional, will be auto-detected if not provided
     */
    @Pattern(regexp = "^[a-z]{2}$", message = "Language code must be a 2-letter ISO 639-1 code")
    private String languageCode;

    /**
     * Optional description or notes about the audio
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Enable speaker diarization (identify multiple speakers)
     */
    @Builder.Default
    private Boolean enableDiarization = false;

    /**
     * Expected number of speakers (hint for diarization)
     */
    private Integer expectedSpeakers;

    /**
     * Enable timestamps in transcription
     */
    @Builder.Default
    private Boolean enableTimestamps = true;
}
