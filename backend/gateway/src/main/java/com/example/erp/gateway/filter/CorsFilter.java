package com.example.erp.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorsFilter implements GlobalFilter, Ordered {
    private static final CorsConfiguration CONFIG = new CorsConfiguration();
    static {
        CONFIG.addAllowedOriginPattern("*");
        CONFIG.addAllowedHeader("*");
        CONFIG.addAllowedMethod("*");
        CONFIG.setAllowCredentials(true);
        CONFIG.addExposedHeader("X-Trace-Id");
        CONFIG.setMaxAge(3600L);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!CorsUtils.isCorsRequest(exchange.getRequest())) return chain.filter(exchange);
        HttpHeaders headers = exchange.getRequest().getHeaders();
        HttpHeaders response = exchange.getResponse().getHeaders();
        String origin = headers.getFirst(HttpHeaders.ORIGIN);
        if (origin != null) response.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        response.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
        response.add("Access-Expose-Headers", "X-Trace-Id");
        if (HttpMethod.OPTIONS.matches(exchange.getRequest().getMethod().name())) {
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() { return -190; }
}
