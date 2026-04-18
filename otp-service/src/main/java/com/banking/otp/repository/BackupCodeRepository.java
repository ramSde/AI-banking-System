package com.banking.otp.repository;

import com.banking.otp.domain.BackupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for backup code operations
 */
@Repository
public interface BackupCodeRepository extends JpaRepository<BackupCode, UUID> {

    /**
     * Find all unused backup codes for a user
     */
    @Query("SELECT b FROM BackupCode b WHERE b.userId = :userId AND b.used = false AND b.deletedAt IS NULL")
    List<BackupCode> findUnusedByUserId(@Param("userId") UUID userId);

    /**
     * Find all backup codes for a user (used and unused)
     */
    @Query("SELECT b FROM BackupCode b WHERE b.userId = :userId AND b.deletedAt IS NULL ORDER BY b.createdAt DESC")
    List<BackupCode> findByUserId(@Param("userId") UUID userId);

    /**
     * Count unused backup codes for a user
     */
    @Query("SELECT COUNT(b) FROM BackupCode b WHERE b.userId = :userId AND b.used = false AND b.deletedAt IS NULL")
    long countUnusedByUserId(@Param("userId") UUID userId);

    /**
     * Delete all backup codes for a user (soft delete by setting deletedAt)
     */
    @Query("UPDATE BackupCode b SET b.deletedAt = CURRENT_TIMESTAMP WHERE b.userId = :userId AND b.deletedAt IS NULL")
    void softDeleteByUserId(@Param("userId") UUID userId);
}
