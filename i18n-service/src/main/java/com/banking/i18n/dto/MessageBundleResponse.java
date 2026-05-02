package com.banking.i18n.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBundleResponse {

    private String localeCode;
    private Map<String, String> messages;
    private Integer totalKeys;
    private Integer translatedKeys;
    private Integer missingKeys;
    private Double completionPercentage;
}
