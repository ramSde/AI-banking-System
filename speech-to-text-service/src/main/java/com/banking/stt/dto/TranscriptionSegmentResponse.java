package com.banking.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for transcription segment.
 * Represents a time-stamped segment of transcription.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionSegmentResponse {

    /**
     * Segment ID
     */
    private UUID id;

    /**
     * Segment index (order)
     */
    private Integer segmentIndex;

    /**
     * Start time in seconds
     */
    private BigDecimal startTimeSeconds;

    /**
     * End time in seconds
     */
    private BigDecimal endTimeSeconds;

    /**
     * Duration in seconds
     */
    private BigDecimal durationSeconds;

    /**
     * Transcribed text
     */
    private String text;

    /**
     * Speaker ID (if diarization enabled)
     */
    private String speakerId;

    /**
     * Confidence score (0-100)
     */
    private BigDecimal confidenceScore;

    /**
     * Word count
     */
    private Integer wordCount;

    /**
     * Formatted time range (MM:SS - MM:SS)
     */
    private String timeRange;
}
