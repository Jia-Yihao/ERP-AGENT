package com.example.erp.common.security;

import com.example.erp.common.core.Constants;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.InternalSignature;
import com.example.erp.common.core.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * /internal/** 的服务间签名校验。配置了 ERP_INTERNAL_SECRET 时校验 HMAC 与 60 秒时间窗，
 * 未配置时放行并在启动阶段提示。
 */
public class InternalSignatureFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(InternalSignatureFilter.class);

    private final ObjectMapper objectMapper;

    public InternalSignatureFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        if (!InternalSignature.isConfigured()) {
            log.warn("ERP_INTERNAL_SECRET 未配置，/internal/** 不校验服务间签名");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !InternalSignature.isConfigured()
                || !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        byte[] body = request.getInputStream().readAllBytes();
        String bodyText = body.length == 0 ? "" : new String(body, StandardCharsets.UTF_8);
        String header = request.getHeader(Constants.INTERNAL_TOKEN_HEADER);
        if (!InternalSignature.verify(header, bodyText)) {
            log.warn("internal signature rejected: {} {}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getWriter(), R.fail(ErrorCode.UNAUTHORIZED));
            return;
        }
        chain.doFilter(new CachedBodyRequest(request, body), response);
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream source = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override
                public int read() {
                    return source.read();
                }

                @Override
                public boolean isFinished() {
                    return source.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(jakarta.servlet.ReadListener readListener) {
                    throw new UnsupportedOperationException("async read not supported");
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
