package com.banking.orchestration.exception;

public class ModelUnavailableException extends AiOrchestrationException {

    private final String modelName;

    public ModelUnavailableException(String message, String modelName) {
        super(message, "MODEL_UNAVAILABLE");
        this.modelName = modelName;
    }

    public ModelUnavailableException(String message, String modelName, Throwable cause) {
        super(message, "MODEL_UNAVAILABLE", cause);
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }
}
