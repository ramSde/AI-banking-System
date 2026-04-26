package com.banking.insight.service;

import com.banking.insight.domain.Insight;
import com.banking.insight.dto.InsightRequest;
import com.banking.insight.dto.InsightSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InsightService {

    Insight generateInsights(InsightRequest request);

    Insight getInsightById(UUID insightId, UUID userId);

    Page<Insight> getUserInsights(UUID userId, Pageable pageable);

    List<Insight> getUnreadInsights(UUID userId);

    List<Insight> getActiveInsights(UUID userId);

    InsightSummaryResponse getInsightSummary(UUID userId);

    Insight markAsRead(UUID insightId, UUID userId);

    Insight dismissInsight(UUID insightId, UUID userId);

    void deleteInsight(UUID insightId, UUID userId);

    Page<Insight> getAllInsights(Pageable pageable);
}
