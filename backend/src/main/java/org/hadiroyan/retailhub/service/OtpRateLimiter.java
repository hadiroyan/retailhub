package org.hadiroyan.retailhub.service;

import java.time.Duration;
import java.util.UUID;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OtpRateLimiter {

    private static final String KEY_PREFIX = "otp:resend:count:";
    private static final Duration WINDOW = Duration.ofHours(1);

    private final ValueCommands<String, String> valueCommands;
    private final KeyCommands<String> keyCommands;

    public OtpRateLimiter(RedisDataSource redisDataSource) {
        this.valueCommands = redisDataSource.value(String.class);
        this.keyCommands = redisDataSource.key();
    }

    /**
     * Increments the counter for the given user. 
     * Called every time an OTP is actually generated and sent
     */
    public long increment(UUID userId) {
        String key = KEY_PREFIX + userId;
        long count = valueCommands.incr(key);
        if (count == 1) {
            keyCommands.expire(key, WINDOW);
        }
        return count;
    }

    /**
     * Reads the current count without incrementing it. Used by
     * {@code resendOtp} to check the limit BEFORE generating a new OTP
     */
    public long peek(UUID userId) {
        String value = valueCommands.get(KEY_PREFIX + userId);
        return value == null ? 0 : Long.parseLong(value);
    }

    /**
     * Clears the counter for a user. Mainly useful in tests
     */
    public void reset(UUID userId) {
        keyCommands.del(KEY_PREFIX + userId);
    }
}
