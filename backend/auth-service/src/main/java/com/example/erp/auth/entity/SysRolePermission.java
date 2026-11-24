package com.example.erp.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_role_permission")
public class SysRolePermission extends BaseEntity {

    private Long roleId;
    private Long permissionId;
}
