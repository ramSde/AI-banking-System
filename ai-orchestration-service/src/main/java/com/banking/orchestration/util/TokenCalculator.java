package com.banking.orchestration.util;

import org.springframework.stereotype.Component;

@Component
public class TokenCalculator {

    private static final double CHARS_PER_TOKEN = 4.0;

    public Integer estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }

    public Integer countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }
}
