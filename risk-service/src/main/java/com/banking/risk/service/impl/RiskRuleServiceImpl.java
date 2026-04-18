package com.banking.risk.service.impl;

import com.banking.risk.domain.RiskRule;
import com.banking.risk.dto.RiskRuleRequest;
import com.banking.risk.dto.RiskRuleResponse;
import com.banking.risk.exception.DuplicateRiskRuleException;
import com.banking.risk.exception.RiskRuleNotFoundException;
import com.banking.risk.repository.RiskRuleRepository;
import com.banking.risk.service.RiskRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of RiskRuleService.
 * Handles risk rule management operations.
 */
@Service
@Transactional
public class RiskRuleServiceImpl implements RiskRuleService {

    private static final Logger logger = LoggerFactory.getLogger(RiskRuleServiceImpl.class);

    private final RiskRuleRepository riskRuleRepository;

    public RiskRuleServiceImpl(RiskRuleRepository riskRuleRepository) {
        this.riskRuleRepository = riskRuleRepository;
    }

    @Override
    @CacheEvict(value = "riskRules", allEntries = true)
    public RiskRuleResponse createRiskRule(RiskRuleRequest request) {
        logger.info("Creating risk rule: {}", request.name());

        // Check for duplicate name
        if (riskRuleRepository.existsByName(request.name())) {
            throw new DuplicateRiskRuleException(request.name());
        }

        // Create risk rule entity
        RiskRule riskRule = RiskRule.builder()
                .name(request.name())
                .description(request.description())
                .ruleType(request.ruleType())
                .condition(request.condition())
                .riskScoreImpact(request.riskScoreImpact())
                .enabled(request.enabled())
                .priority(request.priority())
                .build();

        // Save risk rule
        riskRule = riskRuleRepository.save(riskRule);

        logger.info("Created risk rule: id={}, name={}", riskRule.getId(), riskRule.getName());

        return mapToResponse(riskRule);
    }

    @Override
    @CacheEvict(value = "riskRules", allEntries = true)
    public RiskRuleResponse updateRiskRule(UUID ruleId, RiskRuleRequest request) {
        logger.info("Updating risk rule: {}", ruleId);

        // Find existing rule
        RiskRule riskRule = riskRuleRepository.findByIdAndNotDeleted(ruleId)
                .orElseThrow(() -> new RiskRuleNotFoundException(ruleId));

        // Check for duplicate name (excluding current rule)
        if (riskRuleRepository.existsByNameExcludingId(request.name(), ruleId)) {
            throw new DuplicateRiskRuleException(request.name());
        }

        // Update fields
        riskRule.setName(request.name());
        riskRule.setDescription(request.description());
        riskRule.setRuleType(request.ruleType());
        riskRule.setCondition(request.condition());
        riskRule.setRiskScoreImpact(request.riskScoreImpact());
        riskRule.setEnabled(request.enabled());
        riskRule.setPriority(request.priority());

        // Save updated rule
        riskRule = riskRuleRepository.save(riskRule);

        logger.info("Updated risk rule: id={}, name={}", riskRule.getId(), riskRule.getName());

        return mapToResponse(riskRule);
    }

    @Override
    @Cacheable(value = "riskRules", key = "#ruleId")
    public RiskRuleResponse getRiskRuleById(UUID ruleId) {
        logger.debug("Retrieving risk rule: {}", ruleId);

        RiskRule riskRule = riskRuleRepository.findByIdAndNotDeleted(ruleId)
                .orElseThrow(() -> new RiskRuleNotFoundException(ruleId));

        return mapToResponse(riskRule);
    }

    @Override
    public Page<RiskRuleResponse> getAllRiskRules(Pageable pageable) {
        logger.debug("Retrieving all risk rules with pagination");

        return riskRuleRepository.findAllNotDeleted(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @CacheEvict(value = "riskRules", allEntries = true)
    public void deleteRiskRule(UUID ruleId) {
        logger.info("Deleting risk rule: {}", ruleId);

        RiskRule riskRule = riskRuleRepository.findByIdAndNotDeleted(ruleId)
                .orElseThrow(() -> new RiskRuleNotFoundException(ruleId));

        // Soft delete
        riskRule.softDelete();
        riskRuleRepository.save(riskRule);

        logger.info("Deleted risk rule: id={}, name={}", riskRule.getId(), riskRule.getName());
    }

    @Override
    @CacheEvict(value = "riskRules", allEntries = true)
    public RiskRuleResponse toggleRiskRule(UUID ruleId, boolean enabled) {
        logger.info("Toggling risk rule: id={}, enabled={}", ruleId, enabled);

        RiskRule riskRule = riskRuleRepository.findByIdAndNotDeleted(ruleId)
                .orElseThrow(() -> new RiskRuleNotFoundException(ruleId));

        riskRule.setEnabled(enabled);
        riskRule = riskRuleRepository.save(riskRule);

        logger.info("Toggled risk rule: id={}, name={}, enabled={}", 
                riskRule.getId(), riskRule.getName(), riskRule.getEnabled());

        return mapToResponse(riskRule);
    }

    /**
     * Map RiskRule entity to response DTO.
     */
    private RiskRuleResponse mapToResponse(RiskRule riskRule) {
        return new RiskRuleResponse(
                riskRule.getId(),
                riskRule.getName(),
                riskRule.getDescription(),
                riskRule.getRuleType(),
                riskRule.getCondition(),
                riskRule.getRiskScoreImpact(),
                riskRule.getEnabled(),
                riskRule.getPriority(),
                riskRule.getCreatedAt(),
                riskRule.getUpdatedAt()
        );
    }
}
