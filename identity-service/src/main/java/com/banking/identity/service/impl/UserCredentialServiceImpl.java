package com.banking.identity.service.impl;

import com.banking.identity.config.IdentityProperties;
import com.banking.identity.domain.Credential;
import com.banking.identity.domain.User;
import com.banking.identity.domain.UserStatus;
import com.banking.identity.exception.AccountLockedException;
import com.banking.identity.exception.InvalidCredentialsException;
import com.banking.identity.exception.UserAlreadyExistsException;
import com.banking.identity.exception.UserNotFoundException;
import com.banking.identity.repository.CredentialRepository;
import com.banking.identity.repository.UserRepository;
import com.banking.identity.service.PasswordService;
import com.banking.identity.service.UserCredentialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * User Credential Service Implementation
 * 
 * Handles user and credential management operations.
 */
@Service
@Slf4j
public class UserCredentialServiceImpl implements UserCredentialService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordService passwordService;
    private final IdentityProperties identityProperties;

    public UserCredentialServiceImpl(
            final UserRepository userRepository,
            final CredentialRepository credentialRepository,
            final PasswordService passwordService,
            final IdentityProperties identityProperties) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.passwordService = passwordService;
        this.identityProperties = identityProperties;
    }

    @Override
    @Transactional
    public User createUser(final String email, final String password, final String phoneNumber, final String username) {
        log.info("Creating new user with email: {}", email);

        if (emailExists(email)) {
            throw new UserAlreadyExistsException("Email already registered: " + email);
        }

        if (username != null && usernameExists(username)) {
            throw new UserAlreadyExistsException("Username already taken: " + username);
        }

        passwordService.validatePasswordStrength(password);

        final User user = new User();
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setUsername(username);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setFailedLoginAttempts(0);

        final User savedUser = userRepository.save(user);
        log.debug("User created with ID: {}", savedUser.getId());

        final String passwordHash = passwordService.hashPassword(password);
        final Credential credential = new Credential();
        credential.setUserId(savedUser.getId());
        credential.setPasswordHash(passwordHash);
        credential.setPasswordChangedAt(Instant.now());
        credential.setMustChangePassword(false);

        credentialRepository.save(credential);
        log.info("User and credentials created successfully for email: {}", email);

        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByEmail(final String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(final UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    @Override
    @Transactional
    public User verifyCredentials(final String email, final String password) {
        log.debug("Verifying credentials for email: {}", email);

        final User user = findUserByEmail(email);

        if (isAccountLocked(user)) {
            log.warn("Account is locked for user: {}", user.getId());
            throw new AccountLockedException(
                    String.format("Account is locked until %s due to multiple failed login attempts", 
                            user.getLockedUntil())
            );
        }

        final Credential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("User credentials not found"));

        if (!passwordService.verifyPassword(password, credential.getPasswordHash())) {
            log.warn("Invalid password for user: {}", user.getId());
            handleFailedLoginAttempt(user);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.debug("Credentials verified successfully for user: {}", user.getId());
        return user;
    }

    @Override
    @Transactional
    public void handleFailedLoginAttempt(final User user) {
        user.incrementFailedLoginAttempts();

        final int maxAttempts = identityProperties.getSecurity().getMaxLoginAttempts();
        if (user.getFailedLoginAttempts() >= maxAttempts) {
            final int lockoutMinutes = identityProperties.getSecurity().getLockoutDurationMinutes();
            final Instant lockoutUntil = Instant.now().plusSeconds(lockoutMinutes * 60L);
            user.lockUntil(lockoutUntil);
            log.warn("Account locked for user: {} until {}", user.getId(), lockoutUntil);
        }

        userRepository.save(user);
        log.info("Failed login attempt recorded for user: {}. Total attempts: {}", 
                user.getId(), user.getFailedLoginAttempts());
    }

    @Override
    @Transactional
    public void handleSuccessfulLogin(final User user, final String ipAddress) {
        user.recordSuccessfulLogin(ipAddress);
        userRepository.save(user);
        log.info("Successful login recorded for user: {} from IP: {}", user.getId(), ipAddress);
    }

    @Override
    public boolean isAccountLocked(final User user) {
        return user.isLocked();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(final String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(final String username) {
        if (username == null) {
            return false;
        }
        return userRepository.existsByUsername(username);
    }
}
