package com.banking.vision.repository;

import com.banking.vision.domain.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for OcrResult entity.
 * 
 * Provides data access methods for OCR results with support for:
 * - Document-scoped queries
 * - Page-based retrieval
 * - Confidence filtering
 */
@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, UUID> {

    /**
     * Find all OCR results for a document (ordered by page number).
     */
    @Query("SELECT ocr FROM OcrResult ocr WHERE ocr.documentId = :documentId AND ocr.deletedAt IS NULL ORDER BY ocr.pageNumber ASC")
    List<OcrResult> findByDocumentId(@Param("documentId") UUID documentId);

    /**
     * Find OCR result for specific page of a document.
     */
    @Query("SELECT ocr FROM OcrResult ocr WHERE ocr.documentId = :documentId AND ocr.pageNumber = :pageNumber AND ocr.deletedAt IS NULL")
    Optional<OcrResult> findByDocumentIdAndPageNumber(
        @Param("documentId") UUID documentId,
        @Param("pageNumber") Integer pageNumber
    );

    /**
     * Find first OCR result for a document (page 1).
     */
    @Query("SELECT ocr FROM OcrResult ocr WHERE ocr.documentId = :documentId AND ocr.deletedAt IS NULL ORDER BY ocr.pageNumber ASC LIMIT 1")
    Optional<OcrResult> findFirstByDocumentId(@Param("documentId") UUID documentId);

    /**
     * Count OCR results for a document (page count).
     */
    @Query("SELECT COUNT(ocr) FROM OcrResult ocr WHERE ocr.documentId = :documentId AND ocr.deletedAt IS NULL")
    long countByDocumentId(@Param("documentId") UUID documentId);

    /**
     * Find OCR results with confidence below threshold.
     */
    @Query("SELECT ocr FROM OcrResult ocr WHERE ocr.documentId = :documentId AND ocr.confidenceScore < :threshold AND ocr.deletedAt IS NULL")
    List<OcrResult> findByDocumentIdAndConfidenceScoreLessThan(
        @Param("documentId") UUID documentId,
        @Param("threshold") Double threshold
    );

    /**
     * Calculate average confidence score for a document.
     */
    @Query("SELECT AVG(ocr.confidenceScore) FROM OcrResult ocr WHERE ocr.documentId = :documentId AND ocr.deletedAt IS NULL")
    Double calculateAverageConfidenceScore(@Param("documentId") UUID documentId);

    /**
     * Delete all OCR results for a document (hard delete for cleanup).
     */
    void deleteByDocumentId(UUID documentId);
}
