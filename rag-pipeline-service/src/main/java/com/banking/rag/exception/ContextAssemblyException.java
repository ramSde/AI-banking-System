package com.banking.rag.exception;

public class ContextAssemblyException extends RagException {

    public ContextAssemblyException(String message) {
        super(message, "CONTEXT_ASSEMBLY_ERROR");
    }

    public ContextAssemblyException(String message, Throwable cause) {
        super(message, "CONTEXT_ASSEMBLY_ERROR", cause);
    }
}
