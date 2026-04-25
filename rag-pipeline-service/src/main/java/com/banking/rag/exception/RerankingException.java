package com.banking.rag.exception;

public class RerankingException extends RagException {

    public RerankingException(String message) {
        super(message, "RERANKING_ERROR");
    }

    public RerankingException(String message, Throwable cause) {
        super(message, "RERANKING_ERROR", cause);
    }
}
