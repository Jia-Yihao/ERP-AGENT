package com.example.erp.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    private Long userId;
    private Long roleId;
}
