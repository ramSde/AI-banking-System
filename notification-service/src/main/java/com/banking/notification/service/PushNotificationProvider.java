package com.banking.notification.service;

import com.banking.notification.config.NotificationProperties;
import com.banking.notification.exception.NotificationDeliveryException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
public class PushNotificationProvider {

    private final NotificationProperties properties;
    private final String credentialsPath;
    private final String projectId;

    public PushNotificationProvider(
            NotificationProperties properties,
            @Value("${firebase.credentials-path:}") String credentialsPath,
            @Value("${firebase.project-id:}") String projectId
    ) {
        this.properties = properties;
        this.credentialsPath = credentialsPath;
        this.projectId = projectId;
    }

    @PostConstruct
    public void initialize() {
        if (properties.getPush().getEnabled() && credentialsPath != null && !credentialsPath.isEmpty()) {
            try {
                FileInputStream serviceAccount = new FileInputStream(credentialsPath);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase initialized successfully");
                }
            } catch (IOException e) {
                log.error("Failed to initialize Firebase", e);
            }
        }
    }

    @CircuitBreaker(name = "pushProvider", fallbackMethod = "sendPushFallback")
    @Retry(name = "notificationRetry")
    public void sendPush(String deviceToken, String title, String body) {
        if (!properties.getPush().getEnabled()) {
            log.warn("Push notifications are disabled");
            throw new NotificationDeliveryException("Push notifications are disabled");
        }

        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase not initialized");
            throw new NotificationDeliveryException("Push notification provider not configured");
        }

        try {
            Message message = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification sent successfully to device: {} with response: {}", deviceToken, response);
        } catch (Exception e) {
            log.error("Failed to send push notification to device: {}", deviceToken, e);
            throw new NotificationDeliveryException("Failed to send push notification", e);
        }
    }

    private void sendPushFallback(String deviceToken, String title, String body, Exception ex) {
        log.error("Push notification circuit breaker activated for device: {} - {}", deviceToken, ex.getMessage());
        throw new NotificationDeliveryException("Push notification service is currently unavailable");
    }
}
