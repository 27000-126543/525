package com.ecommerce.platform.product.service.impl;

import cn.hutool.core.util.IdUtil;
import com.ecommerce.platform.common.constant.RedisConstants;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.result.ResultCode;
import com.ecommerce.platform.common.util.DistributedLockUtil;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.product.dto.StockDeductionDTO;
import com.ecommerce.platform.product.entity.ProductSku;
import com.ecommerce.platform.product.entity.SeckillProduct;
import com.ecommerce.platform.product.mapper.ProductSkuMapper;
import com.ecommerce.platform.product.mapper.SeckillProductMapper;
import com.ecommerce.platform.product.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductSkuMapper skuMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final RedisUtil redisUtil;
    private final DistributedLockUtil distributedLockUtil;

    @Override
    public Integer getStock(Long skuId) {
        String key = RedisConstants.PRODUCT_STOCK_PREFIX + skuId;
        Object stock = redisUtil.get(key);
        if (stock != null) {
            return (Integer) stock;
        }

        ProductSku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        Integer availableStock = sku.getStock() - (sku.getLockedStock() != null ? sku.getLockedStock() : 0);
        redisUtil.set(key, availableStock, RedisConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return availableStock;
    }

    @Override
    public Map<Long, Integer> getStocks(List<Long> skuIds) {
        Map<Long, Integer> result = new HashMap<>();
        List<Long> needQuery = new ArrayList<>();

        for (Long skuId : skuIds) {
            String key = RedisConstants.PRODUCT_STOCK_PREFIX + skuId;
            Object stock = redisUtil.get(key);
            if (stock != null) {
                result.put(skuId, (Integer) stock);
            } else {
                needQuery.add(skuId);
            }
        }

        if (!needQuery.isEmpty()) {
            List<ProductSku> skus = skuMapper.selectBatchIds(needQuery);
            for (ProductSku sku : skus) {
                Integer availableStock = sku.getStock() - (sku.getLockedStock() != null ? sku.getLockedStock() : 0);
                result.put(sku.getId(), availableStock);
                String key = RedisConstants.PRODUCT_STOCK_PREFIX + sku.getId();
                redisUtil.set(key, availableStock, RedisConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
        }

        return result;
    }

    @Override
    public boolean deductStock(StockDeductionDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return false;
        }

        for (StockDeductionDTO.StockItem item : dto.getItems()) {
            String lockKey = RedisConstants.ORDER_LOCK_PREFIX + "stock:" + item.getSkuId();
            boolean success = distributedLockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
                ProductSku sku = skuMapper.selectById(item.getSkuId());
                if (sku == null) {
                    throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
                }

                int availableStock = sku.getStock() - (sku.getLockedStock() != null ? sku.getLockedStock() : 0);
                if (availableStock < item.getQuantity()) {
                    throw new BusinessException(ResultCode.PRODUCT_STOCK_NOT_ENOUGH);
                }

                int rows = skuMapper.deductStock(item.getSkuId(), item.getQuantity());
                if (rows > 0) {
                    String stockKey = RedisConstants.PRODUCT_STOCK_PREFIX + item.getSkuId();
                    redisUtil.decrement(stockKey, item.getQuantity());
                }
                return rows > 0;
            });

            if (!success) {
                log.warn("库存扣减失败, skuId: {}, quantity: {}", item.getSkuId(), item.getQuantity());
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean restoreStock(StockDeductionDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return false;
        }

        for (StockDeductionDTO.StockItem item : dto.getItems()) {
            int rows = skuMapper.restoreStock(item.getSkuId(), item.getQuantity());
            if (rows > 0) {
                String stockKey = RedisConstants.PRODUCT_STOCK_PREFIX + item.getSkuId();
                redisUtil.increment(stockKey, item.getQuantity());
            }
        }

        return true;
    }

    @Override
    public boolean lockStock(StockDeductionDTO dto) {
        for (StockDeductionDTO.StockItem item : dto.getItems()) {
            String lockKey = RedisConstants.ORDER_LOCK_PREFIX + "lock:" + item.getSkuId();
            distributedLockUtil.tryLock(lockKey, 3, 10, TimeUnit.SECONDS, () -> {
                ProductSku sku = skuMapper.selectById(item.getSkuId());
                if (sku == null) {
                    throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
                }

                int availableStock = sku.getStock() - (sku.getLockedStock() != null ? sku.getLockedStock() : 0);
                if (availableStock < item.getQuantity()) {
                    throw new BusinessException(ResultCode.PRODUCT_STOCK_NOT_ENOUGH);
                }

                int rows = skuMapper.lockStock(item.getSkuId(), item.getQuantity());
                if (rows > 0) {
                    String stockKey = RedisConstants.PRODUCT_STOCK_PREFIX + item.getSkuId();
                    redisUtil.decrement(stockKey, item.getQuantity());
                }
                return rows > 0;
            });
        }

        return true;
    }

    @Override
    public boolean unlockStock(StockDeductionDTO dto) {
        for (StockDeductionDTO.StockItem item : dto.getItems()) {
            int rows = skuMapper.unlockStock(item.getSkuId(), item.getQuantity());
            if (rows > 0) {
                String stockKey = RedisConstants.PRODUCT_STOCK_PREFIX + item.getSkuId();
                redisUtil.increment(stockKey, item.getQuantity());
            }
        }
        return true;
    }

    @Override
    public boolean seckillDeductStock(Long seckillId, Long skuId, Long userId, Integer quantity) {
        String userKey = RedisConstants.SECKILL_USER_PREFIX + seckillId + ":" + userId;
        Boolean hasBought = redisUtil.sIsMember(userKey, String.valueOf(userId));
        if (Boolean.TRUE.equals(hasBought)) {
            throw new BusinessException(ResultCode.SECKILL_LIMIT_EXCEED);
        }

        String stockKey = RedisConstants.SECKILL_STOCK_PREFIX + seckillId;
        Object stockObj = redisUtil.get(stockKey);
        if (stockObj == null) {
            SeckillProduct seckillProduct = seckillProductMapper.selectById(seckillId);
            if (seckillProduct == null) {
                throw new BusinessException("秒杀商品不存在");
            }
            redisUtil.set(stockKey, seckillProduct.getStock(), 24, TimeUnit.HOURS);
        }

        Long remainStock = redisUtil.decrement(stockKey, quantity);
        if (remainStock < 0) {
            redisUtil.increment(stockKey, quantity);
            throw new BusinessException(ResultCode.SECKILL_SOLD_OUT);
        }

        redisUtil.sAdd(userKey, String.valueOf(userId));
        redisUtil.expire(userKey, 24, TimeUnit.HOURS);

        log.info("秒杀库存预扣成功, seckillId: {}, userId: {}, remainStock: {}", seckillId, userId, remainStock);
        return true;
    }

    @Override
    public void syncStockToRedis(Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        if (sku != null) {
            Integer availableStock = sku.getStock() - (sku.getLockedStock() != null ? sku.getLockedStock() : 0);
            redisUtil.set(RedisConstants.PRODUCT_STOCK_PREFIX + skuId, availableStock,
                    RedisConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public void batchSyncStockToRedis(List<Long> skuIds) {
        List<ProductSku> skus = skuMapper.selectBatchIds(skuIds);
        for (ProductSku sku : skus) {
            Integer availableStock = sku.getStock() - (sku.getLockedStock() != null ? sku.getLockedStock() : 0);
            redisUtil.set(RedisConstants.PRODUCT_STOCK_PREFIX + sku.getId(), availableStock,
                    RedisConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public ProductSku getSkuById(Long skuId) {
        return skuMapper.selectById(skuId);
    }

    @Override
    public List<ProductSku> getSkusBySpuId(Long spuId) {
        return skuMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getSpuId, spuId)
                .eq(ProductSku::getStatus, 1)
                .eq(ProductSku::getDeleted, 0)
                .orderByAsc(ProductSku::getSortOrder));
    }
}
