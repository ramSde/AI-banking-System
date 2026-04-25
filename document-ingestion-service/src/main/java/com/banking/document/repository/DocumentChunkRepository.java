package com.banking.document.repository;

import com.banking.document.domain.DocumentChunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.id = :id AND dc.deletedAt IS NULL")
    Optional<DocumentChunk> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.documentId = :documentId AND dc.deletedAt IS NULL ORDER BY dc.chunkIndex ASC")
    List<DocumentChunk> findByDocumentIdAndNotDeleted(@Param("documentId") UUID documentId);

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.documentId = :documentId AND dc.deletedAt IS NULL ORDER BY dc.chunkIndex ASC")
    Page<DocumentChunk> findByDocumentIdAndNotDeleted(@Param("documentId") UUID documentId, Pageable pageable);

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.documentId = :documentId AND dc.chunkIndex = :chunkIndex AND dc.deletedAt IS NULL")
    Optional<DocumentChunk> findByDocumentIdAndChunkIndexAndNotDeleted(
            @Param("documentId") UUID documentId,
            @Param("chunkIndex") Integer chunkIndex
    );

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.vectorId = :vectorId AND dc.deletedAt IS NULL")
    Optional<DocumentChunk> findByVectorIdAndNotDeleted(@Param("vectorId") String vectorId);

    @Query("SELECT COUNT(dc) FROM DocumentChunk dc WHERE dc.documentId = :documentId AND dc.deletedAt IS NULL")
    Long countByDocumentIdAndNotDeleted(@Param("documentId") UUID documentId);

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.documentId = :documentId AND dc.vectorId IS NULL AND dc.deletedAt IS NULL")
    List<DocumentChunk> findByDocumentIdAndVectorIdIsNullAndNotDeleted(@Param("documentId") UUID documentId);

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.vectorId IS NOT NULL AND dc.deletedAt IS NULL")
    List<DocumentChunk> findAllWithVectorIdAndNotDeleted();
}
