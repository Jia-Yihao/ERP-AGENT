package com.example.erp.order.controller;

import com.example.erp.common.core.R;
import com.example.erp.order.dto.ReceiveCreateDTO;
import com.example.erp.order.entity.PurchaseReceive;
import com.example.erp.order.service.ReceiveService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/receives")
@RequiredArgsConstructor
public class ReceiveController {
    private final ReceiveService service;

    @PostMapping("/create")
    public R<PurchaseReceive> create(@Valid @RequestBody ReceiveCreateDTO body) {
        PurchaseReceive receive = new PurchaseReceive();
        receive.setReceiveNo(body.getReceiveNo());
        receive.setOrderId(body.getOrderId());
        receive.setPartId(body.getPartId());
        receive.setQuantity(body.getQuantity());
        receive.setReceiver(body.getReceiver());
        receive.setReceivedAt(body.getReceivedAt());
        receive.setStatus(body.getStatus());
        return R.ok(service.create(receive));
    }

    /** Seata 全局事务版本：本地收货 + 跨服务库存入库，失败时两库一起回滚。 */
    @PostMapping("/create-with-stock")
    public R<PurchaseReceive> createWithStock(@Valid @RequestBody ReceiveCreateDTO body) {
        PurchaseReceive receive = new PurchaseReceive();
        receive.setReceiveNo(body.getReceiveNo());
        receive.setOrderId(body.getOrderId());
        receive.setPartId(body.getPartId());
        receive.setQuantity(body.getQuantity());
        receive.setReceiver(body.getReceiver());
        receive.setReceivedAt(body.getReceivedAt());
        receive.setStatus(body.getStatus());
        return R.ok(service.createWithStock(receive));
    }

    @GetMapping("/list/{orderId}")
    public R<List<PurchaseReceive>> list(@PathVariable Long orderId) {
        return R.ok(service.listByOrder(orderId));
    }
}
