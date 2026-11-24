package com.example.erp.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.Dtos;
import com.example.erp.common.core.PageResult;
import com.example.erp.common.core.UserContextHolder;
import com.example.erp.order.client.CatalogClient;
import com.example.erp.order.entity.OrderStatusLog;
import com.example.erp.order.entity.PurchaseOrder;
import com.example.erp.order.entity.PurchaseOrderDetail;
import com.example.erp.order.entity.PurchaseRequisition;
import com.example.erp.order.entity.RequisitionApproval;
import com.example.erp.order.mapper.OrderStatusLogMapper;
import com.example.erp.order.mapper.RequisitionApprovalMapper;
import com.example.erp.order.mapper.RequisitionMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequisitionService {
    private static final Logger log = LoggerFactory.getLogger(RequisitionService.class);
    private final RequisitionMapper mapper;
    private final RequisitionApprovalMapper approvalMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final OrderService orderService;
    private final CatalogClient catalogClient;

    public PurchaseRequisition get(Long id) {
        PurchaseRequisition r = mapper.selectById(id);
        if (r == null) throw new BizException(404, "采购申请不存在");
        return r;
    }

    public PageResult<PurchaseRequisition> page(long current, long size, Integer status, String applicant) {
        Page<PurchaseRequisition> p = mapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<PurchaseRequisition>()
                        .eq(status != null, PurchaseRequisition::getStatus, status)
                        .eq(StringUtils.hasText(applicant), PurchaseRequisition::getApplicant, applicant)
                        .orderByDesc(PurchaseRequisition::getId));
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    @Transactional
    public PurchaseRequisition create(PurchaseRequisition body) {
        if (body.getPartId() == null || body.getQuantity() == null || body.getQuantity() <= 0
                || !StringUtils.hasText(body.getReason())) {
            throw new BizException(400, "partId / quantity / reason 必填");
        }
        if (!StringUtils.hasText(body.getRequisitionNo())) {
            body.setRequisitionNo("PR" + LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                    + (int) (Math.random() * 1000));
        }
        if (!StringUtils.hasText(body.getApplicant())) {
            body.setApplicant(UserContextHolder.currentUsername());
        }
        if (body.getStatus() == null) body.setStatus(0);
        mapper.insert(body);
        return body;
    }

    @Transactional
    public PurchaseRequisition submit(Long id) {
        PurchaseRequisition r = get(id);
        if (!Integer.valueOf(0).equals(r.getStatus())) {
            throw new BizException(409, "仅草稿状态的采购申请可以提交");
        }
        r.setStatus(1);
        mapper.updateById(r);
        return r;
    }

    @Transactional
    public PurchaseOrder approve(Long id, String approver, String comment) {
        PurchaseRequisition r = get(id);
        if (!Integer.valueOf(1).equals(r.getStatus())) {
            throw new BizException(409, "仅待审批状态的采购申请可以审批");
        }
        String operator = StringUtils.hasText(approver) ? approver : UserContextHolder.currentUsername();

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNumber("PO" + r.getRequisitionNo().replaceFirst("^PR", ""));
        order.setRemark("由采购申请 " + r.getRequisitionNo() + " 生成");
        PurchaseOrderDetail detail = new PurchaseOrderDetail();
        detail.setPartId(r.getPartId());
        detail.setQuantity(r.getQuantity());
        detail.setUnitPrice(resolveUnitPrice(r.getPartId()));
        detail.setRemark(r.getReason());
        order.setOrderDetail(List.of(detail));
        PurchaseOrder created = orderService.create(order);

        r.setStatus(2);
        r.setApprover(operator);
        r.setApprovedAt(LocalDateTime.now());
        mapper.updateById(r);

        RequisitionApproval approval = new RequisitionApproval();
        approval.setRequisitionId(id);
        approval.setApprover(operator);
        approval.setAction("APPROVE");
        approval.setComment(comment);
        approvalMapper.insert(approval);

        OrderStatusLog statusLog = new OrderStatusLog();
        statusLog.setOrderId(created.getId());
        statusLog.setToStatus(created.getStatus());
        statusLog.setOperator(operator);
        statusLog.setRemark("采购申请 " + r.getRequisitionNo() + " 审批通过生成订单");
        statusLogMapper.insert(statusLog);
        return created;
    }

    @Transactional
    public PurchaseRequisition reject(Long id, String approver, String comment) {
        PurchaseRequisition r = get(id);
        if (!Integer.valueOf(1).equals(r.getStatus())) {
            throw new BizException(409, "仅待审批状态的采购申请可以驳回");
        }
        String operator = StringUtils.hasText(approver) ? approver : UserContextHolder.currentUsername();
        r.setStatus(3);
        r.setApprover(operator);
        r.setApprovedAt(LocalDateTime.now());
        mapper.updateById(r);

        RequisitionApproval approval = new RequisitionApproval();
        approval.setRequisitionId(id);
        approval.setApprover(operator);
        approval.setAction("REJECT");
        approval.setComment(comment);
        approvalMapper.insert(approval);
        return r;
    }

    private BigDecimal resolveUnitPrice(Long partId) {
        try {
            List<Dtos.PartBrief> briefs = catalogClient.briefs(List.of(partId));
            if (briefs != null && !briefs.isEmpty() && briefs.get(0).getPurchasePrice() != null) {
                return briefs.get(0).getPurchasePrice();
            }
        } catch (Exception e) {
            log.warn("catalog unavailable while resolving unit price for part {}", partId);
        }
        return BigDecimal.ONE;
    }
}
