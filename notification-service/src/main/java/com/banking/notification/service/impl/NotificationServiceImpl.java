package com.banking.notification.service.impl;

import com.banking.notification.domain.NotificationChannel;
import com.banking.notification.domain.NotificationHistory;
import com.banking.notification.domain.NotificationStatus;
import com.banking.notification.domain.NotificationTemplate;
import com.banking.notification.dto.NotificationHistoryResponse;
import com.banking.notification.dto.NotificationQueryRequest;
import com.banking.notification.dto.NotificationSendRequest;
import com.banking.notification.dto.NotificationStatsResponse;
import com.banking.notification.exception.NotificationDeliveryException;
import com.banking.notification.exception.NotificationException;
import com.banking.notification.exception.TemplateNotFoundException;
import com.banking.notification.mapper.NotificationMapper;
import com.banking.notification.repository.NotificationHistoryRepository;
import com.banking.notification.repository.NotificationTemplateRepository;
import com.banking.notification.service.EmailNotificationProvider;
import com.banking.notification.service.NotificationService;
import com.banking.notification.service.PushNotificationProvider;
import com.banking.notification.service.SmsNotificationProvider;
import com.banking.notification.util.DeduplicationService;
import com.banking.notification.util.RateLimiter;
import com.banking.notification.util.TemplateRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationHistoryRepository historyRepository;
    private final NotificationTemplateRepository templateRepository;
    private final EmailNotificationProvider emailProvider;
    private final SmsNotificationProvider smsProvider;
    private final PushNotificationProvider pushProvider;
    private final TemplateRenderer templateRenderer;
    private final RateLimiter rateLimiter;
    private final DeduplicationService deduplicationService;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(
            NotificationHistoryRepository historyRepository,
            NotificationTemplateRepository templateRepository,
            EmailNotificationProvider emailProvider,
            SmsNotificationProvider smsProvider,
            PushNotificationProvider pushProvider,
            TemplateRenderer templateRenderer,
            RateLimiter rateLimiter,
            DeduplicationService deduplicationService,
            NotificationMapper notificationMapper
    ) {
        this.historyRepository = historyRepository;
        this.templateRepository = templateRepository;
        this.emailProvider = emailProvider;
        this.smsProvider = smsProvider;
        this.pushProvider = pushProvider;
        this.templateRenderer = templateRenderer;
        this.rateLimiter = rateLimiter;
        this.deduplicationService = deduplicationService;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Async("notificationExecutor")
    public void sendNotification(NotificationSendRequest request, UUID idempotencyKey, UUID traceId) {
        log.info("Processing notification request for userId: {}, templateCode: {}, channel: {}, traceId: {}",
                request.userId(), request.templateCode(), request.channel(), traceId);

        Optional<NotificationHistory> existing = historyRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Duplicate notification request detected with idempotencyKey: {}", idempotencyKey);
            return;
        }

        if (deduplicationService.isDuplicate(request.userId(), request.templateCode(), request.recipient())) {
            log.info("Notification deduplicated for userId: {}, templateCode: {}", request.userId(), request.templateCode());
            saveHistory(request, idempotencyKey, traceId, NotificationStatus.DEDUPLICATED, null, null, "Duplicate notification within deduplication window");
            return;
        }

        if (!rateLimiter.isAllowed(request.userId(), request.channel())) {
            log.warn("Rate limit exceeded for userId: {}, channel: {}", request.userId(), request.channel());
            saveHistory(request, idempotencyKey, traceId, NotificationStatus.RATE_LIMITED, null, null, "Rate limit exceeded");
            return;
        }

        NotificationTemplate template = templateRepository.findByTemplateCodeAndChannel(
                        request.templateCode(), request.channel())
                .orElseThrow(() -> new TemplateNotFoundException(
                        "Template not found with code: " + request.templateCode() + " and channel: " + request.channel()));

        String subject = template.getSubject() != null ? templateRenderer.render(template.getSubject(), request.variables()) : null;
        String body = templateRenderer.render(template.getBodyTemplate(), request.variables());

        try {
            dispatchNotification(request.channel(), request.recipient(), subject, body);
            saveHistory(request, idempotencyKey, traceId, NotificationStatus.SENT, subject, body, null);
            log.info("Notification sent successfully for userId: {}, channel: {}", request.userId(), request.channel());
        } catch (NotificationDeliveryException e) {
            log.error("Failed to send notification for userId: {}, channel: {}", request.userId(), request.channel(), e);
            saveHistory(request, idempotencyKey, traceId, NotificationStatus.FAILED, subject, body, e.getMessage());
        }
    }

    private void dispatchNotification(NotificationChannel channel, String recipient, String subject, String body) {
        switch (channel) {
            case EMAIL -> emailProvider.sendEmail(recipient, subject, body);
            case SMS -> smsProvider.sendSms(recipient, body);
            case PUSH -> pushProvider.sendPush(recipient, subject, body);
            default -> throw new NotificationException("Unsupported notification channel: " + channel);
        }
    }

    private void saveHistory(NotificationSendRequest request, UUID idempotencyKey, UUID traceId,
                              NotificationStatus status, String subject, String body, String errorMessage) {
        NotificationHistory history = NotificationHistory.builder()
                .userId(request.userId())
                .templateCode(request.templateCode())
                .channel(request.channel())
                .recipient(request.recipient())
                .subject(subject)
                .body(body)
                .status(status)
                .errorMessage(errorMessage)
                .retryCount(0)
                .idempotencyKey(idempotencyKey)
                .correlationId(request.correlationId() != null ? request.correlationId() : UUID.randomUUID())
                .traceId(traceId)
                .sentAt(status == NotificationStatus.SENT ? Instant.now() : null)
                .build();

        historyRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationHistoryResponse> getNotificationHistory(NotificationQueryRequest request) {
        Pageable pageable = PageRequest.of(
                request.page() != null ? request.page() : 0,
                request.size() != null ? request.size() : 20
        );

        Page<NotificationHistory> historyPage;

        if (request.channel() != null && request.status() != null) {
            historyPage = historyRepository.findByUserIdAndChannel(request.userId(), request.channel(), pageable);
        } else if (request.channel() != null) {
            historyPage = historyRepository.findByUserIdAndChannel(request.userId(), request.channel(), pageable);
        } else if (request.status() != null) {
            historyPage = historyRepository.findByUserIdAndStatus(request.userId(), request.status(), pageable);
        } else if (request.startDate() != null && request.endDate() != null) {
            historyPage = historyRepository.findByUserIdAndDateRange(
                    request.userId(), request.startDate(), request.endDate(), pageable);
        } else {
            historyPage = historyRepository.findByUserId(request.userId(), pageable);
        }

        return historyPage.map(notificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationHistoryResponse getNotificationById(UUID id) {
        NotificationHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification not found with ID: " + id));

        return notificationMapper.toResponse(history);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatsResponse getNotificationStats(UUID userId) {
        Long totalSent = historyRepository.countByUserIdAndStatus(userId, NotificationStatus.SENT);
        Long totalFailed = historyRepository.countByUserIdAndStatus(userId, NotificationStatus.FAILED);
        Long totalPending = historyRepository.countByUserIdAndStatus(userId, NotificationStatus.PENDING);

        Map<String, Long> byChannel = new HashMap<>();
        for (NotificationChannel channel : NotificationChannel.values()) {
            Long count = historyRepository.countByUserIdAndChannelSince(
                    userId, channel, Instant.now().minusSeconds(86400 * 30));
            byChannel.put(channel.name(), count);
        }

        Map<String, Long> byStatus = new HashMap<>();
        for (NotificationStatus status : NotificationStatus.values()) {
            Long count = historyRepository.countByUserIdAndStatus(userId, status);
            byStatus.put(status.name(), count);
        }

        return new NotificationStatsResponse(totalSent, totalFailed, totalPending, byChannel, byStatus);
    }
}
