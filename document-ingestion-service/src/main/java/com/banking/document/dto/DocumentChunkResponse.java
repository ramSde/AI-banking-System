package com.banking.document.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentChunkResponse {

    private UUID id;
    private UUID documentId;
    private Integer chunkIndex;
    private String chunkText;
    private Integer tokenCount;
    private String vectorId;
    private Map<String, Object> metadata;
    private Instant createdAt;
}
