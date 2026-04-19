package com.banking.device.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for generating device fingerprints.
 * Creates unique hashes based on device characteristics.
 */
@Component
@Slf4j
public class FingerprintGenerator {

    private final String salt;
    private final String algorithm;

    public FingerprintGenerator(
            @Value("${device.fingerprint.salt:banking-device-salt-2024}") String salt,
            @Value("${device.fingerprint.algorithm:SHA-256}") String algorithm) {
        this.salt = salt;
        this.algorithm = algorithm;
    }

    /**
     * Generate device fingerprint hash from device characteristics
     */
    public String generateFingerprint(String userAgent, String screenResolution, 
                                     String timezone, String language) {
        log.debug("Generating device fingerprint");

        StringBuilder fingerprintData = new StringBuilder();
        fingerprintData.append(userAgent != null ? userAgent : "");
        fingerprintData.append("|");
        fingerprintData.append(screenResolution != null ? screenResolution : "");
        fingerprintData.append("|");
        fingerprintData.append(timezone != null ? timezone : "");
        fingerprintData.append("|");
        fingerprintData.append(language != null ? language : "");
        fingerprintData.append("|");
        fingerprintData.append(salt);

        String hash = DigestUtils.sha256Hex(fingerprintData.toString());
        
        log.debug("Device fingerprint generated: {}", hash.substring(0, 8) + "...");
        return hash;
    }

    /**
     * Generate fingerprint with additional hardware characteristics
     */
    public String generateFingerprintWithHardware(String userAgent, String screenResolution,
                                                  String timezone, String language,
                                                  String cpuCores, String memory, String gpu) {
        log.debug("Generating device fingerprint with hardware info");

        StringBuilder fingerprintData = new StringBuilder();
        fingerprintData.append(userAgent != null ? userAgent : "");
        fingerprintData.append("|");
        fingerprintData.append(screenResolution != null ? screenResolution : "");
        fingerprintData.append("|");
        fingerprintData.append(timezone != null ? timezone : "");
        fingerprintData.append("|");
        fingerprintData.append(language != null ? language : "");
        fingerprintData.append("|");
        fingerprintData.append(cpuCores != null ? cpuCores : "");
        fingerprintData.append("|");
        fingerprintData.append(memory != null ? memory : "");
        fingerprintData.append("|");
        fingerprintData.append(gpu != null ? gpu : "");
        fingerprintData.append("|");
        fingerprintData.append(salt);

        String hash = DigestUtils.sha256Hex(fingerprintData.toString());
        
        log.debug("Device fingerprint with hardware generated: {}", hash.substring(0, 8) + "...");
        return hash;
    }

    /**
     * Validate fingerprint format
     */
    public boolean isValidFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.isEmpty()) {
            return false;
        }
        return fingerprint.matches("^[a-f0-9]{64}$");
    }

    /**
     * Mask fingerprint for display (show first 8 and last 8 characters)
     */
    public String maskFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.length() < 16) {
            return "***";
        }
        return fingerprint.substring(0, 8) + "..." + fingerprint.substring(fingerprint.length() - 8);
    }
}