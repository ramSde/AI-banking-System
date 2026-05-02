package com.banking.i18n.repository;

import com.banking.i18n.domain.SupportedLocale;
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
public interface SupportedLocaleRepository extends JpaRepository<SupportedLocale, UUID> {

    @Query("SELECT sl FROM SupportedLocale sl WHERE sl.deletedAt IS NULL AND sl.localeCode = :localeCode")
    Optional<SupportedLocale> findByLocaleCode(@Param("localeCode") String localeCode);

    @Query("SELECT sl FROM SupportedLocale sl WHERE sl.deletedAt IS NULL AND sl.isEnabled = true ORDER BY sl.displayOrder")
    List<SupportedLocale> findAllEnabled();

    @Query("SELECT sl FROM SupportedLocale sl WHERE sl.deletedAt IS NULL AND sl.isDefault = true")
    Optional<SupportedLocale> findDefault();

    @Query("SELECT sl FROM SupportedLocale sl WHERE sl.deletedAt IS NULL AND sl.isRtl = :isRtl")
    List<SupportedLocale> findByIsRtl(@Param("isRtl") Boolean isRtl);

    @Query("SELECT sl FROM SupportedLocale sl WHERE sl.deletedAt IS NULL ORDER BY sl.displayOrder")
    Page<SupportedLocale> findAllActive(Pageable pageable);

    @Query("SELECT sl FROM SupportedLocale sl WHERE sl.deletedAt IS NULL ORDER BY sl.displayOrder")
    List<SupportedLocale> findAllActive();

    @Query("SELECT COUNT(sl) FROM SupportedLocale sl WHERE sl.deletedAt IS NULL AND sl.isEnabled = true")
    long countEnabled();

    @Query("SELECT sl.localeCode FROM SupportedLocale sl WHERE sl.deletedAt IS NULL AND sl.isEnabled = true ORDER BY sl.displayOrder")
    List<String> findAllEnabledLocaleCodes();
}
