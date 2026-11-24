package com.example.erp.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(prefix = "erp.cache", name = "type", havingValue = "redis")
public class RedisCacheService implements CacheService {
    private static final Duration NULL_TTL = Duration.ofMinutes(2);
    private static final Duration LOCK_TTL = Duration.ofSeconds(5);
    private static final Duration SPIN = Duration.ofMillis(20);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisCacheService(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String json = redis.opsForValue().get(key);
        if (json == null || NULL_MARKER.equals(json)) return Optional.empty();
        try { return Optional.of(mapper.readValue(json, type)); }
        catch (Exception e) { redis.delete(key); return Optional.empty(); }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        if (value == null) {
            redis.opsForValue().set(key, NULL_MARKER, NULL_TTL);
            return;
        }
        try { redis.opsForValue().set(key, mapper.writeValueAsString(value), jitter(ttl)); }
        catch (Exception ignored) { }
    }

    @Override
    public void evict(String key) { redis.delete(key); }

    @Override
    public void evictByPrefix(String prefix) {
        Set<String> keys = redis.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }

    /**
     * 空值缓存防穿透；SET NX 互斥锁防击穿；TTL 抖动防雪崩。
     * 锁失败时短暂自旋后回读一次，仍空才降级为loader直读，避免死锁。
     */
    @Override
    public <T> T getOrLoad(String key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Optional<T> hit = get(key, type);
        if (hit.isPresent()) return hit.get();
        String json = redis.opsForValue().get(key);
        if (NULL_MARKER.equals(json)) return null;

        String lockKey = key + ":lock";
        String token = UUID.randomUUID().toString();
        boolean locked = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lockKey, token, LOCK_TTL));
        if (!locked) {
            for (int i = 0; i < 25; i++) {
                sleep(SPIN.toMillis());
                Optional<T> reread = get(key, type);
                if (reread.isPresent()) return reread.get();
                if (NULL_MARKER.equals(redis.opsForValue().get(key))) return null;
            }
            return loader.get();
        }
        try {
            T value = loader.get();
            put(key, value, ttl);
            return value;
        } finally {
            unlock(lockKey, token);
        }
    }

    private void unlock(String lockKey, String token) {
        String scriptText = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        try {
            org.springframework.data.redis.core.script.DefaultRedisScript<Long> script =
                    new org.springframework.data.redis.core.script.DefaultRedisScript<>();
            script.setScriptText(scriptText);
            script.setResultType(Long.class);
            redis.execute(script, java.util.List.of(lockKey), token);
        } catch (Exception ignored) { }
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
