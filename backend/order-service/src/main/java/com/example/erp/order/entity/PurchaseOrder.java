package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {
    private String orderNumber;
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;
    private LocalDateTime orderTime;
    @TableField(exist = false)
    private List<PurchaseOrderDetail> orderDetail;
}
