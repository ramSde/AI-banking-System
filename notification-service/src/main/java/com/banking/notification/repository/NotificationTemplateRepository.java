package com.banking.notification.repository;

import com.banking.notification.domain.NotificationChannel;
import com.banking.notification.domain.NotificationTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    @Query("SELECT t FROM NotificationTemplate t WHERE t.templateCode = :templateCode AND t.deletedAt IS NULL")
    Optional<NotificationTemplate> findByTemplateCode(@Param("templateCode") String templateCode);

    @Query("SELECT t FROM NotificationTemplate t WHERE t.templateCode = :templateCode AND t.channel = :channel AND t.active = true AND t.deletedAt IS NULL")
    Optional<NotificationTemplate> findByTemplateCodeAndChannel(
            @Param("templateCode") String templateCode,
            @Param("channel") NotificationChannel channel
    );

    @Query("SELECT t FROM NotificationTemplate t WHERE t.channel = :channel AND t.active = true AND t.deletedAt IS NULL")
    Page<NotificationTemplate> findByChannel(@Param("channel") NotificationChannel channel, Pageable pageable);

    @Query("SELECT t FROM NotificationTemplate t WHERE t.active = :active AND t.deletedAt IS NULL")
    Page<NotificationTemplate> findByActive(@Param("active") Boolean active, Pageable pageable);

    @Query("SELECT t FROM NotificationTemplate t WHERE t.deletedAt IS NULL")
    Page<NotificationTemplate> findAllActive(Pageable pageable);
}
