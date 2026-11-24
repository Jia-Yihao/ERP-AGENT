package com.example.erp.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("inventory_log")
public class InventoryLog extends BaseEntity {
    private Long partId;
    private String changeType;
    private Integer quantity;
    private Integer beforeQuantity;
    private Integer afterQuantity;
}
