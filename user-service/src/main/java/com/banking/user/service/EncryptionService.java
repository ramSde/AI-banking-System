package com.banking.user.service;

/**
 * Service interface for encryption and decryption operations.
 * Provides AES-256-GCM encryption for PII data.
 */
public interface EncryptionService {

    /**
     * Encrypt plaintext data
     * 
     * @param plaintext Data to encrypt
     * @return Encrypted data as Base64 string
     */
    String encrypt(String plaintext);

    /**
     * Decrypt encrypted data
     * 
     * @param ciphertext Encrypted data as Base64 string
     * @return Decrypted plaintext
     */
    String decrypt(String ciphertext);

    /**
     * Encrypt sensitive field with error handling
     * 
     * @param plaintext Data to encrypt
     * @return Encrypted data or null if encryption fails
     */
    String encryptSafe(String plaintext);

    /**
     * Decrypt sensitive field with error handling
     * 
     * @param ciphertext Encrypted data
     * @return Decrypted data or masked value if decryption fails
     */
    String decryptSafe(String ciphertext);
}
