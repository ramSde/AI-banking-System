package com.banking.i18n.repository;

import com.banking.i18n.domain.Translation;
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
public interface TranslationRepository extends JpaRepository<Translation, UUID> {

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL AND " +
           "t.translationKey = :translationKey AND t.localeCode = :localeCode")
    Optional<Translation> findByTranslationKeyAndLocaleCode(
        @Param("translationKey") TranslationKey translationKey,
        @Param("localeCode") String localeCode
    );

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL AND t.localeCode = :localeCode")
    Page<Translation> findByLocaleCode(@Param("localeCode") String localeCode, Pageable pageable);

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL AND t.localeCode = :localeCode")
    List<Translation> findAllByLocaleCode(@Param("localeCode") String localeCode);

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL AND t.translationKey = :translationKey")
    List<Translation> findByTranslationKey(@Param("translationKey") TranslationKey translationKey);

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL AND t.isAutoTranslated = :isAutoTranslated")
    Page<Translation> findByIsAutoTranslated(@Param("isAutoTranslated") Boolean isAutoTranslated, Pageable pageable);

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL AND t.reviewedAt IS NULL")
    Page<Translation> findUnreviewed(Pageable pageable);

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL AND " +
           "t.qualityScore IS NOT NULL AND t.qualityScore < :threshold")
    Page<Translation> findLowQuality(@Param("threshold") Integer threshold, Pageable pageable);

    @Query("SELECT t FROM Translation t WHERE t.deletedAt IS NULL")
    Page<Translation> findAllActive(Pageable pageable);

    @Query("SELECT COUNT(t) FROM Translation t WHERE t.deletedAt IS NULL AND t.localeCode = :localeCode")
    long countByLocaleCode(@Param("localeCode") String localeCode);

    @Query("SELECT COUNT(t) FROM Translation t WHERE t.deletedAt IS NULL AND t.isAutoTranslated = true")
    long countAutoTranslated();

    @Query("SELECT COUNT(t) FROM Translation t WHERE t.deletedAt IS NULL AND t.reviewedAt IS NULL")
    long countUnreviewed();
}
