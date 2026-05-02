package com.banking.i18n.exception;

public class TranslationNotFoundException extends I18nException {
    
    public TranslationNotFoundException(String message) {
        super(message);
    }
    
    public TranslationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
