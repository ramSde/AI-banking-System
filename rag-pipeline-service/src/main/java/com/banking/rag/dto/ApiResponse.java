package com.banking.rag.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    
    private T data;
    
    private ErrorDetails error;
    
    private String traceId;
    
    @Builder.Default
    private Instant timestamp = Instant.now();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private String message;
        private Object details;
    }

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .build())
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .details(details)
                        .build())
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }
}
