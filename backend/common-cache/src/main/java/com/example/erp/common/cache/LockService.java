package com.example.erp.common.cache;

import java.time.Duration;
import java.util.function.Supplier;

public interface LockService {
    boolean tryLock(String key, Duration ttl);
    void unlock(String key, String token);
    <T> T withLock(String key, Duration ttl, Supplier<T> action);
}
