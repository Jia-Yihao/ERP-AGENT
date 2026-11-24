package com.example.erp.report.controller;

import com.example.erp.common.core.R;
import com.example.erp.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService service;

    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        return R.ok(service.summary());
    }
}
