package com.banking.document.repository;

import com.banking.document.domain.Document;
import com.banking.document.domain.DocumentType;
import com.banking.document.domain.ProcessingStatus;
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

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    @Query("SELECT d FROM Document d WHERE d.id = :id AND d.deletedAt IS NULL")
    Optional<Document> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.deletedAt IS NULL")
    Page<Document> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.processingStatus = :status AND d.deletedAt IS NULL")
    Page<Document> findByUserIdAndStatusAndNotDeleted(
            @Param("userId") UUID userId,
            @Param("status") ProcessingStatus status,
            Pageable pageable
    );

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.documentType = :documentType AND d.deletedAt IS NULL")
    Page<Document> findByUserIdAndDocumentTypeAndNotDeleted(
            @Param("userId") UUID userId,
            @Param("documentType") DocumentType documentType,
            Pageable pageable
    );

    @Query("SELECT d FROM Document d WHERE d.processingStatus = :status AND d.deletedAt IS NULL")
    List<Document> findByProcessingStatusAndNotDeleted(@Param("status") ProcessingStatus status);

    @Query("SELECT d FROM Document d WHERE d.storageKey = :storageKey AND d.deletedAt IS NULL")
    Optional<Document> findByStorageKeyAndNotDeleted(@Param("storageKey") String storageKey);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.userId = :userId AND d.deletedAt IS NULL")
    Long countByUserIdAndNotDeleted(@Param("userId") UUID userId);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.userId = :userId AND d.processingStatus = :status AND d.deletedAt IS NULL")
    Long countByUserIdAndStatusAndNotDeleted(
            @Param("userId") UUID userId,
            @Param("status") ProcessingStatus status
    );

    @Query("SELECT d FROM Document d WHERE d.createdAt BETWEEN :startDate AND :endDate AND d.deletedAt IS NULL")
    List<Document> findByCreatedAtBetweenAndNotDeleted(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("SELECT d FROM Document d WHERE d.userId = :userId AND d.createdAt BETWEEN :startDate AND :endDate AND d.deletedAt IS NULL")
    Page<Document> findByUserIdAndCreatedAtBetweenAndNotDeleted(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable
    );
}
