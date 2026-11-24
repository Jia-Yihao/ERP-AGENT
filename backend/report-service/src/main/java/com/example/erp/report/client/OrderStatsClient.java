package com.example.erp.report.client;

import com.example.erp.common.core.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "order-service", path = "/api/orders", fallbackFactory = OrderStatsClientFallbackFactory.class)
public interface OrderStatsClient {
    @GetMapping("/statistics")
    R<Map<String, Object>> statistics();
}
