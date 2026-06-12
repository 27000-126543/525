package com.ecommerce.platform.product.service;

import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.product.entity.SeckillProduct;

import java.util.List;

public interface SeckillProductService {

    PageResult<SeckillProduct> page(PageQuery pageQuery, Integer status);

    SeckillProduct getById(Long id);

    void add(SeckillProduct seckillProduct);

    void update(SeckillProduct seckillProduct);

    void delete(Long id);

    List<SeckillProduct> listActiveSeckills();

    boolean seckill(Long seckillId, Long userId, Integer quantity);

    void onShelve(Long id);

    void offShelve(Long id);

    void syncSeckillStockToRedis(Long seckillId);
}
