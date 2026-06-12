package com.ecommerce.platform.product.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.page.PageQuery;
import com.ecommerce.platform.common.page.PageResult;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.product.entity.SeckillProduct;
import com.ecommerce.platform.product.service.SeckillProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "秒杀管理")
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillProductService seckillProductService;

    @Operation(summary = "分页查询秒杀商品")
    @GetMapping("/page")
    public Result<PageResult<SeckillProduct>> page(PageQuery pageQuery, Integer status) {
        return Result.success(seckillProductService.page(pageQuery, status));
    }

    @Operation(summary = "获取秒杀商品详情")
    @GetMapping("/{id}")
    public Result<SeckillProduct> getById(@PathVariable Long id) {
        return Result.success(seckillProductService.getById(id));
    }

    @Operation(summary = "获取正在进行的秒杀活动")
    @GetMapping("/active")
    public Result<List<SeckillProduct>> listActiveSeckills() {
        return Result.success(seckillProductService.listActiveSeckills());
    }

    @Operation(summary = "执行秒杀")
    @PostMapping("/{seckillId}/seckill")
    public Result<Boolean> seckill(@PathVariable Long seckillId,
                                   @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = UserContext.getUserId();
        return Result.success(seckillProductService.seckill(seckillId, userId, quantity));
    }

    @Operation(summary = "新增秒杀商品")
    @PostMapping
    public Result<Void> add(@RequestBody SeckillProduct seckillProduct) {
        seckillProductService.add(seckillProduct);
        return Result.success();
    }

    @Operation(summary = "修改秒杀商品")
    @PutMapping
    public Result<Void> update(@RequestBody SeckillProduct seckillProduct) {
        seckillProductService.update(seckillProduct);
        return Result.success();
    }

    @Operation(summary = "删除秒杀商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        seckillProductService.delete(id);
        return Result.success();
    }

    @Operation(summary = "上架秒杀")
    @PostMapping("/{id}/onShelve")
    public Result<Void> onShelve(@PathVariable Long id) {
        seckillProductService.onShelve(id);
        return Result.success();
    }

    @Operation(summary = "下架秒杀")
    @PostMapping("/{id}/offShelve")
    public Result<Void> offShelve(@PathVariable Long id) {
        seckillProductService.offShelve(id);
        return Result.success();
    }

    @Operation(summary = "同步库存到Redis")
    @PostMapping("/{id}/syncStock")
    public Result<Void> syncStock(@PathVariable Long id) {
        seckillProductService.syncSeckillStockToRedis(id);
        return Result.success();
    }
}
