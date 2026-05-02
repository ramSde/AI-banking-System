package com.banking.stt.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Value object representing speaker information from diarization.
 * Not a JPA entity - used for in-memory speaker analysis.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakerInfo {

    /**
     * Speaker identifier (e.g., "Speaker 1", "Speaker 2")
     */
    private String speakerId;

    /**
     * Total speaking time in seconds
     */
    private BigDecimal totalSpeakingTimeSeconds;

    /**
     * Number of segments spoken by this speaker
     */
    private Integer segmentCount;

    /**
     * Total word count for this speaker
     */
    private Integer wordCount;

    /**
     * Average confidence score for this speaker
     */
    private BigDecimal averageConfidence;

    /**
     * List of segment indices where this speaker spoke
     */
    @Builder.Default
    private List<Integer> segmentIndices = new ArrayList<>();

    /**
     * Get speaking time as percentage of total.
     *
     * @param totalDuration Total audio duration
     * @return percentage of speaking time
     */
    public BigDecimal getSpeakingPercentage(BigDecimal totalDuration) {
        if (totalDuration == null || totalDuration.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalSpeakingTimeSeconds
                .divide(totalDuration, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Get average words per segment.
     *
     * @return average words per segment
     */
    public Double getAverageWordsPerSegment() {
        if (segmentCount == null || segmentCount == 0) {
            return 0.0;
        }
        return wordCount.doubleValue() / segmentCount;
    }

    /**
     * Add a segment to this speaker's information.
     *
     * @param segmentIndex Segment index
     * @param duration     Segment duration
     * @param words        Word count
     * @param confidence   Confidence score
     */
    public void addSegment(Integer segmentIndex, BigDecimal duration, Integer words, BigDecimal confidence) {
        if (segmentIndices == null) {
            segmentIndices = new ArrayList<>();
        }
        segmentIndices.add(segmentIndex);

        if (totalSpeakingTimeSeconds == null) {
            totalSpeakingTimeSeconds = BigDecimal.ZERO;
        }
        totalSpeakingTimeSeconds = totalSpeakingTimeSeconds.add(duration);

        if (segmentCount == null) {
            segmentCount = 0;
        }
        segmentCount++;

        if (wordCount == null) {
            wordCount = 0;
        }
        wordCount += words;

        // Update average confidence
        if (averageConfidence == null) {
            averageConfidence = confidence;
        } else {
            BigDecimal totalConfidence = averageConfidence.multiply(BigDecimal.valueOf(segmentCount - 1));
            totalConfidence = totalConfidence.add(confidence);
            averageConfidence = totalConfidence.divide(BigDecimal.valueOf(segmentCount), 2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
