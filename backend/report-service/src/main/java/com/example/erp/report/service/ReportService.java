package com.example.erp.report.service;

import com.example.erp.common.core.R;
import com.example.erp.report.client.InventoryCheckClient;
import com.example.erp.report.client.OrderStatsClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** 报表汇总：对每个下游维度做熔断与重试，失败维度降级为空对象，不影响其余维度。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final OrderStatsClient orderStatsClient;
    private final InventoryCheckClient inventoryCheckClient;

    @CircuitBreaker(name = "reportDownstream", fallbackMethod = "summaryFallback")
    @Retry(name = "reportDownstream")
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orders", fetchOrders());
        result.put("inventory", fetchInventory());
        return result;
    }

    private Map<String, Object> fetchOrders() {
        try {
            R<Map<String, Object>> orders = orderStatsClient.statistics();
            return orders.getCode() == 200 && orders.getData() != null ? orders.getData() : Map.of();
        } catch (Exception e) {
            log.warn("order statistics unavailable: {}", e.getMessage());
            return Map.of();
        }
    }

    private Map<String, Object> fetchInventory() {
        try {
            R<Map<String, Object>> inventory = inventoryCheckClient.check();
            return inventory.getCode() == 200 && inventory.getData() != null ? inventory.getData() : Map.of();
        } catch (Exception e) {
            log.warn("inventory check unavailable: {}", e.getMessage());
            return Map.of();
        }
    }

    public Map<String, Object> summaryFallback(Throwable throwable) {
        log.warn("report summary degraded: {}", throwable.getMessage());
        return Map.of("orders", Map.of(), "inventory", Map.of());
    }
}
