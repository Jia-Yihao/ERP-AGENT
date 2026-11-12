package com.example.erp.order.controller;

import com.example.erp.common.PageResult;
import com.example.erp.common.R;
import com.example.erp.order.entity.PurchaseOrder;
import com.example.erp.order.entity.PurchaseOrderDetail;
import com.example.erp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping("/create")
    public R<PurchaseOrder> create(@RequestBody PurchaseOrder body) {
        return R.ok(service.create(body));
    }

    @PutMapping("/update/{id}")
    public R<PurchaseOrder> update(@PathVariable Long id, @RequestBody PurchaseOrder body) {
        return R.ok(service.update(id, body));
    }

    @GetMapping("/page")
    public R<PageResult<PurchaseOrder>> page(@RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size,
                                             String orderNumber, Integer status,
                                             String startDate, String endDate) {
        return R.ok(service.page(current, size, orderNumber, status, startDate, endDate));
    }

    @GetMapping("/get/{id}")
    public R<PurchaseOrder> get(@PathVariable Long id) {
        return R.ok(service.get(id));
    }

    @GetMapping("/search-details")
    public R<List<PurchaseOrderDetail>> searchDetails(String partName, String startDate, String endDate) {
        return R.ok(service.searchDetails(partName, startDate, endDate));
    }

    @GetMapping("/statistics")
    public R<Map<String, Object>> statistics(String startDate, String endDate) {
        return R.ok(service.statistics(startDate, endDate));
    }

    @PatchMapping("/update-status/{id}")
    public R<PurchaseOrder> updateStatus(@PathVariable Long id, Integer status) {
        return R.ok(service.updateStatus(id, status));
    }
}
