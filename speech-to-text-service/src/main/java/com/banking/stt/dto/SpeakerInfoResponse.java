package com.banking.stt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO for speaker information.
 * Contains aggregated information about a speaker from diarization.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakerInfoResponse {

    /**
     * Speaker identifier
     */
    private String speakerId;

    /**
     * Total speaking time in seconds
     */
    private BigDecimal totalSpeakingTimeSeconds;

    /**
     * Speaking time as percentage of total
     */
    private BigDecimal speakingPercentage;

    /**
     * Number of segments
     */
    private Integer segmentCount;

    /**
     * Total word count
     */
    private Integer wordCount;

    /**
     * Average words per segment
     */
    private Double averageWordsPerSegment;

    /**
     * Average confidence score
     */
    private BigDecimal averageConfidence;

    /**
     * List of segment indices where this speaker spoke
     */
    private List<Integer> segmentIndices;
}
