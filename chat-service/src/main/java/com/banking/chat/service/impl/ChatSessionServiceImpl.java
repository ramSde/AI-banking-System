package com.banking.chat.service.impl;

import com.banking.chat.domain.ChatSession;
import com.banking.chat.dto.CreateSessionRequest;
import com.banking.chat.dto.SessionResponse;
import com.banking.chat.dto.UpdateSessionRequest;
import com.banking.chat.event.ChatSessionCreatedEvent;
import com.banking.chat.exception.InvalidSessionException;
import com.banking.chat.exception.SessionNotFoundException;
import com.banking.chat.repository.ChatSessionRepository;
import com.banking.chat.service.ChatSessionService;
import com.banking.chat.service.KafkaProducerService;
import com.banking.chat.util.SessionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChatSessionServiceImpl implements ChatSessionService {

    private static final Logger logger = LoggerFactory.getLogger(ChatSessionServiceImpl.class);

    private final ChatSessionRepository sessionRepository;
    private final KafkaProducerService kafkaProducerService;
    private final SessionMapper sessionMapper;

    public ChatSessionServiceImpl(
            ChatSessionRepository sessionRepository,
            KafkaProducerService kafkaProducerService,
            SessionMapper sessionMapper) {
        this.sessionRepository = sessionRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public SessionResponse createSession(UUID userId, CreateSessionRequest request) {
        logger.info("Creating new chat session for user: {}", userId);

        ChatSession session = ChatSession.builder()
                .userId(userId)
                .title(request.title())
                .status(ChatSession.SessionStatus.ACTIVE)
                .messageCount(0)
                .totalTokens(0)
                .metadata(request.metadata())
                .lastActivityAt(Instant.now())
                .build();

        session = sessionRepository.save(session);
        logger.info("Created chat session: {} for user: {}", session.getId(), userId);

        ChatSessionCreatedEvent event = new ChatSessionCreatedEvent(
                session.getId(),
                userId,
                session.getTitle(),
                session.getMetadata()
        );
        kafkaProducerService.publishChatSessionCreatedEvent(event);

        return sessionMapper.toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID sessionId, UUID userId) {
        logger.debug("Fetching session: {} for user: {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        return sessionMapper.toResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getUserSessions(UUID userId, Pageable pageable) {
        logger.debug("Fetching sessions for user: {}", userId);
        return sessionRepository.findByUserIdAndNotDeleted(userId, pageable)
                .map(sessionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getUserSessionsByStatus(UUID userId, ChatSession.SessionStatus status, Pageable pageable) {
        logger.debug("Fetching sessions for user: {} with status: {}", userId, status);
        return sessionRepository.findByUserIdAndStatusAndNotDeleted(userId, status, pageable)
                .map(sessionMapper::toResponse);
    }

    @Override
    public SessionResponse updateSession(UUID sessionId, UUID userId, UpdateSessionRequest request) {
        logger.info("Updating session: {} for user: {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        if (request.title() != null) {
            session.setTitle(request.title());
        }

        if (request.status() != null) {
            session.setStatus(request.status());
        }

        if (request.metadata() != null) {
            session.setMetadata(request.metadata());
        }

        session = sessionRepository.save(session);
        logger.info("Updated session: {}", sessionId);

        return sessionMapper.toResponse(session);
    }

    @Override
    public void deleteSession(UUID sessionId, UUID userId) {
        logger.info("Deleting session: {} for user: {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        sessionRepository.softDelete(sessionId, Instant.now());
        logger.info("Deleted session: {}", sessionId);
    }

    @Override
    public void archiveSession(UUID sessionId, UUID userId) {
        logger.info("Archiving session: {} for user: {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndNotDeleted(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(sessionId));

        if (!session.getUserId().equals(userId)) {
            throw new InvalidSessionException("Session does not belong to user");
        }

        session.setStatus(ChatSession.SessionStatus.ARCHIVED);
        sessionRepository.save(session);
        logger.info("Archived session: {}", sessionId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> searchSessions(UUID userId, String searchTerm, Pageable pageable) {
        logger.debug("Searching sessions for user: {} with term: {}", userId, searchTerm);
        return sessionRepository.searchByUserIdAndTitle(userId, searchTerm, pageable)
                .map(sessionMapper::toResponse);
    }

    @Override
    public void cleanupInactiveSessions() {
        logger.info("Starting cleanup of inactive sessions");

        Instant threshold = Instant.now().minus(30, ChronoUnit.MINUTES);
        List<ChatSession> inactiveSessions = sessionRepository.findInactiveSessions(threshold);

        if (!inactiveSessions.isEmpty()) {
            List<UUID> sessionIds = inactiveSessions.stream()
                    .map(ChatSession::getId)
                    .toList();

            sessionRepository.markSessionsAsInactive(sessionIds, Instant.now());
            logger.info("Marked {} sessions as inactive", sessionIds.size());
        } else {
            logger.debug("No inactive sessions found");
        }
    }
}
