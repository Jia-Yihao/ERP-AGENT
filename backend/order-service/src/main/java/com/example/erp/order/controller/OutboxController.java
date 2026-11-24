package com.example.erp.order.controller;

import com.example.erp.common.core.R;
import com.example.erp.order.entity.OutboxEvent;
import com.example.erp.order.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/outbox")
@RequiredArgsConstructor
public class OutboxController {
    private final OutboxService service;

    @GetMapping("/pending")
    public R<List<OutboxEvent>> pending(@RequestParam(defaultValue = "50") int limit) {
        return R.ok(service.findPending(LocalDateTime.now(), limit));
    }

    @PostMapping("/retry/{id}")
    public R<OutboxEvent> retry(@PathVariable Long id) {
        return R.ok(service.retryFailed(id));
    }
}
