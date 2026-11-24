package com.example.erp.catalog.controller;

import com.example.erp.catalog.service.PartService;
import com.example.erp.common.core.Dtos;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 仅服务间调用：Gateway 只路由 /api/**，/internal/** 不对外暴露。 */
@RestController
@RequestMapping("/internal/parts")
@RequiredArgsConstructor
public class InternalPartController {
    private final PartService service;

    @PostMapping("/batch")
    public List<Dtos.PartBrief> batch(@RequestBody List<Long> ids) {
        return service.briefs(ids);
    }
}
