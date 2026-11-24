package com.example.erp.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;
import java.util.List;

/**
 * 无状态 JWT 安全链。erp.security.enabled=false 时仅用于本地开发，全部放行。
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityAutoConfig {

    private static final String DEFAULT_PERMIT_ALL = "/actuator/health/**,/v3/api-docs/**,/swagger-ui/**";

    @Bean
    @Order(1)
    @ConditionalOnProperty(name = "erp.security.enabled", havingValue = "false")
    public SecurityFilterChain devPermitAllChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "erp.security.enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain jwtSecurityChain(
            HttpSecurity http,
            JwtTokenProvider tokenProvider,
            RestAuthEntryPoint entryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            @Value("${erp.security.permit-all:" + DEFAULT_PERMIT_ALL + "}") String[] permitAll) throws Exception {

        List<AntPathRequestMatcher> permitMatchers = Arrays.stream(permitAll)
                .map(String::trim)
                .filter(path -> !path.isEmpty())
                .map(AntPathRequestMatcher::new)
                .toList();

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider, entryPoint, permitMatchers);

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(requests -> {
                    permitMatchers.forEach(matcher -> requests.requestMatchers(matcher).permitAll());
                    requests.anyRequest().authenticated();
                })
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
