package com.banking.otp.service.impl;

import com.banking.otp.config.OtpProperties;
import com.banking.otp.domain.MfaEnrollment;
import com.banking.otp.domain.MfaMethod;
import com.banking.otp.domain.MfaStatus;
import com.banking.otp.event.OtpVerifiedEvent;
import com.banking.otp.exception.InvalidOtpException;
import com.banking.otp.exception.MfaNotEnrolledException;
import com.banking.otp.exception.OtpExpiredException;
import com.banking.otp.repository.MfaEnrollmentRepository;
import com.banking.otp.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of OTP service for SMS/Email
 */
@Service
public class OtpServiceImpl implements OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpServiceImpl.class);
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp:attempts:";
    private static final String RATE_LIMIT_PREFIX = "otp:ratelimit:";

    private final MfaEnrollmentRepository mfaEnrollmentRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final OtpProperties otpProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SecureRandom secureRandom;

    public OtpServiceImpl(
            MfaEnrollmentRepository mfaEnrollmentRepository,
            StringRedisTemplate redisTemplate,
            PasswordEncoder passwordEncoder,
            OtpProperties otpProperties,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.mfaEnrollmentRepository = mfaEnrollmentRepository;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.otpProperties = otpProperties;
        this.kafkaTemplate = kafkaTemplate;
        this.secureRandom = new SecureRandom();
    }

    @Override
    @Transactional
    public void sendOtp(UUID userId, MfaMethod method, String destination) {
        logger.info("Sending OTP to user {} via {}", userId, method);

        if (method != MfaMethod.SMS && method != MfaMethod.EMAIL) {
            throw new IllegalArgumentException("Invalid MFA method for OTP: " + method);
        }

        // Check rate limit
        if (isRateLimited(userId)) {
            throw new InvalidOtpException("Rate limit exceeded. Please try again later.");
        }

        // Generate OTP
        String otp = generateOtp();
        String otpHash = passwordEncoder.encode(otp);

        // Store OTP hash in Redis with TTL
        String otpKey = OTP_KEY_PREFIX + userId + ":" + method;
        redisTemplate.opsForValue().set(otpKey, otpHash, Duration.ofSeconds(otpProperties.getTtlSeconds()));

        // Reset attempts counter
        String attemptsKey = OTP_ATTEMPTS_PREFIX + userId + ":" + method;
        redisTemplate.opsForValue().set(attemptsKey, "0", Duration.ofSeconds(otpProperties.getTtlSeconds()));

        // Increment rate limit counter
        incrementRateLimitCounter(userId);

        // Create or update MFA enrollment
        Optional<MfaEnrollment> existing = mfaEnrollmentRepository.findByUserIdAndMethod(userId, method);
        MfaEnrollment enrollment = existing.orElse(MfaEnrollment.builder()
                .userId(userId)
                .mfaMethod(method)
                .status(MfaStatus.ACTIVE)
                .verified(false)
                .build());

        if (method == MfaMethod.SMS) {
            enrollment.setPhoneNumber(destination);
        } else {
            enrollment.setEmail(destination);
        }

        mfaEnrollmentRepository.save(enrollment);

        // TODO: Send OTP via Notification Service
        // For now, log it (in production, this would call Notification Service)
        logger.info("OTP for user {}: {} (THIS SHOULD BE SENT VIA NOTIFICATION SERVICE)", userId, otp);

        logger.info("OTP sent to user {} via {}", userId, method);
    }

    @Override
    @Transactional
    public boolean verifyOtp(UUID userId, MfaMethod method, String code) {
        logger.info("Verifying OTP for user {} via {}", userId, method);

        if (method != MfaMethod.SMS && method != MfaMethod.EMAIL) {
            throw new IllegalArgumentException("Invalid MFA method for OTP: " + method);
        }

        String otpKey = OTP_KEY_PREFIX + userId + ":" + method;
        String attemptsKey = OTP_ATTEMPTS_PREFIX + userId + ":" + method;

        // Check if OTP exists
        String storedOtpHash = redisTemplate.opsForValue().get(otpKey);
        if (storedOtpHash == null) {
            throw new OtpExpiredException("OTP has expired or does not exist");
        }

        // Check attempts
        String attemptsStr = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : 0;

        if (attempts >= otpProperties.getMaxAttempts()) {
            // Delete OTP to prevent further attempts
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptsKey);
            throw new InvalidOtpException("Maximum OTP verification attempts exceeded");
        }

        // Verify OTP
        boolean isValid = passwordEncoder.matches(code, storedOtpHash);

        if (isValid) {
            // Delete OTP after successful verification
            redisTemplate.delete(otpKey);
            redisTemplate.delete(attemptsKey);

            // Update enrollment
            MfaEnrollment enrollment = mfaEnrollmentRepository.findByUserIdAndMethod(userId, method)
                    .orElseThrow(() -> new MfaNotEnrolledException("MFA enrollment not found"));

            if (!enrollment.getVerified()) {
                enrollment.setVerified(true);
                enrollment.setVerifiedAt(Instant.now());
            }
            enrollment.setLastUsedAt(Instant.now());
            mfaEnrollmentRepository.save(enrollment);

            // Publish event
            OtpVerifiedEvent event = OtpVerifiedEvent.create(userId, method, true, UUID.randomUUID().toString());
            kafkaTemplate.send("banking.otp.otp-verified", event);

            logger.info("OTP verified successfully for user {}", userId);
            return true;
        } else {
            // Increment attempts
            redisTemplate.opsForValue().increment(attemptsKey);
            logger.warn("Invalid OTP for user {}, attempts: {}", userId, attempts + 1);
            throw new InvalidOtpException("Invalid OTP code");
        }
    }

    @Override
    public boolean isRateLimited(UUID userId) {
        String rateLimitKey = RATE_LIMIT_PREFIX + userId;
        String countStr = redisTemplate.opsForValue().get(rateLimitKey);
        int count = countStr != null ? Integer.parseInt(countStr) : 0;
        return count >= otpProperties.getRateLimit().getPerUser();
    }

    private void incrementRateLimitCounter(UUID userId) {
        String rateLimitKey = RATE_LIMIT_PREFIX + userId;
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        if (count != null && count == 1) {
            // Set expiry on first increment
            redisTemplate.expire(rateLimitKey, otpProperties.getRateLimit().getWindowSeconds(), TimeUnit.SECONDS);
        }
    }

    private String generateOtp() {
        int length = otpProperties.getLength();
        int bound = (int) Math.pow(10, length);
        int otp = secureRandom.nextInt(bound);
        return String.format("%0" + length + "d", otp);
    }
}
