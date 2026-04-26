package com.banking.orchestration.service.impl;

import com.banking.orchestration.domain.AiBudget;
import com.banking.orchestration.domain.AiModel;
import com.banking.orchestration.event.BudgetExceededEvent;
import com.banking.orchestration.exception.BudgetExceededException;
import com.banking.orchestration.repository.AiBudgetRepository;
import com.banking.orchestration.repository.AiModelRepository;
import com.banking.orchestration.service.CostControlService;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class BudgetEnforcementService implements CostControlService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetEnforcementService.class);
    private final AiBudgetRepository aiBudgetRepository;
    private final AiModelRepository aiModelRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;

    public BudgetEnforcementService(AiBudgetRepository aiBudgetRepository,
                                    AiModelRepository aiModelRepository,
                                    KafkaTemplate<String, Object> kafkaTemplate,
                                    Tracer tracer) {
        this.aiBudgetRepository = aiBudgetRepository;
        this.aiModelRepository = aiModelRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.tracer = tracer;
    }

    @Override
    @Transactional(readOnly = true)
    public void checkBudget(UUID userId, BigDecimal estimatedCost) {
        AiBudget budget = aiBudgetRepository.findByUserIdAndNotDeleted(userId)
                .orElseGet(() -> createDefaultBudget(userId));

        resetBudgetsIfNeeded(budget);

        if (budget.getDailySpentUsd().add(estimatedCost).compareTo(budget.getDailyBudgetUsd()) > 0) {
            String traceId = getTraceId();
            logger.warn("Daily budget exceeded for user: {}, limit: {}, spent: {}, attempted: {}",
                    userId, budget.getDailyBudgetUsd(), budget.getDailySpentUsd(), estimatedCost);
            
            publishBudgetExceededEvent(userId, "DAILY", budget.getDailyBudgetUsd(),
                    budget.getDailySpentUsd(), estimatedCost, traceId);
            
            throw new BudgetExceededException(
                    "Daily budget exceeded",
                    budget.getDailyBudgetUsd(),
                    budget.getDailySpentUsd()
            );
        }

        if (budget.getMonthlySpentUsd().add(estimatedCost).compareTo(budget.getMonthlyBudgetUsd()) > 0) {
            String traceId = getTraceId();
            logger.warn("Monthly budget exceeded for user: {}, limit: {}, spent: {}, attempted: {}",
                    userId, budget.getMonthlyBudgetUsd(), budget.getMonthlySpentUsd(), estimatedCost);
            
            publishBudgetExceededEvent(userId, "MONTHLY", budget.getMonthlyBudgetUsd(),
                    budget.getMonthlySpentUsd(), estimatedCost, traceId);
            
            throw new BudgetExceededException(
                    "Monthly budget exceeded",
                    budget.getMonthlyBudgetUsd(),
                    budget.getMonthlySpentUsd()
            );
        }

        BigDecimal dailyUsagePercentage = budget.getDailySpentUsd()
                .divide(budget.getDailyBudgetUsd(), 4, RoundingMode.HALF_UP);
        
        if (!budget.getAlertSent() && dailyUsagePercentage.compareTo(budget.getAlertThreshold()) >= 0) {
            logger.info("Budget alert threshold reached for user: {}, usage: {}%",
                    userId, dailyUsagePercentage.multiply(BigDecimal.valueOf(100)));
            budget.setAlertSent(true);
            aiBudgetRepository.save(budget);
        }
    }

    @Override
    @Transactional
    public void recordCost(UUID userId, BigDecimal actualCost) {
        Instant now = Instant.now();
        aiBudgetRepository.incrementDailySpent(userId, actualCost, now);
        aiBudgetRepository.incrementMonthlySpent(userId, actualCost, now);
        logger.debug("Recorded cost for user: {}, amount: ${}", userId, actualCost);
    }

    @Override
    public BigDecimal calculateCost(String modelName, Integer inputTokens, Integer outputTokens) {
        AiModel model = aiModelRepository.findByNameAndNotDeleted(modelName)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelName));

        BigDecimal inputCost = model.getInputPricePer1k()
                .multiply(BigDecimal.valueOf(inputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal outputCost = model.getOutputPricePer1k()
                .multiply(BigDecimal.valueOf(outputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        return inputCost.add(outputCost).setScale(6, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void resetDailyBudgets() {
        logger.info("Resetting daily budgets");
    }

    @Override
    @Transactional
    public void resetMonthlyBudgets() {
        logger.info("Resetting monthly budgets");
    }

    @Transactional
    private AiBudget createDefaultBudget(UUID userId) {
        Instant now = Instant.now();
        ZonedDateTime zonedNow = now.atZone(ZoneOffset.UTC);
        
        AiBudget budget = AiBudget.builder()
                .userId(userId)
                .dailyBudgetUsd(new BigDecimal("10.00"))
                .monthlyBudgetUsd(new BigDecimal("300.00"))
                .dailySpentUsd(BigDecimal.ZERO)
                .monthlySpentUsd(BigDecimal.ZERO)
                .dailyResetAt(zonedNow.plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant())
                .monthlyResetAt(zonedNow.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant())
                .alertThreshold(new BigDecimal("0.80"))
                .alertSent(false)
                .build();
        
        return aiBudgetRepository.save(budget);
    }

    private void resetBudgetsIfNeeded(AiBudget budget) {
        Instant now = Instant.now();
        boolean updated = false;
        
        if (now.isAfter(budget.getDailyResetAt())) {
            budget.setDailySpentUsd(BigDecimal.ZERO);
            budget.setAlertSent(false);
            budget.setDailyResetAt(now.atZone(ZoneOffset.UTC).plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant());
            updated = true;
        }
        
        if (now.isAfter(budget.getMonthlyResetAt())) {
            budget.setMonthlySpentUsd(BigDecimal.ZERO);
            budget.setMonthlyResetAt(now.atZone(ZoneOffset.UTC).plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant());
            updated = true;
        }
        
        if (updated) {
            aiBudgetRepository.save(budget);
        }
    }

    private void publishBudgetExceededEvent(UUID userId, String budgetType, BigDecimal budgetLimit,
                                            BigDecimal currentSpent, BigDecimal attemptedAmount, String traceId) {
        BudgetExceededEvent event = BudgetExceededEvent.create(
                userId, budgetType, budgetLimit, currentSpent, attemptedAmount, traceId
        );
        kafkaTemplate.send("banking.ai.budget-exceeded", userId.toString(), event);
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
