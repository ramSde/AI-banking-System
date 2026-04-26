package com.banking.insight.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class StatisticalCalculator {

    public BigDecimal calculateMean(final List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        final BigDecimal sum = values.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateStandardDeviation(final List<BigDecimal> values, final BigDecimal mean) {
        if (values == null || values.size() < 2) {
            return BigDecimal.ZERO;
        }

        final BigDecimal variance = values.stream()
            .map(value -> value.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(values.size() - 1), 4, RoundingMode.HALF_UP);

        return BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
    }

    public BigDecimal calculateZScore(final BigDecimal value, final BigDecimal mean, final BigDecimal stdDev) {
        if (stdDev.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return value.subtract(mean).divide(stdDev, 4, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateMedian(final List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }

        final List<BigDecimal> sorted = values.stream()
            .sorted()
            .toList();

        final int size = sorted.size();
        if (size % 2 == 0) {
            return sorted.get(size / 2 - 1)
                .add(sorted.get(size / 2))
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        } else {
            return sorted.get(size / 2);
        }
    }

    public BigDecimal calculatePercentile(final List<BigDecimal> values, final int percentile) {
        if (values == null || values.isEmpty() || percentile < 0 || percentile > 100) {
            return BigDecimal.ZERO;
        }

        final List<BigDecimal> sorted = values.stream()
            .sorted()
            .toList();

        final int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    public BigDecimal calculateIQR(final List<BigDecimal> values) {
        final BigDecimal q1 = calculatePercentile(values, 25);
        final BigDecimal q3 = calculatePercentile(values, 75);
        return q3.subtract(q1);
    }
}
