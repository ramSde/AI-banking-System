package com.banking.otp.repository;

import com.banking.otp.domain.MfaEnrollment;
import com.banking.otp.domain.MfaMethod;
import com.banking.otp.domain.MfaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for MFA enrollment operations
 */
@Repository
public interface MfaEnrollmentRepository extends JpaRepository<MfaEnrollment, UUID> {

    /**
     * Find all active MFA enrollments for a user
     */
    @Query("SELECT m FROM MfaEnrollment m WHERE m.userId = :userId AND m.deletedAt IS NULL AND m.status = 'ACTIVE'")
    List<MfaEnrollment> findActiveByUserId(@Param("userId") UUID userId);

    /**
     * Find a specific MFA enrollment by user and method
     */
    @Query("SELECT m FROM MfaEnrollment m WHERE m.userId = :userId AND m.mfaMethod = :method AND m.deletedAt IS NULL")
    Optional<MfaEnrollment> findByUserIdAndMethod(@Param("userId") UUID userId, @Param("method") MfaMethod method);

    /**
     * Find an active and verified MFA enrollment by user and method
     */
    @Query("SELECT m FROM MfaEnrollment m WHERE m.userId = :userId AND m.mfaMethod = :method AND m.status = 'ACTIVE' AND m.verified = true AND m.deletedAt IS NULL")
    Optional<MfaEnrollment> findActiveVerifiedByUserIdAndMethod(@Param("userId") UUID userId, @Param("method") MfaMethod method);

    /**
     * Check if user has any active MFA enrollment
     */
    @Query("SELECT COUNT(m) > 0 FROM MfaEnrollment m WHERE m.userId = :userId AND m.status = 'ACTIVE' AND m.verified = true AND m.deletedAt IS NULL")
    boolean hasActiveMfa(@Param("userId") UUID userId);

    /**
     * Find all verified MFA enrollments for a user
     */
    @Query("SELECT m FROM MfaEnrollment m WHERE m.userId = :userId AND m.verified = true AND m.deletedAt IS NULL ORDER BY m.lastUsedAt DESC")
    List<MfaEnrollment> findVerifiedByUserId(@Param("userId") UUID userId);

    /**
     * Count active MFA enrollments for a user
     */
    @Query("SELECT COUNT(m) FROM MfaEnrollment m WHERE m.userId = :userId AND m.status = 'ACTIVE' AND m.deletedAt IS NULL")
    long countActiveByUserId(@Param("userId") UUID userId);
}
