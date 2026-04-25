package com.banking.audit.dto;

import com.banking.audit.domain.EntityType;
import com.banking.audit.domain.EventType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditEventResponse {

    private UUID id;
    private String eventId;
    private EventType eventType;
    private EntityType entityType;
    private String entityId;
    private UUID actorUserId;
    private String actorUsername;
    private String actorIp;
    private String actorDeviceId;
    private String actorUserAgent;
    private Map<String, Object> beforeState;
    private Map<String, Object> afterState;
    private Map<String, Object> changes;
    private Instant occurredAt;
    private String traceId;
    private String spanId;
    private String correlationId;
    private String sessionId;
    private String serviceName;
    private String action;
    private String status;
    private String errorMessage;
    private Map<String, Object> metadata;
    private Instant createdAt;
}
