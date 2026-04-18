package com.banking.risk.repository;

import com.banking.risk.domain.RiskRule;
import com.banking.risk.domain.RiskRuleType;
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
 * Repository for RiskRule entity.
 * Provides data access methods for risk rule management.
 */
@Repository
public interface RiskRuleRepository extends JpaRepository<RiskRule, UUID> {

    /**
     * Find risk rule by ID excluding soft-deleted records.
     */
    @Query("SELECT rr FROM RiskRule rr WHERE rr.id = :id AND rr.deletedAt IS NULL")
    Optional<RiskRule> findByIdAndNotDeleted(@Param("id") UUID id);

    /**
     * Find risk rule by name.
     */
    @Query("SELECT rr FROM RiskRule rr WHERE rr.name = :name AND rr.deletedAt IS NULL")
    Optional<RiskRule> findByName(@Param("name") String name);

    /**
     * Find all active risk rules ordered by priority.
     */
    @Query("SELECT rr FROM RiskRule rr WHERE rr.enabled = true " +
           "AND rr.deletedAt IS NULL ORDER BY rr.priority DESC")
    List<RiskRule> findAllActive();

    /**
     * Find active risk rules by type.
     */
    @Query("SELECT rr FROM RiskRule rr WHERE rr.ruleType = :ruleType " +
           "AND rr.enabled = true AND rr.deletedAt IS NULL ORDER BY rr.priority DESC")
    List<RiskRule> findActiveByRuleType(@Param("ruleType") RiskRuleType ruleType);

    /**
     * Find all risk rules (including disabled) with pagination.
     */
    @Query("SELECT rr FROM RiskRule rr WHERE rr.deletedAt IS NULL ORDER BY rr.priority DESC")
    Page<RiskRule> findAllNotDeleted(Pageable pageable);

    /**
     * Check if a rule name already exists.
     */
    @Query("SELECT COUNT(rr) > 0 FROM RiskRule rr WHERE rr.name = :name AND rr.deletedAt IS NULL")
    boolean existsByName(@Param("name") String name);

    /**
     * Check if a rule name exists excluding a specific ID (for updates).
     */
    @Query("SELECT COUNT(rr) > 0 FROM RiskRule rr WHERE rr.name = :name " +
           "AND rr.id != :excludeId AND rr.deletedAt IS NULL")
    boolean existsByNameExcludingId(@Param("name") String name, @Param("excludeId") UUID excludeId);
}
