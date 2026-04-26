package com.banking.rag.exception;

import java.util.UUID;

public class QueryNotFoundException extends RagException {

    public QueryNotFoundException(UUID queryId) {
        super("Query not found with ID: " + queryId, "QUERY_NOT_FOUND");
    }

    public QueryNotFoundException(String message) {
        super(message, "QUERY_NOT_FOUND");
    }
}
