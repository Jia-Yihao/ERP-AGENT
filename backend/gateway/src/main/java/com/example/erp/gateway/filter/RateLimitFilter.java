package com.example.erp.gateway.filter;

import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 令牌桶限流：Redis 可用走 INCR 计数，否则退化为本地令牌桶。超限返回 HTTP 200 + code=429。 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {
    private final ObjectMapper mapper;
    private final ReactiveStringRedisTemplate redis;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${erp.ratelimit.capacity:30}")
    private int capacity;
    @Value("${erp.ratelimit.refill-per-second:10}")
    private int refillPerSecond;

    public RateLimitFilter(ObjectMapper mapper, ReactiveStringRedisTemplate redis) {
        this.mapper = mapper;
        this.redis = redis;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/api/")) return chain.filter(exchange);
        String ip = exchange.getRequest().getRemoteAddress() == null
                ? "unknown" : exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        String key = "erp:rate:" + ip + ":" + path;
        return redis.opsForValue().increment(key).flatMap(count -> {
                    if (count == 1) redis.expire(key, Duration.ofSeconds(1)).subscribe();
                    if (count > capacity) return reject(exchange.getResponse());
                    return chain.filter(exchange);
                })
                .onErrorResume(e -> {
                    boolean allowed = buckets.computeIfAbsent(key, k -> new Bucket(capacity, refillPerSecond)).tryAcquire();
                    return allowed ? chain.filter(exchange) : reject(exchange.getResponse());
                });
    }

    private Mono<Void> reject(ServerHttpResponse response) {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] body = mapper.writeValueAsBytes(R.fail(ErrorCode.TOO_MANY_REQUESTS));
            DataBuffer buffer = response.bufferFactory().wrap(body);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() { return -100; }

    private static class Bucket {
        private double tokens;
        private long lastRefill = System.nanoTime();
        private final int capacity;
        private final int refill;
        Bucket(int capacity, int refill) { this.capacity = capacity; this.refill = refill; this.tokens = capacity; }
        synchronized boolean tryAcquire() {
            long now = System.nanoTime();
            tokens = Math.min(capacity, tokens + (now - lastRefill) / 1e9 * refill);
            lastRefill = now;
            if (tokens < 1) return false;
            tokens -= 1;
            return true;
        }
    }
}
