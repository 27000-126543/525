package com.ecommerce.platform.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ecommerce.platform.common.constant.RedisConstants;
import com.ecommerce.platform.common.exception.BusinessException;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.util.RedisUtil;
import com.ecommerce.platform.product.entity.ProductSpu;
import com.ecommerce.platform.product.mapper.ProductSpuMapper;
import com.ecommerce.platform.product.service.ProductSpuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductSpuServiceImpl implements ProductSpuService {

    private final ProductSpuMapper spuMapper;
    private final RedisUtil redisUtil;

    @Override
    public PageResult<ProductSpu> page(PageQuery pageQuery, ProductSpu spu) {
        Page<ProductSpu> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        Page<ProductSpu> result = spuMapper.selectSpuPage(page, spu);
        return PageResult.of(result);
    }

    @Override
    public ProductSpu getById(Long id) {
        String cacheKey = RedisConstants.PRODUCT_INFO_PREFIX + id;
        ProductSpu spu = (ProductSpu) redisUtil.get(cacheKey);
        if (spu != null) {
            return spu;
        }

        spu = spuMapper.selectById(id);
        if (spu != null) {
            redisUtil.set(cacheKey, spu, RedisConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        return spu;
    }

    @Override
    public ProductSpu getDetail(Long id) {
        ProductSpu spu = getById(id);
        if (spu == null) {
            throw new BusinessException("商品不存在");
        }
        return spu;
    }

    @Override
    public void add(ProductSpu spu) {
        spuMapper.insert(spu);
    }

    @Override
    public void update(ProductSpu spu) {
        spuMapper.updateById(spu);
        redisUtil.delete(RedisConstants.PRODUCT_INFO_PREFIX + spu.getId());
    }

    @Override
    public void delete(Long id) {
        spuMapper.deleteById(id);
        redisUtil.delete(RedisConstants.PRODUCT_INFO_PREFIX + id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ProductSpu spu = new ProductSpu();
        spu.setId(id);
        spu.setStatus(status);
        spuMapper.updateById(spu);
        redisUtil.delete(RedisConstants.PRODUCT_INFO_PREFIX + id);
    }

    @Override
    public List<ProductSpu> listByIds(List<Long> ids) {
        return spuMapper.selectBatchIds(ids);
    }

    @Override
    public List<ProductSpu> listHotProducts(Integer limit) {
        return spuMapper.selectHotProducts(limit);
    }

    @Override
    public List<ProductSpu> listRecommendProducts(Integer limit) {
        return spuMapper.selectRecommendProducts(limit);
    }
}
