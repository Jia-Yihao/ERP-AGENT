package com.example.erp.common.data;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.erp.common.core.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * insert 填充 createTime/createBy/updateTime/updateBy，
 * update 填充 updateTime/updateBy；deleted 与 version 由数据库默认值初始化。
 */
@Component
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String user = UserContextHolder.currentUsername();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "createBy", String.class, user);
        strictInsertFill(metaObject, "updateBy", String.class, user);
        strictInsertFill(metaObject, "deleted", Integer.class, 0);
        strictInsertFill(metaObject, "version", Integer.class, 0);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        strictUpdateFill(metaObject, "updateBy", String.class, UserContextHolder.currentUsername());
    }
}
