package com.banking.i18n.repository;

import com.banking.i18n.domain.TranslationKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranslationKeyRepository extends JpaRepository<TranslationKey, UUID> {

    @Query("SELECT tk FROM TranslationKey tk WHERE tk.deletedAt IS NULL AND tk.keyName = :keyName")
    Optional<TranslationKey> findByKeyName(@Param("keyName") String keyName);

    @Query("SELECT tk FROM TranslationKey tk WHERE tk.deletedAt IS NULL AND tk.category = :category")
    Page<TranslationKey> findByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT tk FROM TranslationKey tk WHERE tk.deletedAt IS NULL AND tk.isDynamic = :isDynamic")
    Page<TranslationKey> findByIsDynamic(@Param("isDynamic") Boolean isDynamic, Pageable pageable);

    @Query("SELECT tk FROM TranslationKey tk WHERE tk.deletedAt IS NULL")
    Page<TranslationKey> findAllActive(Pageable pageable);

    @Query("SELECT tk FROM TranslationKey tk WHERE tk.deletedAt IS NULL")
    List<TranslationKey> findAllActive();

    @Query("SELECT tk FROM TranslationKey tk WHERE tk.deletedAt IS NULL AND " +
           "(LOWER(tk.keyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(tk.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<TranslationKey> searchKeys(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT COUNT(tk) FROM TranslationKey tk WHERE tk.deletedAt IS NULL")
    long countActive();

    @Query("SELECT COUNT(tk) FROM TranslationKey tk WHERE tk.deletedAt IS NULL AND tk.category = :category")
    long countByCategory(@Param("category") String category);

    @Query("SELECT DISTINCT tk.category FROM TranslationKey tk WHERE tk.deletedAt IS NULL ORDER BY tk.category")
    List<String> findAllCategories();
}
