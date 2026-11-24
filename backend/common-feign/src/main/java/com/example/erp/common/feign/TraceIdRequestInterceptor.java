package com.example.erp.common.feign;

import com.example.erp.common.core.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 调用透传 X-Trace-Id 与网关注入的用户上下文头，
 * 使跨服务日志可用同一 traceId 串联，方法级权限拿到同一用户身份。
 */
@Component
public class TraceIdRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(Constants.TRACE_ID_MDC_KEY);
        if (traceId != null && !traceId.isBlank()) {
            template.header(Constants.TRACE_ID_HEADER, traceId);
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            copyHeader(request, template, Constants.USER_ID_HEADER);
            copyHeader(request, template, Constants.USER_NAME_HEADER);
            copyHeader(request, template, Constants.USER_ROLES_HEADER);
            copyHeader(request, template, "Authorization");
        }
    }

    private void copyHeader(HttpServletRequest request, RequestTemplate template, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) template.header(name, value);
    }
}
