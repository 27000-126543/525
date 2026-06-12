package com.ecommerce.platform.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.platform.common.constant.RedisConstants;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.product.entity.SeckillProduct;
import com.ecommerce.platform.product.mapper.SeckillProductMapper;
import com.ecommerce.platform.product.service.SeckillProductService;
import com.ecommerce.platform.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillProductServiceImpl implements SeckillProductService {

    private final SeckillProductMapper seckillProductMapper;
    private final StockService stockService;
    private final RedisUtil redisUtil;

    @Override
    public PageResult<SeckillProduct> page(PageQuery pageQuery, Integer status) {
        Page<SeckillProduct> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<SeckillProduct> result = seckillProductMapper.selectSeckillPage(page, status);
        return PageResult.of(result);
    }

    @Override
    public SeckillProduct getById(Long id) {
        return seckillProductMapper.selectById(id);
    }

    @Override
    public void add(SeckillProduct seckillProduct) {
        seckillProduct.setStock(seckillProduct.getTotalStock());
        seckillProduct.setSoldCount(0);
        seckillProduct.setLockedStock(0);
        seckillProductMapper.insert(seckillProduct);
    }

    @Override
    public void update(SeckillProduct seckillProduct) {
        seckillProductMapper.updateById(seckillProduct);
        String cacheKey = RedisConstants.SECKILL_STOCK_PREFIX + seckillProduct.getId();
        redisUtil.delete(cacheKey);
    }

    @Override
    public void delete(Long id) {
        seckillProductMapper.deleteById(id);
        String cacheKey = RedisConstants.SECKILL_STOCK_PREFIX + id;
        redisUtil.delete(cacheKey);
    }

    @Override
    public List<SeckillProduct> listActiveSeckills() {
        return seckillProductMapper.selectActiveSeckills();
    }

    @Override
    public boolean seckill(Long seckillId, Long userId, Integer quantity) {
        SeckillProduct seckill = seckillProductMapper.selectById(seckillId);
        if (seckill == null) {
            throw new BusinessException("秒杀商品不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckill.getStartTime())) {
            throw new BusinessException("秒杀未开始");
        }
        if (now.isAfter(seckill.getEndTime())) {
            throw new BusinessException("秒杀已结束");
        }
        if (seckill.getStatus() != 1) {
            throw new BusinessException("秒杀活动未启用");
        }

        if (quantity == null || quantity < 1) {
            quantity = 1;
        }
        if (seckill.getLimitPerUser() != null && quantity > seckill.getLimitPerUser()) {
            quantity = seckill.getLimitPerUser();
        }

        return stockService.seckillDeductStock(seckillId, seckill.getSkuId(), userId, quantity);
    }

    @Override
    public void onShelve(Long id) {
        SeckillProduct seckill = new SeckillProduct();
        seckill.setId(id);
        seckill.setStatus(1);
        seckillProductMapper.updateById(seckill);
        syncSeckillStockToRedis(id);
    }

    @Override
    public void offShelve(Long id) {
        SeckillProduct seckill = new SeckillProduct();
        seckill.setId(id);
        seckill.setStatus(0);
        seckillProductMapper.updateById(seckill);
        String cacheKey = RedisConstants.SECKILL_STOCK_PREFIX + id;
        redisUtil.delete(cacheKey);
    }

    @Override
    public void syncSeckillStockToRedis(Long seckillId) {
        SeckillProduct seckill = seckillProductMapper.selectById(seckillId);
        if (seckill != null) {
            String cacheKey = RedisConstants.SECKILL_STOCK_PREFIX + seckillId;
            redisUtil.set(cacheKey, seckill.getStock(), 24, TimeUnit.HOURS);
            log.info("秒杀库存同步到Redis, seckillId: {}, stock: {}", seckillId, seckill.getStock());
        }
    }
}
