package com.banking.risk.service.impl;

import com.banking.risk.domain.*;
import com.banking.risk.dto.RiskAssessmentRequest;
import com.banking.risk.dto.RiskAssessmentResponse;
import com.banking.risk.dto.RiskHistoryResponse;
import com.banking.risk.event.HighRiskDetectedEvent;
import com.banking.risk.event.MfaRequiredEvent;
import com.banking.risk.event.RiskAssessmentCompletedEvent;
import com.banking.risk.exception.RiskAssessmentException;
import com.banking.risk.exception.RiskAssessmentNotFoundException;
import com.banking.risk.repository.RiskAssessmentRepository;
import com.banking.risk.repository.RiskHistoryRepository;
import com.banking.risk.service.RiskAssessmentService;
import com.banking.risk.util.RiskScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of RiskAssessmentService.
 * Handles risk assessment logic and publishes events.
 */
@Service
@Transactional
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentServiceImpl.class);

    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskHistoryRepository riskHistoryRepository;
    private final RiskScoreCalculator riskScoreCalculator;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.risk-assessment}")
    private String riskAssessmentTopic;

    @Value("${kafka.topics.high-risk}")
    private String highRiskTopic;

    @Value("${kafka.topics.mfa-required}")
    private String mfaRequiredTopic;

    public RiskAssessmentServiceImpl(
            RiskAssessmentRepository riskAssessmentRepository,
            RiskHistoryRepository riskHistoryRepository,
            RiskScoreCalculator riskScoreCalculator,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.riskHistoryRepository = riskHistoryRepository;
        this.riskScoreCalculator = riskScoreCalculator;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public RiskAssessmentResponse assessRisk(RiskAssessmentRequest request) {
        logger.info("Assessing risk for user: {}, session: {}", request.userId(), request.sessionId());

        try {
            // Calculate risk factors
            Map<String, Integer> factors = riskScoreCalculator.calculateRiskFactors(request);

            // Calculate total risk score
            int totalScore = riskScoreCalculator.calculateTotalScore(factors);

            // Determine risk level and action
            RiskLevel riskLevel = riskScoreCalculator.determineRiskLevel(totalScore);
            RiskAction riskAction = riskScoreCalculator.determineRiskAction(riskLevel);

            // Create risk assessment entity
            RiskAssessment assessment = RiskAssessment.builder()
                    .userId(request.userId())
                    .sessionId(request.sessionId())
                    .riskScore(totalScore)
                    .riskLevel(riskLevel)
                    .riskAction(riskAction)
                    .deviceFingerprint(request.deviceFingerprint())
                    .ipAddress(request.ipAddress())
                    .geolocation(request.geolocation() != null ? request.geolocation().toMap() : null)
                    .factors(factors)
                    .assessedAt(Instant.now())
                    .build();

            // Save assessment
            assessment = riskAssessmentRepository.save(assessment);

            // Create history record
            RiskHistory history = RiskHistory.builder()
                    .userId(request.userId())
                    .assessmentId(assessment.getId())
                    .riskScore(totalScore)
                    .riskLevel(riskLevel)
                    .actionTaken(riskAction)
                    .build();
            riskHistoryRepository.save(history);

            // Publish events
            publishRiskAssessmentCompletedEvent(assessment, factors);

            if (riskLevel == RiskLevel.HIGH) {
                publishHighRiskDetectedEvent(assessment);
            }

            if (riskAction == RiskAction.REQUIRE_MFA) {
                publishMfaRequiredEvent(assessment);
            }

            logger.info("Risk assessment completed: assessmentId={}, score={}, level={}, action={}",
                    assessment.getId(), totalScore, riskLevel, riskAction);

            // Build response
            return new RiskAssessmentResponse(
                    assessment.getId(),
                    totalScore,
                    riskLevel,
                    riskAction,
                    factors,
                    riskAction == RiskAction.REQUIRE_MFA,
                    assessment.getAssessedAt()
            );

        } catch (Exception e) {
            logger.error("Risk assessment failed for user: {}", request.userId(), e);
            throw new RiskAssessmentException("Failed to assess risk", e);
        }
    }

    @Override
    @Cacheable(value = "riskAssessments", key = "#assessmentId")
    public RiskAssessmentResponse getAssessmentById(UUID assessmentId) {
        logger.debug("Retrieving risk assessment: {}", assessmentId);

        RiskAssessment assessment = riskAssessmentRepository.findByIdAndNotDeleted(assessmentId)
                .orElseThrow(() -> new RiskAssessmentNotFoundException(assessmentId));

        return new RiskAssessmentResponse(
                assessment.getId(),
                assessment.getRiskScore(),
                assessment.getRiskLevel(),
                assessment.getRiskAction(),
                assessment.getFactors(),
                assessment.getRiskAction() == RiskAction.REQUIRE_MFA,
                assessment.getAssessedAt()
        );
    }

    @Override
    public Page<RiskHistoryResponse> getUserRiskHistory(UUID userId, Pageable pageable) {
        logger.debug("Retrieving risk history for user: {}", userId);

        return riskHistoryRepository.findByUserId(userId, pageable)
                .map(history -> new RiskHistoryResponse(
                        history.getId(),
                        history.getUserId(),
                        history.getAssessmentId(),
                        history.getRiskScore(),
                        history.getRiskLevel(),
                        history.getActionTaken(),
                        history.getCreatedAt()
                ));
    }

    @Override
    public Page<RiskHistoryResponse> getUserRiskHistoryByDateRange(
            UUID userId,
            Instant startDate,
            Instant endDate,
            Pageable pageable
    ) {
        logger.debug("Retrieving risk history for user: {} between {} and {}", userId, startDate, endDate);

        return riskHistoryRepository.findByUserIdAndDateRange(userId, startDate, endDate, pageable)
                .map(history -> new RiskHistoryResponse(
                        history.getId(),
                        history.getUserId(),
                        history.getAssessmentId(),
                        history.getRiskScore(),
                        history.getRiskLevel(),
                        history.getActionTaken(),
                        history.getCreatedAt()
                ));
    }

    /**
     * Publish risk assessment completed event.
     */
    private void publishRiskAssessmentCompletedEvent(RiskAssessment assessment, Map<String, Integer> factors) {
        RiskAssessmentCompletedEvent event = RiskAssessmentCompletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("RiskAssessmentCompleted")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(assessment.getSessionId().toString())
                .payload(RiskAssessmentCompletedEvent.Payload.builder()
                        .assessmentId(assessment.getId())
                        .userId(assessment.getUserId())
                        .sessionId(assessment.getSessionId())
                        .riskScore(assessment.getRiskScore())
                        .riskLevel(assessment.getRiskLevel())
                        .riskAction(assessment.getRiskAction())
                        .factors(factors)
                        .mfaRequired(assessment.getRiskAction() == RiskAction.REQUIRE_MFA)
                        .build())
                .build();

        kafkaTemplate.send(riskAssessmentTopic, event);
        logger.info("Published RiskAssessmentCompletedEvent for assessment: {}", assessment.getId());
    }

    /**
     * Publish high risk detected event.
     */
    private void publishHighRiskDetectedEvent(RiskAssessment assessment) {
        HighRiskDetectedEvent event = HighRiskDetectedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("HighRiskDetected")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(assessment.getSessionId().toString())
                .payload(HighRiskDetectedEvent.Payload.builder()
                        .assessmentId(assessment.getId())
                        .userId(assessment.getUserId())
                        .sessionId(assessment.getSessionId())
                        .riskScore(assessment.getRiskScore())
                        .ipAddress(assessment.getIpAddress())
                        .deviceFingerprint(assessment.getDeviceFingerprint())
                        .reason("High risk score detected")
                        .build())
                .build();

        kafkaTemplate.send(highRiskTopic, event);
        logger.warn("Published HighRiskDetectedEvent for assessment: {}", assessment.getId());
    }

    /**
     * Publish MFA required event.
     */
    private void publishMfaRequiredEvent(RiskAssessment assessment) {
        MfaRequiredEvent event = MfaRequiredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("MfaRequired")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(assessment.getSessionId().toString())
                .payload(MfaRequiredEvent.Payload.builder()
                        .assessmentId(assessment.getId())
                        .userId(assessment.getUserId())
                        .sessionId(assessment.getSessionId())
                        .riskScore(assessment.getRiskScore())
                        .reason("Elevated risk level requires MFA")
                        .build())
                .build();

        kafkaTemplate.send(mfaRequiredTopic, event);
        logger.info("Published MfaRequiredEvent for assessment: {}", assessment.getId());
    }
}
