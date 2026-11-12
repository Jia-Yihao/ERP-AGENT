package com.example.erp.inventory.controller;

import com.example.erp.common.PageResult;
import com.example.erp.common.R;
import com.example.erp.inventory.entity.Inventory;
import com.example.erp.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService service;

    @GetMapping("/warning")
    public R<List<Inventory>> warning() {
        return R.ok(service.warning());
    }

    @GetMapping("/page")
    public R<PageResult<Inventory>> page(@RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size,
                                         String partName, String warehouseLocation) {
        return R.ok(service.page(current, size, partName, warehouseLocation));
    }

    @GetMapping("/check")
    public R<Map<String, Object>> check() {
        return R.ok(service.check());
    }

    /** MCP 侧用 erp_client.post(path, params=...)，参数在 query string 上，不能使用 @RequestBody。 */
    @PostMapping("/inbound")
    public R<Inventory> inbound(Long partId, Integer quantity, String warehouseLocation) {
        return R.ok(service.inbound(partId, quantity, warehouseLocation));
    }

    @PostMapping("/outbound")
    public R<Inventory> outbound(Long partId, Integer quantity) {
        return R.ok(service.outbound(partId, quantity));
    }

    @GetMapping("/get/{id}")
    public R<Inventory> get(@PathVariable Long id) {
        return R.ok(service.get(id));
    }
}
