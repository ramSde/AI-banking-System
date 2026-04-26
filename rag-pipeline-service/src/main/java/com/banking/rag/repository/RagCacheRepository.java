package com.banking.rag.repository;

import com.banking.rag.domain.RagCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RagCacheRepository extends JpaRepository<RagCache, UUID> {

    Optional<RagCache> findByIdAndDeletedAtIsNull(UUID id);

    @Query(value = "SELECT * FROM rag_cache " +
                   "WHERE deleted_at IS NULL AND expires_at > NOW() " +
                   "ORDER BY query_embedding <=> CAST(:embedding AS vector) " +
                   "LIMIT 1", nativeQuery = true)
    Optional<RagCache> findMostSimilarCache(@Param("embedding") String embedding);

    @Query("SELECT rc FROM RagCache rc WHERE rc.expiresAt < :now AND rc.deletedAt IS NULL")
    List<RagCache> findExpiredCaches(@Param("now") Instant now);

    @Modifying
    @Query("UPDATE RagCache rc SET rc.deletedAt = :now WHERE rc.expiresAt < :now AND rc.deletedAt IS NULL")
    int deleteExpiredCaches(@Param("now") Instant now);

    @Query("SELECT COUNT(rc) FROM RagCache rc WHERE rc.deletedAt IS NULL AND rc.expiresAt > :now")
    Long countActiveCaches(@Param("now") Instant now);

    @Query("SELECT SUM(rc.hitCount) FROM RagCache rc WHERE rc.deletedAt IS NULL")
    Long getTotalCacheHits();

    @Modifying
    @Query("UPDATE RagCache rc SET rc.hitCount = rc.hitCount + 1, rc.lastHitAt = :now WHERE rc.id = :id")
    void incrementHitCount(@Param("id") UUID id, @Param("now") Instant now);
}
