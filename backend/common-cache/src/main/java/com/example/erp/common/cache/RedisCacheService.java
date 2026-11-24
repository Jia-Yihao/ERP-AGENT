package com.example.erp.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Component
@ConditionalOnProperty(prefix = "erp.cache", name = "type", havingValue = "redis")
public class RedisCacheService implements CacheService {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisCacheService(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String json = redis.opsForValue().get(key);
        if (json == null) return Optional.empty();
        try { return Optional.of(mapper.readValue(json, type)); }
        catch (Exception e) { redis.delete(key); return Optional.empty(); }
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        if (value == null) return;
        try { redis.opsForValue().set(key, mapper.writeValueAsString(value), ttl); }
        catch (Exception ignored) { }
    }

    @Override
    public void evict(String key) { redis.delete(key); }

    @Override
    public void evictByPrefix(String prefix) {
        Set<String> keys = redis.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }

    @Override
    public <T> T getOrLoad(String key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Optional<T> hit = get(key, type);
        if (hit.isPresent()) return hit.get();
        T value = loader.get();
        if (value != null) put(key, value, ttl);
        return value;
    }
}
