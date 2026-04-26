package com.banking.rag.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetails error;
    private String traceId;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(T data, String traceId) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .traceId(traceId)
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(new ErrorDetails(code, message, null))
                .traceId(traceId)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorDetails error, String traceId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .traceId(traceId)
                .build();
    }

    public record ErrorDetails(
            String code,
            String message,
            java.util.List<String> details
    ) {
    }
}
