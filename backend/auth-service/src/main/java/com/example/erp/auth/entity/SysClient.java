package com.example.erp.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.erp.common.data.BaseEntity;
import lombok.Data;

@Data
@TableName("sys_client")
public class SysClient extends BaseEntity {

    private String clientId;
    private String clientSecretHash;
    private String clientName;
    private String scopes;
    private Integer status;
}
