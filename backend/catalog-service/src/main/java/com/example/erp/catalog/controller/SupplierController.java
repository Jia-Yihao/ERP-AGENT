package com.example.erp.catalog.controller;

import com.example.erp.catalog.entity.Supplier;
import com.example.erp.catalog.service.SupplierService;
import com.example.erp.common.PageResult;
import com.example.erp.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {
    private final SupplierService service;

    @GetMapping("/search")
    public R<List<Supplier>> search(String name) {
        return R.ok(service.search(name));
    }

    @GetMapping("/page")
    public R<PageResult<Supplier>> page(@RequestParam(defaultValue = "1") long current,
                                        @RequestParam(defaultValue = "10") long size,
                                        String name, Integer status, String creditRating) {
        return R.ok(service.page(current, size, name, status, creditRating));
    }

    @GetMapping("/get/{id}")
    public R<Supplier> get(@PathVariable Long id) {
        return R.ok(service.get(id));
    }

    @PostMapping("/create")
    public R<Supplier> create(@RequestBody Supplier body) {
        return R.ok(service.create(body));
    }

    @PatchMapping("/update-status/{id}")
    public R<Supplier> updateStatus(@PathVariable Long id, Integer status) {
        return R.ok(service.updateStatus(id, status));
    }
}
