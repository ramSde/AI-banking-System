package com.banking.risk.util;

import com.banking.risk.config.RiskProperties;
import com.banking.risk.domain.RiskAction;
import com.banking.risk.domain.RiskLevel;
import com.banking.risk.dto.RiskAssessmentRequest;
import com.banking.risk.repository.RiskAssessmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class for calculating risk scores based on multiple factors.
 * Implements the multi-factor risk scoring algorithm.
 */
@Component
public class RiskScoreCalculator {

    private static final Logger logger = LoggerFactory.getLogger(RiskScoreCalculator.class);

    private final RiskProperties riskProperties;
    private final RiskAssessmentRepository riskAssessmentRepository;

    public RiskScoreCalculator(
            RiskProperties riskProperties,
            RiskAssessmentRepository riskAssessmentRepository
    ) {
        this.riskProperties = riskProperties;
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    /**
     * Calculate risk score based on multiple factors.
     *
     * @param request Risk assessment request
     * @return Map of factor names to their scores
     */
    public Map<String, Integer> calculateRiskFactors(RiskAssessmentRequest request) {
        Map<String, Integer> factors = new HashMap<>();

        // Factor 1: New Device (25% weight)
        int newDeviceScore = calculateNewDeviceScore(request.userId(), request.deviceFingerprint());
        factors.put("newDevice", newDeviceScore);

        // Factor 2: New Location (20% weight)
        int newLocationScore = calculateNewLocationScore(request.userId(), request.geolocation());
        factors.put("newLocation", newLocationScore);

        // Factor 3: Velocity (15% weight)
        int velocityScore = calculateVelocityScore(request.userId());
        factors.put("velocity", velocityScore);

        // Factor 4: Time of Day (10% weight)
        int timeOfDayScore = calculateTimeOfDayScore();
        factors.put("timeOfDay", timeOfDayScore);

        // Factor 5: Failed Attempts (30% weight)
        int failedAttemptsScore = calculateFailedAttemptsScore(request.userId());
        factors.put("failedAttempts", failedAttemptsScore);

        logger.debug("Calculated risk factors for user {}: {}", request.userId(), factors);

        return factors;
    }

    /**
     * Calculate total risk score from individual factors.
     *
     * @param factors Map of factor scores
     * @return Total risk score (0-100)
     */
    public int calculateTotalScore(Map<String, Integer> factors) {
        int newDevice = factors.getOrDefault("newDevice", 0);
        int newLocation = factors.getOrDefault("newLocation", 0);
        int velocity = factors.getOrDefault("velocity", 0);
        int timeOfDay = factors.getOrDefault("timeOfDay", 0);
        int failedAttempts = factors.getOrDefault("failedAttempts", 0);

        // Apply weights
        double weightedScore = 
                (newDevice * riskProperties.getWeights().getNewDevice() / 100.0) +
                (newLocation * riskProperties.getWeights().getNewLocation() / 100.0) +
                (velocity * riskProperties.getWeights().getVelocity() / 100.0) +
                (timeOfDay * riskProperties.getWeights().getTimeOfDay() / 100.0) +
                (failedAttempts * riskProperties.getWeights().getFailedAttempts() / 100.0);

        int totalScore = (int) Math.round(weightedScore);

        // Ensure score is within bounds
        totalScore = Math.max(0, Math.min(100, totalScore));

        logger.debug("Calculated total risk score: {}", totalScore);

        return totalScore;
    }

    /**
     * Determine risk level from score.
     *
     * @param score Risk score (0-100)
     * @return Risk level
     */
    public RiskLevel determineRiskLevel(int score) {
        if (score <= riskProperties.getThresholds().getLow()) {
            return RiskLevel.LOW;
        } else if (score <= riskProperties.getThresholds().getMedium()) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.HIGH;
        }
    }

    /**
     * Determine risk action from risk level.
     *
     * @param riskLevel Risk level
     * @return Recommended action
     */
    public RiskAction determineRiskAction(RiskLevel riskLevel) {
        return switch (riskLevel) {
            case LOW -> RiskAction.ALLOW;
            case MEDIUM -> RiskAction.REQUIRE_MFA;
            case HIGH -> RiskAction.BLOCK;
        };
    }

    /**
     * Calculate new device score.
     * Returns high score if device fingerprint hasn't been seen in last 30 days.
     */
    private int calculateNewDeviceScore(UUID userId, String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.isBlank()) {
            return riskProperties.getWeights().getNewDevice();
        }

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        var recentAssessments = riskAssessmentRepository.findRecentByUserId(userId, thirtyDaysAgo);

        boolean deviceSeen = recentAssessments.stream()
                .anyMatch(assessment -> deviceFingerprint.equals(assessment.getDeviceFingerprint()));

        return deviceSeen ? 0 : riskProperties.getWeights().getNewDevice();
    }

    /**
     * Calculate new location score.
     * Returns high score if location is significantly different from recent logins.
     */
    private int calculateNewLocationScore(UUID userId, RiskAssessmentRequest.GeolocationData geolocation) {
        if (geolocation == null || geolocation.country() == null) {
            return 0;
        }

        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        var recentAssessments = riskAssessmentRepository.findRecentByUserId(userId, sevenDaysAgo);

        if (recentAssessments.isEmpty()) {
            return 0; // First login, no baseline
        }

        // Check if country matches any recent assessment
        boolean countryMatches = recentAssessments.stream()
                .anyMatch(assessment -> {
                    if (assessment.getGeolocation() == null) return false;
                    String country = (String) assessment.getGeolocation().get("country");
                    return geolocation.country().equals(country);
                });

        return countryMatches ? 0 : riskProperties.getWeights().getNewLocation();
    }

    /**
     * Calculate velocity score.
     * Returns high score if multiple login attempts in short time period.
     */
    private int calculateVelocityScore(UUID userId) {
        Instant fifteenMinutesAgo = Instant.now().minus(15, ChronoUnit.MINUTES);
        var recentAssessments = riskAssessmentRepository.findRecentByUserId(userId, fifteenMinutesAgo);

        int attemptCount = recentAssessments.size();

        if (attemptCount >= 5) {
            return riskProperties.getWeights().getVelocity();
        } else if (attemptCount >= 3) {
            return riskProperties.getWeights().getVelocity() / 2;
        }

        return 0;
    }

    /**
     * Calculate time of day score.
     * Returns high score if login is at unusual hours (2 AM - 6 AM).
     */
    private int calculateTimeOfDayScore() {
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        int hour = now.getHour();

        // Unusual hours: 2 AM - 6 AM UTC
        if (hour >= 2 && hour < 6) {
            return riskProperties.getWeights().getTimeOfDay();
        }

        return 0;
    }

    /**
     * Calculate failed attempts score.
     * Returns high score if there are recent high-risk assessments.
     */
    private int calculateFailedAttemptsScore(UUID userId) {
        Instant thirtyMinutesAgo = Instant.now().minus(30, ChronoUnit.MINUTES);
        long highRiskCount = riskAssessmentRepository.countHighRiskByUserIdSince(userId, thirtyMinutesAgo);

        if (highRiskCount >= 3) {
            return riskProperties.getWeights().getFailedAttempts();
        } else if (highRiskCount >= 1) {
            return riskProperties.getWeights().getFailedAttempts() / 2;
        }

        return 0;
    }
}
