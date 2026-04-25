package com.banking.fraud.repository;

import com.banking.fraud.domain.FraudRule;
import com.banking.fraud.domain.RuleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Fraud Rule entities
 */
@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, UUID> {

    /**
     * Find all active rules (enabled and not deleted)
     * 
     * @return List of active fraud rules
     */
    @Query("SELECT fr FROM FraudRule fr WHERE fr.enabled = true AND fr.deletedAt IS NULL ORDER BY fr.weight DESC")
    List<FraudRule> findAllActive();

    /**
     * Find active rules by type
     * 
     * @param ruleType Rule type
     * @return List of active fraud rules of specified type
     */
    @Query("SELECT fr FROM FraudRule fr WHERE fr.ruleType = :ruleType AND fr.enabled = true AND fr.deletedAt IS NULL ORDER BY fr.weight DESC")
    List<FraudRule> findActiveByType(@Param("ruleType") RuleType ruleType);

    /**
     * Find rule by name
     * 
     * @param ruleName Rule name
     * @return Optional fraud rule
     */
    Optional<FraudRule> findByRuleNameAndDeletedAtIsNull(String ruleName);

    /**
     * Find all rules with pagination
     * 
     * @param pageable Pagination parameters
     * @return Page of fraud rules
     */
    @Query("SELECT fr FROM FraudRule fr WHERE fr.deletedAt IS NULL")
    Page<FraudRule> findAllNotDeleted(Pageable pageable);

    /**
     * Find rules by type with pagination
     * 
     * @param ruleType Rule type
     * @param pageable Pagination parameters
     * @return Page of fraud rules
     */
    Page<FraudRule> findByRuleTypeAndDeletedAtIsNull(RuleType ruleType, Pageable pageable);

    /**
     * Find rules by enabled status
     * 
     * @param enabled Enabled status
     * @param pageable Pagination parameters
     * @return Page of fraud rules
     */
    Page<FraudRule> findByEnabledAndDeletedAtIsNull(Boolean enabled, Pageable pageable);

    /**
     * Check if rule name exists
     * 
     * @param ruleName Rule name
     * @return True if exists
     */
    boolean existsByRuleNameAndDeletedAtIsNull(String ruleName);
}
