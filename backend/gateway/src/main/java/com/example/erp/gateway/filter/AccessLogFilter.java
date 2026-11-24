package com.example.erp.gateway.filter;

import com.example.erp.common.core.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
            exchange.getResponse().getHeaders().set(Constants.TRACE_ID_HEADER, traceId == null ? "" : traceId);
        });
    }

    @Override
    public int getOrder() { return -180; }
}
