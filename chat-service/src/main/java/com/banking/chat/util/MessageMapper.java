package com.banking.chat.util;

import com.banking.chat.domain.ChatMessage;
import com.banking.chat.dto.MessageResponse;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageResponse toResponse(ChatMessage message) {
        if (message == null) {
            return null;
        }

        return new MessageResponse(
                message.getId(),
                message.getSessionId(),
                message.getRole(),
                message.getContent(),
                message.getModelName(),
                message.getInputTokens(),
                message.getOutputTokens(),
                message.getTotalTokens(),
                message.getLatencyMs(),
                message.getSources(),
                message.getMetadata(),
                message.getParentMessageId(),
                message.getErrorMessage(),
                message.getIsError(),
                message.getCreatedAt(),
                message.getUpdatedAt()
        );
    }
}
