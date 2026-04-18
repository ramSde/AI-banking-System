package com.banking.identity.service.impl;

import com.banking.identity.config.IdentityProperties;
import com.banking.identity.domain.Credential;
import com.banking.identity.exception.InvalidCredentialsException;
import com.banking.identity.exception.UserNotFoundException;
import com.banking.identity.exception.WeakPasswordException;
import com.banking.identity.repository.CredentialRepository;
import com.banking.identity.service.PasswordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Password Service Implementation
 * 
 * Handles password hashing, validation, and password change operations using BCrypt.
 */
@Service
@Slf4j
public class PasswordServiceImpl implements PasswordService {

    private final CredentialRepository credentialRepository;
    private final IdentityProperties identityProperties;
    private final BCryptPasswordEncoder passwordEncoder;

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    public PasswordServiceImpl(
            final CredentialRepository credentialRepository,
            final IdentityProperties identityProperties) {
        this.credentialRepository = credentialRepository;
        this.identityProperties = identityProperties;
        this.passwordEncoder = new BCryptPasswordEncoder(identityProperties.getPassword().getBcryptStrength());
    }

    @Override
    public String hashPassword(final String plainPassword) {
        log.debug("Hashing password with BCrypt strength: {}", identityProperties.getPassword().getBcryptStrength());
        return passwordEncoder.encode(plainPassword);
    }

    @Override
    public boolean verifyPassword(final String plainPassword, final String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    @Override
    public void validatePasswordStrength(final String password) {
        final var passwordConfig = identityProperties.getPassword();

        if (password == null || password.length() < passwordConfig.getMinLength()) {
            throw new WeakPasswordException(
                    String.format("Password must be at least %d characters long", passwordConfig.getMinLength())
            );
        }

        if (Boolean.TRUE.equals(passwordConfig.getRequireUppercase()) && !UPPERCASE_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain at least one uppercase letter");
        }

        if (Boolean.TRUE.equals(passwordConfig.getRequireLowercase()) && !LOWERCASE_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain at least one lowercase letter");
        }

        if (Boolean.TRUE.equals(passwordConfig.getRequireDigit()) && !DIGIT_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain at least one digit");
        }

        if (Boolean.TRUE.equals(passwordConfig.getRequireSpecial()) && !SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            throw new WeakPasswordException("Password must contain at least one special character");
        }

        log.debug("Password strength validation passed");
    }

    @Override
    @Transactional
    public void changePassword(final UUID userId, final String currentPassword, final String newPassword) {
        log.info("Changing password for user: {}", userId);

        final Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User credentials not found"));

        if (!verifyPassword(currentPassword, credential.getPasswordHash())) {
            log.warn("Invalid current password provided for user: {}", userId);
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (isPasswordRecentlyUsed(userId, newPassword)) {
            throw new WeakPasswordException("Password was recently used. Please choose a different password");
        }

        validatePasswordStrength(newPassword);

        final String newPasswordHash = hashPassword(newPassword);
        credential.updatePasswordHash(newPasswordHash);
        credentialRepository.save(credential);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    public boolean isPasswordRecentlyUsed(final UUID userId, final String newPassword) {
        final Credential credential = credentialRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User credentials not found"));

        if (credential.getPreviousPasswordHashes() == null) {
            return false;
        }

        for (final String previousHash : credential.getPreviousPasswordHashes()) {
            if (verifyPassword(newPassword, previousHash)) {
                log.debug("Password was recently used for user: {}", userId);
                return true;
            }
        }

        return false;
    }
}
