package com.banking.orchestration.exception;

import java.math.BigDecimal;

public class BudgetExceededException extends AiOrchestrationException {

    private final BigDecimal budgetLimit;
    private final BigDecimal currentSpent;

    public BudgetExceededException(String message, BigDecimal budgetLimit, BigDecimal currentSpent) {
        super(message, "BUDGET_EXCEEDED");
        this.budgetLimit = budgetLimit;
        this.currentSpent = currentSpent;
    }

    public BigDecimal getBudgetLimit() {
        return budgetLimit;
    }

    public BigDecimal getCurrentSpent() {
        return currentSpent;
    }
}
