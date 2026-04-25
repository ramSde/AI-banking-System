package com.banking.transaction.service.impl;

import com.banking.transaction.config.TransactionProperties;
import com.banking.transaction.domain.IdempotencyKey;
import com.banking.transaction.repository.IdempotencyKeyRepository;
import com.banking.transaction.service.IdempotencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Idempotency Service Implementation
 * 
 * Handles idempotency key storage and validation for duplicate prevention.
 */
@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyServiceImpl.class);

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final TransactionProperties transactionProperties;

    public IdempotencyServiceImpl(
            IdempotencyKeyRepository idempotencyKeyRepository,
            TransactionProperties transactionProperties) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.transactionProperties = transactionProperties;
    }

    @Override
    public Optional<IdempotencyKey> findByKey(String key) {
        log.debug("Looking up idempotency key: {}", key);
        return idempotencyKeyRepository.findByKeyAndNotExpired(key, Instant.now());
    }

    @Override
    @Transactional
    public void storeKey(String key, UUID transactionId, String requestHash, String responseBody, int responseStatus) {
        log.info("Storing idempotency key: {}", key);

        Instant expiresAt = Instant.now().plusSeconds(transactionProperties.getIdempotency().getTtlHours() * 3600L);

        IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                .idempotencyKey(key)
                .transactionId(transactionId)
                .requestHash(requestHash)
                .responseBody(responseBody)
                .responseStatus(responseStatus)
                .expiresAt(expiresAt)
                .build();

        idempotencyKeyRepository.save(idempotencyKey);
        log.debug("Idempotency key stored successfully: {}", key);
    }

    @Override
    public boolean validateRequestHash(String key, String requestHash) {
        Optional<IdempotencyKey> existing = findByKey(key);
        if (existing.isEmpty()) {
            return true;
        }

        boolean matches = existing.get().getRequestHash().equals(requestHash);
        if (!matches) {
            log.warn("Request hash mismatch for idempotency key: {}", key);
        }
        return matches;
    }

    @Override
    @Transactional
    public int cleanupExpiredKeys() {
        log.info("Cleaning up expired idempotency keys");
        int deleted = idempotencyKeyRepository.deleteExpiredKeys(Instant.now());
        log.info("Deleted {} expired idempotency keys", deleted);
        return deleted;
    }
}
