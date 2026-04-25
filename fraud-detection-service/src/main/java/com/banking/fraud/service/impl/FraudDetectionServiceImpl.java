package com.banking.fraud.service.impl;

import com.banking.fraud.config.FraudProperties;
import com.banking.fraud.domain.*;
import com.banking.fraud.dto.FraudCheckRequest;
import com.banking.fraud.dto.RiskScoreResponse;
import com.banking.fraud.event.FraudAlertRaisedEvent;
import com.banking.fraud.event.FraudEventPublisher;
import com.banking.fraud.event.TransactionBlockedEvent;
import com.banking.fraud.repository.FraudAlertRepository;
import com.banking.fraud.repository.FraudCheckRepository;
import com.banking.fraud.repository.FraudRuleRepository;
import com.banking.fraud.service.FraudDetectionService;
import com.banking.fraud.util.RiskScoreCalculator;
import com.banking.fraud.util.VelocityChecker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Fraud Detection Service Implementation
 * 
 * Core fraud detection logic for analyzing transactions.
 */
@Service
@Transactional
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionServiceImpl.class);
    private static final String RISK_SCORE_CACHE_KEY = "fraud:risk-score:";

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudCheckRepository fraudCheckRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudEventPublisher fraudEventPublisher;
    private final RiskScoreCalculator riskScoreCalculator;
    private final VelocityChecker velocityChecker;
    private final FraudProperties fraudProperties;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public FraudDetectionServiceImpl(
            FraudRuleRepository fraudRuleRepository,
            FraudCheckRepository fraudCheckRepository,
            FraudAlertRepository fraudAlertRepository,
            FraudEventPublisher fraudEventPublisher,
            RiskScoreCalculator riskScoreCalculator,
            VelocityChecker velocityChecker,
            FraudProperties fraudProperties,
            RedisTemplate<String, Object> redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.fraudCheckRepository = fraudCheckRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.fraudEventPublisher = fraudEventPublisher;
        this.riskScoreCalculator = riskScoreCalculator;
        this.velocityChecker = velocityChecker;
        this.fraudProperties = fraudProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public RiskScoreResponse performFraudCheck(FraudCheckRequest request) {
        log.info("Performing fraud check for transaction: {}", request.transactionId());

        List<FraudRule> activeRules = fraudRuleRepository.findAllActive();
        List<FraudRule> triggeredRules = new ArrayList<>();

        long velocityCount = velocityChecker.checkVelocity(request.userId());

        for (FraudRule rule : activeRules) {
            if (evaluateRule(rule, request, velocityCount)) {
                triggeredRules.add(rule);
                log.debug("Rule triggered: {} for transaction {}", rule.getRuleName(), request.transactionId());
            }
        }

        int riskScore = riskScoreCalculator.calculateRiskScore(triggeredRules);
        RiskLevel riskLevel = riskScoreCalculator.determineRiskLevel(riskScore);
        boolean shouldBlock = riskScoreCalculator.shouldAutoBlock(riskScore);

        FraudCheck fraudCheck = createFraudCheck(request, riskScore, riskLevel, shouldBlock, triggeredRules, velocityCount);
        FraudCheck savedCheck = fraudCheckRepository.save(fraudCheck);

        if (riskLevel == RiskLevel.HIGH || shouldBlock) {
            createFraudAlert(savedCheck, triggeredRules);
        }

        if (shouldBlock) {
            publishTransactionBlockedEvent(savedCheck, triggeredRules);
        }

        RiskScoreResponse response = buildRiskScoreResponse(savedCheck, triggeredRules);
        cacheRiskScore(request.transactionId(), response);

        log.info("Fraud check completed: transactionId={}, riskScore={}, riskLevel={}, blocked={}",
                request.transactionId(), riskScore, riskLevel, shouldBlock);

        return response;
    }

    @Override
    public void processTransactionEvent(Map<String, Object> transactionEvent) {
        try {
            UUID transactionId = UUID.fromString(transactionEvent.get("transactionId").toString());
            UUID userId = UUID.fromString(transactionEvent.get("userId").toString());
            BigDecimal amount = new BigDecimal(transactionEvent.get("amount").toString());
            String transactionType = transactionEvent.get("transactionType").toString();

            log.info("Processing transaction event: transactionId={}, userId={}", transactionId, userId);

            FraudCheckRequest request = new FraudCheckRequest(
                    transactionId,
                    userId,
                    amount,
                    transactionType,
                    transactionEvent
            );

            performFraudCheck(request);
        } catch (Exception e) {
            log.error("Failed to process transaction event: {}", e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "riskScores", key = "#transactionId")
    public RiskScoreResponse getCachedRiskScore(UUID transactionId) {
        String cacheKey = RISK_SCORE_CACHE_KEY + transactionId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            return objectMapper.convertValue(cached, RiskScoreResponse.class);
        }
        
        return null;
    }

    private boolean evaluateRule(FraudRule rule, FraudCheckRequest request, long velocityCount) {
        return switch (rule.getRuleType()) {
            case VELOCITY -> evaluateVelocityRule(rule, velocityCount);
            case AMOUNT -> evaluateAmountRule(rule, request.amount());
            case GEOGRAPHIC -> evaluateGeographicRule(rule, request.metadata());
            case TIME_PATTERN -> evaluateTimePatternRule(rule);
            case ACCOUNT_AGE -> evaluateAccountAgeRule(rule, request.metadata());
            case FAILED_ATTEMPTS -> evaluateFailedAttemptsRule(rule, request.userId());
            default -> false;
        };
    }

    private boolean evaluateVelocityRule(FraudRule rule, long velocityCount) {
        Map<String, Object> config = rule.getRuleConfig();
        int maxTransactions = ((Number) config.getOrDefault("maxTransactions", 10)).intValue();
        return velocityCount >= maxTransactions;
    }

    private boolean evaluateAmountRule(FraudRule rule, BigDecimal amount) {
        Map<String, Object> config = rule.getRuleConfig();
        BigDecimal threshold = new BigDecimal(config.getOrDefault("threshold", "10000.00").toString());
        return amount.compareTo(threshold) >= 0;
    }

    private boolean evaluateGeographicRule(FraudRule rule, Map<String, Object> metadata) {
        if (metadata == null || !metadata.containsKey("location")) {
            return false;
        }
        return false;
    }

    private boolean evaluateTimePatternRule(FraudRule rule) {
        Map<String, Object> config = rule.getRuleConfig();
        int unusualHoursStart = ((Number) config.getOrDefault("unusualHoursStart", 2)).intValue();
        int unusualHoursEnd = ((Number) config.getOrDefault("unusualHoursEnd", 5)).intValue();
        
        int currentHour = java.time.LocalTime.now().getHour();
        return currentHour >= unusualHoursStart && currentHour <= unusualHoursEnd;
    }

    private boolean evaluateAccountAgeRule(FraudRule rule, Map<String, Object> metadata) {
        if (metadata == null || !metadata.containsKey("accountCreatedAt")) {
            return false;
        }
        return false;
    }

    private boolean evaluateFailedAttemptsRule(FraudRule rule, UUID userId) {
        return false;
    }

    private FraudCheck createFraudCheck(
            FraudCheckRequest request,
            int riskScore,
            RiskLevel riskLevel,
            boolean blocked,
            List<FraudRule> triggeredRules,
            long velocityCount
    ) {
        Map<String, Object> rulesTriggered = riskScoreCalculator.buildTriggeredRulesMap(triggeredRules);
        Map<String, Object> checkDetails = riskScoreCalculator.buildCheckDetails(
                request.amount(),
                request.transactionType(),
                velocityCount,
                request.metadata()
        );

        return FraudCheck.builder()
                .transactionId(request.transactionId())
                .userId(request.userId())
                .riskScore(riskScore)
                .riskLevel(riskLevel)
                .blocked(blocked)
                .rulesTriggered(rulesTriggered)
                .checkDetails(checkDetails)
                .build();
    }

    private void createFraudAlert(FraudCheck fraudCheck, List<FraudRule> triggeredRules) {
        String alertType = fraudCheck.getBlocked() ? "TRANSACTION_BLOCKED" : "HIGH_RISK_DETECTED";
        String severity = fraudCheck.getRiskLevel() == RiskLevel.HIGH ? "HIGH" : "MEDIUM";
        String description = buildAlertDescription(fraudCheck, triggeredRules);

        FraudAlert alert = FraudAlert.builder()
                .fraudCheckId(fraudCheck.getId())
                .transactionId(fraudCheck.getTransactionId())
                .userId(fraudCheck.getUserId())
                .alertType(alertType)
                .severity(severity)
                .status(AlertStatus.OPEN)
                .description(description)
                .build();

        FraudAlert savedAlert = fraudAlertRepository.save(alert);

        FraudAlertRaisedEvent event = FraudAlertRaisedEvent.create(
                savedAlert.getId(),
                fraudCheck.getId(),
                fraudCheck.getTransactionId(),
                fraudCheck.getUserId(),
                alertType,
                severity,
                fraudCheck.getRiskScore(),
                description
        );

        fraudEventPublisher.publishAlertRaised(event);
        log.info("Fraud alert created: alertId={}, transactionId={}", savedAlert.getId(), fraudCheck.getTransactionId());
    }

    private void publishTransactionBlockedEvent(FraudCheck fraudCheck, List<FraudRule> triggeredRules) {
        List<String> ruleNames = triggeredRules.stream()
                .map(FraudRule::getRuleName)
                .collect(Collectors.toList());

        TransactionBlockedEvent event = TransactionBlockedEvent.create(
                fraudCheck.getTransactionId(),
                fraudCheck.getUserId(),
                fraudCheck.getId(),
                fraudCheck.getRiskScore(),
                fraudCheck.getRiskLevel().toString(),
                ruleNames,
                "Transaction blocked due to high fraud risk score: " + fraudCheck.getRiskScore()
        );

        fraudEventPublisher.publishTransactionBlocked(event);
        log.warn("Transaction blocked: transactionId={}, riskScore={}", 
                fraudCheck.getTransactionId(), fraudCheck.getRiskScore());
    }

    private RiskScoreResponse buildRiskScoreResponse(FraudCheck fraudCheck, List<FraudRule> triggeredRules) {
        List<String> ruleNames = triggeredRules.stream()
                .map(FraudRule::getRuleName)
                .collect(Collectors.toList());

        String recommendation = riskScoreCalculator.generateRecommendation(
                fraudCheck.getRiskLevel(),
                fraudCheck.getBlocked()
        );

        return new RiskScoreResponse(
                fraudCheck.getTransactionId(),
                fraudCheck.getUserId(),
                fraudCheck.getRiskScore(),
                fraudCheck.getRiskLevel(),
                fraudCheck.getBlocked(),
                ruleNames,
                recommendation
        );
    }

    private String buildAlertDescription(FraudCheck fraudCheck, List<FraudRule> triggeredRules) {
        StringBuilder description = new StringBuilder();
        description.append("Fraud alert raised for transaction ")
                .append(fraudCheck.getTransactionId())
                .append(". Risk score: ")
                .append(fraudCheck.getRiskScore())
                .append(". Triggered rules: ");

        String ruleNames = triggeredRules.stream()
                .map(FraudRule::getRuleName)
                .collect(Collectors.joining(", "));

        description.append(ruleNames);
        return description.toString();
    }

    private void cacheRiskScore(UUID transactionId, RiskScoreResponse response) {
        String cacheKey = RISK_SCORE_CACHE_KEY + transactionId;
        redisTemplate.opsForValue().set(cacheKey, response, Duration.ofHours(24));
        log.debug("Cached risk score for transaction: {}", transactionId);
    }
}
