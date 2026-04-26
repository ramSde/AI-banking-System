package com.banking.rag.exception;

public class VectorSearchException extends RagException {

    public VectorSearchException(String message) {
        super(message, "VECTOR_SEARCH_ERROR");
    }

    public VectorSearchException(String message, Throwable cause) {
        super(message, "VECTOR_SEARCH_ERROR", cause);
    }
}
