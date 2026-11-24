package com.example.erp.common.feign;

import com.example.erp.common.core.BizException;
import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 将下游 HTTP 错误解码为带业务 code 的 BizException，
 * 与网关、服务侧统一的 HTTP 200 + 非零 code 契约保持一致。
 */
@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper;
    private final ErrorDecoder delegate = new Default();

    public FeignErrorDecoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            if (response.body() != null) {
                byte[] bytes = response.body().asInputStream().readAllBytes();
                String body = new String(bytes, StandardCharsets.UTF_8);
                R<?> r = mapper.readValue(body, R.class);
                if (r != null && r.getCode() != ErrorCode.OK.getCode()) {
                    return new BizException(r.getCode(), r.getMessage());
                }
            }
        } catch (Exception e) {
            log.debug("decode feign error body failed for {}: {}", methodKey, e.getMessage());
        }
        return delegate.decode(methodKey, response);
    }
}
