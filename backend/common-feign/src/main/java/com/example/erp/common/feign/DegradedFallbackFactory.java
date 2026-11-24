package com.example.erp.common.feign;

import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 降级工厂基类：下游不可用时记录原因并返回统一 503 响应。
 * 子类覆写 create 返回具体接口代理即可复用日志与响应封装。
 */
public abstract class DegradedFallbackFactory<T> implements FallbackFactory<T> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    protected R<Void> degraded(Throwable cause) {
        log.warn("feign degraded: {}", cause == null ? "unknown" : cause.getMessage());
        return R.fail(ErrorCode.SERVICE_UNAVAILABLE);
    }
}
