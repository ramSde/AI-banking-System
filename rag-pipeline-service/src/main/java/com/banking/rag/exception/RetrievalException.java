package com.banking.rag.exception;

public class RetrievalException extends RagException {

    public RetrievalException(String message) {
        super(message, "RETRIEVAL_ERROR");
    }

    public RetrievalException(String message, Throwable cause) {
        super(message, "RETRIEVAL_ERROR", cause);
    }
}
