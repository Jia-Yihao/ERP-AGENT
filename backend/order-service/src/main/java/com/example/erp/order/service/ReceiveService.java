package com.example.erp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.UserContextHolder;
import com.example.erp.order.entity.OrderStatusLog;
import com.example.erp.order.entity.OutboxEvent;
import com.example.erp.order.entity.PurchaseOrder;
import com.example.erp.order.entity.PurchaseReceive;
import com.example.erp.order.mapper.PurchaseReceiveMapper;
import com.example.erp.order.mapper.OrderStatusLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiveService {
    private final PurchaseReceiveMapper mapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final OrderService orderService;
    private final OutboxService outboxService;
    private final OrderStateMachine stateMachine;

    public List<PurchaseReceive> listByOrder(Long orderId) {
        return mapper.selectList(new LambdaQueryWrapper<PurchaseReceive>()
                .eq(PurchaseReceive::getOrderId, orderId)
                .orderByDesc(PurchaseReceive::getId));
    }

    @Transactional
    public PurchaseReceive create(PurchaseReceive body) {
        PurchaseOrder order = orderService.get(body.getOrderId());
        Integer fromStatus = order.getStatus();
        stateMachine.assertTransition(fromStatus, OrderStateMachine.RECEIVED);
        if (body.getPartId() == null || (order.getOrderDetail() != null && !order.getOrderDetail().isEmpty()
                && order.getOrderDetail().stream().noneMatch(d -> body.getPartId().equals(d.getPartId())))) {
            throw new BizException(400, "零件不属于该订单");
        }
        if (body.getQuantity() == null || body.getQuantity() <= 0) {
            throw new BizException(400, "quantity 必须大于 0");
        }
        if (!StringUtils.hasText(body.getReceiveNo())) {
            body.setReceiveNo("RC" + LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                    + (int) (Math.random() * 1000));
        }
        if (!StringUtils.hasText(body.getReceiver())) {
            body.setReceiver(UserContextHolder.currentUsername());
        }
        if (body.getReceivedAt() == null) body.setReceivedAt(LocalDateTime.now());
        if (body.getStatus() == null) body.setStatus(1);

        mapper.insert(body);
        orderService.updateStatus(body.getOrderId(), OrderStateMachine.RECEIVED);

        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(body.getOrderId());
        statusLog.setFromStatus(fromStatus);
        statusLog.setToStatus(OrderStateMachine.RECEIVED);
        statusLog.setOperator(body.getReceiver());
        statusLog.setRemark("收货单 " + body.getReceiveNo() + " 完成收货");
        statusLogMapper.insert(statusLog);

        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("PURCHASE_RECEIVE");
        event.setAggregateId(String.valueOf(body.getId()));
        event.setEventType("STOCK_INBOUND");
        event.setPayload(String.format("{\"receiveId\":%d,\"orderId\":%d,\"partId\":%d,\"quantity\":%d}",
                body.getId(), body.getOrderId(), body.getPartId(), body.getQuantity()));
        outboxService.publish(event);
        return body;
    }
}
