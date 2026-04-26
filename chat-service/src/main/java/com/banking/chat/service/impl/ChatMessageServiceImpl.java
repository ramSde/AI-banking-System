package com.banking.chat.service.impl;

import com.banking.chat.config.ChatProperties;
import com.banking.chat.domain.ChatMessage;
import com.banking.chat.domain.ChatSession;
import com.banking.chat.dto.ChatHistoryResponse;
import com.banking.chat.dto.MessageResponse;
import com.banking.chat.dto.SendMessageRequest;
import com.banking.chat.dto.SessionResponse;
import com.banking.chat.event.ChatMessageSentEvent;
import com.banking.chat.exception.AiServiceException;
import com.banking.chat.exception.InvalidSessionException;
import com.banking.chat.exception.MessageNotFoundException;
import com.banking.chat.exception.SessionNotFoundException;
import com.banking.chat.repository.ChatMessageRepository;
import com.banking.chat.repository.ChatSessionRepository;
import com.banking.chat.service.ChatMessageService;
import com.banking.chat.service.KafkaProducerService;
import com.banking.chat.util.MessageMapper;
import com.banking.chat.util.SessionMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMessageServiceImpl.class);

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final KafkaProducerService kafkaProducerService;
    private final MessageMapper messageMapper;
    private final SessionMapper sessionMapper;
    private final ChatProperties chatProperties;
    private final RestClient aiOrchestrationClient;
    private final RestClient ragPipelineClient;

    public ChatMessageServiceImpl(
            ChatMessageRepository messageRepository,
            ChatSessionRepository sessionRepository,
            KafkaProducerService kafkaProducerService,
            MessageMapper messageMapper,
            SessionMapper sessionMapper,
            ChatProperties chatProperties,
            RestClient aiOrchestrationClient,
            RestClient ragPipelineClient) {
        this.messageRepository = messageRepository;
        this.sessionRepository = sessionRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.messageMapper = messageMapper;
        this.sessionMapper = sessionMapper;
        this.chatProperties = chatProperties;
        this.aiOrchestrationClient = aiOrchestrationClient;
        this.ragPipelineClient = ragPipelineClient;
    }

    @Override
    public MessageResponse sendMessage(UUID userId, SendMessageRequest request) {
        logger.info("Sending message for user: {} in session: {}", userId, request.sessionId());

        ChatSession session = sessionRepository.findByIdAndNotDeleted(request.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(request.sessionId()));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        if (!session.isActive()) {
            throw new InvalidSessionException("Session is not active");
        }

        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(request.sessionId())
                .role(ChatMessage.MessageRole.USER)
                .content(request.content())
                .metadata(request.metadata())
                .build();

        userMessage = messageRepository.save(userMessage);
        session.incrementMessageCount();

        ChatMessage assistantMessage;
        try {
            assistantMessage = generateAiResponse(session, userMessage, request.includeRagContext());
        } catch (Exception e) {
            logger.error("Failed to generate AI response", e);
            assistantMessage = createErrorMessage(session.getId(), "Failed to generate response. Please try again.");
        }

        assistantMessage = messageRepository.save(assistantMessage);
        session.incrementMessageCount();

        if (assistantMessage.getTotalTokens() != null) {
            session.addTokens(assistantMessage.getTotalTokens());
        }

        sessionRepository.save(session);

        ChatMessageSentEvent event = new ChatMessageSentEvent(
                assistantMessage.getId(),
                session.getId(),
                userId,
                assistantMessage.getRole(),
                assistantMessage.getContent(),
                assistantMessage.getTotalTokens()
        );
        kafkaProducerService.publishChatMessageSentEvent(event);

        return messageMapper.toResponse(assistantMessage);
    }

    @CircuitBreaker(name = "aiOrchestration", fallbackMethod = "generateAiResponseFallback")
    @Retry(name = "aiOrchestration")
    private ChatMessage generateAiResponse(ChatSession session, ChatMessage userMessage, Boolean includeRagContext) {
        long startTime = System.currentTimeMillis();

        List<ChatMessage> recentMessages = messageRepository.findRecentMessagesBySessionId(
                session.getId(),
                PageRequest.of(0, chatProperties.getMaxHistoryMessages())
        );

        Map<String, Object> aiRequest = new HashMap<>();
        aiRequest.put("sessionId", session.getId().toString());
        aiRequest.put("userId", session.getUserId().toString());
        aiRequest.put("message", userMessage.getContent());
        aiRequest.put("history", buildConversationHistory(recentMessages));
        aiRequest.put("includeRagContext", includeRagContext);

        Map<String, Object> aiResponse = aiOrchestrationClient
                .post()
                .uri("/v1/ai/chat")
                .body(aiRequest)
                .retrieve()
                .body(Map.class);

        if (aiResponse == null || !aiResponse.containsKey("data")) {
            throw new AiServiceException("Invalid AI service response");
        }

        Map<String, Object> data = (Map<String, Object>) aiResponse.get("data");
        long latency = System.currentTimeMillis() - startTime;

        return ChatMessage.builder()
                .sessionId(session.getId())
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content((String) data.get("content"))
                .modelName((String) data.get("modelName"))
                .inputTokens((Integer) data.get("inputTokens"))
                .outputTokens((Integer) data.get("outputTokens"))
                .totalTokens((Integer) data.get("totalTokens"))
                .latencyMs(latency)
                .sources((List<Map<String, Object>>) data.get("sources"))
                .parentMessageId(userMessage.getId())
                .build();
    }

    private ChatMessage generateAiResponseFallback(ChatSession session, ChatMessage userMessage, Boolean includeRagContext, Exception ex) {
        logger.error("AI service fallback triggered for session: {}", session.getId(), ex);
        return createErrorMessage(session.getId(), "AI service is temporarily unavailable. Please try again later.");
    }

    private ChatMessage createErrorMessage(UUID sessionId, String errorMessage) {
        return ChatMessage.builder()
                .sessionId(sessionId)
                .role(ChatMessage.MessageRole.ASSISTANT)
                .content("I apologize, but I'm having trouble processing your request right now.")
                .errorMessage(errorMessage)
                .isError(true)
                .build();
    }

    private List<Map<String, String>> buildConversationHistory(List<ChatMessage> messages) {
        return messages.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole().name().toLowerCase(),
                        "content", msg.getContent()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getMessage(UUID messageId, UUID userId) {
        logger.debug("Fetching message: {} for user: {}", messageId, userId);

        ChatMessage message = messageRepository.findByIdAndNotDeleted(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        ChatSession session = sessionRepository.findByIdAndNotDeleted(message.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(message.getSessionId()));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Message does not belong to user");
        }

        return messageMapper.toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getSessionMessages(UUID sessionId, UUID userId, Pageable pageable) {
        logger.debug("Fetching messages for session: {} and user: {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        return messageRepository.findBySessionIdAndNotDeletedPageable(sessionId, pageable)
                .map(messageMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatHistoryResponse getChatHistory(UUID sessionId, UUID userId, Integer limit) {
        logger.debug("Fetching chat history for session: {} and user: {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        int messageLimit = limit != null ? limit : chatProperties.getMaxHistoryMessages();
        Pageable pageable = PageRequest.of(0, messageLimit, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<ChatMessage> messages = messageRepository.findRecentMessagesBySessionId(sessionId, pageable);
        long totalMessages = messageRepository.countBySessionIdAndNotDeleted(sessionId);

        SessionResponse sessionResponse = sessionMapper.toResponse(session);
        List<MessageResponse> messageResponses = messages.stream()
                .map(messageMapper::toResponse)
                .toList();

        return new ChatHistoryResponse(
                sessionResponse,
                messageResponses,
                (int) totalMessages,
                totalMessages > messageLimit
        );
    }

    @Override
    public void deleteMessage(UUID messageId, UUID userId) {
        logger.info("Deleting message: {} for user: {}", messageId, userId);

        ChatMessage message = messageRepository.findByIdAndNotDeleted(messageId)
                .orElseThrow(() -> new MessageNotFoundException(messageId));

        ChatSession session = sessionRepository.findByIdAndNotDeleted(message.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(message.getSessionId()));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Message does not belong to user");
        }

        messageRepository.softDelete(messageId, Instant.now());
        logger.info("Deleted message: {}", messageId);
    }

    @Override
    public void deleteSessionMessages(UUID sessionId, UUID userId) {
        logger.info("Deleting all messages for session: {} and user: {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        messageRepository.softDeleteBySessionId(sessionId, Instant.now());
        logger.info("Deleted all messages for session: {}", sessionId);
    }
}
