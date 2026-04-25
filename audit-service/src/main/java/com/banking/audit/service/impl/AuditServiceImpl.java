package com.banking.audit.service.impl;

import com.banking.audit.config.AuditProperties;
import com.banking.audit.domain.AuditEvent;
import com.banking.audit.domain.EntityType;
import com.banking.audit.domain.EventType;
import com.banking.audit.dto.AuditQueryRequest;
import com.banking.audit.exception.AuditException;
import com.banking.audit.repository.AuditEventRepository;
import com.banking.audit.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);
    private final AuditEventRepository auditEventRepository;
    private final AuditProperties auditProperties;

    public AuditServiceImpl(AuditEventRepository auditEventRepository, AuditProperties auditProperties) {
        this.auditEventRepository = auditEventRepository;
        this.auditProperties = auditProperties;
    }

    @Override
    @Transactional
    public AuditEvent saveAuditEvent(AuditEvent auditEvent) {
        logger.info("Saving audit event: eventId={}, eventType={}, entityType={}, entityId={}",
                auditEvent.getEventId(), auditEvent.getEventType(), auditEvent.getEntityType(), auditEvent.getEntityId());

        try {
            AuditEvent savedEvent = auditEventRepository.save(auditEvent);
            logger.info("Successfully saved audit event: id={}, eventId={}", savedEvent.getId(), savedEvent.getEventId());
            return savedEvent;
        } catch (Exception e) {
            logger.error("Error saving audit event: eventId={}, error={}", auditEvent.getEventId(), e.getMessage(), e);
            throw new AuditException("AUDIT_SAVE_ERROR", "Failed to save audit event", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuditEvent getAuditEventById(UUID id) {
        logger.debug("Fetching audit event by id: {}", id);
        return auditEventRepository.findById(id)
                .orElseThrow(() -> new AuditException("AUDIT_NOT_FOUND", "Audit event not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public AuditEvent getAuditEventByEventId(String eventId) {
        logger.debug("Fetching audit event by eventId: {}", eventId);
        return auditEventRepository.findByEventId(eventId)
                .orElseThrow(() -> new AuditException("AUDIT_NOT_FOUND", "Audit event not found with eventId: " + eventId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> queryAuditEvents(AuditQueryRequest request) {
        logger.info("Querying audit events with request: {}", request);

        validateQueryRequest(request);

        Pageable pageable = createPageable(request);

        if (request.getEntityType() != null && request.getEntityId() != null) {
            return auditEventRepository.findByEntityAndDateRange(
                    request.getEntityType(),
                    request.getEntityId(),
                    request.getFromDate(),
                    request.getToDate(),
                    pageable
            );
        } else if (request.getActorUserId() != null) {
            return auditEventRepository.findByUserAndDateRange(
                    request.getActorUserId(),
                    request.getFromDate(),
                    request.getToDate(),
                    pageable
            );
        } else if (request.getServiceName() != null && request.getEventType() != null) {
            return auditEventRepository.findByServiceAndEventTypeAndDateRange(
                    request.getServiceName(),
                    request.getEventType(),
                    request.getFromDate(),
                    request.getToDate(),
                    pageable
            );
        } else if (request.getAction() != null) {
            return auditEventRepository.findByActionAndDateRange(
                    request.getAction(),
                    request.getFromDate(),
                    request.getToDate(),
                    pageable
            );
        } else if (request.getStatus() != null) {
            return auditEventRepository.findByStatusAndDateRange(
                    request.getStatus(),
                    request.getFromDate(),
                    request.getToDate(),
                    pageable
            );
        } else if (request.getActorIp() != null) {
            return auditEventRepository.findByIpAddressAndDateRange(
                    request.getActorIp(),
                    request.getFromDate(),
                    request.getToDate(),
                    pageable
            );
        } else if (request.getActorDeviceId() != null) {
            return auditEventRepository.findByDeviceId(request.getActorDeviceId(), pageable);
        } else {
            return auditEventRepository.findByDateRange(
                    request.getFromDate(),
                    request.getToDate(),
                    pageable
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEventsByEntity(EntityType entityType, String entityId, int page, int size) {
        logger.debug("Fetching audit events by entity: entityType={}, entityId={}, page={}, size={}",
                entityType, entityId, page, size);

        validatePageSize(size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        return auditEventRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEventsByUser(UUID userId, int page, int size) {
        logger.debug("Fetching audit events by user: userId={}, page={}, size={}", userId, page, size);

        validatePageSize(size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        return auditEventRepository.findByActorUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEventsByEventType(EventType eventType, int page, int size) {
        logger.debug("Fetching audit events by event type: eventType={}, page={}, size={}", eventType, page, size);

        validatePageSize(size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        return auditEventRepository.findByEventType(eventType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditEvent> getAuditEventsByDateRange(Instant fromDate, Instant toDate, int page, int size) {
        logger.debug("Fetching audit events by date range: fromDate={}, toDate={}, page={}, size={}",
                fromDate, toDate, page, size);

        validatePageSize(size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));

        return auditEventRepository.findByDateRange(fromDate, toDate, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditEventsByTraceId(String traceId) {
        logger.debug("Fetching audit events by trace ID: traceId={}", traceId);
        return auditEventRepository.findByTraceId(traceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEvent> getAuditEventsByCorrelationId(String correlationId) {
        logger.debug("Fetching audit events by correlation ID: correlationId={}", correlationId);
        return auditEventRepository.findByCorrelationId(correlationId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAuditEventsByEntity(EntityType entityType, String entityId) {
        logger.debug("Counting audit events by entity: entityType={}, entityId={}", entityType, entityId);
        return auditEventRepository.countByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAuditEventsByUserAndDateRange(UUID userId, Instant fromDate, Instant toDate) {
        logger.debug("Counting audit events by user and date range: userId={}, fromDate={}, toDate={}",
                userId, fromDate, toDate);
        return auditEventRepository.countByUserAndDateRange(userId, fromDate, toDate);
    }

    private void validateQueryRequest(AuditQueryRequest request) {
        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new AuditException("INVALID_QUERY", "From date and to date are required");
        }

        if (request.getFromDate().isAfter(request.getToDate())) {
            throw new AuditException("INVALID_QUERY", "From date must be before to date");
        }

        validatePageSize(request.getSize());
    }

    private void validatePageSize(int size) {
        if (size > auditProperties.getQueryMaxResults()) {
            throw new AuditException("INVALID_PAGE_SIZE",
                    "Page size exceeds maximum allowed: " + auditProperties.getQueryMaxResults());
        }
    }

    private Pageable createPageable(AuditQueryRequest request) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction, request.getSortBy())
        );
    }
}
