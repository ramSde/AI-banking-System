package com.banking.fraud.service.impl;

import com.banking.fraud.domain.FraudRule;
import com.banking.fraud.domain.RuleType;
import com.banking.fraud.dto.FraudRuleRequest;
import com.banking.fraud.dto.FraudRuleResponse;
import com.banking.fraud.exception.FraudRuleNotFoundException;
import com.banking.fraud.exception.InvalidRuleException;
import com.banking.fraud.mapper.FraudMapper;
import com.banking.fraud.repository.FraudRuleRepository;
import com.banking.fraud.service.FraudRuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fraud Rule Service Implementation
 * 
 * Manages fraud detection rules.
 */
@Service
@Transactional
public class FraudRuleServiceImpl implements FraudRuleService {

    private static final Logger log = LoggerFactory.getLogger(FraudRuleServiceImpl.class);

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudMapper fraudMapper;

    public FraudRuleServiceImpl(
            FraudRuleRepository fraudRuleRepository,
            FraudMapper fraudMapper
    ) {
        this.fraudRuleRepository = fraudRuleRepository;
        this.fraudMapper = fraudMapper;
    }

    @Override
    public FraudRuleResponse createRule(FraudRuleRequest request, UUID createdBy) {
        log.info("Creating fraud rule: {}", request.ruleName());

        if (fraudRuleRepository.existsByRuleNameAndDeletedAtIsNull(request.ruleName())) {
            throw new InvalidRuleException("Fraud rule with name '" + request.ruleName() + "' already exists");
        }

        validateRuleConfig(request.ruleConfig());

        FraudRule fraudRule = fraudMapper.toEntity(request);
        fraudRule.setCreatedBy(createdBy);
        fraudRule.setEnabled(request.enabled() != null ? request.enabled() : true);

        FraudRule savedRule = fraudRuleRepository.save(fraudRule);
        log.info("Created fraud rule: id={}, name={}", savedRule.getId(), savedRule.getRuleName());

        return fraudMapper.toResponse(savedRule);
    }

    @Override
    public FraudRuleResponse updateRule(UUID ruleId, FraudRuleRequest request) {
        log.info("Updating fraud rule: {}", ruleId);

        FraudRule fraudRule = fraudRuleRepository.findById(ruleId)
                .filter(rule -> rule.getDeletedAt() == null)
                .orElseThrow(() -> new FraudRuleNotFoundException(ruleId));

        if (!fraudRule.getRuleName().equals(request.ruleName()) &&
                fraudRuleRepository.existsByRuleNameAndDeletedAtIsNull(request.ruleName())) {
            throw new InvalidRuleException("Fraud rule with name '" + request.ruleName() + "' already exists");
        }

        validateRuleConfig(request.ruleConfig());

        fraudMapper.updateEntity(request, fraudRule);

        FraudRule updatedRule = fraudRuleRepository.save(fraudRule);
        log.info("Updated fraud rule: id={}, name={}", updatedRule.getId(), updatedRule.getRuleName());

        return fraudMapper.toResponse(updatedRule);
    }

    @Override
    public void deleteRule(UUID ruleId) {
        log.info("Deleting fraud rule: {}", ruleId);

        FraudRule fraudRule = fraudRuleRepository.findById(ruleId)
                .filter(rule -> rule.getDeletedAt() == null)
                .orElseThrow(() -> new FraudRuleNotFoundException(ruleId));

        fraudRule.setDeletedAt(Instant.now());
        fraudRuleRepository.save(fraudRule);

        log.info("Deleted fraud rule: id={}", ruleId);
    }

    @Override
    @Transactional(readOnly = true)
    public FraudRuleResponse getRuleById(UUID ruleId) {
        FraudRule fraudRule = fraudRuleRepository.findById(ruleId)
                .filter(rule -> rule.getDeletedAt() == null)
                .orElseThrow(() -> new FraudRuleNotFoundException(ruleId));

        return fraudMapper.toResponse(fraudRule);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FraudRuleResponse> getAllRules(Pageable pageable) {
        return fraudRuleRepository.findAllNotDeleted(pageable)
                .map(fraudMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudRule> getActiveRules() {
        return fraudRuleRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudRule> getActiveRulesByType(RuleType ruleType) {
        return fraudRuleRepository.findActiveByType(ruleType);
    }

    @Override
    public FraudRuleResponse toggleRuleStatus(UUID ruleId, boolean enabled) {
        log.info("Toggling fraud rule status: id={}, enabled={}", ruleId, enabled);

        FraudRule fraudRule = fraudRuleRepository.findById(ruleId)
                .filter(rule -> rule.getDeletedAt() == null)
                .orElseThrow(() -> new FraudRuleNotFoundException(ruleId));

        fraudRule.setEnabled(enabled);
        FraudRule updatedRule = fraudRuleRepository.save(fraudRule);

        log.info("Toggled fraud rule status: id={}, enabled={}", ruleId, enabled);
        return fraudMapper.toResponse(updatedRule);
    }

    private void validateRuleConfig(Object ruleConfig) {
        if (ruleConfig == null) {
            throw new InvalidRuleException("Rule configuration cannot be null");
        }
    }
}
