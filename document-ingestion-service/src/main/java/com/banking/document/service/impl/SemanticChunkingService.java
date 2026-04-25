package com.banking.document.service.impl;

import com.banking.document.service.ChunkingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SemanticChunkingService implements ChunkingService {

    private final int defaultChunkSize;
    private final int defaultOverlap;

    public SemanticChunkingService(
            @Value("${document.processing.chunk-size:1000}") int defaultChunkSize,
            @Value("${document.processing.chunk-overlap:200}") int defaultOverlap) {
        this.defaultChunkSize = defaultChunkSize;
        this.defaultOverlap = defaultOverlap;
    }

    @Override
    public List<String> chunkText(String text, int chunkSize, int overlap) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        StringBuilder currentChunk = new StringBuilder();
        int currentTokenCount = 0;

        for (String sentence : sentences) {
            int sentenceTokenCount = estimateTokenCount(sentence);
            
            if (currentTokenCount + sentenceTokenCount > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
                
                String overlapText = getOverlapText(currentChunk.toString(), overlap);
                currentChunk = new StringBuilder(overlapText);
                currentTokenCount = estimateTokenCount(overlapText);
            }
            
            currentChunk.append(sentence).append(" ");
            currentTokenCount += sentenceTokenCount;
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        log.debug("Chunked text into {} chunks with chunk size {} and overlap {}", 
                chunks.size(), chunkSize, overlap);
        
        return chunks;
    }

    @Override
    public int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        String[] words = text.split("\\s+");
        return (int) Math.ceil(words.length * 1.3);
    }

    private String getOverlapText(String text, int overlapTokens) {
        String[] words = text.split("\\s+");
        int overlapWords = (int) Math.ceil(overlapTokens / 1.3);
        
        if (words.length <= overlapWords) {
            return text;
        }
        
        int startIndex = words.length - overlapWords;
        StringBuilder overlap = new StringBuilder();
        
        for (int i = startIndex; i < words.length; i++) {
            overlap.append(words[i]).append(" ");
        }
        
        return overlap.toString().trim();
    }
}
