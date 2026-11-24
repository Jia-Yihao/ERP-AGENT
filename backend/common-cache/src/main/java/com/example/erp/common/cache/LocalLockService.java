package com.example.erp.common.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@ConditionalOnMissingBean(name = "redisLockService")
public class LocalLockService implements LockService {
    private final Map<String, Instant> locks = new ConcurrentHashMap<>();
    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(String key, Duration ttl) {
        Instant now = Instant.now();
        Instant existing = locks.compute(key, (k, v) -> v == null || v.isBefore(now) ? now.plus(ttl) : v);
        boolean acquired = existing.equals(now.plus(ttl));
        if (acquired) tokens.put(key, UUID.randomUUID().toString());
        return acquired;
    }

    @Override
    public void unlock(String key, String token) {
        if (token != null && token.equals(tokens.get(key))) {
            locks.remove(key);
            tokens.remove(key);
        }
    }

    @Override
    public <T> T withLock(String key, Duration ttl, Supplier<T> action) {
        String token = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Instant existing = locks.compute(key, (k, v) -> v == null || v.isBefore(now) ? now.plus(ttl) : v);
        if (!existing.equals(now.plus(ttl))) return null;
        tokens.put(key, token);
        try { return action.get(); }
        finally { unlock(key, token); }
    }
}
