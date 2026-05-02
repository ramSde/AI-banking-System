package com.banking.stt.repository;

import com.banking.stt.domain.TranscriptionSegment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for TranscriptionSegment entity.
 * Provides database access methods for transcription segments.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Repository
public interface TranscriptionSegmentRepository extends JpaRepository<TranscriptionSegment, UUID> {

    /**
     * Find all segments for a transcription ordered by segment index.
     *
     * @param transcriptionId Transcription ID
     * @return List of segments
     */
    List<TranscriptionSegment> findByTranscriptionIdOrderBySegmentIndexAsc(UUID transcriptionId);

    /**
     * Find segments for a transcription with pagination.
     *
     * @param transcriptionId Transcription ID
     * @param pageable        Pagination parameters
     * @return Page of segments
     */
    Page<TranscriptionSegment> findByTranscriptionId(UUID transcriptionId, Pageable pageable);

    /**
     * Find segments by speaker ID.
     *
     * @param transcriptionId Transcription ID
     * @param speakerId       Speaker ID
     * @return List of segments
     */
    List<TranscriptionSegment> findByTranscriptionIdAndSpeakerIdOrderBySegmentIndexAsc(
            UUID transcriptionId,
            String speakerId
    );

    /**
     * Count segments for a transcription.
     *
     * @param transcriptionId Transcription ID
     * @return Number of segments
     */
    long countByTranscriptionId(UUID transcriptionId);

    /**
     * Get total word count for a transcription.
     *
     * @param transcriptionId Transcription ID
     * @return Total word count
     */
    @Query("SELECT SUM(LENGTH(s.text) - LENGTH(REPLACE(s.text, ' ', '')) + 1) " +
            "FROM TranscriptionSegment s WHERE s.transcriptionId = :transcriptionId")
    Long getTotalWordCount(@Param("transcriptionId") UUID transcriptionId);

    /**
     * Find segments with confidence score below threshold.
     *
     * @param transcriptionId Transcription ID
     * @param threshold       Confidence threshold
     * @return List of low-confidence segments
     */
    @Query("SELECT s FROM TranscriptionSegment s WHERE s.transcriptionId = :transcriptionId " +
            "AND s.confidenceScore < :threshold ORDER BY s.segmentIndex ASC")
    List<TranscriptionSegment> findLowConfidenceSegments(
            @Param("transcriptionId") UUID transcriptionId,
            @Param("threshold") Double threshold
    );

    /**
     * Get distinct speaker IDs for a transcription.
     *
     * @param transcriptionId Transcription ID
     * @return List of speaker IDs
     */
    @Query("SELECT DISTINCT s.speakerId FROM TranscriptionSegment s " +
            "WHERE s.transcriptionId = :transcriptionId AND s.speakerId IS NOT NULL " +
            "ORDER BY s.speakerId")
    List<String> findDistinctSpeakerIds(@Param("transcriptionId") UUID transcriptionId);

    /**
     * Count segments by speaker.
     *
     * @param transcriptionId Transcription ID
     * @param speakerId       Speaker ID
     * @return Number of segments
     */
    long countByTranscriptionIdAndSpeakerId(UUID transcriptionId, String speakerId);

    /**
     * Delete all segments for a transcription.
     *
     * @param transcriptionId Transcription ID
     */
    void deleteByTranscriptionId(UUID transcriptionId);

    /**
     * Find segments within a time range.
     *
     * @param transcriptionId Transcription ID
     * @param startTime       Start time in seconds
     * @param endTime         End time in seconds
     * @return List of segments
     */
    @Query("SELECT s FROM TranscriptionSegment s WHERE s.transcriptionId = :transcriptionId " +
            "AND s.startTimeSeconds >= :startTime AND s.endTimeSeconds <= :endTime " +
            "ORDER BY s.segmentIndex ASC")
    List<TranscriptionSegment> findSegmentsInTimeRange(
            @Param("transcriptionId") UUID transcriptionId,
            @Param("startTime") Double startTime,
            @Param("endTime") Double endTime
    );

    /**
     * Get average confidence score for a transcription.
     *
     * @param transcriptionId Transcription ID
     * @return Average confidence score
     */
    @Query("SELECT AVG(s.confidenceScore) FROM TranscriptionSegment s " +
            "WHERE s.transcriptionId = :transcriptionId")
    Double getAverageConfidenceScore(@Param("transcriptionId") UUID transcriptionId);
}
