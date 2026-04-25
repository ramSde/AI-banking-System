package com.banking.audit.service;

import com.banking.audit.domain.AuditEvent;
import com.banking.audit.domain.EntityType;
import com.banking.audit.domain.EventType;
import com.banking.audit.dto.AuditQueryRequest;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditService {

    AuditEvent saveAuditEvent(AuditEvent auditEvent);

    AuditEvent getAuditEventById(UUID id);

    AuditEvent getAuditEventByEventId(String eventId);

    Page<AuditEvent> queryAuditEvents(AuditQueryRequest request);

    Page<AuditEvent> getAuditEventsByEntity(EntityType entityType, String entityId, int page, int size);

    Page<AuditEvent> getAuditEventsByUser(UUID userId, int page, int size);

    Page<AuditEvent> getAuditEventsByEventType(EventType eventType, int page, int size);

    Page<AuditEvent> getAuditEventsByDateRange(Instant fromDate, Instant toDate, int page, int size);

    List<AuditEvent> getAuditEventsByTraceId(String traceId);

    List<AuditEvent> getAuditEventsByCorrelationId(String correlationId);

    long countAuditEventsByEntity(EntityType entityType, String entityId);

    long countAuditEventsByUserAndDateRange(UUID userId, Instant fromDate, Instant toDate);
}
