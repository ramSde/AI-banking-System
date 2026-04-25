package com.banking.audit.repository;

import com.banking.audit.domain.AuditEvent;
import com.banking.audit.domain.EntityType;
import com.banking.audit.domain.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    Optional<AuditEvent> findByEventId(String eventId);

    Page<AuditEvent> findByEntityTypeAndEntityId(
            EntityType entityType,
            String entityId,
            Pageable pageable
    );

    Page<AuditEvent> findByActorUserId(UUID actorUserId, Pageable pageable);

    Page<AuditEvent> findByEventType(EventType eventType, Pageable pageable);

    Page<AuditEvent> findByServiceName(String serviceName, Pageable pageable);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.entityType = :entityType AND ae.entityId = :entityId " +
           "AND ae.occurredAt BETWEEN :fromDate AND :toDate ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByEntityAndDateRange(
            @Param("entityType") EntityType entityType,
            @Param("entityId") String entityId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.actorUserId = :userId " +
           "AND ae.occurredAt BETWEEN :fromDate AND :toDate ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByUserAndDateRange(
            @Param("userId") UUID userId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.traceId = :traceId ORDER BY ae.occurredAt ASC")
    List<AuditEvent> findByTraceId(@Param("traceId") String traceId);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.correlationId = :correlationId ORDER BY ae.occurredAt ASC")
    List<AuditEvent> findByCorrelationId(@Param("correlationId") String correlationId);

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.occurredAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByDateRange(
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.serviceName = :serviceName " +
           "AND ae.eventType = :eventType AND ae.occurredAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByServiceAndEventTypeAndDateRange(
            @Param("serviceName") String serviceName,
            @Param("eventType") EventType eventType,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.action = :action " +
           "AND ae.occurredAt BETWEEN :fromDate AND :toDate ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByActionAndDateRange(
            @Param("action") String action,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.status = :status " +
           "AND ae.occurredAt BETWEEN :fromDate AND :toDate ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByStatusAndDateRange(
            @Param("status") String status,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT COUNT(ae) FROM AuditEvent ae WHERE ae.entityType = :entityType AND ae.entityId = :entityId")
    long countByEntityTypeAndEntityId(
            @Param("entityType") EntityType entityType,
            @Param("entityId") String entityId
    );

    @Query("SELECT COUNT(ae) FROM AuditEvent ae WHERE ae.actorUserId = :userId " +
           "AND ae.occurredAt BETWEEN :fromDate AND :toDate")
    long countByUserAndDateRange(
            @Param("userId") UUID userId,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.actorIp = :ipAddress " +
           "AND ae.occurredAt BETWEEN :fromDate AND :toDate ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByIpAddressAndDateRange(
            @Param("ipAddress") String ipAddress,
            @Param("fromDate") Instant fromDate,
            @Param("toDate") Instant toDate,
            Pageable pageable
    );

    @Query("SELECT ae FROM AuditEvent ae WHERE ae.actorDeviceId = :deviceId " +
           "ORDER BY ae.occurredAt DESC")
    Page<AuditEvent> findByDeviceId(@Param("deviceId") String deviceId, Pageable pageable);
}
