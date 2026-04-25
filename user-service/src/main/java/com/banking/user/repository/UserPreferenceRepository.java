package com.banking.user.repository;

import com.banking.user.domain.UserPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserPreference entity operations.
 * Provides CRUD operations and custom queries for user preferences management.
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    /**
     * Find preference by user ID (not deleted)
     */
    @Query("SELECT up FROM UserPreference up WHERE up.userId = :userId AND up.deletedAt IS NULL")
    Optional<UserPreference> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

    /**
     * Find preferences by language
     */
    @Query("SELECT up FROM UserPreference up WHERE up.language = :language AND up.deletedAt IS NULL")
    Page<UserPreference> findByLanguage(@Param("language") String language, Pageable pageable);

    /**
     * Find preferences by timezone
     */
    @Query("SELECT up FROM UserPreference up WHERE up.timezone = :timezone AND up.deletedAt IS NULL")
    Page<UserPreference> findByTimezone(@Param("timezone") String timezone, Pageable pageable);

    /**
     * Find preferences by currency
     */
    @Query("SELECT up FROM UserPreference up WHERE up.currency = :currency AND up.deletedAt IS NULL")
    Page<UserPreference> findByCurrency(@Param("currency") String currency, Pageable pageable);

    /**
     * Find users with email notifications enabled
     */
    @Query("SELECT up FROM UserPreference up WHERE up.notificationEmail = true AND up.deletedAt IS NULL")
    Page<UserPreference> findWithEmailNotificationsEnabled(Pageable pageable);

    /**
     * Find users with SMS notifications enabled
     */
    @Query("SELECT up FROM UserPreference up WHERE up.notificationSms = true AND up.deletedAt IS NULL")
    Page<UserPreference> findWithSmsNotificationsEnabled(Pageable pageable);

    /**
     * Find users with push notifications enabled
     */
    @Query("SELECT up FROM UserPreference up WHERE up.notificationPush = true AND up.deletedAt IS NULL")
    Page<UserPreference> findWithPushNotificationsEnabled(Pageable pageable);

    /**
     * Find users with two-factor authentication enabled
     */
    @Query("SELECT up FROM UserPreference up WHERE up.twoFactorEnabled = true AND up.deletedAt IS NULL")
    Page<UserPreference> findWithTwoFactorEnabled(Pageable pageable);

    /**
     * Find users with biometric authentication enabled
     */
    @Query("SELECT up FROM UserPreference up WHERE up.biometricEnabled = true AND up.deletedAt IS NULL")
    Page<UserPreference> findWithBiometricEnabled(Pageable pageable);

    /**
     * Check if preference exists for user
     */
    @Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END FROM UserPreference up " +
           "WHERE up.userId = :userId AND up.deletedAt IS NULL")
    boolean existsByUserId(@Param("userId") UUID userId);

    /**
     * Count preferences by language
     */
    @Query("SELECT COUNT(up) FROM UserPreference up WHERE up.language = :language AND up.deletedAt IS NULL")
    Long countByLanguage(@Param("language") String language);

    /**
     * Count preferences with two-factor enabled
     */
    @Query("SELECT COUNT(up) FROM UserPreference up WHERE up.twoFactorEnabled = true AND up.deletedAt IS NULL")
    Long countWithTwoFactorEnabled();

    /**
     * Soft delete preference
     */
    @Modifying
    @Query("UPDATE UserPreference up SET up.deletedAt = :deletedAt, up.updatedAt = :updatedAt " +
           "WHERE up.userId = :userId")
    void softDeleteByUserId(@Param("userId") UUID userId, 
                            @Param("deletedAt") Instant deletedAt, 
                            @Param("updatedAt") Instant updatedAt);

    /**
     * Update notification settings
     */
    @Modifying
    @Query("UPDATE UserPreference up SET up.notificationEmail = :email, " +
           "up.notificationSms = :sms, up.notificationPush = :push, " +
           "up.updatedAt = :updatedAt WHERE up.userId = :userId")
    void updateNotificationSettings(@Param("userId") UUID userId, 
                                     @Param("email") Boolean email, 
                                     @Param("sms") Boolean sms, 
                                     @Param("push") Boolean push, 
                                     @Param("updatedAt") Instant updatedAt);

    /**
     * Update language preference
     */
    @Modifying
    @Query("UPDATE UserPreference up SET up.language = :language, up.updatedAt = :updatedAt " +
           "WHERE up.userId = :userId")
    void updateLanguage(@Param("userId") UUID userId, 
                        @Param("language") String language, 
                        @Param("updatedAt") Instant updatedAt);

    /**
     * Update timezone preference
     */
    @Modifying
    @Query("UPDATE UserPreference up SET up.timezone = :timezone, up.updatedAt = :updatedAt " +
           "WHERE up.userId = :userId")
    void updateTimezone(@Param("userId") UUID userId, 
                        @Param("timezone") String timezone, 
                        @Param("updatedAt") Instant updatedAt);

    /**
     * Enable two-factor authentication
     */
    @Modifying
    @Query("UPDATE UserPreference up SET up.twoFactorEnabled = :enabled, up.updatedAt = :updatedAt " +
           "WHERE up.userId = :userId")
    void updateTwoFactorEnabled(@Param("userId") UUID userId, 
                                 @Param("enabled") Boolean enabled, 
                                 @Param("updatedAt") Instant updatedAt);
}
