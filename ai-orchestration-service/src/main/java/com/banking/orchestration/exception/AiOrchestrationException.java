package com.banking.orchestration.exception;

public class AiOrchestrationException extends RuntimeException {

    private final String errorCode;

    public AiOrchestrationException(String message) {
        super(message);
        this.errorCode = "AI_ORCHESTRATION_ERROR";
    }

    public AiOrchestrationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public AiOrchestrationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_ORCHESTRATION_ERROR";
    }

    public AiOrchestrationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
