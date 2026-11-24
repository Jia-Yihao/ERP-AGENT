package com.example.erp.gateway.fallback;

import com.example.erp.common.core.ErrorCode;
import com.example.erp.common.core.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** CircuitBreaker default-filters 的 forward:/fallback 落点，维持 HTTP 200 + 非零 code 契约。 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    public Mono<R<Void>> fallback() {
        return Mono.just(R.fail(ErrorCode.SERVICE_UNAVAILABLE));
    }
}
