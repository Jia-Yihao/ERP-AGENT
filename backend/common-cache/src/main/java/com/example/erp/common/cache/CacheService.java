package com.example.erp.common.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

public interface CacheService {
    <T> Optional<T> get(String key, Class<T> type);
    <T> void put(String key, T value, Duration ttl);
    void evict(String key);
    void evictByPrefix(String prefix);
    <T> T getOrLoad(String key, Class<T> type, Duration ttl, Supplier<T> loader);

    /** 空值哨兵：缓存未命中结果，防止缓存穿透。 */
    String NULL_MARKER = "__erp_null__";

    /** 在基础 TTL 上叠加最多 10% 的随机抖动，防止批量 key 同时过期引发雪崩。 */
    default Duration jitter(Duration ttl) {
        long spread = Math.max(1L, ttl.toMillis() / 10);
        return ttl.plusMillis((long) (Math.random() * spread));
    }
}
