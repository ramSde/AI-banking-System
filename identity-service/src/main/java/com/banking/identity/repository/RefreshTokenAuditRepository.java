package com.banking.identity.repository;

import com.banking.identity.domain.RefreshTokenAudit;
import com.banking.identity.domain.RefreshTokenStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * RefreshTokenAudit Repository
 * 
 * Spring Data JPA repository for RefreshTokenAudit entity operations.
 */
@Repository
public interface RefreshTokenAuditRepository extends JpaRepository<RefreshTokenAudit, UUID> {

    /**
     * Find refresh token by token hash (for validation)
     */
    @Query("SELECT r FROM RefreshTokenAudit r WHERE r.tokenHash = :tokenHash AND r.deletedAt IS NULL")
    Optional<RefreshTokenAudit> findByTokenHash(@Param("tokenHash") String tokenHash);

    /**
     * Find active refresh tokens for user
     */
    @Query("SELECT r FROM RefreshTokenAudit r WHERE r.userId = :userId AND r.status = 'ACTIVE' AND r.expiresAt > :now AND r.deletedAt IS NULL")
    List<RefreshTokenAudit> findActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Find all refresh tokens for user (with pagination)
     */
    @Query("SELECT r FROM RefreshTokenAudit r WHERE r.userId = :userId AND r.deletedAt IS NULL ORDER BY r.issuedAt DESC")
    Page<RefreshTokenAudit> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find tokens by token family ID (rotation chain)
     */
    @Query("SELECT r FROM RefreshTokenAudit r WHERE r.tokenFamilyId = :tokenFamilyId AND r.deletedAt IS NULL ORDER BY r.issuedAt DESC")
    List<RefreshTokenAudit> findByTokenFamilyId(@Param("tokenFamilyId") UUID tokenFamilyId);

    /**
     * Find tokens by device ID
     */
    @Query("SELECT r FROM RefreshTokenAudit r WHERE r.deviceId = :deviceId AND r.deletedAt IS NULL ORDER BY r.issuedAt DESC")
    Page<RefreshTokenAudit> findByDeviceId(@Param("deviceId") String deviceId, Pageable pageable);

    /**
     * Find expired tokens that need cleanup
     */
    @Query("SELECT r FROM RefreshTokenAudit r WHERE r.expiresAt < :now AND r.status = 'ACTIVE' AND r.deletedAt IS NULL")
    List<RefreshTokenAudit> findExpiredTokens(@Param("now") Instant now);

    /**
     * Revoke all active tokens for user
     */
    @Modifying
    @Query("UPDATE RefreshTokenAudit r SET r.status = 'REVOKED', r.revokedAt = :revokedAt WHERE r.userId = :userId AND r.status = 'ACTIVE' AND r.deletedAt IS NULL")
    int revokeAllTokensByUserId(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    /**
     * Revoke all tokens in a token family (security breach detection)
     */
    @Modifying
    @Query("UPDATE RefreshTokenAudit r SET r.status = 'REVOKED', r.revokedAt = :revokedAt WHERE r.tokenFamilyId = :tokenFamilyId AND r.status = 'ACTIVE' AND r.deletedAt IS NULL")
    int revokeTokenFamily(@Param("tokenFamilyId") UUID tokenFamilyId, @Param("revokedAt") Instant revokedAt);

    /**
     * Mark expired tokens as EXPIRED status
     */
    @Modifying
    @Query("UPDATE RefreshTokenAudit r SET r.status = 'EXPIRED' WHERE r.expiresAt < :now AND r.status = 'ACTIVE' AND r.deletedAt IS NULL")
    int markExpiredTokens(@Param("now") Instant now);

    /**
     * Delete old audit records (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshTokenAudit r WHERE r.expiresAt < :thresholdDate")
    int deleteOldAuditRecords(@Param("thresholdDate") Instant thresholdDate);

    /**
     * Count active tokens for user
     */
    @Query("SELECT COUNT(r) FROM RefreshTokenAudit r WHERE r.userId = :userId AND r.status = 'ACTIVE' AND r.expiresAt > :now AND r.deletedAt IS NULL")
    long countActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") Instant now);

    /**
     * Find tokens by IP address (security analysis)
     */
    @Query("SELECT r FROM RefreshTokenAudit r WHERE r.ipAddress = :ipAddress AND r.deletedAt IS NULL ORDER BY r.issuedAt DESC")
    Page<RefreshTokenAudit> findByIpAddress(@Param("ipAddress") String ipAddress, Pageable pageable);
}
