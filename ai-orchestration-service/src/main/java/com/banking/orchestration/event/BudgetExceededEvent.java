package com.banking.orchestration.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetExceededEvent {

    private String eventId;
    private String eventType;
    private String version;
    private Instant occurredAt;
    private String correlationId;
    private Payload payload;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private UUID userId;
        private String budgetType;
        private BigDecimal budgetLimit;
        private BigDecimal currentSpent;
        private BigDecimal attemptedAmount;
        private String traceId;
    }

    public static BudgetExceededEvent create(UUID userId, String budgetType, BigDecimal budgetLimit,
                                             BigDecimal currentSpent, BigDecimal attemptedAmount, String traceId) {
        return BudgetExceededEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("BudgetExceeded")
                .version("1.0")
                .occurredAt(Instant.now())
                .correlationId(traceId)
                .payload(Payload.builder()
                        .userId(userId)
                        .budgetType(budgetType)
                        .budgetLimit(budgetLimit)
                        .currentSpent(currentSpent)
                        .attemptedAmount(attemptedAmount)
                        .traceId(traceId)
                        .build())
                .build();
    }
}
