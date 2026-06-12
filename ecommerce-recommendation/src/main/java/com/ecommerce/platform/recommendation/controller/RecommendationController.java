package com.ecommerce.platform.recommendation.controller;

import com.ecommerce.platform.common.context.UserContext;
import com.ecommerce.platform.common.result.Result;
import com.ecommerce.platform.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "推荐引擎")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "个性化推荐")
    @GetMapping("/personalized")
    public Result<List<Long>> personalizedRecommend(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Long userId = UserContext.getUserId();
        return Result.success(recommendationService.recommendForUser(userId, limit));
    }

    @Operation(summary = "热门商品推荐")
    @GetMapping("/hot")
    public Result<List<Long>> hotProducts(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(recommendationService.getHotProducts(limit));
    }

    @Operation(summary = "相似商品推荐")
    @GetMapping("/similar/{productId}")
    public Result<List<Long>> similarProducts(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(recommendationService.getSimilarProducts(productId, limit));
    }

    @Operation(summary = "新品推荐")
    @GetMapping("/new")
    public Result<List<Long>> newProducts(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(recommendationService.getNewProducts(limit));
    }

    @Operation(summary = "分类推荐")
    @GetMapping("/category/{categoryId}")
    public Result<List<Long>> categoryRecommend(
            @PathVariable Long categoryId,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return Result.success(recommendationService.getRecommendByCategory(categoryId, limit));
    }

    @Operation(summary = "记录用户行为")
    @PostMapping("/behavior")
    public Result<Void> recordBehavior(
            @RequestParam Long productId,
            @RequestParam String behaviorType,
            @RequestParam(required = false) Integer score) {
        Long userId = UserContext.getUserId();
        recommendationService.recordUserBehavior(userId, productId, behaviorType, score);
        return Result.success();
    }

    @Operation(summary = "获取推荐统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(recommendationService.getStats());
    }

    @Operation(summary = "刷新热门推荐")
    @PostMapping("/refresh/hot")
    public Result<Void> refreshHot() {
        recommendationService.refreshHotProducts();
        return Result.success();
    }
}
