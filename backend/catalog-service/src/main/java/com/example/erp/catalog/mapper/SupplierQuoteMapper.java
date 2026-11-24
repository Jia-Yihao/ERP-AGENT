package com.example.erp.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.erp.catalog.entity.SupplierQuote;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SupplierQuoteMapper extends BaseMapper<SupplierQuote> {

    List<SupplierQuote> selectActiveByPart(@Param("partId") Long partId);
}
