package com.banking.otp.service.impl;

import com.banking.otp.config.OtpProperties;
import com.banking.otp.domain.MfaEnrollment;
import com.banking.otp.domain.MfaMethod;
import com.banking.otp.domain.MfaStatus;
import com.banking.otp.dto.EnrollTotpResponse;
import com.banking.otp.event.MfaEnrolledEvent;
import com.banking.otp.exception.MfaAlreadyEnrolledException;
import com.banking.otp.exception.MfaNotEnrolledException;
import com.banking.otp.exception.InvalidOtpException;
import com.banking.otp.repository.MfaEnrollmentRepository;
import com.banking.otp.service.TotpService;
import com.banking.otp.util.QrCodeGenerator;
import com.banking.otp.util.TotpGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of TOTP service
 */
@Service
public class TotpServiceImpl implements TotpService {

    private static final Logger logger = LoggerFactory.getLogger(TotpServiceImpl.class);

    private final MfaEnrollmentRepository mfaEnrollmentRepository;
    private final TotpGenerator totpGenerator;
    private final QrCodeGenerator qrCodeGenerator;
    private final OtpProperties otpProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public TotpServiceImpl(
            MfaEnrollmentRepository mfaEnrollmentRepository,
            TotpGenerator totpGenerator,
            QrCodeGenerator qrCodeGenerator,
            OtpProperties otpProperties,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.mfaEnrollmentRepository = mfaEnrollmentRepository;
        this.totpGenerator = totpGenerator;
        this.qrCodeGenerator = qrCodeGenerator;
        this.otpProperties = otpProperties;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Transactional
    public EnrollTotpResponse enrollTotp(UUID userId, String accountName) {
        logger.info("Enrolling user {} in TOTP", userId);

        // Check if already enrolled
        Optional<MfaEnrollment> existing = mfaEnrollmentRepository.findByUserIdAndMethod(userId, MfaMethod.TOTP);
        if (existing.isPresent() && existing.get().getStatus() == MfaStatus.ACTIVE) {
            throw new MfaAlreadyEnrolledException("User is already enrolled in TOTP");
        }

        // Generate secret
        String secret = totpGenerator.generateSecret();

        // Create or update enrollment
        MfaEnrollment enrollment = existing.orElse(MfaEnrollment.builder()
                .userId(userId)
                .mfaMethod(MfaMethod.TOTP)
                .build());

        enrollment.setTotpSecret(secret);
        enrollment.setStatus(MfaStatus.ACTIVE);
        enrollment.setVerified(false);

        mfaEnrollmentRepository.save(enrollment);

        // Generate QR code
        String totpUri = totpGenerator.generateTotpUri(
                secret,
                otpProperties.getTotp().getIssuer(),
                accountName,
                otpProperties.getTotp().getPeriodSeconds(),
                otpProperties.getTotp().getDigits(),
                otpProperties.getTotp().getAlgorithm()
        );
        String qrCode = qrCodeGenerator.generateQrCode(totpUri);
        String manualEntryKey = totpGenerator.formatSecretForManualEntry(secret);

        logger.info("TOTP enrollment initiated for user {}", userId);

        return new EnrollTotpResponse(
                secret,
                qrCode,
                manualEntryKey,
                otpProperties.getTotp().getIssuer(),
                accountName
        );
    }

    @Override
    @Transactional
    public boolean verifyTotpEnrollment(UUID userId, String code) {
        logger.info("Verifying TOTP enrollment for user {}", userId);

        MfaEnrollment enrollment = mfaEnrollmentRepository.findByUserIdAndMethod(userId, MfaMethod.TOTP)
                .orElseThrow(() -> new MfaNotEnrolledException("User is not enrolled in TOTP"));

        boolean isValid = totpGenerator.verifyCode(
                enrollment.getTotpSecret(),
                code,
                otpProperties.getTotp().getPeriodSeconds(),
                otpProperties.getTotp().getDigits(),
                otpProperties.getTotp().getAlgorithm(),
                otpProperties.getTotp().getTimeStepTolerance()
        );

        if (isValid) {
            enrollment.setVerified(true);
            enrollment.setVerifiedAt(Instant.now());
            enrollment.setLastUsedAt(Instant.now());
            mfaEnrollmentRepository.save(enrollment);

            // Publish event
            MfaEnrolledEvent event = MfaEnrolledEvent.create(userId, MfaMethod.TOTP, true, UUID.randomUUID().toString());
            kafkaTemplate.send("banking.otp.mfa-enrolled", event);

            logger.info("TOTP enrollment verified for user {}", userId);
            return true;
        }

        logger.warn("Invalid TOTP code for user {} during enrollment", userId);
        return false;
    }

    @Override
    @Transactional
    public boolean verifyTotp(UUID userId, String code) {
        logger.info("Verifying TOTP for user {}", userId);

        MfaEnrollment enrollment = mfaEnrollmentRepository.findActiveVerifiedByUserIdAndMethod(userId, MfaMethod.TOTP)
                .orElseThrow(() -> new MfaNotEnrolledException("User is not enrolled in TOTP or enrollment not verified"));

        boolean isValid = totpGenerator.verifyCode(
                enrollment.getTotpSecret(),
                code,
                otpProperties.getTotp().getPeriodSeconds(),
                otpProperties.getTotp().getDigits(),
                otpProperties.getTotp().getAlgorithm(),
                otpProperties.getTotp().getTimeStepTolerance()
        );

        if (isValid) {
            enrollment.setLastUsedAt(Instant.now());
            mfaEnrollmentRepository.save(enrollment);
            logger.info("TOTP verified successfully for user {}", userId);
            return true;
        }

        logger.warn("Invalid TOTP code for user {}", userId);
        throw new InvalidOtpException("Invalid TOTP code");
    }

    @Override
    @Transactional
    public void disableTotp(UUID userId) {
        logger.info("Disabling TOTP for user {}", userId);

        MfaEnrollment enrollment = mfaEnrollmentRepository.findByUserIdAndMethod(userId, MfaMethod.TOTP)
                .orElseThrow(() -> new MfaNotEnrolledException("User is not enrolled in TOTP"));

        enrollment.setStatus(MfaStatus.DISABLED);
        enrollment.setDeletedAt(Instant.now());
        mfaEnrollmentRepository.save(enrollment);

        logger.info("TOTP disabled for user {}", userId);
    }
}
