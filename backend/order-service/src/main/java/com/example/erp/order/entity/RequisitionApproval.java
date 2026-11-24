package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("requisition_approval")
public class RequisitionApproval extends BaseEntity {
    private Long requisitionId;
    private String approver;
    private String action;
    private String comment;
}
