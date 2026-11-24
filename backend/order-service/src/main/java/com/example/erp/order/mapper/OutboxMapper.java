package com.example.erp.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.order.entity.OutboxEvent;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxMapper extends BaseMapper<OutboxEvent> {

    List<OutboxEvent> findPending(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
