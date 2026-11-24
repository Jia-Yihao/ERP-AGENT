package com.example.erp.common.cache;

import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

@Aspect
@Component
public class IdempotentAspect {
    private final CacheService cache;

    public IdempotentAspect(CacheService cache) { this.cache = cache; }

    @Around("@annotation(com.example.erp.common.cache.Idempotent)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Idempotent anno = method.getAnnotation(Idempotent.class);
        StringBuilder key = new StringBuilder(anno.value()).append(":");
        for (Object arg : pjp.getArgs()) {
            key.append(arg == null ? "null" : String.valueOf(arg).hashCode()).append(":");
        }
        String cacheKey = key.toString();
        if (cache.get(cacheKey, String.class).isPresent()) {
            throw new BizException(ErrorCode.IDEMPOTENT_REJECTED);
        }
        cache.put(cacheKey, "1", Duration.ofSeconds(anno.ttlSeconds()));
        try { return pjp.proceed(); }
        catch (Throwable e) { cache.evict(cacheKey); throw e; }
    }
}
