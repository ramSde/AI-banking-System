package com.banking.account.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Account Updated Event
 * 
 * Published when account settings are updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUpdatedEvent {

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
        private BigDecimal overdraftLimit;
        private BigDecimal interestRate;
        private Instant updatedAt;
    }

    public static AccountUpdatedEvent create(UUID accountId, UUID userId, String accountNumber,
                                            BigDecimal overdraftLimit, BigDecimal interestRate) {
        return AccountUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AccountUpdated")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .payload(Payload.builder()
                        .accountId(accountId)
                        .userId(userId)
                        .accountNumber(accountNumber)
                        .overdraftLimit(overdraftLimit)
                        .interestRate(interestRate)
                        .updatedAt(Instant.now())
                        .build())
                .build();
    }
}
