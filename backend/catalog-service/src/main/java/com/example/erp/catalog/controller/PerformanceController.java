package com.example.erp.catalog.controller;

import com.example.erp.catalog.entity.SupplierPerformance;
import com.example.erp.catalog.service.PerformanceService;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.R;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@Validated
@RequestMapping("/api/performance")
@RequiredArgsConstructor
public class PerformanceController {
    private final PerformanceService service;

    @GetMapping("/{supplierId}")
    public R<SupplierPerformance> get(@PathVariable Long supplierId) {
        return R.ok(service.get(supplierId));
    }

    @PostMapping("/recalculate")
    public R<SupplierPerformance> recalculate(@RequestParam Long supplierId,
                                              @RequestParam(required = false) String statDate) {
        LocalDate date = null;
        if (StringUtils.hasText(statDate)) {
            try {
                date = LocalDate.parse(statDate);
            } catch (DateTimeParseException e) {
                throw new BizException(400, "statDate 格式错误，应为 yyyy-MM-dd");
            }
        }
        return R.ok(service.recalculate(supplierId, date));
    }
}
