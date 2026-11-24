package com.example.erp.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/** 缓存与分布式锁装配：erp.cache.type=redis 走 Redis 实现，其余场景退化为进程内实现。 */
@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(prefix = "erp.cache", name = "type", havingValue = "redis")
    public CacheService redisCacheService(StringRedisTemplate redis, ObjectMapper mapper) {
        return new RedisCacheService(redis, mapper);
    }

    @Bean
    @ConditionalOnMissingBean(CacheService.class)
    public CacheService localCacheService() {
        return new LocalCacheService();
    }

    @Bean
    @ConditionalOnProperty(prefix = "erp.cache", name = "type", havingValue = "redis")
    public LockService redisLockService(StringRedisTemplate redis) {
        return new RedisLockService(redis);
    }

    @Bean
    @ConditionalOnMissingBean(LockService.class)
    public LockService localLockService() {
        return new LocalLockService();
    }
}
