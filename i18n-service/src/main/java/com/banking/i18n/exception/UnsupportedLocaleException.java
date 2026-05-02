package com.banking.i18n.exception;

public class UnsupportedLocaleException extends I18nException {
    
    public UnsupportedLocaleException(String message) {
        super(message);
    }
    
    public UnsupportedLocaleException(String message, Throwable cause) {
        super(message, cause);
    }
}
