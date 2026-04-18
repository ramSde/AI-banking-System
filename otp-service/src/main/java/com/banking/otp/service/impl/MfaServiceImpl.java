package com.banking.otp.service.impl;

import com.banking.otp.domain.MfaEnrollment;
import com.banking.otp.domain.MfaMethod;
import com.banking.otp.domain.MfaStatus;
import com.banking.otp.exception.MfaNotEnrolledException;
import com.banking.otp.repository.MfaEnrollmentRepository;
import com.banking.otp.service.MfaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of MFA management service
 */
@Service
public class MfaServiceImpl implements MfaService {

    private static final Logger logger = LoggerFactory.getLogger(MfaServiceImpl.class);

    private final MfaEnrollmentRepository mfaEnrollmentRepository;

    public MfaServiceImpl(MfaEnrollmentRepository mfaEnrollmentRepository) {
        this.mfaEnrollmentRepository = mfaEnrollmentRepository;
    }

    @Override
    public List<MfaEnrollment> getUserMfaEnrollments(UUID userId) {
        logger.debug("Getting MFA enrollments for user {}", userId);
        return mfaEnrollmentRepository.findActiveByUserId(userId);
    }

    @Override
    public boolean hasActiveMfa(UUID userId) {
        return mfaEnrollmentRepository.hasActiveMfa(userId);
    }

    @Override
    public MfaEnrollment getMfaEnrollment(UUID userId, MfaMethod method) {
        return mfaEnrollmentRepository.findByUserIdAndMethod(userId, method)
                .orElseThrow(() -> new MfaNotEnrolledException("MFA enrollment not found for method: " + method));
    }

    @Override
    @Transactional
    public void disableMfaMethod(UUID userId, MfaMethod method) {
        logger.info("Disabling MFA method {} for user {}", method, userId);

        MfaEnrollment enrollment = mfaEnrollmentRepository.findByUserIdAndMethod(userId, method)
                .orElseThrow(() -> new MfaNotEnrolledException("MFA enrollment not found for method: " + method));

        enrollment.setStatus(MfaStatus.DISABLED);
        enrollment.setDeletedAt(Instant.now());
        mfaEnrollmentRepository.save(enrollment);

        logger.info("MFA method {} disabled for user {}", method, userId);
    }

    @Override
    @Transactional
    public void disableAllMfa(UUID userId) {
        logger.info("Disabling all MFA methods for user {}", userId);

        List<MfaEnrollment> enrollments = mfaEnrollmentRepository.findActiveByUserId(userId);

        for (MfaEnrollment enrollment : enrollments) {
            enrollment.setStatus(MfaStatus.DISABLED);
            enrollment.setDeletedAt(Instant.now());
        }

        mfaEnrollmentRepository.saveAll(enrollments);

        logger.info("All MFA methods disabled for user {}", userId);
    }
}
