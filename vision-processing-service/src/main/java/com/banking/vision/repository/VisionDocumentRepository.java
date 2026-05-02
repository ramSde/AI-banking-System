package com.banking.vision.repository;

import com.banking.vision.domain.DocumentType;
import com.banking.vision.domain.ProcessingStatus;
import com.banking.vision.domain.VisionDocument;
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
 * Repository for VisionDocument entity.
 * 
 * Provides data access methods for document management with support for:
 * - User-scoped queries
 * - Status filtering
 * - Type filtering
 * - Soft delete handling
 */
@Repository
public interface VisionDocumentRepository extends JpaRepository<VisionDocument, UUID> {

    /**
     * Find document by ID and user ID (ensures user owns the document).
     */
    @Query("SELECT vd FROM VisionDocument vd WHERE vd.id = :id AND vd.userId = :userId AND vd.deletedAt IS NULL")
    Optional<VisionDocument> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    /**
     * Find all documents for a user (paginated, non-deleted only).
     */
    @Query("SELECT vd FROM VisionDocument vd WHERE vd.userId = :userId AND vd.deletedAt IS NULL ORDER BY vd.createdAt DESC")
    Page<VisionDocument> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find documents by user and type.
     */
    @Query("SELECT vd FROM VisionDocument vd WHERE vd.userId = :userId AND vd.documentType = :type AND vd.deletedAt IS NULL ORDER BY vd.createdAt DESC")
    Page<VisionDocument> findByUserIdAndDocumentType(
        @Param("userId") UUID userId,
        @Param("type") DocumentType type,
        Pageable pageable
    );

    /**
     * Find documents by user and status.
     */
    @Query("SELECT vd FROM VisionDocument vd WHERE vd.userId = :userId AND vd.processingStatus = :status AND vd.deletedAt IS NULL ORDER BY vd.createdAt DESC")
    Page<VisionDocument> findByUserIdAndProcessingStatus(
        @Param("userId") UUID userId,
        @Param("status") ProcessingStatus status,
        Pageable pageable
    );

    /**
     * Find pending documents for processing (global, not user-scoped).
     */
    @Query("SELECT vd FROM VisionDocument vd WHERE vd.processingStatus = 'PENDING' AND vd.deletedAt IS NULL ORDER BY vd.createdAt ASC")
    List<VisionDocument> findPendingDocuments(Pageable pageable);

    /**
     * Find documents by status (admin query).
     */
    @Query("SELECT vd FROM VisionDocument vd WHERE vd.processingStatus = :status AND vd.deletedAt IS NULL ORDER BY vd.createdAt DESC")
    Page<VisionDocument> findByProcessingStatus(@Param("status") ProcessingStatus status, Pageable pageable);

    /**
     * Count documents by user and status.
     */
    @Query("SELECT COUNT(vd) FROM VisionDocument vd WHERE vd.userId = :userId AND vd.processingStatus = :status AND vd.deletedAt IS NULL")
    long countByUserIdAndProcessingStatus(@Param("userId") UUID userId, @Param("status") ProcessingStatus status);

    /**
     * Find documents created within date range.
     */
    @Query("SELECT vd FROM VisionDocument vd WHERE vd.userId = :userId AND vd.createdAt BETWEEN :startDate AND :endDate AND vd.deletedAt IS NULL ORDER BY vd.createdAt DESC")
    Page<VisionDocument> findByUserIdAndCreatedAtBetween(
        @Param("userId") UUID userId,
        @Param("startDate") Instant startDate,
        @Param("endDate") Instant endDate,
        Pageable pageable
    );

    /**
     * Find documents by storage key (for cleanup operations).
     */
    Optional<VisionDocument> findByStorageKey(String storageKey);
}
