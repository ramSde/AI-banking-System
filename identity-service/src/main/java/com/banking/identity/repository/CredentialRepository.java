package com.banking.identity.repository;

import com.banking.identity.domain.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Credential Repository
 * 
 * Spring Data JPA repository for Credential entity operations.
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    /**
     * Find credential by user ID (excluding soft-deleted)
     */
    @Query("SELECT c FROM Credential c WHERE c.userId = :userId AND c.deletedAt IS NULL")
    Optional<Credential> findByUserId(@Param("userId") UUID userId);

    /**
     * Check if credential exists for user
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Credential c WHERE c.userId = :userId AND c.deletedAt IS NULL")
    boolean existsByUserId(@Param("userId") UUID userId);

    /**
     * Find credentials that require password change
     */
    @Query("SELECT c FROM Credential c WHERE c.mustChangePassword = true AND c.deletedAt IS NULL")
    java.util.List<Credential> findCredentialsRequiringPasswordChange();

    /**
     * Find credentials with passwords older than specified date
     */
    @Query("SELECT c FROM Credential c WHERE c.passwordChangedAt < :thresholdDate AND c.deletedAt IS NULL")
    java.util.List<Credential> findCredentialsWithOldPasswords(@Param("thresholdDate") Instant thresholdDate);

    /**
     * Update password hash for user
     */
    @Modifying
    @Query("UPDATE Credential c SET c.passwordHash = :passwordHash, c.passwordChangedAt = :changedAt, c.mustChangePassword = false WHERE c.userId = :userId AND c.deletedAt IS NULL")
    int updatePasswordHash(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash, @Param("changedAt") Instant changedAt);

    /**
     * Soft delete credential by user ID
     */
    @Modifying
    @Query("UPDATE Credential c SET c.deletedAt = :deletedAt WHERE c.userId = :userId AND c.deletedAt IS NULL")
    int softDeleteByUserId(@Param("userId") UUID userId, @Param("deletedAt") Instant deletedAt);
}
