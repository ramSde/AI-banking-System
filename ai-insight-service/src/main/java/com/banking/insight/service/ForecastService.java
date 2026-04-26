package com.banking.insight.service;

import com.banking.insight.dto.ForecastResponse;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ForecastService {

    List<ForecastResponse> forecastSpending(UUID userId, Instant startDate, Instant endDate);

    ForecastResponse forecastCategorySpending(UUID userId, String category, Instant forecastDate);

    List<ForecastResponse> forecastMonthlySpending(UUID userId, int months);
}
