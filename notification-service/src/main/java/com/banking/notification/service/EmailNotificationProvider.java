package com.banking.notification.service;

import com.banking.notification.config.NotificationProperties;
import com.banking.notification.exception.NotificationDeliveryException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationProvider {

    private final JavaMailSender mailSender;
    private final NotificationProperties properties;

    public EmailNotificationProvider(JavaMailSender mailSender, NotificationProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @CircuitBreaker(name = "emailProvider", fallbackMethod = "sendEmailFallback")
    @Retry(name = "notificationRetry")
    public void sendEmail(String recipient, String subject, String body) {
        if (!properties.getEmail().getEnabled()) {
            log.warn("Email notifications are disabled");
            throw new NotificationDeliveryException("Email notifications are disabled");
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(properties.getEmail().getFrom(), properties.getEmail().getFromName());
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", recipient);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", recipient, e);
            throw new NotificationDeliveryException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to: {}", recipient, e);
            throw new NotificationDeliveryException("Unexpected error sending email", e);
        }
    }

    private void sendEmailFallback(String recipient, String subject, String body, Exception ex) {
        log.error("Email circuit breaker activated for recipient: {} - {}", recipient, ex.getMessage());
        throw new NotificationDeliveryException("Email service is currently unavailable");
    }
}
