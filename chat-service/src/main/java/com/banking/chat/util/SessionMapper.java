package com.banking.chat.util;

import com.banking.chat.domain.ChatSession;
import com.banking.chat.dto.SessionResponse;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionResponse toResponse(ChatSession session) {
        if (session == null) {
            return null;
        }

        return new SessionResponse(
                session.getId(),
                session.getUserId(),
                session.getTitle(),
                session.getStatus(),
                session.getMessageCount(),
                session.getTotalTokens(),
                session.getMetadata(),
                session.getLastActivityAt(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
