package com.banking.stt.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a segment of a transcription.
 * Stores individual time-stamped segments with speaker information.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Entity
@Table(name = "transcription_segments")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Reference to the parent transcription
     */
    @Column(name = "transcription_id", nullable = false)
    private UUID transcriptionId;

    /**
     * Segment index (order in the transcription)
     */
    @Column(name = "segment_index", nullable = false)
    private Integer segmentIndex;

    /**
     * Start time in seconds
     */
    @Column(name = "start_time_seconds", nullable = false, precision = 10, scale = 3)
    private BigDecimal startTimeSeconds;

    /**
     * End time in seconds
     */
    @Column(name = "end_time_seconds", nullable = false, precision = 10, scale = 3)
    private BigDecimal endTimeSeconds;

    /**
     * Transcribed text for this segment
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    /**
     * Speaker identifier (for diarization)
     */
    @Column(name = "speaker_id", length = 50)
    private String speakerId;

    /**
     * Confidence score for this segment (0-100)
     */
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    /**
     * Creation timestamp
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Get segment duration in seconds.
     *
     * @return duration in seconds
     */
    public BigDecimal getDurationSeconds() {
        return endTimeSeconds.subtract(startTimeSeconds);
    }

    /**
     * Get formatted time range (MM:SS - MM:SS).
     *
     * @return formatted time range
     */
    public String getFormattedTimeRange() {
        return formatTime(startTimeSeconds) + " - " + formatTime(endTimeSeconds);
    }

    /**
     * Format time in seconds to MM:SS format.
     *
     * @param seconds Time in seconds
     * @return formatted time string
     */
    private String formatTime(BigDecimal seconds) {
        int totalSeconds = seconds.intValue();
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Get word count for this segment.
     *
     * @return word count
     */
    public int getWordCount() {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }

    /**
     * Check if this segment has a speaker assigned.
     *
     * @return true if speaker ID is present
     */
    public boolean hasSpeaker() {
        return speakerId != null && !speakerId.trim().isEmpty();
    }
}
