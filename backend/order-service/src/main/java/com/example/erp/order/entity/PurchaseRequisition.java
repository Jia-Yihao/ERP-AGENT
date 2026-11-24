package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("purchase_requisition")
public class PurchaseRequisition extends BaseEntity {
    private String requisitionNo;
    private Long partId;
    private Integer quantity;
    private String reason;
    private String applicant;
    private Integer status;
    private String approver;
    private LocalDateTime approvedAt;
}
