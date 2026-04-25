package com.banking.rag.repository;

import com.banking.rag.domain.RagContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RagContextRepository extends JpaRepository<RagContext, UUID> {

    @Query("SELECT rc FROM RagContext rc WHERE rc.id = :id AND rc.deletedAt IS NULL")
    Optional<RagContext> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT rc FROM RagContext rc WHERE rc.queryId = :queryId AND rc.deletedAt IS NULL")
    Optional<RagContext> findByQueryIdAndNotDeleted(@Param("queryId") UUID queryId);

    @Query("SELECT rc FROM RagContext rc WHERE rc.deletedAt IS NULL ORDER BY rc.createdAt DESC")
    Page<RagContext> findAllNotDeleted(Pageable pageable);

    @Query("SELECT AVG(rc.tokenCount) FROM RagContext rc WHERE rc.deletedAt IS NULL")
    Double getAverageTokenCount();

    @Query("SELECT AVG(rc.sourceCount) FROM RagContext rc WHERE rc.deletedAt IS NULL")
    Double getAverageSourceCount();
}
