package com.banking.chat.service;

import com.banking.chat.dto.ChatHistoryResponse;
import com.banking.chat.dto.MessageResponse;
import com.banking.chat.dto.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChatMessageService {

    MessageResponse sendMessage(UUID userId, SendMessageRequest request);

    MessageResponse getMessage(UUID messageId, UUID userId);

    Page<MessageResponse> getSessionMessages(UUID sessionId, UUID userId, Pageable pageable);

    ChatHistoryResponse getChatHistory(UUID sessionId, UUID userId, Integer limit);

    void deleteMessage(UUID messageId, UUID userId);

    void deleteSessionMessages(UUID sessionId, UUID userId);
}
