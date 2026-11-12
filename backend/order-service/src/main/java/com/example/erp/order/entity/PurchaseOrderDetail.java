package com.example.erp.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_order_detail")
public class PurchaseOrderDetail {
    private Long id;
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
