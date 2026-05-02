package com.banking.stt.repository;

import com.banking.stt.domain.AudioFile;
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
 * Repository interface for AudioFile entity.
 * Provides database access methods for audio file metadata.
 *
 * @author Banking Platform Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@Repository
public interface AudioFileRepository extends JpaRepository<AudioFile, UUID> {

    /**
     * Find audio file by ID excluding deleted files.
     *
     * @param id Audio file ID
     * @return Optional containing the audio file if found
     */
    Optional<AudioFile> findByIdAndDeletedAtIsNull(UUID id);

    /**
     * Find all audio files for a user excluding deleted files.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return Page of audio files
     */
    Page<AudioFile> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    /**
     * Find audio files by user and language code.
     *
     * @param userId       User ID
     * @param languageCode Language code
     * @param pageable     Pagination parameters
     * @return Page of audio files
     */
    Page<AudioFile> findByUserIdAndLanguageCodeAndDeletedAtIsNull(
            UUID userId,
            String languageCode,
            Pageable pageable
    );

    /**
     * Count audio files uploaded by user after a specific time.
     * Used for rate limiting.
     *
     * @param userId User ID
     * @param since  Time threshold
     * @return Count of audio files
     */
    long countByUserIdAndCreatedAtAfterAndDeletedAtIsNull(UUID userId, Instant since);

    /**
     * Find audio files by storage path.
     *
     * @param storagePath Storage path
     * @return Optional containing the audio file if found
     */
    Optional<AudioFile> findByStoragePathAndDeletedAtIsNull(String storagePath);

    /**
     * Get total storage used by user in bytes.
     *
     * @param userId User ID
     * @return Total file size in bytes
     */
    @Query("SELECT COALESCE(SUM(a.fileSizeBytes), 0) FROM AudioFile a " +
            "WHERE a.userId = :userId AND a.deletedAt IS NULL")
    Long getTotalStorageUsedByUser(@Param("userId") UUID userId);

    /**
     * Find audio files created within a date range.
     *
     * @param userId    User ID
     * @param startDate Start date
     * @param endDate   End date
     * @param pageable  Pagination parameters
     * @return Page of audio files
     */
    @Query("SELECT a FROM AudioFile a WHERE a.userId = :userId " +
            "AND a.createdAt BETWEEN :startDate AND :endDate " +
            "AND a.deletedAt IS NULL")
    Page<AudioFile> findByUserIdAndCreatedAtBetween(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );

    /**
     * Find audio files by user ordered by creation date descending.
     *
     * @param userId   User ID
     * @param pageable Pagination parameters
     * @return List of audio files
     */
    List<AudioFile> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Check if audio file exists for user.
     *
     * @param id     Audio file ID
     * @param userId User ID
     * @return true if exists, false otherwise
     */
    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    /**
     * Delete old audio files (soft delete).
     * Used for cleanup jobs.
     *
     * @param before Delete files created before this date
     * @return Number of files marked as deleted
     */
    @Query("UPDATE AudioFile a SET a.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE a.createdAt < :before AND a.deletedAt IS NULL")
    int softDeleteOldFiles(@Param("before") Instant before);
}
