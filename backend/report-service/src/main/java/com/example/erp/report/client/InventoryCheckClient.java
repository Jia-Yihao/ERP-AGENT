package com.example.erp.report.client;

import com.example.erp.common.core.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "inventory-service", path = "/api/inventory")
public interface InventoryCheckClient {
    @GetMapping("/check")
    R<Map<String, Object>> check();
}
