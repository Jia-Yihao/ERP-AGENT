package com.example.erp.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser extends BaseEntity {

    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private Integer status;
    private LocalDateTime lastLoginAt;
}
