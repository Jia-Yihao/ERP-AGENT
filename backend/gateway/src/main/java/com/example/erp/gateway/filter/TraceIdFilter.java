package com.example.erp.gateway.filter;

import com.example.erp.common.core.Constants;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class TraceIdFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(Constants.TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) traceId = UUID.randomUUID().toString().replace("-", "");
        String tid = traceId;
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(h -> h.set(Constants.TRACE_ID_HEADER, tid)).build();
        exchange.getResponse().getHeaders().set(Constants.TRACE_ID_HEADER, tid);
        MDC.put(Constants.TRACE_ID_MDC_KEY, tid);
        return chain.filter(exchange.mutate().request(request).build())
                .doFinally(s -> MDC.remove(Constants.TRACE_ID_MDC_KEY));
    }

    @Override
    public int getOrder() { return -200; }
}
