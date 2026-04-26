package com.banking.chat.exception;

import java.util.UUID;

public class SessionNotFoundException extends ChatException {

    public SessionNotFoundException(UUID sessionId) {
        super("SESSION_NOT_FOUND", "Chat session not found: " + sessionId);
    }

    public SessionNotFoundException(String message) {
        super("SESSION_NOT_FOUND", message);
    }
}
