package com.banking.i18n.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkTranslateRequest {

    @NotEmpty(message = "Key names list cannot be empty")
    private List<String> keyNames;

    @NotBlank(message = "Locale code is required")
    @Size(min = 2, max = 10, message = "Locale code must be between 2 and 10 characters")
    private String localeCode;

    private Map<String, Map<String, String>> placeholders;

    private Boolean fallbackToDefault;
}
