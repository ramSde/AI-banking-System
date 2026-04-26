package com.banking.rag.util;

public class TokenCounter {

    private static final int AVERAGE_CHARS_PER_TOKEN = 4;

    private TokenCounter() {
    }

    public static int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return text.length() / AVERAGE_CHARS_PER_TOKEN;
    }

    public static int estimateTokensForDocuments(String... documents) {
        int total = 0;
        for (String doc : documents) {
            total += countTokens(doc);
        }
        return total;
    }

    public static boolean fitsWithinLimit(String text, int maxTokens) {
        return countTokens(text) <= maxTokens;
    }
}
