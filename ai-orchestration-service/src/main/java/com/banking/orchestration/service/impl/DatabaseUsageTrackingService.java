package com.banking.orchestration.service.impl;

import com.banking.orchestration.domain.AiUsage;
import com.banking.orchestration.dto.UsageStatsResponse;
import com.banking.orchestration.repository.AiUsageRepository;
import com.banking.orchestration.service.UsageTrackingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatabaseUsageTrackingService implements UsageTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseUsageTrackingService.class);
    private final AiUsageRepository aiUsageRepository;

    public DatabaseUsageTrackingService(AiUsageRepository aiUsageRepository) {
        this.aiUsageRepository = aiUsageRepository;
    }

    @Override
    @Transactional
    public AiUsage recordUsage(AiUsage usage) {
        logger.info("Recording AI usage for user: {}, model: {}, tokens: {}", 
                usage.getUserId(), usage.getModelName(), usage.getTotalTokens());
        return aiUsageRepository.save(usage);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiUsage> getUserUsage(UUID userId, Pageable pageable) {
        return aiUsageRepository.findByUserIdAndNotDeleted(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiUsage> getUserUsageByDateRange(UUID userId, Instant startDate, Instant endDate, Pageable pageable) {
        return aiUsageRepository.findByUserIdAndDateRangeAndNotDeleted(userId, startDate, endDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public UsageStatsResponse getUserStats(UUID userId, Instant startDate, Instant endDate) {
        Page<AiUsage> usagePage = aiUsageRepository.findByUserIdAndDateRangeAndNotDeleted(
                userId, startDate, endDate, Pageable.unpaged());
        
        List<AiUsage> usageList = usagePage.getContent();

        int totalRequests = usageList.size();
        int successfulRequests = (int) usageList.stream().filter(AiUsage::getSuccess).count();
        int failedRequests = totalRequests - successfulRequests;

        long totalTokens = usageList.stream()
                .mapToLong(u -> u.getTotalTokens() != null ? u.getTotalTokens() : 0)
                .sum();
        
        long inputTokens = usageList.stream()
                .mapToLong(u -> u.getInputTokens() != null ? u.getInputTokens() : 0)
                .sum();
        
        long outputTokens = usageList.stream()
                .mapToLong(u -> u.getOutputTokens() != null ? u.getOutputTokens() : 0)
                .sum();

        BigDecimal totalCost = usageList.stream()
                .map(u -> u.getCostUsd() != null ? u.getCostUsd() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long averageLatencyMs = usageList.stream()
                .filter(u -> u.getLatencyMs() != null)
                .mapToLong(AiUsage::getLatencyMs)
                .average()
                .orElse(0.0)
                .longValue();

        Map<String, Integer> modelDistribution = usageList.stream()
                .collect(Collectors.groupingBy(
                        AiUsage::getModelName,
                        Collectors.summingInt(u -> 1)
                ));

        Map<String, Integer> featureDistribution = usageList.stream()
                .filter(u -> u.getFeature() != null)
                .collect(Collectors.groupingBy(
                        AiUsage::getFeature,
                        Collectors.summingInt(u -> 1)
                ));

        List<UsageStatsResponse.UsageEntry> recentUsage = usageList.stream()
                .limit(10)
                .map(u -> new UsageStatsResponse.UsageEntry(
                        u.getModelName(),
                        u.getProvider(),
                        u.getFeature(),
                        u.getTotalTokens(),
                        u.getCostUsd(),
                        u.getLatencyMs(),
                        u.getSuccess(),
                        u.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new UsageStatsResponse(
                totalRequests,
                successfulRequests,
                failedRequests,
                totalTokens,
                inputTokens,
                outputTokens,
                totalCost,
                averageLatencyMs,
                modelDistribution,
                featureDistribution,
                recentUsage,
                startDate,
                endDate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AiUsage> getAllUsage(Pageable pageable) {
        return aiUsageRepository.findAllNotDeleted(pageable);
    }
}
