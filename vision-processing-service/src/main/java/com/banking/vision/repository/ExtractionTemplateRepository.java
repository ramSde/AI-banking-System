package com.banking.vision.repository;

import com.banking.vision.domain.DocumentType;
import com.banking.vision.domain.ExtractionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ExtractionTemplate entity.
 * 
 * Provides data access methods for extraction templates with support for:
 * - Document type lookup
 * - Active template filtering
 * - Template management
 */
@Repository
public interface ExtractionTemplateRepository extends JpaRepository<ExtractionTemplate, UUID> {

    /**
     * Find active template by document type.
     */
    @Query("SELECT et FROM ExtractionTemplate et WHERE et.documentType = :documentType AND et.isActive = true AND et.deletedAt IS NULL")
    Optional<ExtractionTemplate> findActiveByDocumentType(@Param("documentType") DocumentType documentType);

    /**
     * Find template by document type (regardless of active status).
     */
    @Query("SELECT et FROM ExtractionTemplate et WHERE et.documentType = :documentType AND et.deletedAt IS NULL")
    Optional<ExtractionTemplate> findByDocumentType(@Param("documentType") DocumentType documentType);

    /**
     * Find all active templates.
     */
    @Query("SELECT et FROM ExtractionTemplate et WHERE et.isActive = true AND et.deletedAt IS NULL ORDER BY et.documentType ASC")
    List<ExtractionTemplate> findAllActive();

    /**
     * Find all templates (including inactive).
     */
    @Query("SELECT et FROM ExtractionTemplate et WHERE et.deletedAt IS NULL ORDER BY et.documentType ASC")
    List<ExtractionTemplate> findAllNonDeleted();

    /**
     * Check if active template exists for document type.
     */
    @Query("SELECT CASE WHEN COUNT(et) > 0 THEN true ELSE false END FROM ExtractionTemplate et WHERE et.documentType = :documentType AND et.isActive = true AND et.deletedAt IS NULL")
    boolean existsActiveByDocumentType(@Param("documentType") DocumentType documentType);

    /**
     * Find templates created by specific user (admin tracking).
     */
    @Query("SELECT et FROM ExtractionTemplate et WHERE et.createdBy = :userId AND et.deletedAt IS NULL ORDER BY et.createdAt DESC")
    List<ExtractionTemplate> findByCreatedBy(@Param("userId") UUID userId);
}
