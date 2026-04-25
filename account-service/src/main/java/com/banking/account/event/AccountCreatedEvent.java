package com.banking.account.event;

import com.banking.account.domain.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Account Created Event
 * 
 * Published when a new account is created.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreatedEvent {

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
        private String iban;
        private AccountType accountType;
        private String currency;
        private BigDecimal initialBalance;
        private Instant openedAt;
    }

    public static AccountCreatedEvent create(UUID accountId, UUID userId, String accountNumber, 
                                            String iban, AccountType accountType, String currency, 
                                            BigDecimal initialBalance, Instant openedAt) {
        return AccountCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("AccountCreated")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(UUID.randomUUID().toString())
                .payload(Payload.builder()
                        .accountId(accountId)
                        .userId(userId)
                        .accountNumber(accountNumber)
                        .iban(iban)
                        .accountType(accountType)
                        .currency(currency)
                        .initialBalance(initialBalance)
                        .openedAt(openedAt)
                        .build())
                .build();
    }
}
