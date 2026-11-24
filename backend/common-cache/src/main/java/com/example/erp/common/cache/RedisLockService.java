package com.example.erp.common.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(prefix = "erp.cache", name = "type", havingValue = "redis")
public class RedisLockService implements LockService {
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private final StringRedisTemplate redis;

    public RedisLockService(StringRedisTemplate redis) { this.redis = redis; }

    @Override
    public boolean tryLock(String key, Duration ttl) {
        String token = UUID.randomUUID().toString();
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, token, ttl));
    }

    @Override
    public void unlock(String key, String token) {
        if (token != null) {
            org.springframework.data.redis.core.script.DefaultRedisScript<Long> script =
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);
            redis.execute(script, java.util.List.of(key), token);
        }
    }

    @Override
    public <T> T withLock(String key, Duration ttl, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        boolean locked = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, token, ttl));
        if (!locked) return null;
        try { return action.get(); }
        finally { unlock(key, token); }
    }
}
