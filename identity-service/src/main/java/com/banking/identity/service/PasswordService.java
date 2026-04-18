package com.banking.identity.service;

import java.util.UUID;

/**
 * Password Service Interface
 * 
 * Handles password hashing, validation, and password change operations.
 */
public interface PasswordService {

    /**
     * Hash password using BCrypt
     * 
     * @param plainPassword Plain text password
     * @return BCrypt hashed password
     */
    String hashPassword(String plainPassword);

    /**
     * Verify password against hash
     * 
     * @param plainPassword Plain text password
     * @param hashedPassword BCrypt hashed password
     * @return true if password matches, false otherwise
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);

    /**
     * Validate password strength
     * 
     * @param password Password to validate
     * @throws com.banking.identity.exception.WeakPasswordException if password is weak
     */
    void validatePasswordStrength(String password);

    /**
     * Change user password
     * 
     * @param userId User ID
     * @param currentPassword Current password
     * @param newPassword New password
     */
    void changePassword(UUID userId, String currentPassword, String newPassword);

    /**
     * Check if password was recently used
     * 
     * @param userId User ID
     * @param newPassword New password to check
     * @return true if password was recently used, false otherwise
     */
    boolean isPasswordRecentlyUsed(UUID userId, String newPassword);
}
