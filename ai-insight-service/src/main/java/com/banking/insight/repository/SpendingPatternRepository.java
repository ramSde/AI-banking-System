package com.banking.insight.repository;

import com.banking.insight.domain.SpendingPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpendingPatternRepository extends JpaRepository<SpendingPattern, UUID> {

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.id = :id AND sp.deletedAt IS NULL")
    Optional<SpendingPattern> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.deletedAt IS NULL ORDER BY sp.lastOccurrence DESC")
    Page<SpendingPattern> findByUserIdAndNotDeleted(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.category = :category AND sp.deletedAt IS NULL ORDER BY sp.lastOccurrence DESC")
    List<SpendingPattern> findByUserIdAndCategory(
        @Param("userId") UUID userId,
        @Param("category") String category
    );

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.isRecurring = true AND sp.deletedAt IS NULL ORDER BY sp.nextPredictedDate ASC")
    List<SpendingPattern> findRecurringByUserId(@Param("userId") UUID userId);

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.isSeasonal = true AND sp.deletedAt IS NULL ORDER BY sp.lastOccurrence DESC")
    List<SpendingPattern> findSeasonalByUserId(@Param("userId") UUID userId);

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.patternType = :type AND sp.deletedAt IS NULL ORDER BY sp.lastOccurrence DESC")
    List<SpendingPattern> findByUserIdAndType(
        @Param("userId") UUID userId,
        @Param("type") SpendingPattern.PatternType type
    );

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.merchantName = :merchantName AND sp.deletedAt IS NULL ORDER BY sp.lastOccurrence DESC")
    List<SpendingPattern> findByUserIdAndMerchant(
        @Param("userId") UUID userId,
        @Param("merchantName") String merchantName
    );

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.frequency = :frequency AND sp.deletedAt IS NULL ORDER BY sp.lastOccurrence DESC")
    List<SpendingPattern> findByUserIdAndFrequency(
        @Param("userId") UUID userId,
        @Param("frequency") SpendingPattern.Frequency frequency
    );

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.nextPredictedDate BETWEEN :start AND :end AND sp.deletedAt IS NULL ORDER BY sp.nextPredictedDate ASC")
    List<SpendingPattern> findUpcomingPatterns(
        @Param("start") Instant start,
        @Param("end") Instant end
    );

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.userId = :userId AND sp.category = :category AND sp.merchantName = :merchantName AND sp.deletedAt IS NULL")
    Optional<SpendingPattern> findByUserIdAndCategoryAndMerchant(
        @Param("userId") UUID userId,
        @Param("category") String category,
        @Param("merchantName") String merchantName
    );

    @Query("SELECT sp FROM SpendingPattern sp WHERE sp.deletedAt IS NULL ORDER BY sp.lastOccurrence DESC")
    Page<SpendingPattern> findAllNotDeleted(Pageable pageable);
}
