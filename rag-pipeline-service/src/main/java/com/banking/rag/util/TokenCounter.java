package com.banking.rag.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class TokenCounter {

    private static final Pattern WORD_PATTERN = Pattern.compile("\\w+");
    private static final double TOKENS_PER_WORD = 1.3;

    /**
     * Estimates token count using a simple heuristic.
     * For production, consider using tiktoken or similar library for accurate counting.
     * 
     * @param text The text to count tokens for
     * @return Estimated token count
     */
    public int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        Matcher matcher = WORD_PATTERN.matcher(text);
        int wordCount = 0;
        while (matcher.find()) {
            wordCount++;
        }

        int estimatedTokens = (int) Math.ceil(wordCount * TOKENS_PER_WORD);
        log.debug("Estimated {} tokens for {} words", estimatedTokens, wordCount);
        return estimatedTokens;
    }

    /**
     * Checks if text exceeds the maximum token limit.
     * 
     * @param text The text to check
     * @param maxTokens Maximum allowed tokens
     * @return true if text exceeds limit, false otherwise
     */
    public boolean exceedsLimit(String text, int maxTokens) {
        return countTokens(text) > maxTokens;
    }

    /**
     * Truncates text to fit within token limit.
     * 
     * @param text The text to truncate
     * @param maxTokens Maximum allowed tokens
     * @return Truncated text
     */
    public String truncateToTokenLimit(String text, int maxTokens) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        int currentTokens = countTokens(text);
        if (currentTokens <= maxTokens) {
            return text;
        }

        double ratio = (double) maxTokens / currentTokens;
        int targetLength = (int) (text.length() * ratio);
        
        String truncated = text.substring(0, Math.min(targetLength, text.length()));
        
        while (countTokens(truncated) > maxTokens && truncated.length() > 0) {
            truncated = truncated.substring(0, (int) (truncated.length() * 0.9));
        }

        log.debug("Truncated text from {} to {} tokens", currentTokens, countTokens(truncated));
        return truncated;
    }
}
