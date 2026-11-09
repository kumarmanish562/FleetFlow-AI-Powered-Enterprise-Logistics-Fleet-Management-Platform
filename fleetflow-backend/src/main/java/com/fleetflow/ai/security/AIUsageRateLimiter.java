package com.fleetflow.ai.security;

import com.fleetflow.ai.exception.AIRateLimitException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@ConditionalOnProperty(prefix = "fleetflow.ai", name = "enabled", havingValue = "true")
public class AIUsageRateLimiter {
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>("""
            redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[1])
            local count = redis.call('ZCARD', KEYS[1])
            if count >= tonumber(ARGV[3]) then return 0 end
            redis.call('ZADD', KEYS[1], ARGV[2], ARGV[4])
            redis.call('EXPIRE', KEYS[1], 120)
            return count + 1
            """, Long.class);
    private final StringRedisTemplate redis;

    public AIUsageRateLimiter(StringRedisTemplate redis) { this.redis = redis; }

    public void check(String user, int limit) {
        long now = System.currentTimeMillis();
        Long count = redis.execute(SCRIPT, List.of("fleetflow:ai:rate:" + digest(user)),
                Long.toString(now - 60_000L), Long.toString(now), Integer.toString(limit), UUID.randomUUID().toString());
        if (count == null) throw new IllegalStateException("AI rate limiter unavailable");
        if (count == 0) throw new AIRateLimitException("You have reached your AI request limit. Try again later.");
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
