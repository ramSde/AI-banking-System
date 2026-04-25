package com.banking.account.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Account Closed Event
 * 
 * Published when an account is permanently closed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountClosedEvent {

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
        private BigDecimal finalBalance;
        private String closureReason;
        private UUID closedBy;
        private Instant closedAt;
    }

    public static AccountClosedEvent create(UUID accountId, UUID userId, String accountNumber,
                                           BigDecimal finalBalance, String closureReason, UUID closedBy) {
        return AccountClosedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AccountClosed")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .payload(Payload.builder()
                        .accountId(accountId)
                        .userId(userId)
                        .accountNumber(accountNumber)
                        .finalBalance(finalBalance)
                        .closureReason(closureReason)
                        .closedBy(closedBy)
                        .closedAt(Instant.now())
                        .build())
                .build();
    }
}
