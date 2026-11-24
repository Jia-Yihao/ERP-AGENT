package com.example.erp.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.catalog.entity.Part;
import com.example.erp.common.core.Dtos;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PartMapper extends BaseMapper<Part> {

    List<Dtos.PartBrief> selectBriefs(@Param("ids") List<Long> ids);

    Page<Part> selectPageWithSupplier(Page<?> page,
                                      @Param("name") String name,
                                      @Param("category") String category,
                                      @Param("supplierId") Long supplierId);
}
