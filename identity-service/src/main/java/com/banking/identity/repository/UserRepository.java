package com.banking.identity.repository;

import com.banking.identity.domain.User;
import com.banking.identity.domain.UserStatus;
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
 * User Repository
 * 
 * Spring Data JPA repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email (active users only)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Find user by username (active users only)
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findByUsername(@Param("username") String username);

    /**
     * Find user by phone number (active users only)
     */
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.deletedAt IS NULL")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Check if email exists (excluding soft-deleted)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.deletedAt IS NULL")
    boolean existsByEmail(@Param("email") String email);

    /**
     * Check if username exists (excluding soft-deleted)
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.deletedAt IS NULL")
    boolean existsByUsername(@Param("username") String username);

    /**
     * Find all users by status with pagination
     */
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deletedAt IS NULL")
    Page<User> findByStatus(@Param("status") UserStatus status, Pageable pageable);

    /**
     * Find all locked users (locked_until is in the future)
     */
    @Query("SELECT u FROM User u WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil > :now AND u.deletedAt IS NULL")
    Page<User> findLockedUsers(@Param("now") Instant now, Pageable pageable);

    /**
     * Unlock users whose lockout period has expired
     */
    @Modifying
    @Query("UPDATE User u SET u.lockedUntil = NULL, u.failedLoginAttempts = 0 WHERE u.lockedUntil IS NOT NULL AND u.lockedUntil <= :now")
    int unlockExpiredAccounts(@Param("now") Instant now);

    /**
     * Find users created within a date range
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate AND u.deletedAt IS NULL")
    Page<User> findByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate, Pageable pageable);

    /**
     * Count active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE' AND u.deletedAt IS NULL")
    long countActiveUsers();

    /**
     * Soft delete user by ID
     */
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt, u.status = 'INACTIVE' WHERE u.id = :userId AND u.deletedAt IS NULL")
    int softDeleteById(@Param("userId") UUID userId, @Param("deletedAt") Instant deletedAt);
}
