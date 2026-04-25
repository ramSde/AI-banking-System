package com.banking.user.service;

import com.banking.user.domain.KycStatus;
import com.banking.user.domain.UserStatus;
import com.banking.user.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for user management operations.
 * Handles user profile CRUD, status management, and KYC tracking.
 */
public interface UserService {

    /**
     * Create new user profile
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * Get user by ID
     */
    Optional<UserResponse> getUserById(UUID userId);

    /**
     * Get user by email
     */
    Optional<UserResponse> getUserByEmail(String email);

    /**
     * Update user profile
     */
    UserResponse updateUser(UUID userId, UserUpdateRequest request);

    /**
     * Delete user (soft delete)
     */
    void deleteUser(UUID userId);

    /**
     * Get users by status
     */
    Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable);

    /**
     * Get users by KYC status
     */
    Page<UserResponse> getUsersByKycStatus(KycStatus kycStatus, Pageable pageable);

    /**
     * Get users by country
     */
    Page<UserResponse> getUsersByCountry(String country, Pageable pageable);

    /**
     * Get inactive users since date
     */
    Page<UserResponse> getInactiveUsersSince(Instant since, Pageable pageable);

    /**
     * Get locked users
     */
    Page<UserResponse> getLockedUsers(Pageable pageable);

    /**
     * Update user status
     */
    UserResponse updateUserStatus(UUID userId, UserStatus status);

    /**
     * Update KYC status
     */
    UserResponse updateKycStatus(UUID userId, KycStatus kycStatus);

    /**
     * Update last login
     */
    void updateLastLogin(UUID userId);

    /**
     * Increment failed login attempts
     */
    void incrementFailedLoginAttempts(UUID userId);

    /**
     * Lock user account
     */
    void lockAccount(UUID userId, int durationMinutes);

    /**
     * Unlock user account
     */
    void unlockAccount(UUID userId);

    /**
     * Check if email exists
     */
    boolean emailExists(String email);

    /**
     * Count users by status
     */
    Long countByStatus(UserStatus status);

    /**
     * Count users by KYC status
     */
    Long countByKycStatus(KycStatus kycStatus);

    /**
     * Search users by name or email
     */
    Page<UserResponse> searchUsers(String searchTerm, Pageable pageable);
}
