package com.banking.risk.service;

import com.banking.risk.dto.RiskRuleRequest;
import com.banking.risk.dto.RiskRuleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for risk rule management.
 * Provides methods for creating, updating, and managing risk rules.
 */
public interface RiskRuleService {

    /**
     * Create a new risk rule.
     *
     * @param request Risk rule request
     * @return Created risk rule
     */
    RiskRuleResponse createRiskRule(RiskRuleRequest request);

    /**
     * Update an existing risk rule.
     *
     * @param ruleId Rule ID
     * @param request Risk rule request
     * @return Updated risk rule
     */
    RiskRuleResponse updateRiskRule(UUID ruleId, RiskRuleRequest request);

    /**
     * Get risk rule by ID.
     *
     * @param ruleId Rule ID
     * @return Risk rule
     */
    RiskRuleResponse getRiskRuleById(UUID ruleId);

    /**
     * Get all risk rules with pagination.
     *
     * @param pageable Pagination parameters
     * @return Page of risk rules
     */
    Page<RiskRuleResponse> getAllRiskRules(Pageable pageable);

    /**
     * Delete a risk rule (soft delete).
     *
     * @param ruleId Rule ID
     */
    void deleteRiskRule(UUID ruleId);

    /**
     * Enable or disable a risk rule.
     *
     * @param ruleId Rule ID
     * @param enabled Whether to enable or disable the rule
     * @return Updated risk rule
     */
    RiskRuleResponse toggleRiskRule(UUID ruleId, boolean enabled);
}
