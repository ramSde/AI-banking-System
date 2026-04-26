package com.banking.insight.mapper;

import com.banking.insight.domain.Anomaly;
import com.banking.insight.domain.Insight;
import com.banking.insight.domain.Recommendation;
import com.banking.insight.domain.SpendingPattern;
import com.banking.insight.dto.AnomalyResponse;
import com.banking.insight.dto.InsightResponse;
import com.banking.insight.dto.RecommendationResponse;
import com.banking.insight.dto.SpendingPatternResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InsightMapper {

    @Mapping(target = "insightType", expression = "java(insight.getInsightType().name())")
    @Mapping(target = "priority", expression = "java(insight.getPriority().name())")
    InsightResponse toResponse(Insight insight);

    @Mapping(target = "patternType", expression = "java(pattern.getPatternType().name())")
    @Mapping(target = "frequency", expression = "java(pattern.getFrequency().name())")
    @Mapping(target = "season", expression = "java(pattern.getSeason() != null ? pattern.getSeason().name() : null)")
    @Mapping(target = "trend", expression = "java(pattern.getTrend() != null ? pattern.getTrend().name() : null)")
    SpendingPatternResponse toPatternResponse(SpendingPattern pattern);

    @Mapping(target = "anomalyType", expression = "java(anomaly.getAnomalyType().name())")
    @Mapping(target = "severity", expression = "java(anomaly.getSeverity().name())")
    @Mapping(target = "detectionMethod", expression = "java(anomaly.getDetectionMethod().name())")
    AnomalyResponse toAnomalyResponse(Anomaly anomaly);

    @Mapping(target = "recommendationType", expression = "java(recommendation.getRecommendationType().name())")
    @Mapping(target = "priority", expression = "java(recommendation.getPriority().name())")
    @Mapping(target = "status", expression = "java(recommendation.getStatus().name())")
    RecommendationResponse toRecommendationResponse(Recommendation recommendation);
}
