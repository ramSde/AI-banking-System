package com.banking.identity.service;

import com.banking.identity.domain.User;

import java.util.UUID;

/**
 * User Credential Service Interface
 * 
 * Handles user and credential management operations.
 */
public interface UserCredentialService {

    /**
     * Create new user with credentials
     * 
     * @param email User email
     * @param password Plain text password
     * @param phoneNumber Optional phone number
     * @param username Optional username
     * @return Created user
     */
    User createUser(String email, String password, String phoneNumber, String username);

    /**
     * Find user by email
     * 
     * @param email User email
     * @return User entity
     */
    User findUserByEmail(String email);

    /**
     * Find user by ID
     * 
     * @param userId User ID
     * @return User entity
     */
    User findUserById(UUID userId);

    /**
     * Verify user credentials
     * 
     * @param email User email
     * @param password Plain text password
     * @return User entity if credentials are valid
     */
    User verifyCredentials(String email, String password);

    /**
     * Handle failed login attempt
     * 
     * @param user User entity
     */
    void handleFailedLoginAttempt(User user);

    /**
     * Handle successful login
     * 
     * @param user User entity
     * @param ipAddress IP address
     */
    void handleSuccessfulLogin(User user, String ipAddress);

    /**
     * Check if user account is locked
     * 
     * @param user User entity
     * @return true if account is locked, false otherwise
     */
    boolean isAccountLocked(User user);

    /**
     * Check if email already exists
     * 
     * @param email Email to check
     * @return true if email exists, false otherwise
     */
    boolean emailExists(String email);

    /**
     * Check if username already exists
     * 
     * @param username Username to check
     * @return true if username exists, false otherwise
     */
    boolean usernameExists(String username);
}
