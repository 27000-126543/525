package com.ecommerce.platform.product.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.product.entity.ProductSpu;
import com.ecommerce.platform.product.service.ProductSpuService;
import com.ecommerce.platform.product.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商品管理")
@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductSpuService productSpuService;
    private final StockService stockService;

    @Operation(summary = "分页查询商品")
    @GetMapping("/page")
    public Result<PageResult<ProductSpu>> page(PageQuery pageQuery, ProductSpu spu) {
        return Result.success(productSpuService.page(pageQuery, spu));
    }

    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<ProductSpu> getById(@PathVariable Long id) {
        return Result.success(productSpuService.getDetail(id));
    }

    @Operation(summary = "获取商品库存")
    @GetMapping("/stock/{skuId}")
    public Result<Integer> getStock(@PathVariable Long skuId) {
        return Result.success(stockService.getStock(skuId));
    }

    @Operation(summary = "批量获取商品库存")
    @PostMapping("/stocks")
    public Result<Map<Long, Integer>> getStocks(@RequestBody List<Long> skuIds) {
        return Result.success(stockService.getStocks(skuIds));
    }

    @Operation(summary = "热销商品")
    @GetMapping("/hot")
    public Result<List<ProductSpu>> hotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productSpuService.listHotProducts(limit));
    }

    @Operation(summary = "推荐商品")
    @GetMapping("/recommend")
    public Result<List<ProductSpu>> recommendProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productSpuService.listRecommendProducts(limit));
    }

    @Operation(summary = "新增商品")
    @PostMapping
    public Result<Void> add(@RequestBody ProductSpu spu) {
        productSpuService.add(spu);
        return Result.success();
    }

    @Operation(summary = "修改商品")
    @PutMapping
    public Result<Void> update(@RequestBody ProductSpu spu) {
        productSpuService.update(spu);
        return Result.success();
    }

    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productSpuService.delete(id);
        return Result.success();
    }
}
