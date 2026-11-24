package com.example.erp.order.controller;

import com.example.erp.common.core.PageResult;
import com.example.erp.common.core.R;
import com.example.erp.order.dto.RequisitionCreateDTO;
import com.example.erp.order.entity.PurchaseOrder;
import com.example.erp.order.entity.PurchaseRequisition;
import com.example.erp.order.service.RequisitionService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/requisitions")
@RequiredArgsConstructor
public class RequisitionController {
    private final RequisitionService service;

    @PostMapping("/create")
    public R<PurchaseRequisition> create(@Valid @RequestBody RequisitionCreateDTO body) {
        PurchaseRequisition requisition = new PurchaseRequisition();
        requisition.setRequisitionNo(body.getRequisitionNo());
        requisition.setPartId(body.getPartId());
        requisition.setQuantity(body.getQuantity());
        requisition.setReason(body.getReason());
        requisition.setApplicant(body.getApplicant());
        return R.ok(service.create(requisition));
    }

    @PostMapping("/submit/{id}")
    public R<PurchaseRequisition> submit(@PathVariable Long id) {
        return R.ok(service.submit(id));
    }

    @PostMapping("/approve/{id}")
    public R<PurchaseOrder> approve(@PathVariable Long id,
                                    @RequestParam(required = false) String approver,
                                    @RequestParam(required = false) String comment) {
        return R.ok(service.approve(id, approver, comment));
    }

    @PostMapping("/reject/{id}")
    public R<PurchaseRequisition> reject(@PathVariable Long id,
                                         @RequestParam(required = false) String approver,
                                         @RequestParam(required = false) String comment) {
        return R.ok(service.reject(id, approver, comment));
    }

    @GetMapping("/page")
    public R<PageResult<PurchaseRequisition>> page(@RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   Integer status, String applicant) {
        return R.ok(service.page(current, size, status, applicant));
    }
}
