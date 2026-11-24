package com.example.erp.auth.service;

import com.example.erp.common.core.Constants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注销令牌黑名单。Redis 不可达时降级到进程内 Map，重启后失效。
 */
@Component
public class TokenBlacklist {

    private final ObjectProvider<StringRedisTemplate> redisTemplate;
    private final ConcurrentHashMap<String, Long> localBlacklist = new ConcurrentHashMap<>();
    private volatile boolean redisAvailable = true;

    public TokenBlacklist(ObjectProvider<StringRedisTemplate> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void add(String jti, long ttlSeconds) {
        if (jti == null || jti.isBlank() || ttlSeconds <= 0) {
            return;
        }
        String key = Constants.BLACKLIST_PREFIX + jti;
        if (redisAvailable) {
            try {
                StringRedisTemplate template = redisTemplate.getIfAvailable();
                if (template != null) {
                    template.opsForValue().set(key, "1", Duration.ofSeconds(ttlSeconds));
                    return;
                }
            } catch (RedisConnectionFailureException | IllegalStateException e) {
                redisAvailable = false;
            }
        }
        localBlacklist.put(key, System.currentTimeMillis() + ttlSeconds * 1000);
    }

    public boolean contains(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        String key = Constants.BLACKLIST_PREFIX + jti;
        Long localExpiry = localBlacklist.get(key);
        if (localExpiry != null) {
            if (localExpiry > System.currentTimeMillis()) {
                return true;
            }
            localBlacklist.remove(key);
        }
        if (!redisAvailable) {
            return false;
        }
        try {
            StringRedisTemplate template = redisTemplate.getIfAvailable();
            return template != null && Boolean.TRUE.equals(template.hasKey(key));
        } catch (RedisConnectionFailureException | IllegalStateException e) {
            redisAvailable = false;
            return false;
        }
    }
}
