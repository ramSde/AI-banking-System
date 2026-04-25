package com.banking.document.event;

import com.banking.document.domain.DocumentType;
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
public class DocumentProcessingFailedEvent {

    private String eventId;
    private String eventType;
    @Builder.Default
    private String version = "1.0";
    private Instant occurredAt;
    private String correlationId;
    private Payload payload;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payload {
        private UUID documentId;
        private UUID userId;
        private String originalFilename;
        private DocumentType documentType;
        private String errorMessage;
        private String errorCode;
    }
}
