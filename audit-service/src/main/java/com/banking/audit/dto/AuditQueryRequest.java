package com.banking.audit.dto;

import com.banking.audit.domain.EntityType;
import com.banking.audit.domain.EventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditQueryRequest {

    private EntityType entityType;
    
    private String entityId;
    
    private UUID actorUserId;
    
    private EventType eventType;
    
    private String serviceName;
    
    private String action;
    
    private String status;
    
    private String traceId;
    
    private String correlationId;
    
    private String actorIp;
    
    private String actorDeviceId;
    
    @NotNull(message = "From date is required")
    private Instant fromDate;
    
    @NotNull(message = "To date is required")
    private Instant toDate;
    
    @Min(value = 0, message = "Page must be greater than or equal to 0")
    @Builder.Default
    private int page = 0;
    
    @Min(value = 1, message = "Size must be greater than or equal to 1")
    @Max(value = 100, message = "Size must be less than or equal to 100")
    @Builder.Default
    private int size = 20;
    
    @Builder.Default
    private String sortBy = "occurredAt";
    
    @Builder.Default
    private String sortDirection = "DESC";
}
