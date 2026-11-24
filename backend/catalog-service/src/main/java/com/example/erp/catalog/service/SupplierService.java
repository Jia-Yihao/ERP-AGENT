package com.example.erp.catalog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.erp.catalog.entity.Supplier;
import com.example.erp.catalog.mapper.SupplierMapper;
import com.example.erp.common.core.BizException;
import com.example.erp.common.core.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {
    private final SupplierMapper mapper;

    public List<Supplier> search(String name) {
        return mapper.selectList(new LambdaQueryWrapper<Supplier>()
                .like(StringUtils.hasText(name), Supplier::getName, name));
    }

    public PageResult<Supplier> page(long current, long size, String name, Integer status, String creditRating) {
        LambdaQueryWrapper<Supplier> w = new LambdaQueryWrapper<Supplier>()
                .like(StringUtils.hasText(name), Supplier::getName, name)
                .eq(status != null, Supplier::getStatus, status)
                .eq(StringUtils.hasText(creditRating), Supplier::getCreditRating, creditRating);
        Page<Supplier> p = mapper.selectPage(new Page<>(current, size), w);
        return new PageResult<>(p.getRecords(), p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    public Supplier get(Long id) {
        Supplier s = mapper.selectById(id);
        if (s == null) throw new BizException(404, "供应商不存在");
        return s;
    }

    @Transactional
    public Supplier create(Supplier body) {
        if (!StringUtils.hasText(body.getSupplierCode()) || !StringUtils.hasText(body.getName())) {
            throw new BizException(400, "供应商编码与名称必填");
        }
        if (body.getStatus() == null) body.setStatus(1);
        mapper.insert(body);
        return body;
    }

    @Transactional
    public Supplier updateStatus(Long id, Integer status) {
        if (status == null) throw new BizException(400, "status 必填");
        Supplier s = get(id);
        s.setStatus(status);
        mapper.updateById(s);
        return s;
    }
}
