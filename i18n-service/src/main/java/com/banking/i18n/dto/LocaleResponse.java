package com.banking.i18n.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocaleResponse {

    private UUID id;
    private String localeCode;
    private String languageName;
    private String nativeName;
    private Boolean isRtl;
    private Boolean isEnabled;
    private Boolean isDefault;
    private Integer displayOrder;
}
