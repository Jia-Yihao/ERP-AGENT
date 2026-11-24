package com.example.erp.gateway.filter;

import com.example.erp.common.core.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 访问日志：记录 traceId、方法、路径、下游状态与耗时。
 * 响应头在 doFinally 阶段已只读，X-Trace-Id 由 TraceIdFilter 注入请求、
 * CorsFilter 暴露，此处只记录不改写响应头。
 */
@Slf4j
@Component
public class AccessLogFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        return chain.filter(exchange).doFinally(signal -> {
            long cost = System.currentTimeMillis() - start;
            int status = exchange.getResponse().getStatusCode() == null
                    ? 0 : exchange.getResponse().getStatusCode().value();
            String traceId = exchange.getRequest().getHeaders().getFirst(Constants.TRACE_ID_HEADER);
            log.info("traceId={} {} {} status={} cost={}ms", traceId, method, path, status, cost);
        });
    }

    @Override
    public int getOrder() { return -180; }
}
