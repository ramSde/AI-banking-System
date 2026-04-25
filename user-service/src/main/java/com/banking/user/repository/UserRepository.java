package com.banking.user.repository;

import com.banking.user.domain.KycStatus;
import com.banking.user.domain.User;
import com.banking.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (not deleted)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    /**
     * Find user by ID (not deleted)
     */
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deletedAt IS NULL")
    Optional<User> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * Find users by status
     */
    @Query("SELECT u FROM User u WHERE u.userStatus = :status AND u.deletedAt IS NULL")
    Page<User> findByUserStatus(@Param("status") UserStatus status, Pageable pageable);

    /**
     * Find users by KYC status
     */
    @Query("SELECT u FROM User u WHERE u.kycStatus = :kycStatus AND u.deletedAt IS NULL")
    Page<User> findByKycStatus(@Param("kycStatus") KycStatus kycStatus, Pageable pageable);

    /**
     * Find users by country
     */
    @Query("SELECT u FROM User u WHERE u.country = :country AND u.deletedAt IS NULL")
    Page<User> findByCountry(@Param("country") String country, Pageable pageable);

    /**
     * Find users created within date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate AND u.deletedAt IS NULL")
    Page<User> findByCreatedAtBetween(@Param("startDate") Instant startDate, 
                                       @Param("endDate") Instant endDate, 
                                       Pageable pageable);

    /**
     * Find users who haven't logged in since specified date
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :since AND u.deletedAt IS NULL")
    Page<User> findInactiveUsersSince(@Param("since") Instant since, Pageable pageable);

    /**
     * Find locked users
     */
    @Query("SELECT u FROM User u WHERE u.userStatus = 'LOCKED' AND u.deletedAt IS NULL")
    Page<User> findLockedUsers(Pageable pageable);

    /**
     * Find users with expired account locks
     */
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL " +
           "AND u.accountLockedUntil < :now AND u.deletedAt IS NULL")
    Page<User> findUsersWithExpiredLocks(@Param("now") Instant now, Pageable pageable);

    /**
     * Count users by status
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.userStatus = :status AND u.deletedAt IS NULL")
    Long countByUserStatus(@Param("status") UserStatus status);

    /**
     * Count users by KYC status
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.kycStatus = :kycStatus AND u.deletedAt IS NULL")
    Long countByKycStatus(@Param("kycStatus") KycStatus kycStatus);

    /**
     * Check if email exists (not deleted)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Soft delete user
     */
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt, u.updatedAt = :updatedAt " +
           "WHERE u.id = :id")
    void softDelete(@Param("id") UUID id, 
                    @Param("deletedAt") Instant deletedAt, 
                    @Param("updatedAt") Instant updatedAt);

    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt, u.failedLoginAttempts = 0, " +
           "u.updatedAt = :updatedAt WHERE u.id = :id")
    void updateLastLogin(@Param("id") UUID id, 
                         @Param("lastLoginAt") Instant lastLoginAt, 
                         @Param("updatedAt") Instant updatedAt);

    /**
     * Increment failed login attempts
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1, " +
           "u.updatedAt = :updatedAt WHERE u.id = :id")
    void incrementFailedLoginAttempts(@Param("id") UUID id, @Param("updatedAt") Instant updatedAt);

    /**
     * Lock user account
     */
    @Modifying
    @Query("UPDATE User u SET u.userStatus = 'LOCKED', u.accountLockedUntil = :lockedUntil, " +
           "u.updatedAt = :updatedAt WHERE u.id = :id")
    void lockAccount(@Param("id") UUID id, 
                     @Param("lockedUntil") Instant lockedUntil, 
                     @Param("updatedAt") Instant updatedAt);

    /**
     * Unlock user account
     */
    @Modifying
    @Query("UPDATE User u SET u.userStatus = 'ACTIVE', u.accountLockedUntil = NULL, " +
           "u.failedLoginAttempts = 0, u.updatedAt = :updatedAt WHERE u.id = :id")
    void unlockAccount(@Param("id") UUID id, @Param("updatedAt") Instant updatedAt);

    /**
     * Update user status
     */
    @Modifying
    @Query("UPDATE User u SET u.userStatus = :status, u.updatedAt = :updatedAt WHERE u.id = :id")
    void updateUserStatus(@Param("id") UUID id, 
                          @Param("status") UserStatus status, 
                          @Param("updatedAt") Instant updatedAt);

    /**
     * Update KYC status
     */
    @Modifying
    @Query("UPDATE User u SET u.kycStatus = :kycStatus, u.kycVerifiedAt = :verifiedAt, " +
           "u.updatedAt = :updatedAt WHERE u.id = :id")
    void updateKycStatus(@Param("id") UUID id, 
                         @Param("kycStatus") KycStatus kycStatus, 
                         @Param("verifiedAt") Instant verifiedAt, 
                         @Param("updatedAt") Instant updatedAt);

    /**
     * Search users by name or email (case-insensitive)
     */
    @Query("SELECT u FROM User u WHERE (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND u.deletedAt IS NULL")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
}
