package com.banking.i18n.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranslateResponse {

    private String keyName;
    private String localeCode;
    private String translatedText;
    private Boolean isAutoTranslated;
    private Boolean isFallback;
    private String fallbackLocale;
}
