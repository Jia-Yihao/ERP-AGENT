package com.example.erp.gateway.filter;

import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 网关下游异常统一改写为 HTTP 200 + 非零 code。 */
@Component
public class GlobalErrorFilter implements GlobalFilter, Ordered {
    private final ObjectMapper mapper;

    public GlobalErrorFilter(ObjectMapper mapper) { this.mapper = mapper; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).onErrorResume(e -> {
            int status = exchange.getResponse().getStatusCode() == null
                    ? 503 : exchange.getResponse().getStatusCode().value();
            ErrorCode code = status == 404 ? ErrorCode.NOT_FOUND : ErrorCode.SERVICE_UNAVAILABLE;
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.OK);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                byte[] body = mapper.writeValueAsBytes(R.fail(code));
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body);
                return exchange.getResponse().writeWith(Mono.just(buffer));
            } catch (Exception ignored) {
                return exchange.getResponse().setComplete();
            }
        });
    }

    @Override
    public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
