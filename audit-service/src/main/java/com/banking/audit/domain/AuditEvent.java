package com.banking.audit.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 100)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_username")
    private String actorUsername;

    @Column(name = "actor_ip", length = 45)
    private String actorIp;

    @Column(name = "actor_device_id")
    private String actorDeviceId;

    @Column(name = "actor_user_agent", columnDefinition = "TEXT")
    private String actorUserAgent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_state", columnDefinition = "jsonb")
    private Map<String, Object> beforeState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_state", columnDefinition = "jsonb")
    private Map<String, Object> afterState;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changes", columnDefinition = "jsonb")
    private Map<String, Object> changes;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "span_id")
    private String spanId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "action", length = 100)
    private String action;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
