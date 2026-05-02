package com.banking.stt.repository;

import com.banking.stt.domain.Transcription;
import com.banking.stt.domain.TranscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Transcription entity.
 * Provides database access methods for transcription results.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Repository
public interface TranscriptionRepository extends JpaRepository<Transcription, UUID> {

    /**
     * Find transcription by ID excluding deleted transcriptions.
     *
     * @param id Transcription ID
     * @return Optional containing the transcription if found
     */
    Optional<Transcription> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Find transcription by ID and user ID.
     *
     * @param id     Transcription ID
     * @param userId User ID
     * @return Optional containing the transcription if found
     */
    Optional<Transcription> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    /**
     * Find transcription by audio file ID.
     *
     * @param audioFileId Audio file ID
     * @return Optional containing the transcription if found
     */
    Optional<Transcription> findByAudioFileIdAndDeletedAtIsNull(UUID audioFileId);

    /**
     * Find all transcriptions for a user.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of transcriptions
     */
    Page<Transcription> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    /**
     * Find transcriptions by user and status.
     *
     * @param userId   User ID
     * @param status   Transcription status
     * @param pageable Pagination parameters
     * @return Page of transcriptions
     */
    Page<Transcription> findByUserIdAndStatusAndDeletedAtIsNull(
            UUID userId,
            TranscriptionStatus status,
            Pageable pageable
    );

    /**
     * Find transcriptions by status.
     *
     * @param status   Transcription status
     * @param pageable Pagination parameters
     * @return Page of transcriptions
     */
    Page<Transcription> findByStatusAndDeletedAtIsNull(TranscriptionStatus status, Pageable pageable);

    /**
     * Find pending or processing transcriptions older than specified time.
     * Used to identify stuck transcriptions.
     *
     * @param statuses List of statuses to check
     * @param before   Time threshold
     * @return List of stuck transcriptions
     */
    @Query("SELECT t FROM Transcription t WHERE t.status IN :statuses " +
            "AND t.createdAt < :before AND t.deletedAt IS NULL")
    List<Transcription> findStuckTranscriptions(
            @Param("statuses") List<TranscriptionStatus> statuses,
            @Param("before") Instant before
    );

    /**
     * Count transcriptions by user and status.
     *
     * @param userId User ID
     * @param status Transcription status
     * @return Count of transcriptions
     */
    long countByUserIdAndStatusAndDeletedAtIsNull(UUID userId, TranscriptionStatus status);

    /**
     * Get average processing time for completed transcriptions.
     *
     * @return Average processing time in milliseconds
     */
    @Query("SELECT AVG(t.processingTimeMs) FROM Transcription t " +
            "WHERE t.status = 'COMPLETED' AND t.deletedAt IS NULL")
    Double getAverageProcessingTime();

    /**
     * Get average processing time for user.
     *
     * @param userId User ID
     * @return Average processing time in milliseconds
     */
    @Query("SELECT AVG(t.processingTimeMs) FROM Transcription t " +
            "WHERE t.userId = :userId AND t.status = 'COMPLETED' AND t.deletedAt IS NULL")
    Double getAverageProcessingTimeForUser(@Param("userId") UUID userId);

    /**
     * Find transcriptions by language detected.
     *
     * @param languageCode Language code
     * @param pageable     Pagination parameters
     * @return Page of transcriptions
     */
    Page<Transcription> findByLanguageDetectedAndDeletedAtIsNull(String languageCode, Pageable pageable);

    /**
     * Get total word count for user.
     *
     * @param userId User ID
     * @return Total word count
     */
    @Query("SELECT COALESCE(SUM(t.wordCount), 0) FROM Transcription t " +
            "WHERE t.userId = :userId AND t.status = 'COMPLETED' AND t.deletedAt IS NULL")
    Long getTotalWordCountForUser(@Param("userId") UUID userId);

    /**
     * Find transcriptions created within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @param pageable  Pagination parameters
     * @return Page of transcriptions
     */
    @Query("SELECT t FROM Transcription t WHERE t.userId = :userId " +
            "AND t.createdAt BETWEEN :startDate AND :endDate " +
            "AND t.deletedAt IS NULL")
    Page<Transcription> findByUserIdAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Check if transcription exists for user.
     *
     * @param id     Transcription ID
     * @param userId User ID
     * @return true if exists, false otherwise
     */
    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    /**
     * Find recent transcriptions for user.
     *
     * @param userId User ID
     * @param limit  Maximum number of results
     * @return List of recent transcriptions
     */
    @Query("SELECT t FROM Transcription t WHERE t.userId = :userId " +
            "AND t.deletedAt IS NULL ORDER BY t.createdAt DESC")
    List<Transcription> findRecentTranscriptions(@Param("userId") UUID userId, Pageable pageable);
}
