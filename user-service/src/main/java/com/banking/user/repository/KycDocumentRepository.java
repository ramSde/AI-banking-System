package com.banking.user.repository;

import com.banking.user.domain.DocumentType;
import com.banking.user.domain.KycDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for KycDocument entity operations.
 * Provides CRUD operations and custom queries for KYC document management.
 */
@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, UUID> {

    /**
     * Find document by ID (not deleted)
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.id = :id AND kd.deletedAt IS NULL")
    Optional<KycDocument> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * Find all documents for a user (not deleted)
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.userId = :userId AND kd.deletedAt IS NULL")
    Page<KycDocument> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find documents by user ID and document type
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.userId = :userId " +
           "AND kd.documentType = :documentType AND kd.deletedAt IS NULL")
    List<KycDocument> findByUserIdAndDocumentType(@Param("userId") UUID userId, 
                                                    @Param("documentType") DocumentType documentType);

    /**
     * Find documents by verification status
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.verificationStatus = :status AND kd.deletedAt IS NULL")
    Page<KycDocument> findByVerificationStatus(@Param("status") String status, Pageable pageable);

    /**
     * Find documents by user ID and verification status
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.userId = :userId " +
           "AND kd.verificationStatus = :status AND kd.deletedAt IS NULL")
    List<KycDocument> findByUserIdAndVerificationStatus(@Param("userId") UUID userId, 
                                                          @Param("status") String status);

    /**
     * Find documents by document type
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.documentType = :documentType AND kd.deletedAt IS NULL")
    Page<KycDocument> findByDocumentType(@Param("documentType") DocumentType documentType, Pageable pageable);

    /**
     * Find documents by issuing country
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.issuingCountry = :country AND kd.deletedAt IS NULL")
    Page<KycDocument> findByIssuingCountry(@Param("country") String country, Pageable pageable);

    /**
     * Find documents expiring before date
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.expiryDate IS NOT NULL " +
           "AND kd.expiryDate < :date AND kd.deletedAt IS NULL")
    Page<KycDocument> findExpiringBefore(@Param("date") LocalDate date, Pageable pageable);

    /**
     * Find documents expiring within days
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.expiryDate IS NOT NULL " +
           "AND kd.expiryDate BETWEEN :startDate AND :endDate AND kd.deletedAt IS NULL")
    Page<KycDocument> findExpiringBetween(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate, 
                                           Pageable pageable);

    /**
     * Find verified documents for user
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.userId = :userId " +
           "AND kd.verificationStatus = 'VERIFIED' AND kd.deletedAt IS NULL")
    List<KycDocument> findVerifiedDocumentsByUserId(@Param("userId") UUID userId);

    /**
     * Find pending documents
     */
    @Query("SELECT kd FROM KycDocument kd WHERE kd.verificationStatus IN ('PENDING', 'IN_REVIEW') " +
           "AND kd.deletedAt IS NULL")
    Page<KycDocument> findPendingDocuments(Pageable pageable);

    /**
     * Count documents by user ID
     */
    @Query("SELECT COUNT(kd) FROM KycDocument kd WHERE kd.userId = :userId AND kd.deletedAt IS NULL")
    Long countByUserId(@Param("userId") UUID userId);

    /**
     * Count documents by verification status
     */
    @Query("SELECT COUNT(kd) FROM KycDocument kd WHERE kd.verificationStatus = :status " +
           "AND kd.deletedAt IS NULL")
    Long countByVerificationStatus(@Param("status") String status);

    /**
     * Count verified documents for user
     */
    @Query("SELECT COUNT(kd) FROM KycDocument kd WHERE kd.userId = :userId " +
           "AND kd.verificationStatus = 'VERIFIED' AND kd.deletedAt IS NULL")
    Long countVerifiedDocumentsByUserId(@Param("userId") UUID userId);

    /**
     * Check if user has verified document of type
     */
    @Query("SELECT CASE WHEN COUNT(kd) > 0 THEN true ELSE false END FROM KycDocument kd " +
           "WHERE kd.userId = :userId AND kd.documentType = :documentType " +
           "AND kd.verificationStatus = 'VERIFIED' AND kd.deletedAt IS NULL")
    boolean hasVerifiedDocumentOfType(@Param("userId") UUID userId, 
                                       @Param("documentType") DocumentType documentType);

    /**
     * Soft delete document
     */
    @Modifying
    @Query("UPDATE KycDocument kd SET kd.deletedAt = :deletedAt, kd.updatedAt = :updatedAt " +
           "WHERE kd.id = :id")
    void softDelete(@Param("id") UUID id, 
                    @Param("deletedAt") Instant deletedAt, 
                    @Param("updatedAt") Instant updatedAt);

    /**
     * Update verification status
     */
    @Modifying
    @Query("UPDATE KycDocument kd SET kd.verificationStatus = :status, " +
           "kd.verifiedAt = :verifiedAt, kd.verifiedBy = :verifiedBy, " +
           "kd.rejectionReason = :rejectionReason, kd.updatedAt = :updatedAt " +
           "WHERE kd.id = :id")
    void updateVerificationStatus(@Param("id") UUID id, 
                                   @Param("status") String status, 
                                   @Param("verifiedAt") Instant verifiedAt, 
                                   @Param("verifiedBy") UUID verifiedBy, 
                                   @Param("rejectionReason") String rejectionReason, 
                                   @Param("updatedAt") Instant updatedAt);

    /**
     * Mark documents as expired
     */
    @Modifying
    @Query("UPDATE KycDocument kd SET kd.verificationStatus = 'EXPIRED', kd.updatedAt = :updatedAt " +
           "WHERE kd.expiryDate < :date AND kd.verificationStatus = 'VERIFIED' AND kd.deletedAt IS NULL")
    int markExpiredDocuments(@Param("date") LocalDate date, @Param("updatedAt") Instant updatedAt);

    /**
     * Delete all documents for user (soft delete)
     */
    @Modifying
    @Query("UPDATE KycDocument kd SET kd.deletedAt = :deletedAt, kd.updatedAt = :updatedAt " +
           "WHERE kd.userId = :userId")
    void softDeleteByUserId(@Param("userId") UUID userId, 
                            @Param("deletedAt") Instant deletedAt, 
                            @Param("updatedAt") Instant updatedAt);
}
