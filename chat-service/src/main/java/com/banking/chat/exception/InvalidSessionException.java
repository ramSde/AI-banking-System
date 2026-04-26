package com.banking.chat.exception;

public class InvalidSessionException extends ChatException {

    public InvalidSessionException(String message) {
        super("INVALID_SESSION", message);
    }

    public InvalidSessionException(String message, Throwable cause) {
        super("INVALID_SESSION", message, cause);
    }
}
