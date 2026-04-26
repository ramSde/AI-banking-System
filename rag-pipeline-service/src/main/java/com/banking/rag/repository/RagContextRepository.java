package com.banking.rag.repository;

import com.banking.rag.domain.RagContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RagContextRepository extends JpaRepository<RagContext, UUID> {

    Optional<RagContext> findByIdAndDeletedAtIsNull(UUID id);

    Optional<RagContext> findByQueryIdAndDeletedAtIsNull(UUID queryId);

    @Query("SELECT rc FROM RagContext rc WHERE rc.queryId IN :queryIds AND rc.deletedAt IS NULL")
    List<RagContext> findByQueryIds(@Param("queryIds") List<UUID> queryIds);

    @Query("SELECT rc FROM RagContext rc WHERE rc.deletedAt IS NULL ORDER BY rc.createdAt DESC")
    List<RagContext> findAllActive();
}
