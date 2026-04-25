package com.banking.fraud.service;

import com.banking.fraud.domain.FraudRule;
import com.banking.fraud.domain.RuleType;
import com.banking.fraud.dto.FraudRuleRequest;
import com.banking.fraud.dto.FraudRuleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Fraud Rule Service Interface
 * 
 * Manages fraud detection rules.
 */
public interface FraudRuleService {

    /**
     * Create new fraud rule
     * 
     * @param request Fraud rule request
     * @param createdBy Creator user ID
     * @return Created fraud rule response
     */
    FraudRuleResponse createRule(FraudRuleRequest request, UUID createdBy);

    /**
     * Update existing fraud rule
     * 
     * @param ruleId Rule ID
     * @param request Fraud rule request
     * @return Updated fraud rule response
     */
    FraudRuleResponse updateRule(UUID ruleId, FraudRuleRequest request);

    /**
     * Delete fraud rule (soft delete)
     * 
     * @param ruleId Rule ID
     */
    void deleteRule(UUID ruleId);

    /**
     * Get fraud rule by ID
     * 
     * @param ruleId Rule ID
     * @return Fraud rule response
     */
    FraudRuleResponse getRuleById(UUID ruleId);

    /**
     * Get all fraud rules with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of fraud rules
     */
    Page<FraudRuleResponse> getAllRules(Pageable pageable);

    /**
     * Get all active fraud rules
     * 
     * @return List of active fraud rules
     */
    List<FraudRule> getActiveRules();

    /**
     * Get active rules by type
     * 
     * @param ruleType Rule type
     * @return List of active fraud rules
     */
    List<FraudRule> getActiveRulesByType(RuleType ruleType);

    /**
     * Enable or disable fraud rule
     * 
     * @param ruleId Rule ID
     * @param enabled Enable status
     * @return Updated fraud rule response
     */
    FraudRuleResponse toggleRuleStatus(UUID ruleId, boolean enabled);
}
