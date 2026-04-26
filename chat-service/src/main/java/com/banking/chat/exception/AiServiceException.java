package com.banking.chat.exception;

public class AiServiceException extends ChatException {

    public AiServiceException(String message) {
        super("AI_SERVICE_ERROR", message);
    }

    public AiServiceException(String message, Throwable cause) {
        super("AI_SERVICE_ERROR", message, cause);
    }
}
