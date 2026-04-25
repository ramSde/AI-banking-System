package com.banking.fraud.util;

import com.banking.fraud.config.FraudProperties;
import com.banking.fraud.repository.FraudCheckRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Velocity Checker
 * 
 * Checks transaction velocity (frequency) for fraud detection.
 */
@Component
public class VelocityChecker {

    private static final Logger log = LoggerFactory.getLogger(VelocityChecker.class);

    private final FraudCheckRepository fraudCheckRepository;
    private final FraudProperties fraudProperties;

    public VelocityChecker(
            FraudCheckRepository fraudCheckRepository,
            FraudProperties fraudProperties
    ) {
        this.fraudCheckRepository = fraudCheckRepository;
        this.fraudProperties = fraudProperties;
    }

    /**
     * Check transaction velocity for user
     * 
     * @param userId User ID
     * @return Transaction count in time window
     */
    public long checkVelocity(UUID userId) {
        int windowMinutes = fraudProperties.getVelocity().getTransactionCountWindowMinutes();
        Instant startTime = Instant.now().minus(windowMinutes, ChronoUnit.MINUTES);

        long count = fraudCheckRepository.countByUserIdAndCheckedAtAfter(userId, startTime);
        
        log.debug("Velocity check for user {}: {} transactions in last {} minutes", 
                userId, count, windowMinutes);
        
        return count;
    }

    /**
     * Check if velocity exceeds threshold
     * 
     * @param userId User ID
     * @return True if velocity is suspicious
     */
    public boolean isVelocitySuspicious(UUID userId) {
        long count = checkVelocity(userId);
        int maxTransactions = fraudProperties.getVelocity().getMaxTransactionsPerWindow();
        
        boolean suspicious = count >= maxTransactions;
        
        if (suspicious) {
            log.warn("Suspicious velocity detected for user {}: {} transactions (threshold: {})", 
                    userId, count, maxTransactions);
        }
        
        return suspicious;
    }

    /**
     * Get velocity score contribution
     * 
     * @param userId User ID
     * @return Score contribution (0-30)
     */
    public int getVelocityScoreContribution(UUID userId) {
        long count = checkVelocity(userId);
        int maxTransactions = fraudProperties.getVelocity().getMaxTransactionsPerWindow();

        if (count < maxTransactions) {
            return 0;
        }

        double ratio = (double) count / maxTransactions;
        int score = (int) Math.min(ratio * 30, 30);

        log.debug("Velocity score contribution for user {}: {}", userId, score);
        return score;
    }
}
