package com.banking.notification.service;

import com.banking.notification.config.NotificationProperties;
import com.banking.notification.exception.NotificationDeliveryException;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsNotificationProvider {

    private final NotificationProperties properties;
    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    public SmsNotificationProvider(
            NotificationProperties properties,
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.from-number}") String fromNumber
    ) {
        this.properties = properties;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;

        if (properties.getSms().getEnabled() && accountSid != null && !accountSid.isEmpty()) {
            Twilio.init(accountSid, authToken);
        }
    }

    @CircuitBreaker(name = "smsProvider", fallbackMethod = "sendSmsFallback")
    @Retry(name = "notificationRetry")
    public void sendSms(String recipient, String body) {
        if (!properties.getSms().getEnabled()) {
            log.warn("SMS notifications are disabled");
            throw new NotificationDeliveryException("SMS notifications are disabled");
        }

        if (accountSid == null || accountSid.isEmpty()) {
            log.warn("Twilio credentials not configured");
            throw new NotificationDeliveryException("SMS provider not configured");
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(recipient),
                    new PhoneNumber(fromNumber),
                    body
            ).create();

            log.info("SMS sent successfully to: {} with SID: {}", recipient, message.getSid());
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", recipient, e);
            throw new NotificationDeliveryException("Failed to send SMS", e);
        }
    }

    private void sendSmsFallback(String recipient, String body, Exception ex) {
        log.error("SMS circuit breaker activated for recipient: {} - {}", recipient, ex.getMessage());
        throw new NotificationDeliveryException("SMS service is currently unavailable");
    }
}
