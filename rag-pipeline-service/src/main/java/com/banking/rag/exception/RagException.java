package com.banking.rag.exception;

public class RagException extends RuntimeException {

    private final String errorCode;

    public RagException(String message) {
        super(message);
        this.errorCode = "RAG_ERROR";
    }

    public RagException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RagException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "RAG_ERROR";
    }

    public RagException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
