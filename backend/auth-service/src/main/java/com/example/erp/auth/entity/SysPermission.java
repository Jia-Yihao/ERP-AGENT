package com.example.erp.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private String permCode;
    private String permName;
    private String resourceType;
    private String path;
}
