package com.banking.i18n.exception;

public class I18nException extends RuntimeException {
    
    public I18nException(String message) {
        super(message);
    }
    
    public I18nException(String message, Throwable cause) {
        super(message, cause);
    }
}
