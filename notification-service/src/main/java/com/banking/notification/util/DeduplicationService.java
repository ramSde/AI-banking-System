package com.banking.notification.util;

import com.banking.notification.config.NotificationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
public class DeduplicationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationProperties properties;

    public DeduplicationService(RedisTemplate<String, Object> redisTemplate, NotificationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public boolean isDuplicate(UUID userId, String templateCode, String recipient) {
        String key = buildKey(userId, templateCode, recipient);
        Boolean exists = redisTemplate.hasKey(key);

        if (Boolean.TRUE.equals(exists)) {
            log.info("Duplicate notification detected for userId: {}, templateCode: {}, recipient: {}",
                    userId, templateCode, recipient);
            return true;
        }

        redisTemplate.opsForValue().set(key, "1",
                Duration.ofSeconds(properties.getDeduplication().getWindowSeconds()));
        return false;
    }

    private String buildKey(UUID userId, String templateCode, String recipient) {
        return String.format("notification:dedup:%s:%s:%s", userId, templateCode, recipient);
    }
}
