package com.banking.orchestration.service;

import com.banking.orchestration.domain.AiUsage;
import com.banking.orchestration.dto.UsageStatsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface UsageTrackingService {

    AiUsage recordUsage(AiUsage usage);

    Page<AiUsage> getUserUsage(UUID userId, Pageable pageable);

    Page<AiUsage> getUserUsageByDateRange(UUID userId, Instant startDate, Instant endDate, Pageable pageable);

    UsageStatsResponse getUserStats(UUID userId, Instant startDate, Instant endDate);

    Page<AiUsage> getAllUsage(Pageable pageable);
}
