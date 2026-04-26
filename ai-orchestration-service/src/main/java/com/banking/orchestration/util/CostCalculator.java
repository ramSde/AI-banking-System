package com.banking.orchestration.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CostCalculator {

    public BigDecimal calculateCost(BigDecimal inputPricePer1k, BigDecimal outputPricePer1k,
                                    Integer inputTokens, Integer outputTokens) {
        BigDecimal inputCost = inputPricePer1k
                .multiply(BigDecimal.valueOf(inputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        BigDecimal outputCost = outputPricePer1k
                .multiply(BigDecimal.valueOf(outputTokens))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);

        return inputCost.add(outputCost).setScale(6, RoundingMode.HALF_UP);
    }
}
