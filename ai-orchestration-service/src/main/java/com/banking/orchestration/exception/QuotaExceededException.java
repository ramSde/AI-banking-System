package com.banking.orchestration.exception;

public class QuotaExceededException extends AiOrchestrationException {

    private final Integer quotaLimit;
    private final Integer currentUsage;

    public QuotaExceededException(String message, Integer quotaLimit, Integer currentUsage) {
        super(message, "QUOTA_EXCEEDED");
        this.quotaLimit = quotaLimit;
        this.currentUsage = currentUsage;
    }

    public Integer getQuotaLimit() {
        return quotaLimit;
    }

    public Integer getCurrentUsage() {
        return currentUsage;
    }
}
