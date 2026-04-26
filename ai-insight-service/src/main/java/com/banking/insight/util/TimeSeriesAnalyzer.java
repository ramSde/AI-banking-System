package com.banking.insight.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class TimeSeriesAnalyzer {

    public BigDecimal calculateMovingAverage(final List<BigDecimal> values, final int window) {
        if (values == null || values.isEmpty() || window <= 0 || window > values.size()) {
            return BigDecimal.ZERO;
        }

        final List<BigDecimal> lastValues = values.subList(Math.max(0, values.size() - window), values.size());
        final BigDecimal sum = lastValues.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(lastValues.size()), 2, RoundingMode.HALF_UP);
    }

    public List<BigDecimal> calculateExponentialMovingAverage(final List<BigDecimal> values, final double alpha) {
        if (values == null || values.isEmpty() || alpha <= 0 || alpha > 1) {
            return new ArrayList<>();
        }

        final List<BigDecimal> ema = new ArrayList<>();
        ema.add(values.get(0));

        for (int i = 1; i < values.size(); i++) {
            final BigDecimal current = values.get(i);
            final BigDecimal previous = ema.get(i - 1);
            final BigDecimal alphaDecimal = BigDecimal.valueOf(alpha);
            final BigDecimal oneMinusAlpha = BigDecimal.ONE.subtract(alphaDecimal);

            final BigDecimal newEma = current.multiply(alphaDecimal)
                .add(previous.multiply(oneMinusAlpha));

            ema.add(newEma.setScale(2, RoundingMode.HALF_UP));
        }

        return ema;
    }

    public String detectTrend(final List<BigDecimal> values) {
        if (values == null || values.size() < 2) {
            return "STABLE";
        }

        final BigDecimal first = values.get(0);
        final BigDecimal last = values.get(values.size() - 1);
        final BigDecimal change = last.subtract(first);
        final BigDecimal percentChange = change.divide(first, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        if (percentChange.compareTo(BigDecimal.valueOf(10)) > 0) {
            return "INCREASING";
        } else if (percentChange.compareTo(BigDecimal.valueOf(-10)) < 0) {
            return "DECREASING";
        } else {
            return "STABLE";
        }
    }

    public BigDecimal calculateGrowthRate(final List<BigDecimal> values) {
        if (values == null || values.size() < 2) {
            return BigDecimal.ZERO;
        }

        final BigDecimal first = values.get(0);
        final BigDecimal last = values.get(values.size() - 1);

        if (first.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return last.subtract(first)
            .divide(first, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }

    public boolean detectSeasonality(final List<BigDecimal> values, final int period) {
        if (values == null || values.size() < period * 2) {
            return false;
        }

        final List<BigDecimal> firstPeriod = values.subList(0, period);
        final List<BigDecimal> secondPeriod = values.subList(period, Math.min(period * 2, values.size()));

        final BigDecimal firstAvg = firstPeriod.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(firstPeriod.size()), 2, RoundingMode.HALF_UP);

        final BigDecimal secondAvg = secondPeriod.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(secondPeriod.size()), 2, RoundingMode.HALF_UP);

        final BigDecimal difference = firstAvg.subtract(secondAvg).abs();
        final BigDecimal threshold = firstAvg.multiply(BigDecimal.valueOf(0.2));

        return difference.compareTo(threshold) < 0;
    }
}
