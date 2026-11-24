package com.example.erp.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.order.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderMapper extends BaseMapper<PurchaseOrder> {

    List<Map<String, Object>> selectStatistics(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}
