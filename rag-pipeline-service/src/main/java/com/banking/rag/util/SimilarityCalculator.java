package com.banking.rag.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
public class SimilarityCalculator {

    /**
     * Calculates cosine similarity between two embedding vectors.
     * 
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Cosine similarity score (0.0 to 1.0)
     */
    public BigDecimal cosineSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null) {
            throw new IllegalArgumentException("Embeddings cannot be null");
        }

        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return BigDecimal.ZERO;
        }

        double similarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        
        BigDecimal result = BigDecimal.valueOf(similarity)
                .setScale(4, RoundingMode.HALF_UP);

        log.debug("Calculated cosine similarity: {}", result);
        return result;
    }

    /**
     * Calculates Euclidean distance between two embedding vectors.
     * 
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Euclidean distance
     */
    public BigDecimal euclideanDistance(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null) {
            throw new IllegalArgumentException("Embeddings cannot be null");
        }

        if (embedding1.length != embedding2.length) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        double sumSquaredDiff = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            double diff = embedding1[i] - embedding2[i];
            sumSquaredDiff += diff * diff;
        }

        double distance = Math.sqrt(sumSquaredDiff);
        
        BigDecimal result = BigDecimal.valueOf(distance)
                .setScale(4, RoundingMode.HALF_UP);

        log.debug("Calculated Euclidean distance: {}", result);
        return result;
    }

    /**
     * Checks if two embeddings are similar based on a threshold.
     * 
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @param threshold Similarity threshold (0.0 to 1.0)
     * @return true if similarity exceeds threshold, false otherwise
     */
    public boolean areSimilar(float[] embedding1, float[] embedding2, BigDecimal threshold) {
        BigDecimal similarity = cosineSimilarity(embedding1, embedding2);
        return similarity.compareTo(threshold) >= 0;
    }
}
