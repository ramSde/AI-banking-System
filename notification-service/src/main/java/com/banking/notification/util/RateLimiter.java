package com.banking.notification.util;

import com.banking.notification.config.NotificationProperties;
import com.banking.notification.domain.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
public class RateLimiter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationProperties properties;

    public RateLimiter(RedisTemplate<String, Object> redisTemplate, NotificationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public boolean isAllowed(UUID userId, NotificationChannel channel) {
        String key = buildKey(userId, channel);
        Integer limit = getLimit(channel);

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            count = 0L;
        }

        if (count == 1) {
            redisTemplate.expire(key, Duration.ofHours(1));
        }

        boolean allowed = count <= limit;
        if (!allowed) {
            log.warn("Rate limit exceeded for userId: {}, channel: {}, count: {}, limit: {}",
                    userId, channel, count, limit);
        }

        return allowed;
    }

    private String buildKey(UUID userId, NotificationChannel channel) {
        return String.format("notification:rate-limit:%s:%s", userId, channel.name().toLowerCase());
    }

    private Integer getLimit(NotificationChannel channel) {
        return switch (channel) {
            case EMAIL -> properties.getRateLimit().getEmailPerUserPerHour();
            case SMS -> properties.getRateLimit().getSmsPerUserPerHour();
            case PUSH -> properties.getRateLimit().getPushPerUserPerHour();
        };
    }
}
