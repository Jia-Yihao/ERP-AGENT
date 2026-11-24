package com.example.erp.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@ConditionalOnMissingBean(name = "redisCacheService")
public class LocalCacheService implements CacheService {
    private final Cache<String, Object> cache = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object v = cache.getIfPresent(key);
        return v == null ? Optional.empty() : Optional.of(type.cast(v));
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        if (value != null) cache.put(key, value);
    }

    @Override
    public void evict(String key) { cache.invalidate(key); }

    @Override
    public void evictByPrefix(String prefix) {
        cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    @Override
    public <T> T getOrLoad(String key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Object v = cache.get(key, k -> loader.get());
        return v == null ? null : type.cast(v);
    }
}
