package com.banking.orchestration.service.impl;

import com.banking.orchestration.domain.AiQuota;
import com.banking.orchestration.event.QuotaExceededEvent;
import com.banking.orchestration.exception.QuotaExceededException;
import com.banking.orchestration.repository.AiQuotaRepository;
import com.banking.orchestration.service.QuotaManagementService;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RedisQuotaManagementService implements QuotaManagementService {

    private static final Logger logger = LoggerFactory.getLogger(RedisQuotaManagementService.class);
    private static final String DAILY_QUOTA_KEY = "ai:quota:daily:";
    private static final String MONTHLY_QUOTA_KEY = "ai:quota:monthly:";

    private final AiQuotaRepository aiQuotaRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Tracer tracer;

    public RedisQuotaManagementService(AiQuotaRepository aiQuotaRepository,
                                       RedisTemplate<String, Object> redisTemplate,
                                       KafkaTemplate<String, Object> kafkaTemplate,
                                       Tracer tracer) {
        this.aiQuotaRepository = aiQuotaRepository;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.tracer = tracer;
    }

    @Override
    @Transactional(readOnly = true)
    public void checkQuota(UUID userId, Integer estimatedTokens) {
        AiQuota quota = aiQuotaRepository.findByUserIdAndNotDeleted(userId)
                .orElseGet(() -> createDefaultQuota(userId));

        resetQuotasIfNeeded(quota);

        Integer dailyUsed = getCurrentDailyUsage(userId);
        Integer monthlyUsed = getCurrentMonthlyUsage(userId);

        if (dailyUsed + estimatedTokens > quota.getDailyTokenLimit()) {
            String traceId = getTraceId();
            logger.warn("Daily quota exceeded for user: {}, limit: {}, used: {}, attempted: {}",
                    userId, quota.getDailyTokenLimit(), dailyUsed, estimatedTokens);
            
            publishQuotaExceededEvent(userId, "DAILY", quota.getDailyTokenLimit(), 
                    dailyUsed, estimatedTokens, quota.getUserTier(), traceId);
            
            throw new QuotaExceededException(
                    "Daily token quota exceeded",
                    quota.getDailyTokenLimit(),
                    dailyUsed
            );
        }

        if (monthlyUsed + estimatedTokens > quota.getMonthlyTokenLimit()) {
            String traceId = getTraceId();
            logger.warn("Monthly quota exceeded for user: {}, limit: {}, used: {}, attempted: {}",
                    userId, quota.getMonthlyTokenLimit(), monthlyUsed, estimatedTokens);
            
            publishQuotaExceededEvent(userId, "MONTHLY", quota.getMonthlyTokenLimit(),
                    monthlyUsed, estimatedTokens, quota.getUserTier(), traceId);
            
            throw new QuotaExceededException(
                    "Monthly token quota exceeded",
                    quota.getMonthlyTokenLimit(),
                    monthlyUsed
            );
        }
    }

    @Override
    public void recordTokenUsage(UUID userId, Integer actualTokens) {
        String dailyKey = DAILY_QUOTA_KEY + userId;
        String monthlyKey = MONTHLY_QUOTA_KEY + userId;

        redisTemplate.opsForValue().increment(dailyKey, actualTokens);
        redisTemplate.opsForValue().increment(monthlyKey, actualTokens);

        redisTemplate.expire(dailyKey, 25, TimeUnit.HOURS);
        redisTemplate.expire(monthlyKey, 32, TimeUnit.DAYS);

        aiQuotaRepository.incrementDailyTokens(userId, actualTokens, Instant.now());
        aiQuotaRepository.incrementMonthlyTokens(userId, actualTokens, Instant.now());

        logger.debug("Recorded token usage for user: {}, tokens: {}", userId, actualTokens);
    }

    @Override
    public void resetDailyQuotas() {
        logger.info("Resetting daily quotas");
        redisTemplate.keys(DAILY_QUOTA_KEY + "*").forEach(redisTemplate::delete);
    }

    @Override
    public void resetMonthlyQuotas() {
        logger.info("Resetting monthly quotas");
        redisTemplate.keys(MONTHLY_QUOTA_KEY + "*").forEach(redisTemplate::delete);
    }

    @Override
    public Integer getRemainingDailyTokens(UUID userId) {
        AiQuota quota = aiQuotaRepository.findByUserIdAndNotDeleted(userId)
                .orElseGet(() -> createDefaultQuota(userId));
        
        Integer used = getCurrentDailyUsage(userId);
        return Math.max(0, quota.getDailyTokenLimit() - used);
    }

    @Override
    public Integer getRemainingMonthlyTokens(UUID userId) {
        AiQuota quota = aiQuotaRepository.findByUserIdAndNotDeleted(userId)
                .orElseGet(() -> createDefaultQuota(userId));
        
        Integer used = getCurrentMonthlyUsage(userId);
        return Math.max(0, quota.getMonthlyTokenLimit() - used);
    }

    private Integer getCurrentDailyUsage(UUID userId) {
        String key = DAILY_QUOTA_KEY + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? ((Number) value).intValue() : 0;
    }

    private Integer getCurrentMonthlyUsage(UUID userId) {
        String key = MONTHLY_QUOTA_KEY + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? ((Number) value).intValue() : 0;
    }

    @Transactional
    private AiQuota createDefaultQuota(UUID userId) {
        Instant now = Instant.now();
        ZonedDateTime zonedNow = now.atZone(ZoneOffset.UTC);
        
        AiQuota quota = AiQuota.builder()
                .userId(userId)
                .userTier("FREE")
                .dailyTokenLimit(10000)
                .monthlyTokenLimit(300000)
                .dailyTokensUsed(0)
                .monthlyTokensUsed(0)
                .dailyResetAt(zonedNow.plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant())
                .monthlyResetAt(zonedNow.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant())
                .build();
        
        return aiQuotaRepository.save(quota);
    }

    private void resetQuotasIfNeeded(AiQuota quota) {
        Instant now = Instant.now();
        
        if (now.isAfter(quota.getDailyResetAt())) {
            String dailyKey = DAILY_QUOTA_KEY + quota.getUserId();
            redisTemplate.delete(dailyKey);
        }
        
        if (now.isAfter(quota.getMonthlyResetAt())) {
            String monthlyKey = MONTHLY_QUOTA_KEY + quota.getUserId();
            redisTemplate.delete(monthlyKey);
        }
    }

    private void publishQuotaExceededEvent(UUID userId, String quotaType, Integer quotaLimit,
                                           Integer currentUsage, Integer attemptedTokens,
                                           String userTier, String traceId) {
        QuotaExceededEvent event = QuotaExceededEvent.create(
                userId, quotaType, quotaLimit, currentUsage, attemptedTokens, userTier, traceId
        );
        kafkaTemplate.send("banking.ai.quota-exceeded", userId.toString(), event);
    }

    private String getTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return UUID.randomUUID().toString();
    }
}
