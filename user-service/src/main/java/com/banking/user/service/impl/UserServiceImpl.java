package com.banking.user.service.impl;

import com.banking.user.domain.KycStatus;
import com.banking.user.domain.User;
import com.banking.user.domain.UserStatus;
import com.banking.user.dto.*;
import com.banking.user.event.KycStatusChangedEvent;
import com.banking.user.event.UserCreatedEvent;
import com.banking.user.event.UserEventPublisher;
import com.banking.user.event.UserUpdatedEvent;
import com.banking.user.exception.UserAlreadyExistsException;
import com.banking.user.exception.UserNotFoundException;
import com.banking.user.mapper.UserMapper;
import com.banking.user.repository.UserRepository;
import com.banking.user.service.EncryptionService;
import com.banking.user.service.UserService;
import com.banking.user.util.MaskingUtil;
import com.banking.user.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserService for user management operations.
 * Handles user profile CRUD with PII encryption and event publishing.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final UserMapper userMapper;
    private final UserEventPublisher eventPublisher;

    public UserServiceImpl(
            UserRepository userRepository,
            EncryptionService encryptionService,
            UserMapper userMapper,
            UserEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.userMapper = userMapper;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.email());

        if (!ValidationUtil.isValidEmail(request.email())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (!ValidationUtil.isValidPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("Invalid phone number format");
        }

        if (!ValidationUtil.isValidDateOfBirth(request.dateOfBirth())) {
            throw new IllegalArgumentException("User must be at least 18 years old");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with email already exists: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .phoneNumber(MaskingUtil.maskPhoneNumber(request.phoneNumber()))
                .phoneNumberEncrypted(encryptionService.encrypt(request.phoneNumber()))
                .firstName(MaskingUtil.maskName(request.firstName()))
                .firstNameEncrypted(encryptionService.encrypt(request.firstName()))
                .lastName(MaskingUtil.maskName(request.lastName()))
                .lastNameEncrypted(encryptionService.encrypt(request.lastName()))
                .dateOfBirth(LocalDate.of(1900, 1, 1))
                .dateOfBirthEncrypted(encryptionService.encrypt(request.dateOfBirth().toString()))
                .address(request.address() != null ? MaskingUtil.maskAddress(request.address()) : null)
                .addressEncrypted(request.address() != null ? encryptionService.encrypt(request.address()) : null)
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .postalCode(request.postalCode())
                .userStatus(UserStatus.PENDING_VERIFICATION)
                .kycStatus(KycStatus.PENDING)
                .build();

        User savedUser = userRepository.save(user);

        eventPublisher.publishUserCreated(
                UserCreatedEvent.create(savedUser.getId(), savedUser.getEmail(), UUID.randomUUID().toString())
        );

        log.info("User created successfully: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Cacheable(value = "user", key = "#userId")
    public Optional<UserResponse> getUserById(UUID userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findByIdAndNotDeleted(userId)
                .map(userMapper::toResponse);
    }

    @Override
    @Cacheable(value = "user", key = "#email")
    public Optional<UserResponse> getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);
        return userRepository.findByEmailAndNotDeleted(email)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        if (request.phoneNumber() != null) {
            if (!ValidationUtil.isValidPhoneNumber(request.phoneNumber())) {
                throw new IllegalArgumentException("Invalid phone number format");
            }
            user.setPhoneNumber(MaskingUtil.maskPhoneNumber(request.phoneNumber()));
            user.setPhoneNumberEncrypted(encryptionService.encrypt(request.phoneNumber()));
        }

        if (request.address() != null) {
            user.setAddress(MaskingUtil.maskAddress(request.address()));
            user.setAddressEncrypted(encryptionService.encrypt(request.address()));
        }

        if (request.city() != null) {
            user.setCity(request.city());
        }

        if (request.state() != null) {
            user.setState(request.state());
        }

        if (request.country() != null) {
            user.setCountry(request.country());
        }

        if (request.postalCode() != null) {
            user.setPostalCode(request.postalCode());
        }

        User updatedUser = userRepository.save(user);

        eventPublisher.publishUserUpdated(
                UserUpdatedEvent.create(updatedUser.getId(), updatedUser.getEmail(), UUID.randomUUID().toString())
        );

        log.info("User updated successfully: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public void deleteUser(UUID userId) {
        log.info("Soft deleting user: {}", userId);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        userRepository.softDelete(userId, Instant.now(), Instant.now());
        log.info("User soft deleted: {}", userId);
    }

    @Override
    public Page<UserResponse> getUsersByStatus(UserStatus status, Pageable pageable) {
        log.debug("Fetching users by status: {}", status);
        return userRepository.findByUserStatus(status, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getUsersByKycStatus(KycStatus kycStatus, Pageable pageable) {
        log.debug("Fetching users by KYC status: {}", kycStatus);
        return userRepository.findByKycStatus(kycStatus, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getUsersByCountry(String country, Pageable pageable) {
        log.debug("Fetching users by country: {}", country);
        return userRepository.findByCountry(country, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getInactiveUsersSince(Instant since, Pageable pageable) {
        log.debug("Fetching inactive users since: {}", since);
        return userRepository.findInactiveUsersSince(since, pageable)
                .map(userMapper::toResponse);
    }

    @Override
    public Page<UserResponse> getLockedUsers(Pageable pageable) {
        log.debug("Fetching locked users");
        return userRepository.findLockedUsers(pageable)
                .map(userMapper::toResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public UserResponse updateUserStatus(UUID userId, UserStatus status) {
        log.info("Updating user status: {} to {}", userId, status);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        user.setUserStatus(status);
        User updatedUser = userRepository.save(user);

        log.info("User status updated: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user", key = "#userId")
    public UserResponse updateKycStatus(UUID userId, KycStatus kycStatus) {
        log.info("Updating KYC status: {} to {}", userId, kycStatus);

        User user = userRepository.findByIdAndNotDeleted(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        KycStatus oldStatus = user.getKycStatus();
        user.setKycStatus(kycStatus);

        if (kycStatus == KycStatus.VERIFIED) {
            user.setKycVerifiedAt(Instant.now());
        }

        User updatedUser = userRepository.save(user);

        eventPublisher.publishKycStatusChanged(
                KycStatusChangedEvent.create(userId, oldStatus, kycStatus, UUID.randomUUID().toString())
        );

        log.info("KYC status updated: {}", userId);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID userId) {
        log.debug("Updating last login for user: {}", userId);
        userRepository.updateLastLogin(userId, Instant.now(), Instant.now());
    }

    @Override
    @Transactional
    public void incrementFailedLoginAttempts(UUID userId) {
        log.debug("Incrementing failed login attempts for user: {}", userId);
        userRepository.incrementFailedLoginAttempts(userId, Instant.now());
    }

    @Override
    @Transactional
    public void lockAccount(UUID userId, int durationMinutes) {
        log.warn("Locking account: {} for {} minutes", userId, durationMinutes);
        Instant lockedUntil = Instant.now().plusSeconds(durationMinutes * 60L);
        userRepository.lockAccount(userId, lockedUntil, Instant.now());
    }

    @Override
    @Transactional
    public void unlockAccount(UUID userId) {
        log.info("Unlocking account: {}", userId);
        userRepository.unlockAccount(userId, Instant.now());
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Long countByStatus(UserStatus status) {
        return userRepository.countByUserStatus(status);
    }

    @Override
    public Long countByKycStatus(KycStatus kycStatus) {
        return userRepository.countByKycStatus(kycStatus);
    }

    @Override
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        log.debug("Searching users with term: {}", searchTerm);
        return userRepository.searchUsers(searchTerm, pageable)
                .map(userMapper::toResponse);
    }
}
