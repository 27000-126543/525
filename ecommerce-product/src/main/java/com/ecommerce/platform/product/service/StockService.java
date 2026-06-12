package com.ecommerce.platform.product.service;

import com.ecommerce.platform.product.dto.StockDeductionDTO;
import com.ecommerce.platform.product.entity.ProductSku;

import java.util.List;
import java.util.Map;

public interface StockService {

    Integer getStock(Long skuId);

    Map<Long, Integer> getStocks(List<Long> skuIds);

    boolean deductStock(StockDeductionDTO dto);

    boolean restoreStock(StockDeductionDTO dto);

    boolean lockStock(StockDeductionDTO dto);

    boolean unlockStock(StockDeductionDTO dto);

    boolean seckillDeductStock(Long seckillId, Long skuId, Long userId, Integer quantity);

    void syncStockToRedis(Long skuId);

    void batchSyncStockToRedis(List<Long> skuIds);

    ProductSku getSkuById(Long skuId);

    List<ProductSku> getSkusBySpuId(Long spuId);
}
