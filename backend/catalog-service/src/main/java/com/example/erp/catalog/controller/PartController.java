package com.example.erp.catalog.controller;

import com.example.erp.catalog.entity.Part;
import com.example.erp.catalog.service.PartService;
import com.example.erp.common.core.PageResult;
import com.example.erp.common.core.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {
    private final PartService service;

    @GetMapping("/get/{id}")
    public R<Part> get(@PathVariable Long id) {
        return R.ok(service.get(id));
    }

    @GetMapping("/search")
    public R<List<Part>> search(String name) {
        return R.ok(service.search(name));
    }

    @GetMapping("/supplier/{supplier_id}")
    public R<List<Part>> bySupplier(@PathVariable("supplier_id") Long supplierId) {
        return R.ok(service.bySupplier(supplierId));
    }

    @GetMapping("/page")
    public R<PageResult<Part>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    String name, String category, Long supplierId) {
        return R.ok(service.page(current, size, name, category, supplierId));
    }

    @PostMapping("/create")
    public R<Part> create(@RequestBody Part body) {
        return R.ok(service.create(body));
    }
}
