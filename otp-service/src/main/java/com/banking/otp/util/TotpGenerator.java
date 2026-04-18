package com.banking.otp.util;

import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * TOTP (Time-based One-Time Password) generator following RFC 6238
 */
@Component
public class TotpGenerator {

    private static final Logger logger = LoggerFactory.getLogger(TotpGenerator.class);
    private static final int SECRET_SIZE = 20; // 160 bits
    private static final Base32 base32 = new Base32();

    /**
     * Generate a random Base32-encoded secret
     */
    public String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }

    /**
     * Generate TOTP code for current time
     */
    public String generateCode(String secret, int periodSeconds, int digits, String algorithm) {
        long timeStep = Instant.now().getEpochSecond() / periodSeconds;
        return generateCode(secret, timeStep, digits, algorithm);
    }

    /**
     * Generate TOTP code for specific time step
     */
    public String generateCode(String secret, long timeStep, int digits, String algorithm) {
        try {
            byte[] key = base32.decode(secret);
            byte[] data = ByteBuffer.allocate(8).putLong(timeStep).array();

            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(key, algorithm));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error generating TOTP code", e);
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    /**
     * Verify TOTP code with time step tolerance
     */
    public boolean verifyCode(String secret, String code, int periodSeconds, int digits, String algorithm, int tolerance) {
        long currentTimeStep = Instant.now().getEpochSecond() / periodSeconds;

        // Check current time step and adjacent time steps within tolerance
        for (int i = -tolerance; i <= tolerance; i++) {
            String generatedCode = generateCode(secret, currentTimeStep + i, digits, algorithm);
            if (generatedCode.equals(code)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Format secret for manual entry (groups of 4 characters)
     */
    public String formatSecretForManualEntry(String secret) {
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < secret.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append('-');
            }
            formatted.append(secret.charAt(i));
        }
        return formatted.toString();
    }

    /**
     * Generate TOTP URI for QR code
     * Format: otpauth://totp/{issuer}:{accountName}?secret={secret}&issuer={issuer}&algorithm={algorithm}&digits={digits}&period={period}
     */
    public String generateTotpUri(String secret, String issuer, String accountName, int periodSeconds, int digits, String algorithm) {
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=%s&digits=%d&period=%d",
                issuer,
                accountName,
                secret,
                issuer,
                algorithm.replace("Hmac", ""),
                digits,
                periodSeconds
        );
    }
}
