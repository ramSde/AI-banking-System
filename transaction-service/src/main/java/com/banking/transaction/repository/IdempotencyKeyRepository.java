package com.banking.transaction.repository;

import com.banking.transaction.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Idempotency Key Repository
 * 
 * Data access layer for IdempotencyKey entity.
 * Manages idempotency keys for duplicate request prevention.
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    @Query("SELECT i FROM IdempotencyKey i WHERE i.idempotencyKey = :key AND i.expiresAt > :now")
    Optional<IdempotencyKey> findByKeyAndNotExpired(@Param("key") String key, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyKey i WHERE i.expiresAt < :now")
    int deleteExpiredKeys(@Param("now") Instant now);

    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM IdempotencyKey i WHERE i.idempotencyKey = :key AND i.expiresAt > :now")
    boolean existsByKeyAndNotExpired(@Param("key") String key, @Param("now") Instant now);
}
