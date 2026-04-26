package com.banking.chat.service;

import com.banking.chat.domain.ChatSession;
import com.banking.chat.dto.CreateSessionRequest;
import com.banking.chat.dto.SessionResponse;
import com.banking.chat.dto.UpdateSessionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChatSessionService {

    SessionResponse createSession(UUID userId, CreateSessionRequest request);

    SessionResponse getSession(UUID sessionId, UUID userId);

    Page<SessionResponse> getUserSessions(UUID userId, Pageable pageable);

    Page<SessionResponse> getUserSessionsByStatus(UUID userId, ChatSession.SessionStatus status, Pageable pageable);

    SessionResponse updateSession(UUID sessionId, UUID userId, UpdateSessionRequest request);

    void deleteSession(UUID sessionId, UUID userId);

    void archiveSession(UUID sessionId, UUID userId);

    Page<SessionResponse> searchSessions(UUID userId, String searchTerm, Pageable pageable);

    void cleanupInactiveSessions();
}
