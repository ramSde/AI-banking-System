package com.banking.rag.repository;

import com.banking.rag.domain.RagCache;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RagCacheRepository extends JpaRepository<RagCache, UUID> {

    @Query("SELECT rc FROM RagCache rc WHERE rc.id = :id AND rc.deletedAt IS NULL")
    Optional<RagCache> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT rc FROM RagCache rc WHERE rc.expiresAt > :now AND rc.deletedAt IS NULL ORDER BY rc.hitCount DESC")
    Page<RagCache> findActiveEntriesOrderByHitCount(@Param("now") Instant now, Pageable pageable);

    @Query("SELECT rc FROM RagCache rc WHERE rc.expiresAt <= :now AND rc.deletedAt IS NULL")
    List<RagCache> findExpiredEntries(@Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE RagCache rc SET rc.hitCount = rc.hitCount + 1, rc.lastHitAt = :hitTime, rc.updatedAt = :hitTime WHERE rc.id = :id")
    void incrementHitCount(@Param("id") UUID id, @Param("hitTime") Instant hitTime);

    @Modifying
    @Transactional
    @Query("UPDATE RagCache rc SET rc.deletedAt = :now WHERE rc.expiresAt <= :now AND rc.deletedAt IS NULL")
    int softDeleteExpiredEntries(@Param("now") Instant now);

    @Query("SELECT COUNT(rc) FROM RagCache rc WHERE rc.expiresAt > :now AND rc.deletedAt IS NULL")
    Long countActiveEntries(@Param("now") Instant now);

    @Query("SELECT SUM(rc.hitCount) FROM RagCache rc WHERE rc.deletedAt IS NULL")
    Long getTotalHitCount();

    @Query("SELECT rc FROM RagCache rc WHERE rc.deletedAt IS NULL ORDER BY rc.createdAt DESC")
    Page<RagCache> findAllNotDeleted(Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE RagCache rc SET rc.deletedAt = :now WHERE rc.deletedAt IS NULL")
    int clearAllCache(@Param("now") Instant now);
}
