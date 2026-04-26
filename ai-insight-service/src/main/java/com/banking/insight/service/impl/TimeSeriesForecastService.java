package com.banking.insight.service.impl;

import com.banking.insight.dto.ForecastResponse;
import com.banking.insight.service.DataAggregationService;
import com.banking.insight.service.ForecastService;
import com.banking.insight.util.TimeSeriesAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeSeriesForecastService implements ForecastService {

    private static final Logger logger = LoggerFactory.getLogger(TimeSeriesForecastService.class);

    private final DataAggregationService dataAggregationService;
    private final TimeSeriesAnalyzer timeSeriesAnalyzer;

    public TimeSeriesForecastService(
        final DataAggregationService dataAggregationService,
        final TimeSeriesAnalyzer timeSeriesAnalyzer
    ) {
        this.dataAggregationService = dataAggregationService;
        this.timeSeriesAnalyzer = timeSeriesAnalyzer;
    }

    @Override
    public List<ForecastResponse> forecastSpending(
        final UUID userId,
        final Instant startDate,
        final Instant endDate
    ) {
        logger.info("Forecasting spending for user: {} from {} to {}", userId, startDate, endDate);

        final Instant historicalStart = startDate.minus(90, ChronoUnit.DAYS);
        final List<Map<String, Object>> transactions = dataAggregationService.getUserTransactions(userId, historicalStart, startDate);

        final Map<String, BigDecimal> categorySpending = dataAggregationService.getCategorySpending(userId, historicalStart, startDate);

        final List<ForecastResponse> forecasts = new ArrayList<>();

        for (final Map.Entry<String, BigDecimal> entry : categorySpending.entrySet()) {
            final ForecastResponse forecast = forecastCategorySpending(userId, entry.getKey(), endDate);
            forecasts.add(forecast);
        }

        return forecasts;
    }

    @Override
    public ForecastResponse forecastCategorySpending(
        final UUID userId,
        final String category,
        final Instant forecastDate
    ) {
        logger.debug("Forecasting {} spending for user: {}", category, userId);

        final Instant historicalStart = forecastDate.minus(90, ChronoUnit.DAYS);
        final Map<String, BigDecimal> categorySpending = dataAggregationService.getCategorySpending(userId, historicalStart, forecastDate);

        final BigDecimal historicalAverage = categorySpending.getOrDefault(category, BigDecimal.ZERO);
        final BigDecimal predictedAmount = historicalAverage.multiply(new BigDecimal("1.05"));
        final BigDecimal confidenceInterval = predictedAmount.multiply(new BigDecimal("0.15"));

        return ForecastResponse.builder()
            .period("MONTHLY")
            .forecastDate(forecastDate)
            .predictedAmount(predictedAmount)
            .confidenceInterval(confidenceInterval)
            .lowerBound(predictedAmount.subtract(confidenceInterval))
            .upperBound(predictedAmount.add(confidenceInterval))
            .category(category)
            .metadata(new HashMap<>())
            .build();
    }

    @Override
    public List<ForecastResponse> forecastMonthlySpending(final UUID userId, final int months) {
        logger.info("Forecasting {} months of spending for user: {}", months, userId);

        final Instant now = Instant.now();
        final Instant historicalStart = now.minus(90, ChronoUnit.DAYS);

        final List<Map<String, Object>> transactions = dataAggregationService.getUserTransactions(userId, historicalStart, now);

        final List<BigDecimal> monthlyTotals = calculateMonthlyTotals(transactions);
        final BigDecimal averageMonthly = monthlyTotals.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(monthlyTotals.size()), 2, RoundingMode.HALF_UP);

        final List<ForecastResponse> forecasts = new ArrayList<>();

        for (int i = 1; i <= months; i++) {
            final Instant forecastDate = now.plus(i * 30L, ChronoUnit.DAYS);
            final BigDecimal predictedAmount = averageMonthly.multiply(new BigDecimal("1.02").pow(i));
            final BigDecimal confidenceInterval = predictedAmount.multiply(new BigDecimal("0.20"));

            forecasts.add(ForecastResponse.builder()
                .period("MONTHLY")
                .forecastDate(forecastDate)
                .predictedAmount(predictedAmount)
                .confidenceInterval(confidenceInterval)
                .lowerBound(predictedAmount.subtract(confidenceInterval))
                .upperBound(predictedAmount.add(confidenceInterval))
                .metadata(new HashMap<>())
                .build());
        }

        return forecasts;
    }

    private List<BigDecimal> calculateMonthlyTotals(final List<Map<String, Object>> transactions) {
        final Map<String, BigDecimal> monthlyMap = transactions.stream()
            .collect(Collectors.groupingBy(
                t -> Instant.parse(t.get("transactionDate").toString()).truncatedTo(ChronoUnit.DAYS).toString().substring(0, 7),
                Collectors.reducing(
                    BigDecimal.ZERO,
                    t -> new BigDecimal(t.get("amount").toString()),
                    BigDecimal::add
                )
            ));

        return new ArrayList<>(monthlyMap.values());
    }
}
