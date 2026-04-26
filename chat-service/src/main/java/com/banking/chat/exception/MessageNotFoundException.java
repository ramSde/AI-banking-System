package com.banking.chat.exception;

import java.util.UUID;

public class MessageNotFoundException extends ChatException {

    public MessageNotFoundException(UUID messageId) {
        super("MESSAGE_NOT_FOUND", "Chat message not found: " + messageId);
    }

    public MessageNotFoundException(String message) {
        super("MESSAGE_NOT_FOUND", message);
    }
}
