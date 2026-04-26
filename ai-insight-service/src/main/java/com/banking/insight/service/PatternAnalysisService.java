package com.banking.insight.service;

import com.banking.insight.domain.SpendingPattern;

import java.util.List;
import java.util.UUID;

public interface PatternAnalysisService {

    List<SpendingPattern> analyzeSpendingPatterns(UUID userId);

    List<SpendingPattern> getRecurringPatterns(UUID userId);

    List<SpendingPattern> getSeasonalPatterns(UUID userId);

    List<SpendingPattern> getPatternsByCategory(UUID userId, String category);

    SpendingPattern getPatternById(UUID patternId, UUID userId);
}
