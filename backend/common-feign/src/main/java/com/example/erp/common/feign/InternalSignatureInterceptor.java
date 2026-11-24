package com.example.erp.common.feign;

import com.example.erp.common.core.Constants;
import com.example.erp.common.core.InternalSignature;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/** 为服务间调用补签 X-Internal-Token，与接收方 InternalSignatureFilter 对应。 */
@Component
public class InternalSignatureInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        if (!InternalSignature.isConfigured()) {
            return;
        }
        if (template.headers().containsKey(Constants.INTERNAL_TOKEN_HEADER)) {
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
        byte[] body = template.body();
        String content = body == null ? "" : new String(body, StandardCharsets.UTF_8);
        template.header(Constants.INTERNAL_TOKEN_HEADER, timestamp + "." + InternalSignature.sign(timestamp, content));
    }
}
