package com.example.erp.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class LocalCacheService implements CacheService {
    private final Cache<String, Object> cache = Caffeine.newBuilder()
            .maximumSize(2000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object v = cache.getIfPresent(key);
        if (v == null) return Optional.empty();
        if (NULL_MARKER.equals(v)) return Optional.empty();
        return Optional.of(type.cast(v));
    }

    @Override
    public <T> void put(String key, T value, Duration ttl) {
        cache.put(key, value == null ? NULL_MARKER : value);
    }

    @Override
    public void evict(String key) { cache.invalidate(key); }

    @Override
    public void evictByPrefix(String prefix) {
        cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    /** 空值缓存防穿透；Caffeine 的原子 get 保证同 key 只加载一次，防击穿。 */
    @Override
    public <T> T getOrLoad(String key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Object v = cache.get(key, k -> {
            T loaded = loader.get();
            return loaded == null ? NULL_MARKER : loaded;
        });
        return NULL_MARKER.equals(v) ? null : type.cast(v);
    }
}
