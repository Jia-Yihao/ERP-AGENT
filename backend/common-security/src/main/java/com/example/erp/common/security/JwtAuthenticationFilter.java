package com.example.erp.common.security;

import com.example.erp.common.core.UserContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 从 Bearer Token 解析用户上下文并写入 SecurityContext。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationEntryPoint entryPoint;
    private final List<AntPathRequestMatcher> permitAllMatchers;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, AuthenticationEntryPoint entryPoint,
                                   List<AntPathRequestMatcher> permitAllMatchers) {
        this.tokenProvider = tokenProvider;
        this.entryPoint = entryPoint;
        this.permitAllMatchers = permitAllMatchers;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return permitAllMatchers.stream().anyMatch(matcher -> matcher.matches(request));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token == null || !tokenProvider.validate(token)) {
            entryPoint.commence(request, response, new BadCredentialsException("invalid bearer token"));
            return;
        }

        Claims claims = tokenProvider.parse(token);
        List<String> roles = tokenProvider.getList(claims, JwtTokenProvider.CLAIM_ROLES);
        List<String> perms = tokenProvider.getList(claims, JwtTokenProvider.CLAIM_PERMS);
        String username = tokenProvider.getUsername(claims);
        UserContextHolder.set(tokenProvider.getUserId(claims), username, roles, perms);

        try {
            List<GrantedAuthority> authorities = List.copyOf(java.util.stream.Stream.concat(roles.stream(), perms.stream())
                    .filter(value -> value != null && !value.isBlank())
                    .map(value -> (GrantedAuthority) new SimpleGrantedAuthority(value))
                    .toList());
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, token, authorities));
            filterChain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
