package com.banking.transaction.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Idempotency Validator
 * 
 * Validates idempotency keys and generates request hashes for duplicate detection.
 * Uses SHA-256 for request body hashing.
 */
@Component
public class IdempotencyValidator {

    private static final String HASH_ALGORITHM = "SHA-256";

    public boolean isValidKey(String key) {
        return key != null && !key.trim().isEmpty() && key.length() <= 255;
    }

    public String generateRequestHash(String requestBody) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hash = digest.digest(requestBody.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate request hash", e);
        }
    }
}
