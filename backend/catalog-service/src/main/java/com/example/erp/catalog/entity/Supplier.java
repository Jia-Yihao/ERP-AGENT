package com.example.erp.catalog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("supplier")
public class Supplier extends BaseEntity {
    private String supplierCode;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String creditRating;
    private Integer status;
}
