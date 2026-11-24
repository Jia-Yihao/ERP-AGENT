package com.example.erp.gateway.filter;

import com.example.erp.common.core.Constants;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** 网关 JWT 校验：白名单放行，其余校验签名与 exp，通过后注入用户上下文头。 */
@Slf4j
@Component
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {
    private final ObjectMapper mapper;
    private final SecretKey key;

    @Value("${erp.security.enabled:true}")
    private boolean enabled;

    private static final List<String> WHITELIST = List.of(
            "/api/auth/login", "/api/auth/token", "/api/auth/refresh",
            "/actuator/health", "/v3/api-docs", "/swagger-ui", "/fallback");

    public JwtAuthGatewayFilter(ObjectMapper mapper,
                                @Value("${erp.jwt.secret:erp-local-development-secret-key-32bytes-minimum}") String secret) {
        this.mapper = mapper;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!enabled || WHITELIST.stream().anyMatch(path::startsWith)) return chain.filter(exchange);
        String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) return reject(exchange);
        try {
            Claims claims = Jwts.parser().verifyWith(key).build()
                    .parseSignedClaims(auth.substring(7)).getPayload();
            String userId = claims.getSubject();
            String username = String.valueOf(claims.get("username", String.class));
            List<String> roles = claims.get("roles", List.class);
            ServerHttpRequest req = exchange.getRequest().mutate()
                    .header(Constants.USER_ID_HEADER, userId == null ? "" : userId)
                    .header(Constants.USER_NAME_HEADER, username)
                    .header(Constants.USER_ROLES_HEADER, roles == null ? "" : String.join(",", roles))
                    .build();
            return chain.filter(exchange.mutate().request(req).build());
        } catch (Exception e) {
            return reject(exchange);
        }
    }

    private Mono<Void> reject(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] body = mapper.writeValueAsBytes(R.fail(ErrorCode.UNAUTHORIZED));
            DataBuffer b = exchange.getResponse().bufferFactory().wrap(body);
            return exchange.getResponse().writeWith(Mono.just(b));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() { return -50; }
}
