package com.example.erp.inventory.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.core.Dtos;
import lombok.Data;

@Data
@TableName("inventory")
public class Inventory {
    private Long id;
    private Long partId;
    private Integer currentQuantity;
    private Integer safetyStock;
    private String warehouseLocation;
    @TableField(exist = false)
    private Dtos.PartBrief partDetail;
}
