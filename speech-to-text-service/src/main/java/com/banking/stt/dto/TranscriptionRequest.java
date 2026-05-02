package com.banking.stt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for triggering transcription.
 * Used when transcription needs to be re-triggered or configured.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionRequest {

    /**
     * Audio file ID to transcribe
     */
    @NotNull(message = "Audio file ID is required")
    private UUID audioFileId;

    /**
     * Language code (optional, will be auto-detected if not provided)
     */
    @Pattern(regexp = "^[a-z]{2}$", message = "Language code must be a 2-letter ISO 639-1 code")
    private String languageCode;

    /**
     * Enable speaker diarization
     */
    @Builder.Default
    private Boolean enableDiarization = false;

    /**
     * Expected number of speakers
     */
    private Integer expectedSpeakers;

    /**
     * Enable timestamps
     */
    @Builder.Default
    private Boolean enableTimestamps = true;

    /**
     * Model to use (default: whisper-1)
     */
    private String model;
}
