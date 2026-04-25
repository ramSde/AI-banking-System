package com.banking.fraud.util;

import com.banking.fraud.config.FraudProperties;
import com.banking.fraud.domain.FraudRule;
import com.banking.fraud.domain.RiskLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Risk Score Calculator
 * 
 * Calculates aggregate risk scores based on triggered fraud rules.
 */
@Component
public class RiskScoreCalculator {

    private static final Logger log = LoggerFactory.getLogger(RiskScoreCalculator.class);

    private final FraudProperties fraudProperties;

    public RiskScoreCalculator(FraudProperties fraudProperties) {
        this.fraudProperties = fraudProperties;
    }

    /**
     * Calculate risk score from triggered rules
     * 
     * @param triggeredRules List of triggered fraud rules
     * @return Risk score (0-100)
     */
    public int calculateRiskScore(List<FraudRule> triggeredRules) {
        if (triggeredRules == null || triggeredRules.isEmpty()) {
            return 0;
        }

        int totalScore = triggeredRules.stream()
                .mapToInt(FraudRule::getWeight)
                .sum();

        int normalizedScore = Math.min(totalScore, 100);

        log.debug("Calculated risk score: {} from {} triggered rules", normalizedScore, triggeredRules.size());
        return normalizedScore;
    }

    /**
     * Determine risk level from score
     * 
     * @param score Risk score
     * @return Risk level
     */
    public RiskLevel determineRiskLevel(int score) {
        return RiskLevel.fromScore(
                score,
                fraudProperties.getScoring().getMediumRiskThreshold(),
                fraudProperties.getScoring().getHighRiskThreshold()
        );
    }

    /**
     * Check if transaction should be auto-blocked
     * 
     * @param score Risk score
     * @return True if should be blocked
     */
    public boolean shouldAutoBlock(int score) {
        return score >= fraudProperties.getScoring().getAutoBlockThreshold();
    }

    /**
     * Build triggered rules map for storage
     * 
     * @param triggeredRules List of triggered rules
     * @return Map of rule details
     */
    public Map<String, Object> buildTriggeredRulesMap(List<FraudRule> triggeredRules) {
        Map<String, Object> rulesMap = new HashMap<>();
        List<Map<String, Object>> rulesList = new ArrayList<>();

        for (FraudRule rule : triggeredRules) {
            Map<String, Object> ruleDetails = new HashMap<>();
            ruleDetails.put("ruleId", rule.getId().toString());
            ruleDetails.put("ruleName", rule.getRuleName());
            ruleDetails.put("ruleType", rule.getRuleType().toString());
            ruleDetails.put("weight", rule.getWeight());
            rulesList.add(ruleDetails);
        }

        rulesMap.put("rules", rulesList);
        rulesMap.put("totalRulesTriggered", rulesList.size());

        return rulesMap;
    }

    /**
     * Build check details map
     * 
     * @param amount Transaction amount
     * @param transactionType Transaction type
     * @param velocityCount Velocity count
     * @param additionalData Additional data
     * @return Check details map
     */
    public Map<String, Object> buildCheckDetails(
            BigDecimal amount,
            String transactionType,
            Long velocityCount,
            Map<String, Object> additionalData
    ) {
        Map<String, Object> details = new HashMap<>();
        details.put("amount", amount.toString());
        details.put("transactionType", transactionType);
        details.put("velocityCount", velocityCount);
        
        if (additionalData != null) {
            details.putAll(additionalData);
        }

        return details;
    }

    /**
     * Generate recommendation based on risk level
     * 
     * @param riskLevel Risk level
     * @param blocked Whether transaction is blocked
     * @return Recommendation string
     */
    public String generateRecommendation(RiskLevel riskLevel, boolean blocked) {
        if (blocked) {
            return "Transaction blocked due to high fraud risk. Manual review required.";
        }

        return switch (riskLevel) {
            case LOW -> "Transaction approved. Low fraud risk detected.";
            case MEDIUM -> "Transaction flagged for monitoring. Medium fraud risk detected.";
            case HIGH -> "Transaction requires additional verification. High fraud risk detected.";
        };
    }
}
