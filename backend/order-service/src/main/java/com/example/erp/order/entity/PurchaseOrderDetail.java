package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_order_detail")
public class PurchaseOrderDetail extends BaseEntity {
    private Long orderId;
    private Long partId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String remark;
    @TableField(exist = false)
    private String partName;
    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private String orderNumber;
    @TableField(exist = false)
    private LocalDateTime orderTime;
}
