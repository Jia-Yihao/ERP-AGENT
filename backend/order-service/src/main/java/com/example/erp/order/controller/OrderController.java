package com.example.erp.order.controller;

import com.example.erp.order.dto.OrderCreateDTO;
import com.example.erp.order.convert.OrderConvert;
import com.example.erp.common.core.PageResult;
import com.example.erp.common.core.R;
import com.example.erp.order.entity.PurchaseOrder;
import com.example.erp.order.entity.PurchaseOrderDetail;
import com.example.erp.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    private PurchaseOrder toEntity(OrderCreateDTO dto) {
        PurchaseOrder o = OrderConvert.INSTANCE.toOrder(dto);
        if (dto.getOrderDetail() != null) {
            o.setOrderDetail(dto.getOrderDetail().stream()
                    .map(OrderConvert.INSTANCE::toDetail).toList());
        }
        return o;
    }

    @PostMapping("/create")
    public R<PurchaseOrder> create(@Valid @RequestBody OrderCreateDTO body) {
        return R.ok(service.create(toEntity(body)));
    }

    @PutMapping("/update/{id}")
    public R<PurchaseOrder> update(@PathVariable Long id, @Valid @RequestBody OrderCreateDTO body) {
        return R.ok(service.update(id, toEntity(body)));
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
    public R<PurchaseOrder> updateStatus(@PathVariable Long id, @NotNull(message = "status 不能为空") Integer status) {
        return R.ok(service.updateStatus(id, status));
    }
}
