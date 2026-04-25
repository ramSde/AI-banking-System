package com.banking.audit.mapper;

import com.banking.audit.domain.AuditEvent;
import com.banking.audit.dto.AuditEventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "eventId", source = "eventId")
    @Mapping(target = "eventType", source = "eventType")
    @Mapping(target = "entityType", source = "entityType")
    @Mapping(target = "entityId", source = "entityId")
    @Mapping(target = "actorUserId", source = "actorUserId")
    @Mapping(target = "actorUsername", source = "actorUsername")
    @Mapping(target = "actorIp", source = "actorIp")
    @Mapping(target = "actorDeviceId", source = "actorDeviceId")
    @Mapping(target = "actorUserAgent", source = "actorUserAgent")
    @Mapping(target = "beforeState", source = "beforeState")
    @Mapping(target = "afterState", source = "afterState")
    @Mapping(target = "changes", source = "changes")
    @Mapping(target = "occurredAt", source = "occurredAt")
    @Mapping(target = "traceId", source = "traceId")
    @Mapping(target = "spanId", source = "spanId")
    @Mapping(target = "correlationId", source = "correlationId")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "serviceName", source = "serviceName")
    @Mapping(target = "action", source = "action")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "errorMessage", source = "errorMessage")
    @Mapping(target = "metadata", source = "metadata")
    @Mapping(target = "createdAt", source = "createdAt")
    AuditEventResponse toResponse(AuditEvent auditEvent);

    List<AuditEventResponse> toResponseList(List<AuditEvent> auditEvents);
}
