package com.example.erp.report.service;

import com.example.erp.common.core.R;
import com.example.erp.report.client.InventoryCheckClient;
import com.example.erp.report.client.OrderStatsClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final OrderStatsClient orderStatsClient;
    private final InventoryCheckClient inventoryCheckClient;

    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            R<Map<String, Object>> orders = orderStatsClient.statistics();
            result.put("orders", orders.getCode() == 200 ? orders.getData() : Map.of());
        } catch (Exception e) {
            result.put("orders", Map.of());
        }
        try {
            R<Map<String, Object>> inventory = inventoryCheckClient.check();
            result.put("inventory", inventory.getCode() == 200 ? inventory.getData() : Map.of());
        } catch (Exception e) {
            result.put("inventory", Map.of());
        }
        return result;
    }
}
