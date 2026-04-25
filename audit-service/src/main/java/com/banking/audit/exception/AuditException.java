package com.banking.audit.exception;

public class AuditException extends RuntimeException {

    private final String code;

    public AuditException(String code, String message) {
        super(message);
        this.code = code;
    }

    public AuditException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
