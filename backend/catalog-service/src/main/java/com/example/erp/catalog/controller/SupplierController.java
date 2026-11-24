package com.example.erp.catalog.controller;

import com.example.erp.catalog.dto.SupplierCreateDTO;
import com.example.erp.catalog.convert.CatalogConvert;
import com.example.erp.catalog.entity.Supplier;
import com.example.erp.catalog.service.SupplierService;
import com.example.erp.common.core.PageResult;
import com.example.erp.common.core.R;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
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
    public R<Supplier> create(@Valid @RequestBody SupplierCreateDTO body) {
        return R.ok(service.create(CatalogConvert.INSTANCE.toSupplier(body)));
    }

    @PatchMapping("/update-status/{id}")
    public R<Supplier> updateStatus(@PathVariable Long id, @NotNull(message = "status 不能为空") Integer status) {
        return R.ok(service.updateStatus(id, status));
    }
}
