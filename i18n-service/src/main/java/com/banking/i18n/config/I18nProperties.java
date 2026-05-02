package com.banking.i18n.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "i18n")
public class I18nProperties {

    private String defaultLocale = "en";
    private boolean autoTranslateEnabled = false;
    private String translationApiUrl;
    private String translationApiKey;
    private int cacheExpiryMinutes = 60;
    private int maxTranslationLength = 5000;

    public String getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public boolean isAutoTranslateEnabled() {
        return autoTranslateEnabled;
    }

    public void setAutoTranslateEnabled(boolean autoTranslateEnabled) {
        this.autoTranslateEnabled = autoTranslateEnabled;
    }

    public String getTranslationApiUrl() {
        return translationApiUrl;
    }

    public void setTranslationApiUrl(String translationApiUrl) {
        this.translationApiUrl = translationApiUrl;
    }

    public String getTranslationApiKey() {
        return translationApiKey;
    }

    public void setTranslationApiKey(String translationApiKey) {
        this.translationApiKey = translationApiKey;
    }

    public int getCacheExpiryMinutes() {
        return cacheExpiryMinutes;
    }

    public void setCacheExpiryMinutes(int cacheExpiryMinutes) {
        this.cacheExpiryMinutes = cacheExpiryMinutes;
    }

    public int getMaxTranslationLength() {
        return maxTranslationLength;
    }

    public void setMaxTranslationLength(int maxTranslationLength) {
        this.maxTranslationLength = maxTranslationLength;
    }
}
