package com.testing.bookingsys.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingLockService {

    private final StringRedisTemplate redisTemplate;

    @Value("${app.booking.lock-ttl-seconds:10}")
    private long lockTtlSeconds;

    public String acquire(String key) {
        String token = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                redisKey(key),
                token,
                Duration.ofSeconds(lockTtlSeconds)
        );

        if (Boolean.TRUE.equals(locked)) {
            return token;
        }

        throw new IllegalStateException("Booking is being processed. Please retry.");
    }

    public void release(String key, String token) {
        String redisKey = redisKey(key);
        String currentToken = redisTemplate.opsForValue().get(redisKey);
        if (token.equals(currentToken)) {
            redisTemplate.delete(redisKey);
        }
    }

    private String redisKey(String key) {
        return "booking-lock:" + key;
    }
}
