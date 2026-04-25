package com.banking.rag.dto;

import com.banking.rag.domain.RagSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContextAssemblyResponse {

    private UUID contextId;
    
    private UUID queryId;
    
    private String assembledContext;
    
    private Integer tokenCount;
    
    private Integer sourceCount;
    
    private List<RagSource> sources;
}
