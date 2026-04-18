package com.banking.otp.service.impl;

import com.banking.otp.config.OtpProperties;
import com.banking.otp.domain.BackupCode;
import com.banking.otp.event.BackupCodeUsedEvent;
import com.banking.otp.exception.InvalidOtpException;
import com.banking.otp.repository.BackupCodeRepository;
import com.banking.otp.service.BackupCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of backup code service
 */
@Service
public class BackupCodeServiceImpl implements BackupCodeService {

    private static final Logger logger = LoggerFactory.getLogger(BackupCodeServiceImpl.class);
    private static final String BACKUP_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final BackupCodeRepository backupCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpProperties otpProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SecureRandom secureRandom;

    public BackupCodeServiceImpl(
            BackupCodeRepository backupCodeRepository,
            PasswordEncoder passwordEncoder,
            OtpProperties otpProperties,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.backupCodeRepository = backupCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpProperties = otpProperties;
        this.kafkaTemplate = kafkaTemplate;
        this.secureRandom = new SecureRandom();
    }

    @Override
    @Transactional
    public List<String> generateBackupCodes(UUID userId) {
        logger.info("Generating backup codes for user {}", userId);

        List<String> plainCodes = new ArrayList<>();
        List<BackupCode> backupCodes = new ArrayList<>();

        for (int i = 0; i < otpProperties.getBackupCode().getCount(); i++) {
            String plainCode = generateBackupCode();
            String codeHash = passwordEncoder.encode(plainCode.replace("-", ""));

            BackupCode backupCode = BackupCode.builder()
                    .userId(userId)
                    .codeHash(codeHash)
                    .used(false)
                    .build();

            backupCodes.add(backupCode);
            plainCodes.add(plainCode);
        }

        backupCodeRepository.saveAll(backupCodes);

        logger.info("Generated {} backup codes for user {}", plainCodes.size(), userId);
        return plainCodes;
    }

    @Override
    @Transactional
    public boolean verifyBackupCode(UUID userId, String code) {
        logger.info("Verifying backup code for user {}", userId);

        // Remove hyphens from input code
        String normalizedCode = code.replace("-", "");

        List<BackupCode> unusedCodes = backupCodeRepository.findUnusedByUserId(userId);

        if (unusedCodes.isEmpty()) {
            throw new InvalidOtpException("No unused backup codes available");
        }

        // Try to match against all unused codes
        for (BackupCode backupCode : unusedCodes) {
            if (passwordEncoder.matches(normalizedCode, backupCode.getCodeHash())) {
                // Mark as used
                backupCode.setUsed(true);
                backupCode.setUsedAt(Instant.now());
                backupCodeRepository.save(backupCode);

                // Get remaining count
                long remainingCount = getRemainingBackupCodesCount(userId);

                // Publish event
                BackupCodeUsedEvent event = BackupCodeUsedEvent.create(
                        userId,
                        backupCode.getId(),
                        (int) remainingCount,
                        UUID.randomUUID().toString()
                );
                kafkaTemplate.send("banking.otp.backup-code-used", event);

                logger.info("Backup code verified for user {}, remaining codes: {}", userId, remainingCount);
                return true;
            }
        }

        logger.warn("Invalid backup code for user {}", userId);
        throw new InvalidOtpException("Invalid backup code");
    }

    @Override
    public long getRemainingBackupCodesCount(UUID userId) {
        return backupCodeRepository.countUnusedByUserId(userId);
    }

    @Override
    @Transactional
    public List<String> regenerateBackupCodes(UUID userId) {
        logger.info("Regenerating backup codes for user {}", userId);

        // Soft delete existing codes
        backupCodeRepository.softDeleteByUserId(userId);

        // Generate new codes
        return generateBackupCodes(userId);
    }

    private String generateBackupCode() {
        int length = otpProperties.getBackupCode().getLength();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            if (i > 0 && i % 4 == 0) {
                code.append('-');
            }
            int index = secureRandom.nextInt(BACKUP_CODE_CHARS.length());
            code.append(BACKUP_CODE_CHARS.charAt(index));
        }

        return code.toString();
    }
}
