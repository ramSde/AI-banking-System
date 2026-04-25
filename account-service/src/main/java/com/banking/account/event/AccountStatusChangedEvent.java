package com.banking.account.event;

import com.banking.account.domain.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Account Status Changed Event
 * 
 * Published when account status changes (ACTIVE, INACTIVE, FROZEN, CLOSED).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountStatusChangedEvent {

    private String eventId;
    private String eventType;
    private String version;
    private Instant occurredAt;
    private String correlationId;
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private UUID accountId;
        private UUID userId;
        private String accountNumber;
        private AccountStatus previousStatus;
        private AccountStatus newStatus;
        private String reason;
        private UUID changedBy;
        private Instant changedAt;
    }

    public static AccountStatusChangedEvent create(UUID accountId, UUID userId, String accountNumber,
                                                   AccountStatus previousStatus, AccountStatus newStatus,
                                                   String reason, UUID changedBy) {
        return AccountStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AccountStatusChanged")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .payload(Payload.builder()
                        .accountId(accountId)
                        .userId(userId)
                        .accountNumber(accountNumber)
                        .previousStatus(previousStatus)
                        .newStatus(newStatus)
                        .reason(reason)
                        .changedBy(changedBy)
                        .changedAt(Instant.now())
                        .build())
                .build();
    }
}
